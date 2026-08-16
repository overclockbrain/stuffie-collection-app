package com.stuffie.bff_service.stuffedanimal;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * フロントエンド向けの画像アップロードエンドポイント。
 * フロントから見ると「普通のmultipart/form-dataアップロード」に見えるが、
 * 内部ではImageGrpcClientを通してgRPC Streamingでstuffed-animal-serviceに転送している。
 * フロントはgRPCの存在を意識する必要がない。
 */
@RestController
@RequestMapping("/api/stuffed-animals")
@RequiredArgsConstructor
public class ImageController {

    private final ImageGrpcClient imageGrpcClient;

    /** アップロードを許可する最大サイズ（15MB）。これを超えたら早期に弾く。 */
    private static final long MAX_FILE_SIZE = 15 * 1024 * 1024;

    @PostMapping("/{id}/image")
    public ResponseEntity<?> uploadImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal String userEmail) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "ファイルが選択されていません"));
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            return ResponseEntity.badRequest().body(Map.of("message", "ファイルサイズが大きすぎます（15MBまで）"));
        }

        try {
            byte[] imageData = file.getBytes();
            String imageUrl = imageGrpcClient.uploadImage(id, userEmail, imageData);
            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "ファイルの読み込みに失敗しました"));
        } catch (InterruptedException e) {
            // このスレッドの割り込み状態を正しく復元しておく（Javaのお作法）
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "アップロード処理が中断されました"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "画像のアップロードに失敗しました"));
        }
    }
}