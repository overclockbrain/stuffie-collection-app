package com.stuffie.stuffed_animal_service.stuffedanimal;

import com.google.protobuf.ByteString;
import com.stuffie.grpc.stuffedanimal.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.security.access.AccessDeniedException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * gRPCサーバーとしてStuffedAnimalServiceを公開する実装クラス。
 * 既存のStuffedAnimalService（ビジネスロジック）をそのまま呼び出し、
 * gRPCのリクエスト/レスポンス形式に変換するだけの薄いラッパー。
 *
 * 画像アップロード（uploadImage）だけは他のメソッドと形が異なる。
 * 理由は下のuploadImageのコメントで説明する。
 */
@GrpcService
@RequiredArgsConstructor
public class StuffedAnimalGrpcServiceImpl extends StuffedAnimalServiceGrpc.StuffedAnimalServiceImplBase {

    // 画像の圧縮・MinIOアップロードを担当するサービス
    private final ImageService imageService;
    // ぬいぐるみのCRUDなど既存のビジネスロジックを担当するサービス
    private final StuffedAnimalService stuffedAnimalService;

    /**
     * 一覧取得（Unary RPC：1回のリクエストに1回のレスポンスを返すだけのシンプルな形）。
     */
    @Override
    public void getAnimals(GetAnimalsRequest request, StreamObserver<GetAnimalsResponse> responseObserver) {
        // gRPCのoptionalフィールドは hasXxx() で「値が設定されているか」を先に確認してから取り出す
        String name = request.hasName() ? request.getName() : null;
        Long seriesId = request.hasSeriesId() ? request.getSeriesId() : null;
        String character = request.hasCharacter() ? request.getCharacter() : null;

        var list = stuffedAnimalService.findAll(name, seriesId, character);

        // 複数件のレスポンスは repeated フィールドにまとめて1回で返す
        GetAnimalsResponse.Builder builder = GetAnimalsResponse.newBuilder();
        list.forEach(a -> builder.addAnimals(toMessage(a)));

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    /**
     * 1件取得。
     */
    @Override
    public void getAnimal(GetAnimalRequest request, StreamObserver<StuffedAnimalMessage> responseObserver) {
        try {
            var result = stuffedAnimalService.findById(request.getId());
            responseObserver.onNext(toMessage(result));
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            // 該当データなし → gRPCのNOT_FOUNDに変換して返す
            responseObserver.onError(
                    Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException()
            );
        }
    }

    /**
     * 登録。
     */
    @Override
    public void createAnimal(CreateAnimalRequest request, StreamObserver<StuffedAnimalMessage> responseObserver) {
        try {
            // gRPCのCreateAnimalRequest → 既存のREST用StuffedAnimalRequestに詰め替える
            com.stuffie.stuffed_animal_service.stuffedanimal.StuffedAnimalRequest restRequest =
                    new com.stuffie.stuffed_animal_service.stuffedanimal.StuffedAnimalRequest();
            restRequest.setName(request.getName());
            restRequest.setSeriesId(request.hasSeriesId() ? request.getSeriesId() : null);
            restRequest.setCharacter(request.hasCharacter() ? request.getCharacter() : null);
            restRequest.setPurchaseDate(request.hasPurchaseDate() ? LocalDate.parse(request.getPurchaseDate()) : null);
            restRequest.setPurchasePlace(request.hasPurchasePlace() ? request.getPurchasePlace() : null);
            restRequest.setImageUrl(request.hasImageUrl() ? request.getImageUrl() : null);
            restRequest.setNotes(request.hasNotes() ? request.getNotes() : null);

            // 既存のビジネスロジック（バリデーション・DB保存）はそのまま利用する
            var result = stuffedAnimalService.create(restRequest, request.getUserEmail());

            responseObserver.onNext(toMessage(result));
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException()
            );
        }
    }

