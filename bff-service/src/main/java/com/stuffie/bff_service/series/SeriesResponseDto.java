package com.stuffie.bff_service.series;

import lombok.Data;

/**
 * フロントに返すシリーズレスポンスのDTO。
 */
@Data
public class SeriesResponseDto {
    private Long id;
    private String name;
    private Long createdBy;
    private String createdAt;
}