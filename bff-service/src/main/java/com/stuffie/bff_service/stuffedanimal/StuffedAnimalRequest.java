package com.stuffie.bff_service.stuffedanimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * フロントから受け取るぬいぐるみ登録・更新リクエストのDTO。
 */
@Data
public class StuffedAnimalRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    private Long seriesId;

    @Size(max = 100)
    private String character;

    /** ISO形式の日付文字列（例: 2024-03-15） */
    private String purchaseDate;

    @Size(max = 100)
    private String purchasePlace;

    @Size(max = 500)
    private String imageUrl;

    private String notes;
}