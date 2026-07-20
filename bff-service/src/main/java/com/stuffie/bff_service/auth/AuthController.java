package com.stuffie.bff_service.auth;

import io.grpc.StatusRuntimeException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * フロントエンド向けの認証エンドポイント。
 * リクエストを受け取り、AuthGrpcClient経由でstuffed-animal-serviceに処理を委譲する。
 * フロントから見たURLパス・形式は今まで通り変えていない。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthGrpcClient authGrpcClient;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            var grpcResponse = authGrpcClient.register(
                    request.getUsername(), request.getEmail(), request.getPassword()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(toDto(grpcResponse));
        } catch (StatusRuntimeException e) {
            // gRPCのINVALID_ARGUMENT（メール重複など）はフロントに400で返す
            return ResponseEntity.badRequest().body(errorBody(e));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            var grpcResponse = authGrpcClient.login(request.getEmail(), request.getPassword());
            return ResponseEntity.ok(toDto(grpcResponse));
        } catch (StatusRuntimeException e) {
            // gRPCのUNAUTHENTICATED（認証失敗）はフロントに401で返す
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorBody(e));
        }
    }

    /** gRPCのAuthResponse（生成コード）をフロント向けDTOに変換する */
    private AuthResponseDto toDto(com.stuffie.grpc.auth.AuthResponse grpcResponse) {
        AuthResponseDto dto = new AuthResponseDto();
        dto.setAccessToken(grpcResponse.getAccessToken());
        dto.setTokenType(grpcResponse.getTokenType());
        dto.setExpiresIn(grpcResponse.getExpiresIn());
        return dto;
    }

    /** gRPCエラーからフロントに返すエラーメッセージを組み立てる */
    private java.util.Map<String, String> errorBody(StatusRuntimeException e) {
        return java.util.Map.of("message", e.getStatus().getDescription());
    }
}
