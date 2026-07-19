package com.stuffie.stuffed_animal_service.dto;

import com.stuffie.stuffed_animal_service.entity.Series;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * シリーズレスポンスのDTO。
 */
@Data
public class SeriesResponse {

    private Long id;
    private String name;
    private Long createdBy;
    private LocalDateTime createdAt;

    /**
     * EntityからResponseDTOに変換するファクトリメソッド。
     */
    public static SeriesResponse from(Series entity) {
        SeriesResponse dto = new SeriesResponse();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setCreatedBy(entity.getCreatedBy().getId());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}