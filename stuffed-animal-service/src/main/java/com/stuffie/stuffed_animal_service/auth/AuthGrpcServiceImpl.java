package com.stuffie.stuffed_animal_service.auth;

import com.stuffie.grpc.auth.AuthResponse;
import com.stuffie.grpc.auth.AuthServiceGrpc;
import com.stuffie.grpc.auth.LoginRequest;
import com.stuffie.grpc.auth.RegisterRequest;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * gRPCサーバーとしてAuthServiceを公開する実装クラス。
 * 既存のAuthService（ビジネスロジック）をそのまま呼び出し、
 * gRPCのリクエスト/レスポンス形式に変換するだけの薄いラッパー。
 *
 * @GrpcService により、このクラスがgRPCサーバーのエンドポイントとして自動登録される。
 */
@GrpcService
@RequiredArgsConstructor
public class AuthGrpcServiceImpl extends AuthServiceGrpc.AuthServiceImplBase {

    private final AuthService authService;

    @Override
    public void register(RegisterRequest request, StreamObserver<AuthResponse> responseObserver) {
        try {
            // gRPCのRegisterRequest → 既存のRegisterRequest（REST用DTO）に変換
            com.stuffie.stuffed_animal_service.auth.RegisterRequest restRequest =
                    new com.stuffie.stuffed_animal_service.auth.RegisterRequest();
            restRequest.setUsername(request.getUsername());
            restRequest.setEmail(request.getEmail());
            restRequest.setPassword(request.getPassword());

            var result = authService.register(restRequest);

            // 既存のAuthResponse（REST用DTO） → gRPCのAuthResponseに変換
            AuthResponse grpcResponse = AuthResponse.newBuilder()
                    .setAccessToken(result.getAccessToken())
                    .setTokenType(result.getTokenType())
                    .setExpiresIn(result.getExpiresIn())
                    .build();

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            // 業務エラー（メール重複など）はINVALID_ARGUMENTとして返す
            responseObserver.onError(
                    Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException()
            );
        }
    }

    @Override
    public void login(LoginRequest request, StreamObserver<AuthResponse> responseObserver) {
        try {
            com.stuffie.stuffed_animal_service.auth.LoginRequest restRequest =
                    new com.stuffie.stuffed_animal_service.auth.LoginRequest();
            restRequest.setEmail(request.getEmail());
            restRequest.setPassword(request.getPassword());

            var result = authService.login(restRequest);

            AuthResponse grpcResponse = AuthResponse.newBuilder()
                    .setAccessToken(result.getAccessToken())
                    .setTokenType(result.getTokenType())
                    .setExpiresIn(result.getExpiresIn())
                    .build();

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            // 認証失敗はUNAUTHENTICATEDとして返す
            responseObserver.onError(
                    Status.UNAUTHENTICATED.withDescription(e.getMessage()).asRuntimeException()
            );
        }
    }
}