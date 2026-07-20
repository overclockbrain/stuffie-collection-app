package com.stuffie.bff_service.series;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * フロントから受け取るシリーズ登録・更新リクエストのDTO。
 */
@Data
public class SeriesRequest {

    @NotBlank
    @Size(max = 100)
    private String name;
}