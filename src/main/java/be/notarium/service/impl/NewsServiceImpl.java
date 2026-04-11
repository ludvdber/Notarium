package be.notarium.service.impl;

import be.notarium.dto.response.NewsItem;
import be.notarium.service.NewsService;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {

    private static final String FEED_URL = "https://isfce.blogspot.com/feeds/posts/default";
    private static final String CACHE_KEY = "news:isfce";
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    @SuppressWarnings("unchecked")
    public List<NewsItem> getNews() {
        Object cached = redisTemplate.opsForValue().get(CACHE_KEY);
        if (cached != null) {
            try {
                return objectMapper.convertValue(cached, new TypeReference<List<NewsItem>>() {});
            } catch (Exception e) {
                log.warn("Failed to deserialize cached news, fetching fresh");
            }
        }

        List<NewsItem> news = fetchFromFeed();
        if (!news.isEmpty()) {
            redisTemplate.opsForValue().set(CACHE_KEY, news, CACHE_TTL);
        }
        return news;
    }

    private List<NewsItem> fetchFromFeed() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(FEED_URL))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<InputStream> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofInputStream());

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            // Disable external entities for security
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document doc = builder.parse(response.body());

            NodeList entries = doc.getElementsByTagNameNS("http://www.w3.org/2005/Atom", "entry");
            List<NewsItem> items = new ArrayList<>();

            for (int i = 0; i < Math.min(entries.getLength(), 10); i++) {
                Element entry = (Element) entries.item(i);
                NewsItem item = new NewsItem();

                item.setTitle(getTextContent(entry, "title"));
                item.setDate(getTextContent(entry, "published"));

                // Extract link
                NodeList links = entry.getElementsByTagNameNS("http://www.w3.org/2005/Atom", "link");
                for (int j = 0; j < links.getLength(); j++) {
                    Element link = (Element) links.item(j);
                    if ("alternate".equals(link.getAttribute("rel"))) {
                        item.setUrl(link.getAttribute("href"));
                        break;
                    }
                }

                // Extract labels/categories
                List<String> labels = new ArrayList<>();
                NodeList categories = entry.getElementsByTagNameNS(
                        "http://www.w3.org/2005/Atom", "category");
                for (int j = 0; j < categories.getLength(); j++) {
                    Element cat = (Element) categories.item(j);
                    String term = cat.getAttribute("term");
                    if (term != null && !term.isBlank()) {
                        labels.add(term);
                    }
                }
                item.setLabels(labels);

                items.add(item);
            }

            return items;

        } catch (Exception e) {
            log.error("Failed to fetch ISFCE news feed: {}", e.getMessage());
            return List.of();
        }
    }

    private String getTextContent(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagNameNS("http://www.w3.org/2005/Atom", tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        return null;
    }
}
