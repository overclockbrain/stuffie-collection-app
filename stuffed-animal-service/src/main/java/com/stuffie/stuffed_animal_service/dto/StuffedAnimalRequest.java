package com.stuffie.stuffed_animal_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * ぬいぐるみ登録・更新リクエストのDTO。
 */
@Data
public class StuffedAnimalRequest {

    /** ぬいぐるみの名前（必須） */
    @NotBlank
    @Size(max = 100)
    private String name;

    /** シリーズID（任意） */
    private Long seriesId;

    /** キャラクター名（任意） */
    @Size(max = 100)
    private String character;

    /** 購入日（任意） */
    private LocalDate purchaseDate;

    /** 購入場所（任意） */
    @Size(max = 100)
    private String purchasePlace;

    /** 画像URL（任意） */
    @Size(max = 500)
    private String imageUrl;

    /** メモ（任意） */
    private String notes;
}