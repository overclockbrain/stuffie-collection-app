package com.stuffie.bff_service.stuffedanimal;

import com.google.protobuf.ByteString;
import com.stuffie.grpc.stuffedanimal.StuffedAnimalServiceGrpc;
import com.stuffie.grpc.stuffedanimal.UploadImageChunk;
import com.stuffie.grpc.stuffedanimal.UploadImageResponse;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * stuffed-animal-serviceのUploadImage（Client Streaming RPC）を呼び出すクライアント。
 *
 * 他のGrpcClient（AuthGrpcClientなど）は BlockingStub を使って
 * 「送って→待って→受け取る」だけのシンプルな形だったが、
 * Streaming RPCは複数回送る必要があるため、通常のStubを使い、
 * 「送信が全部終わってレスポンスが返るまで待つ」ための仕組みを自前で用意している。
 */
@Component
public class ImageGrpcClient {

    // Streaming RPCは非同期用の通常のStubを使う（BlockingStubはUnary RPC専用のため）
    @GrpcClient("stuffed-animal-service")
    private StuffedAnimalServiceGrpc.StuffedAnimalServiceStub stub;

    /** 1回のチャンクで送るデータサイズの目安（64KB） */
    private static final int CHUNK_SIZE = 64 * 1024;

    /**
     * 画像データをチャンクに分割してstuffed-animal-serviceに送信し、
     * 保存された画像の公開URLを返す。
     *
     * @param animalId  画像を紐付けるぬいぐるみのID
     * @param userEmail アップロードしたユーザーのメールアドレス（権限チェック用）
     * @param imageData 画像ファイルの生データ
     */
    public String uploadImage(Long animalId, String userEmail, byte[] imageData) throws InterruptedException {
        // 非同期で返ってくる結果を受け取るための入れ物
        AtomicReference<String> resultUrl = new AtomicReference<>();
        AtomicReference<Throwable> resultError = new AtomicReference<>();
        // 「サーバーからのレスポンスが返ってくるまで、このメソッドの呼び出し元を待たせる」ための仕組み
        CountDownLatch latch = new CountDownLatch(1);

        // サーバーからの応答（onNext/onError/onCompleted）を受け取る側
        StreamObserver<UploadImageResponse> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(UploadImageResponse response) {
                resultUrl.set(response.getImageUrl());
            }

            @Override
            public void onError(Throwable t) {
                resultError.set(t);
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        };

        // stub.uploadImage(responseObserver) を呼ぶと、
        // 「チャンクを送るための送信用StreamObserver」が返ってくる
        StreamObserver<UploadImageChunk> requestObserver = stub.uploadImage(responseObserver);

        try {
            // 画像データを CHUNK_SIZE ごとに分割して送信する
            int offset = 0;
            boolean isFirstChunk = true;

            while (offset < imageData.length) {
                int length = Math.min(CHUNK_SIZE, imageData.length - offset);
                ByteString chunkData = ByteString.copyFrom(imageData, offset, length);

                UploadImageChunk.Builder chunkBuilder = UploadImageChunk.newBuilder()
                        .setData(chunkData);

                // メタ情報（animalId・userEmail）は最初のチャンクにだけ乗せる
                if (isFirstChunk) {
                    chunkBuilder.setAnimalId(animalId).setUserEmail(userEmail);
                    isFirstChunk = false;
                }

                requestObserver.onNext(chunkBuilder.build());
                offset += length;
            }

            // 全チャンクを送り終えたことをサーバーに伝える
            requestObserver.onCompleted();
        } catch (RuntimeException e) {
            requestObserver.onError(e);
            throw e;
        }

        // サーバー側の処理（圧縮・保存）が終わってレスポンスが返るまで、ここで待機する
        // （Streaming RPCは非同期なので、待たないとメソッドがレスポンスを受け取る前に終わってしまう）
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        if (!completed) {
            throw new RuntimeException("画像アップロードがタイムアウトしました");
        }
        if (resultError.get() != null) {
            throw new RuntimeException(resultError.get());
        }

        return resultUrl.get();
    }
}