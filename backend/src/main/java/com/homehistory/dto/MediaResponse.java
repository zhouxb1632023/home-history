package com.homehistory.dto;

import com.homehistory.model.enums.MediaType;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class MediaResponse {
    private Long id;
    private String originalName;
    private MediaType mediaType;
    private Long fileSize;
    private String mimeType;
    private Integer width;
    private Integer height;
    private LocalDateTime takenAt;
    private String description;
    private String uploaderName;
    private List<TagDto> tags;
    private LocalDateTime createdAt;

    @Data
    @Builder
    public static class TagDto {
        private Long id;
        private String name;
        private String color;
    }
}
