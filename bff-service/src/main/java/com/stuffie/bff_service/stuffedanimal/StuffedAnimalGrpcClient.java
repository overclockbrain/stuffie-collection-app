package com.stuffie.bff_service.stuffedanimal;

import com.stuffie.grpc.stuffedanimal.*;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * stuffed-animal-serviceのStuffedAnimalServiceをgRPCで呼び出すクライアント。
 */
@Component
public class StuffedAnimalGrpcClient {

    @GrpcClient("stuffed-animal-service")
    private StuffedAnimalServiceGrpc.StuffedAnimalServiceBlockingStub stub;

    /** 一覧取得（絞り込み可能） */
    public List<StuffedAnimalMessage> getAnimals(String name, Long seriesId, String character) {
        GetAnimalsRequest.Builder builder = GetAnimalsRequest.newBuilder();
        if (name != null) builder.setName(name);
        if (seriesId != null) builder.setSeriesId(seriesId);
        if (character != null) builder.setCharacter(character);

        return stub.getAnimals(builder.build()).getAnimalsList();
    }

    /** 1件取得 */
    public StuffedAnimalMessage getAnimal(Long id) {
        return stub.getAnimal(GetAnimalRequest.newBuilder().setId(id).build());
    }

    /** 登録 */
    public StuffedAnimalMessage createAnimal(StuffedAnimalRequest request, String userEmail) {
        CreateAnimalRequest.Builder builder = CreateAnimalRequest.newBuilder()
                .setName(request.getName())
                .setUserEmail(userEmail);
        if (request.getSeriesId() != null) builder.setSeriesId(request.getSeriesId());
        if (request.getCharacter() != null) builder.setCharacter(request.getCharacter());
        if (request.getPurchaseDate() != null) builder.setPurchaseDate(request.getPurchaseDate());
        if (request.getPurchasePlace() != null) builder.setPurchasePlace(request.getPurchasePlace());
        if (request.getImageUrl() != null) builder.setImageUrl(request.getImageUrl());
        if (request.getNotes() != null) builder.setNotes(request.getNotes());

        return stub.createAnimal(builder.build());
    }

    /** 更新 */
    public StuffedAnimalMessage updateAnimal(Long id, StuffedAnimalRequest request, String userEmail) {
        UpdateAnimalRequest.Builder builder = UpdateAnimalRequest.newBuilder()
                .setId(id)
                .setName(request.getName())
                .setUserEmail(userEmail);
        if (request.getSeriesId() != null) builder.setSeriesId(request.getSeriesId());
        if (request.getCharacter() != null) builder.setCharacter(request.getCharacter());
        if (request.getPurchaseDate() != null) builder.setPurchaseDate(request.getPurchaseDate());
        if (request.getPurchasePlace() != null) builder.setPurchasePlace(request.getPurchasePlace());
        if (request.getImageUrl() != null) builder.setImageUrl(request.getImageUrl());
        if (request.getNotes() != null) builder.setNotes(request.getNotes());

        return stub.updateAnimal(builder.build());
    }

    /** 削除 */
    public void deleteAnimal(Long id, String userEmail) {
        stub.deleteAnimal(DeleteAnimalRequest.newBuilder()
                .setId(id)
                .setUserEmail(userEmail)
                .build());
    }

    /** ダブりチェック */
    public List<StuffedAnimalMessage> checkDuplicate(String name, Long seriesId, String character) {
        CheckDuplicateRequest.Builder builder = CheckDuplicateRequest.newBuilder().setName(name);
        if (seriesId != null) builder.setSeriesId(seriesId);
        if (character != null) builder.setCharacter(character);

        return stub.checkDuplicate(builder.build()).getDuplicatesList();
    }
}