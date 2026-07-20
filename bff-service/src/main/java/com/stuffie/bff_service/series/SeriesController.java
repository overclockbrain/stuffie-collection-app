package com.stuffie.bff_service.series;

import io.grpc.StatusRuntimeException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * フロントエンド向けのシリーズ管理エンドポイント。
 * @AuthenticationPrincipal はJwtAuthFilterがSecurityContextにセットした
 * メールアドレス（JWTのsubject）を受け取る。
 */
@RestController
@RequestMapping("/api/series")
@RequiredArgsConstructor
public class SeriesController {

    private final SeriesGrpcClient seriesGrpcClient;

    @GetMapping
    public ResponseEntity<List<SeriesResponseDto>> findAll() {
        var list = seriesGrpcClient.getSeriesList().stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(list);
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody SeriesRequest request,
                                     @AuthenticationPrincipal String userEmail) {
        try {
            var result = seriesGrpcClient.createSeries(request.getName(), userEmail);
            return ResponseEntity.status(HttpStatus.CREATED).body(toDto(result));
        } catch (StatusRuntimeException e) {
            return ResponseEntity.badRequest().body(errorBody(e));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                     @Valid @RequestBody SeriesRequest request,
                                     @AuthenticationPrincipal String userEmail) {
        try {
            var result = seriesGrpcClient.updateSeries(id, request.getName(), userEmail);
            return ResponseEntity.ok(toDto(result));
        } catch (StatusRuntimeException e) {
            return ResponseEntity.status(toHttpStatus(e)).body(errorBody(e));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id,
                                     @AuthenticationPrincipal String userEmail) {
        try {
            seriesGrpcClient.deleteSeries(id, userEmail);
            return ResponseEntity.noContent().build();
        } catch (StatusRuntimeException e) {
            return ResponseEntity.status(toHttpStatus(e)).body(errorBody(e));
        }
    }

    /** gRPCのSeriesMessage（生成コード）をフロント向けDTOに変換する */
    private SeriesResponseDto toDto(com.stuffie.grpc.series.SeriesMessage message) {
        SeriesResponseDto dto = new SeriesResponseDto();
        dto.setId(message.getId());
        dto.setName(message.getName());
        dto.setCreatedBy(message.getCreatedBy());
        dto.setCreatedAt(message.getCreatedAt());
        return dto;
    }

    /** gRPCのStatusコードをHTTPステータスコードに変換する */
    private HttpStatus toHttpStatus(StatusRuntimeException e) {
        return switch (e.getStatus().getCode()) {
            case PERMISSION_DENIED -> HttpStatus.FORBIDDEN;
            case INVALID_ARGUMENT -> HttpStatus.BAD_REQUEST;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    private java.util.Map<String, String> errorBody(StatusRuntimeException e) {
        return java.util.Map.of("message", e.getStatus().getDescription());
    }
}