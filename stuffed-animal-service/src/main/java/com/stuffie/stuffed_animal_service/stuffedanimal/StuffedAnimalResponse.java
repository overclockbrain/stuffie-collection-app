package com.stuffie.stuffed_animal_service.stuffedanimal;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ぬいぐるみレスポンスのDTO。
 * Entityを直接返さずDTOに変換することで、不要な情報の露出を防ぐ。
 */
@Data
public class StuffedAnimalResponse {

    private Long id;
    private String name;
    private Long seriesId;
    private String seriesName;
    private String character;
    private LocalDate purchaseDate;
    private String purchasePlace;
    private String imageUrl;
    private String notes;
    private Long createdBy;
    private Long updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * EntityからResponseDTOに変換するファクトリメソッド。
     */
    public static StuffedAnimalResponse from(StuffedAnimal entity) {
        StuffedAnimalResponse dto = new StuffedAnimalResponse();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        if (entity.getSeries() != null) {
            dto.setSeriesId(entity.getSeries().getId());
            dto.setSeriesName(entity.getSeries().getName());
        }
        dto.setCharacter(entity.getCharacter());
        dto.setPurchaseDate(entity.getPurchaseDate());
        dto.setPurchasePlace(entity.getPurchasePlace());
        dto.setImageUrl(entity.getImageUrl());
        dto.setNotes(entity.getNotes());
        dto.setCreatedBy(entity.getCreatedBy().getId());
        dto.setUpdatedBy(entity.getUpdatedBy().getId());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}