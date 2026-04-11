package be.notarium.service.impl;

import be.notarium.config.MeilisearchConfig;
import be.notarium.entity.Document;
import be.notarium.entity.Tag;
import be.notarium.service.MeilisearchService;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeilisearchServiceImpl implements MeilisearchService {

    private static final String INDEX = "documents";

    private final MeilisearchConfig meilisearchConfig;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public void indexDocument(Document document) {
        try {
            ObjectNode doc = objectMapper.createObjectNode();
            doc.put("id", document.getId());
            doc.put("title", document.getTitle());
            doc.put("category", document.getCategory().name());
            doc.put("courseId", document.getCourse().getId());
            doc.put("courseName", document.getCourse().getName());
            doc.put("sectionName", document.getCourse().getSection().getName());
            doc.put("language", document.getLanguage());
            doc.put("year", document.getYear());
            if (document.getProfessor() != null) {
                doc.put("professorName", document.getProfessor().getName());
            }
            if (document.getSummaryAi() != null) {
                doc.put("summaryAi", document.getSummaryAi());
            }

            ArrayNode tagsArray = doc.putArray("tags");
            if (document.getTags() != null) {
                document.getTags().stream().map(Tag::getLabel).forEach(tagsArray::add);
            }

            ArrayNode docs = objectMapper.createArrayNode();
            docs.add(doc);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(meilisearchConfig.getHost() + "/indexes/" + INDEX + "/documents"))
                    .header("Authorization", "Bearer " + meilisearchConfig.getApiKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(docs.toString()))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
            log.debug("Indexed document {} in Meilisearch", document.getId());

        } catch (Exception e) {
            log.error("Failed to index document {} in Meilisearch: {}", document.getId(), e.getMessage());
        }
    }

    @Override
    public List<Long> search(String query, Long courseId, String category, String sort, Pageable pageable) {
        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("q", query);
            body.put("limit", pageable.getPageSize());
            body.put("offset", (int) pageable.getOffset());

            List<String> filters = new ArrayList<>();
            if (courseId != null) {
                filters.add("courseId = " + courseId);
            }
            if (category != null) {
                filters.add("category = " + category);
            }
            if (!filters.isEmpty()) {
                body.put("filter", String.join(" AND ", filters));
            }
            if (sort != null) {
                ArrayNode sortArray = body.putArray("sort");
                sortArray.add(sort);
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(meilisearchConfig.getHost() + "/indexes/" + INDEX + "/search"))
                    .header("Authorization", "Bearer " + meilisearchConfig.getApiKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode hits = root.get("hits");

            List<Long> ids = new ArrayList<>();
            if (hits != null && hits.isArray()) {
                for (JsonNode hit : hits) {
                    ids.add(hit.get("id").asLong());
                }
            }
            return ids;

        } catch (Exception e) {
            log.error("Meilisearch search failed: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public void deleteDocument(Long documentId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(meilisearchConfig.getHost() + "/indexes/" + INDEX + "/documents/" + documentId))
                    .header("Authorization", "Bearer " + meilisearchConfig.getApiKey())
                    .DELETE()
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
            log.debug("Deleted document {} from Meilisearch", documentId);

        } catch (Exception e) {
            log.error("Failed to delete document {} from Meilisearch: {}", documentId, e.getMessage());
        }
    }
}
