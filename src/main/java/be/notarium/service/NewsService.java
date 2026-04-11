package be.notarium.service;

import be.notarium.dto.response.NewsItem;

import java.util.List;

public interface NewsService {
    List<NewsItem> getNews();
}
