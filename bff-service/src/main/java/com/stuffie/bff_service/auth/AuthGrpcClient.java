package com.stuffie.bff_service.auth;

import com.stuffie.grpc.auth.AuthResponse;
import com.stuffie.grpc.auth.AuthServiceGrpc;
import com.stuffie.grpc.auth.LoginRequest;
import com.stuffie.grpc.auth.RegisterRequest;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

/**
 * stuffed-animal-serviceのAuthServiceをgRPCで呼び出すクライアント。
 * BFFはこのクラスを通して認証処理を委譲する。
 *
 * @GrpcClient により、application.yamlで指定した接続先への
 * gRPCチャネルとスタブが自動生成される。
 */
@Component
public class AuthGrpcClient {

    // application.yaml の grpc.client.stuffed-animal-service で接続先を指定する
    @GrpcClient("stuffed-animal-service")
    private AuthServiceGrpc.AuthServiceBlockingStub authServiceStub;

    /**
     * ユーザー登録をgRPC経由でstuffed-animal-serviceに委譲する。
     */
    public AuthResponse register(String username, String email, String password) {
        RegisterRequest request = RegisterRequest.newBuilder()
                .setUsername(username)
                .setEmail(email)
                .setPassword(password)
                .build();
        return authServiceStub.register(request);
    }

    /**
     * ログインをgRPC経由でstuffed-animal-serviceに委譲する。
     */
    public AuthResponse login(String email, String password) {
        LoginRequest request = LoginRequest.newBuilder()
                .setEmail(email)
                .setPassword(password)
                .build();
        return authServiceStub.login(request);
    }
}
