package be.freenote.service;

import be.freenote.dto.response.NewsItem;

import java.util.List;

public interface NewsService {
    List<NewsItem> getNews();
}
