package com.stuffie.stuffed_animal_service.controller;

import com.stuffie.stuffed_animal_service.dto.StuffedAnimalRequest;
import com.stuffie.stuffed_animal_service.dto.StuffedAnimalResponse;
import com.stuffie.stuffed_animal_service.service.StuffedAnimalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ぬいぐるみ管理のエンドポイント。
 * 全エンドポイントはJWT認証必須（SecurityConfigで設定済み）。
 * ログイン中のユーザー情報は @AuthenticationPrincipal で取得する。
 */
@RestController
@RequestMapping("/api/stuffed-animals")
@RequiredArgsConstructor
public class StuffedAnimalController {

    private final StuffedAnimalService stuffedAnimalService;

    /**
     * ぬいぐるみ一覧取得。
     * クエリパラメータで絞り込み可能。
     */
    @GetMapping
    public ResponseEntity<List<StuffedAnimalResponse>> findAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long seriesId,
            @RequestParam(required = false) String character) {
        return ResponseEntity.ok(stuffedAnimalService.findAll(name, seriesId, character));
    }

    /**
     * ぬいぐるみ1件取得。
     */
    @GetMapping("/{id}")
    public ResponseEntity<StuffedAnimalResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(stuffedAnimalService.findById(id));
    }

    /**
     * ぬいぐるみ登録。
     * @AuthenticationPrincipal でログイン中のメールアドレスを取得する。
     */
    @PostMapping
    public ResponseEntity<StuffedAnimalResponse> create(
            @Valid @RequestBody StuffedAnimalRequest request,
            @AuthenticationPrincipal String userEmail) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(stuffedAnimalService.create(request, userEmail));
    }

    /**
     * ぬいぐるみ更新。
     * 自分のデータかADMINのみ更新可能。
     */
    @PutMapping("/{id}")
    public ResponseEntity<StuffedAnimalResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody StuffedAnimalRequest request,
            @AuthenticationPrincipal String userEmail) {
        return ResponseEntity.ok(stuffedAnimalService.update(id, request, userEmail));
    }

    /**
     * ぬいぐるみ削除。
     * 自分のデータかADMINのみ削除可能。
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal String userEmail) {
        stuffedAnimalService.delete(id, userEmail);
        return ResponseEntity.noContent().build();
    }

    /**
     * ダブりチェック。
     * 名前・シリーズ・キャラクターが一致するものを返す。
     */
    @GetMapping("/duplicate-check")
    public ResponseEntity<List<StuffedAnimalResponse>> duplicateCheck(
            @RequestParam String name,
            @RequestParam(required = false) Long seriesId,
            @RequestParam(required = false) String character) {
        return ResponseEntity.ok(stuffedAnimalService.checkDuplicate(name, seriesId, character));
    }
}