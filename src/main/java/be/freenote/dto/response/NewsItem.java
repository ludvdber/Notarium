package be.freenote.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class NewsItem {
    private String title;
    private String date;
    private List<String> labels;
    private String url;
}
