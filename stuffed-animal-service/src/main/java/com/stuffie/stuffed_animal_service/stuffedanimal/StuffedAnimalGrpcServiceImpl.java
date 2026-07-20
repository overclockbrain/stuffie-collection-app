package com.stuffie.stuffed_animal_service.stuffedanimal;

import com.stuffie.grpc.stuffedanimal.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * gRPCサーバーとしてStuffedAnimalServiceを公開する実装クラス。
 * 既存のStuffedAnimalService（ビジネスロジック）をそのまま呼び出し、
 * gRPCのリクエスト/レスポンス形式に変換するだけの薄いラッパー。
 */
@GrpcService
@RequiredArgsConstructor
public class StuffedAnimalGrpcServiceImpl extends StuffedAnimalServiceGrpc.StuffedAnimalServiceImplBase {

    private final StuffedAnimalService stuffedAnimalService;

    @Override
    public void getAnimals(GetAnimalsRequest request, StreamObserver<GetAnimalsResponse> responseObserver) {
        String name = request.hasName() ? request.getName() : null;
        Long seriesId = request.hasSeriesId() ? request.getSeriesId() : null;
        String character = request.hasCharacter() ? request.getCharacter() : null;

        var list = stuffedAnimalService.findAll(name, seriesId, character);

        GetAnimalsResponse.Builder builder = GetAnimalsResponse.newBuilder();
        list.forEach(a -> builder.addAnimals(toMessage(a)));

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getAnimal(GetAnimalRequest request, StreamObserver<StuffedAnimalMessage> responseObserver) {
        try {
            var result = stuffedAnimalService.findById(request.getId());
            responseObserver.onNext(toMessage(result));
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(
                    Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException()
            );
        }
    }

    @Override
    public void createAnimal(CreateAnimalRequest request, StreamObserver<StuffedAnimalMessage> responseObserver) {
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

            var result = stuffedAnimalService.create(restRequest, request.getUserEmail());

            responseObserver.onNext(toMessage(result));
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException()
            );
        }
    }

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
            responseObserver.onError(
                    Status.PERMISSION_DENIED.withDescription(e.getMessage()).asRuntimeException()
            );
        } catch (IllegalArgumentException e) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException()
            );
        }
    }

    @Override
    public void deleteAnimal(DeleteAnimalRequest request, StreamObserver<com.stuffie.grpc.common.Empty> responseObserver) {
        try {
            stuffedAnimalService.delete(request.getId(), request.getUserEmail());

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

    /** 既存のStuffedAnimalResponse（REST用DTO） → gRPCのStuffedAnimalMessageに変換 */
    private StuffedAnimalMessage toMessage(com.stuffie.stuffed_animal_service.stuffedanimal.StuffedAnimalResponse dto) {
        StuffedAnimalMessage.Builder builder = StuffedAnimalMessage.newBuilder()
                .setId(dto.getId())
                .setName(dto.getName())
                .setCreatedBy(dto.getCreatedBy())
                .setUpdatedBy(dto.getUpdatedBy())
                .setCreatedAt(dto.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .setUpdatedAt(dto.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        // optionalフィールドはnullでない場合のみセットする
        if (dto.getSeriesId() != null) builder.setSeriesId(dto.getSeriesId());
        if (dto.getSeriesName() != null) builder.setSeriesName(dto.getSeriesName());
        if (dto.getCharacter() != null) builder.setCharacter(dto.getCharacter());
        if (dto.getPurchaseDate() != null) builder.setPurchaseDate(dto.getPurchaseDate().toString());
        if (dto.getPurchasePlace() != null) builder.setPurchasePlace(dto.getPurchasePlace());
        if (dto.getImageUrl() != null) builder.setImageUrl(dto.getImageUrl());
        if (dto.getNotes() != null) builder.setNotes(dto.getNotes());

        return builder.build();
    }
}