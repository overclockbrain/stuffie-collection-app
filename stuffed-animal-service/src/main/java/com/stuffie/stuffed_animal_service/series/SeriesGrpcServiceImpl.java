package com.stuffie.stuffed_animal_service.series;

import com.stuffie.grpc.series.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.security.access.AccessDeniedException;

import java.time.format.DateTimeFormatter;

/**
 * gRPCサーバーとしてSeriesServiceを公開する実装クラス。
 * 既存のSeriesService（ビジネスロジック）をそのまま呼び出し、
 * gRPCのリクエスト/レスポンス形式に変換するだけの薄いラッパー。
 */
@GrpcService
@RequiredArgsConstructor
public class SeriesGrpcServiceImpl extends SeriesServiceGrpc.SeriesServiceImplBase {

    private final SeriesService seriesService;

    @Override
    public void getSeriesList(com.stuffie.grpc.common.Empty request,
                               StreamObserver<GetSeriesListResponse> responseObserver) {
        var list = seriesService.findAll();

        GetSeriesListResponse.Builder builder = GetSeriesListResponse.newBuilder();
        list.forEach(s -> builder.addSeries(toMessage(s)));

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void createSeries(CreateSeriesRequest request,
                              StreamObserver<SeriesMessage> responseObserver) {
        try {
            com.stuffie.stuffed_animal_service.series.SeriesRequest restRequest =
                    new com.stuffie.stuffed_animal_service.series.SeriesRequest();
            restRequest.setName(request.getName());

            var result = seriesService.create(restRequest, request.getUserEmail());

            responseObserver.onNext(toMessage(result));
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            // 名前重複などの業務エラー
            responseObserver.onError(
                    Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException()
            );
        }
    }

    @Override
    public void updateSeries(UpdateSeriesRequest request,
                              StreamObserver<SeriesMessage> responseObserver) {
        try {
            com.stuffie.stuffed_animal_service.series.SeriesRequest restRequest =
                    new com.stuffie.stuffed_animal_service.series.SeriesRequest();
            restRequest.setName(request.getName());

            var result = seriesService.update(request.getId(), restRequest, request.getUserEmail());

            responseObserver.onNext(toMessage(result));
            responseObserver.onCompleted();
        } catch (AccessDeniedException e) {
            // 権限エラー（本人・ADMIN以外）
            responseObserver.onError(
                    Status.PERMISSION_DENIED.withDescription(e.getMessage()).asRuntimeException()
            );
        } catch (IllegalArgumentException e) {
            // 該当データなし・名前重複
            responseObserver.onError(
                    Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException()
            );
        }
    }

    @Override
    public void deleteSeries(DeleteSeriesRequest request,
                              StreamObserver<com.stuffie.grpc.common.Empty> responseObserver) {
        try {
            seriesService.delete(request.getId(), request.getUserEmail());

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

    /** 既存のSeriesResponse（REST用DTO） → gRPCのSeriesMessageに変換 */
    private SeriesMessage toMessage(com.stuffie.stuffed_animal_service.series.SeriesResponse dto) {
        return SeriesMessage.newBuilder()
                .setId(dto.getId())
                .setName(dto.getName())
                .setCreatedBy(dto.getCreatedBy())
                .setCreatedAt(dto.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();
    }
}