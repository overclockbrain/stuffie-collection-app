package com.stuffie.stuffed_animal_service.controller;

import com.stuffie.stuffed_animal_service.dto.SeriesRequest;
import com.stuffie.stuffed_animal_service.dto.SeriesResponse;
import com.stuffie.stuffed_animal_service.service.SeriesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * シリーズマスタ管理のエンドポイント。
 * 全エンドポイントはJWT認証必須（SecurityConfigで設定済み）。
 */
@RestController
@RequestMapping("/api/series")
@RequiredArgsConstructor
public class SeriesController {

    private final SeriesService seriesService;

    /**
     * シリーズ一覧取得（プルダウン表示用）。
     */
    @GetMapping
    public ResponseEntity<List<SeriesResponse>> findAll() {
        return ResponseEntity.ok(seriesService.findAll());
    }

    /**
     * シリーズ登録。
     */
    @PostMapping
    public ResponseEntity<SeriesResponse> create(
            @Valid @RequestBody SeriesRequest request,
            @AuthenticationPrincipal String userEmail) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(seriesService.create(request, userEmail));
    }

    /**
     * シリーズ更新。
     * 自分が登録したデータかADMINのみ更新可能。
     */
    @PutMapping("/{id}")
    public ResponseEntity<SeriesResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody SeriesRequest request,
            @AuthenticationPrincipal String userEmail) {
        return ResponseEntity.ok(seriesService.update(id, request, userEmail));
    }

    /**
     * シリーズ削除。
     * 自分が登録したデータかADMINのみ削除可能。
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal String userEmail) {
        seriesService.delete(id, userEmail);
        return ResponseEntity.noContent().build();
    }
}