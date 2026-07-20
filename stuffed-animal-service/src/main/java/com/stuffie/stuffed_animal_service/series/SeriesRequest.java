package com.stuffie.stuffed_animal_service.series;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * シリーズ登録・更新リクエストのDTO。
 */
@Data
public class SeriesRequest {

    /** シリーズ名（必須・一意） */
    @NotBlank
    @Size(max = 100)
    private String name;
}