    /**
     * 更新（本人かADMINのみ実行可能。権限チェックはStuffedAnimalService側で行っている）。
     */
    @Override
    public void updateAnimal(UpdateAnimalRequest request, StreamObserver<StuffedAnimalMessage> responseObserver) {
        try {
            com.stuffie.stuffed_animal_service.stuffedanimal.StuffedAnimalRequest restRequest =
                    new com.stuffie.stuffed_animal_service.stuffedanimal.StuffedAnimalRequest();
            restRequest.setName(request.getName());
            restRequest.setSeriesId(request.hasSeriesId() ? request.getSeriesId() : null);
            restRequest.setCharacter(request.hasCharacter() ? request.getCharacter() : null);
            restRequest.setPurchaseDate(request.hasPurchaseDate() ? LocalDate.parse(request.getPurchaseDate()) : null);
            restRequest.setPurchasePlace(request.hasPurchasePlace() ? request.getPurchasePlace() : null);
            restRequest.setImageUrl(request.hasImageUrl() ? request.getImageUrl() : null);
            restRequest.setNotes(request.hasNotes() ? request.getNotes() : null);

            var result = stuffedAnimalService.update(request.getId(), restRequest, request.getUserEmail());

            responseObserver.onNext(toMessage(result));
            responseObserver.onCompleted();
        } catch (AccessDeniedException e) {
            // 自分のデータでもADMINでもない → gRPCのPERMISSION_DENIEDに変換
            responseObserver.onError(
                    Status.PERMISSION_DENIED.withDescription(e.getMessage()).asRuntimeException()
            );
        } catch (IllegalArgumentException e) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException()
            );
        }
    }

    /**
     * 削除（本人かADMINのみ実行可能）。
     */
    @Override
    public void deleteAnimal(DeleteAnimalRequest request, StreamObserver<com.stuffie.grpc.common.Empty> responseObserver) {
        try {
            stuffedAnimalService.delete(request.getId(), request.getUserEmail());

            // 削除は返すデータが無いので、共通のEmptyメッセージを返す
            responseObserver.onNext(com.stuffie.grpc.common.Empty.newBuilder().build());
            responseObserver.onCompleted();
        } catch (AccessDeniedException e) {
            responseObserver.onError(
                    Status.PERMISSION_DENIED.withDescription(e.getMessage()).asRuntimeException()
            );
        } catch (IllegalArgumentException e) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException()
            );
        }
    }

    /**
     * ダブりチェック。
     */
    @Override
    public void checkDuplicate(CheckDuplicateRequest request, StreamObserver<CheckDuplicateResponse> responseObserver) {
        Long seriesId = request.hasSeriesId() ? request.getSeriesId() : null;
        String character = request.hasCharacter() ? request.getCharacter() : null;

        var duplicates = stuffedAnimalService.checkDuplicate(request.getName(), seriesId, character);

        CheckDuplicateResponse.Builder builder = CheckDuplicateResponse.newBuilder();
        duplicates.forEach(d -> builder.addDuplicates(toMessage(d)));

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    /**
     * 既存のStuffedAnimalResponse（REST用DTO） → gRPCのStuffedAnimalMessageに変換する共通処理。
     * 全メソッドで使い回すことで、変換ロジックを1箇所にまとめている。
     */
    private StuffedAnimalMessage toMessage(com.stuffie.stuffed_animal_service.stuffedanimal.StuffedAnimalResponse dto) {
        StuffedAnimalMessage.Builder builder = StuffedAnimalMessage.newBuilder()
                .setId(dto.getId())
                .setName(dto.getName())
                .setCreatedBy(dto.getCreatedBy())
                .setUpdatedBy(dto.getUpdatedBy())
                .setCreatedAt(dto.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .setUpdatedAt(dto.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        // optionalフィールドはnullでない場合のみセットする（protoのoptionalはセットしないと「未設定」のまま扱われる）
        if (dto.getSeriesId() != null) builder.setSeriesId(dto.getSeriesId());
        if (dto.getSeriesName() != null) builder.setSeriesName(dto.getSeriesName());
        if (dto.getCharacter() != null) builder.setCharacter(dto.getCharacter());
        if (dto.getPurchaseDate() != null) builder.setPurchaseDate(dto.getPurchaseDate().toString());
        if (dto.getPurchasePlace() != null) builder.setPurchasePlace(dto.getPurchasePlace());
        if (dto.getImageUrl() != null) builder.setImageUrl(dto.getImageUrl());
        if (dto.getNotes() != null) builder.setNotes(dto.getNotes());

        return builder.build();
    }

    /**
     * 画像アップロード（Client Streaming RPC）。
     *
     * これまでのメソッドと形が違う理由:
     * 他のメソッドは「1回のリクエスト→1回のレスポンス」（Unary RPC）だったが、
     * 画像データは大きくなり得るため、クライアント側で細かく分割（チャンク）して
     * 何回にも分けて送ってもらう「Client Streaming RPC」という形式を使っている。
     *
     * そのため戻り値も他とは異なり、
     * 「チャンクが届くたびに呼ばれる受信係（StreamObserver）」を組み立てて返す、
     * という特殊な形になっている。
     */
    @Override
    public StreamObserver<UploadImageChunk> uploadImage(StreamObserver<UploadImageResponse> responseObserver) {
        // 匿名クラスで「受信係」をその場で作って返す。
        // このオブジェクトのonNext/onError/onCompletedが、チャンクの送受信に応じて
        // gRPCの内部から自動的に呼び出される。
        return new StreamObserver<UploadImageChunk>() {

            // 受信したチャンクを最終的に1つの画像データにまとめるためのバッファ
            private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            // アップロード対象のぬいぐるみID（最初のチャンクにだけ入ってくる想定）
            private Long animalId;
            // アップロードしたユーザーのメールアドレス（権限チェックに使う。最初のチャンクにだけ入ってくる想定）
            private String userEmail;

            /**
             * クライアントがチャンクを1つ送ってくるたびに呼ばれる。
             * 画像1枚につき、この onNext が複数回呼ばれることになる。
             */
            @Override
            public void onNext(UploadImageChunk chunk) {
                // メタ情報（animalId・userEmail）は最初のチャンクにしか入っていない前提なので、
                // まだ取得していなければここで取得する
                if (animalId == null) {
                    animalId = chunk.getAnimalId();
                    userEmail = chunk.getUserEmail();
                }

                // チャンクの実データ部分をバッファに追記していく
                ByteString data = chunk.getData();
                try {
                    data.writeTo(buffer);
                } catch (IOException e) {
                    responseObserver.onError(
                            Status.INTERNAL.withDescription("画像データの受信に失敗しました").asRuntimeException()
                    );
                }
            }

            /**
             * クライアント側で通信エラーなどが起きて送信が中断された場合に呼ばれる。
             * 今回は特にリカバリ処理は行わない（ログだけ残す等の拡張は将来的に可能）。
             */
            @Override
            public void onError(Throwable t) {
                // 現時点では何もしない
            }

            /**
             * クライアントが「これで送信は終わりです」と伝えてきたときに、1回だけ呼ばれる。
             * ここまでバッファに貯めてきたデータを使って、実際の圧縮・保存処理を行うのはこのタイミング。
             */
            @Override
            public void onCompleted() {
                try {
                    // ① 画像を圧縮してMinIOにアップロードし、公開URLを受け取る
                    String imageUrl = imageService.compressAndUpload(animalId, buffer.toByteArray());

                    // ② 権限チェックをした上で、対象のぬいぐるみのimageUrlをDBに反映する
                    stuffedAnimalService.updateImageUrl(animalId, imageUrl, userEmail);

                    // ③ アップロード結果（画像URL）をクライアントに一度だけ返す
                    responseObserver.onNext(UploadImageResponse.newBuilder().setImageUrl(imageUrl).build());
                    responseObserver.onCompleted();
                } catch (AccessDeniedException e) {
                    // 本人でもADMINでもないユーザーが他人のぬいぐるみに画像をつけようとした場合
                    responseObserver.onError(
                            Status.PERMISSION_DENIED.withDescription(e.getMessage()).asRuntimeException()
                    );
                } catch (IllegalArgumentException e) {
                    // 対象のぬいぐるみが存在しない場合など
                    responseObserver.onError(
                            Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException()
                    );
                } catch (IOException e) {
                    // 画像として読み込めない・MinIOへの保存に失敗した場合など
                    responseObserver.onError(
                            Status.INTERNAL.withDescription("画像の処理に失敗しました").asRuntimeException()
                    );
                }
            }
        };
    }
}