package com.stuffie.bff_service.stuffedanimal;

import com.stuffie.grpc.stuffedanimal.StuffedAnimalMessage;
import io.grpc.StatusRuntimeException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * フロントエンド向けのぬいぐるみ管理エンドポイント。
 */
@RestController
@RequestMapping("/api/stuffed-animals")
@RequiredArgsConstructor
public class StuffedAnimalController {

    private final StuffedAnimalGrpcClient client;

    @GetMapping
    public ResponseEntity<List<StuffedAnimalResponseDto>> findAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long seriesId,
            @RequestParam(required = false) String character) {
        var list = client.getAnimals(name, seriesId, character).stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(toDto(client.getAnimal(id)));
        } catch (StatusRuntimeException e) {
            return ResponseEntity.status(toHttpStatus(e)).body(errorBody(e));
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody StuffedAnimalRequest request,
                                     @AuthenticationPrincipal String userEmail) {
        try {
            var result = client.createAnimal(request, userEmail);
            return ResponseEntity.status(HttpStatus.CREATED).body(toDto(result));
        } catch (StatusRuntimeException e) {
            return ResponseEntity.status(toHttpStatus(e)).body(errorBody(e));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                     @Valid @RequestBody StuffedAnimalRequest request,
                                     @AuthenticationPrincipal String userEmail) {
        try {
            var result = client.updateAnimal(id, request, userEmail);
            return ResponseEntity.ok(toDto(result));
        } catch (StatusRuntimeException e) {
            return ResponseEntity.status(toHttpStatus(e)).body(errorBody(e));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id,
                                     @AuthenticationPrincipal String userEmail) {
        try {
            client.deleteAnimal(id, userEmail);
            return ResponseEntity.noContent().build();
        } catch (StatusRuntimeException e) {
            return ResponseEntity.status(toHttpStatus(e)).body(errorBody(e));
        }
    }

    @GetMapping("/duplicate-check")
    public ResponseEntity<List<StuffedAnimalResponseDto>> duplicateCheck(
            @RequestParam String name,
            @RequestParam(required = false) Long seriesId,
            @RequestParam(required = false) String character) {
        var list = client.checkDuplicate(name, seriesId, character).stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(list);
    }

    /** gRPCのStuffedAnimalMessage（生成コード）をフロント向けDTOに変換する */
    private StuffedAnimalResponseDto toDto(StuffedAnimalMessage message) {
        StuffedAnimalResponseDto dto = new StuffedAnimalResponseDto();
        dto.setId(message.getId());
        dto.setName(message.getName());
        dto.setSeriesId(message.hasSeriesId() ? message.getSeriesId() : null);
        dto.setSeriesName(message.hasSeriesName() ? message.getSeriesName() : null);
        dto.setCharacter(message.hasCharacter() ? message.getCharacter() : null);
        dto.setPurchaseDate(message.hasPurchaseDate() ? message.getPurchaseDate() : null);
        dto.setPurchasePlace(message.hasPurchasePlace() ? message.getPurchasePlace() : null);
        dto.setImageUrl(message.hasImageUrl() ? message.getImageUrl() : null);
        dto.setNotes(message.hasNotes() ? message.getNotes() : null);
        dto.setCreatedBy(message.getCreatedBy());
        dto.setUpdatedBy(message.getUpdatedBy());
        dto.setCreatedAt(message.getCreatedAt());
        dto.setUpdatedAt(message.getUpdatedAt());
        return dto;
    }

    /** gRPCのStatusコードをHTTPステータスコードに変換する */
    private HttpStatus toHttpStatus(StatusRuntimeException e) {
        return switch (e.getStatus().getCode()) {
            case PERMISSION_DENIED -> HttpStatus.FORBIDDEN;
            case INVALID_ARGUMENT -> HttpStatus.BAD_REQUEST;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    private java.util.Map<String, String> errorBody(StatusRuntimeException e) {
        return java.util.Map.of("message", e.getStatus().getDescription());
    }
}