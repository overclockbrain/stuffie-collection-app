package com.stuffie.bff_service.stuffedanimal;

import lombok.Data;

/**
 * フロントに返すぬいぐるみレスポンスのDTO。
 */
@Data
public class StuffedAnimalResponseDto {
    private Long id;
    private String name;
    private Long seriesId;
    private String seriesName;
    private String character;
    private String purchaseDate;
    private String purchasePlace;
    private String imageUrl;
    private String notes;
    private Long createdBy;
    private Long updatedBy;
    private String createdAt;
    private String updatedAt;
}