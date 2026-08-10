package com.stuffie.bff_service.series;

import com.stuffie.grpc.series.*;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * stuffed-animal-serviceのSeriesServiceをgRPCで呼び出すクライアント。
 */
@Component
public class SeriesGrpcClient {

    @GrpcClient("stuffed-animal-service")
    private SeriesServiceGrpc.SeriesServiceBlockingStub seriesServiceStub;

    /** シリーズ一覧取得 */
    public List<SeriesMessage> getSeriesList() {
        var response = seriesServiceStub.getSeriesList(com.stuffie.grpc.common.Empty.newBuilder().build());
        return response.getSeriesList();
    }

    /** シリーズ登録 */
    public SeriesMessage createSeries(String name, String userEmail) {
        CreateSeriesRequest request = CreateSeriesRequest.newBuilder()
                .setName(name)
                .setUserEmail(userEmail)
                .build();
        return seriesServiceStub.createSeries(request);
    }

    /** シリーズ更新 */
    public SeriesMessage updateSeries(Long id, String name, String userEmail) {
        UpdateSeriesRequest request = UpdateSeriesRequest.newBuilder()
                .setId(id)
                .setName(name)
                .setUserEmail(userEmail)
                .build();
        return seriesServiceStub.updateSeries(request);
    }

    /** シリーズ削除 */
    public void deleteSeries(Long id, String userEmail) {
        DeleteSeriesRequest request = DeleteSeriesRequest.newBuilder()
                .setId(id)
                .setUserEmail(userEmail)
                .build();
        seriesServiceStub.deleteSeries(request);
    }
}