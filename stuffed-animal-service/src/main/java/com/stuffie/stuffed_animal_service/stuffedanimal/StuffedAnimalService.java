package com.stuffie.stuffed_animal_service.stuffedanimal;
import com.stuffie.stuffed_animal_service.series.Series;
import com.stuffie.stuffed_animal_service.auth.User;
import com.stuffie.stuffed_animal_service.series.SeriesRepository;
import com.stuffie.stuffed_animal_service.auth.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * ぬいぐるみ管理のビジネスロジックを担うサービスクラス。
 * アクセス制御・ダブりチェックもここで行う。
 */
@Service
@RequiredArgsConstructor
public class StuffedAnimalService {

    private final StuffedAnimalRepository stuffedAnimalRepository;
    private final SeriesRepository seriesRepository;
    private final UserRepository userRepository;

    /**
     * ぬいぐるみ一覧を取得する。
     * 名前・シリーズ・キャラクターで絞り込み可能。
     */
    public List<StuffedAnimalResponse> findAll(String name, Long seriesId, String character) {
        return stuffedAnimalRepository.findByFilters(name, seriesId, character)
                .stream()
                .map(StuffedAnimalResponse::from)
                .toList();
    }

    /**
     * ぬいぐるみ1件を取得する。
     *
     * @throws IllegalArgumentException 該当データが存在しない場合
     */
    public StuffedAnimalResponse findById(Long id) {
        return StuffedAnimalResponse.from(getEntityById(id));
    }

    /**
     * ぬいぐるみを新規登録する。
     *
     * @param request   登録リクエスト
     * @param userEmail ログイン中のユーザーのメールアドレス
     */
    @Transactional
    public StuffedAnimalResponse create(StuffedAnimalRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);

        StuffedAnimal entity = new StuffedAnimal();
        entity.setName(request.getName());
        entity.setCharacter(request.getCharacter());
        entity.setPurchaseDate(request.getPurchaseDate());
        entity.setPurchasePlace(request.getPurchasePlace());
        entity.setImageUrl(request.getImageUrl());
        entity.setNotes(request.getNotes());
        entity.setCreatedBy(user);
        entity.setUpdatedBy(user);

        // シリーズが指定されている場合は紐付ける
        if (request.getSeriesId() != null) {
            Series series = seriesRepository.findById(request.getSeriesId())
                    .orElseThrow(() -> new IllegalArgumentException("指定されたシリーズが見つかりません"));
            entity.setSeries(series);
        }

        return StuffedAnimalResponse.from(stuffedAnimalRepository.save(entity));
    }

    /**
     * ぬいぐるみを更新する。
     * 自分が登録したデータかADMINのみ更新可能。
     *
     * @throws AccessDeniedException 権限がない場合
     */
    @Transactional
    public StuffedAnimalResponse update(Long id, StuffedAnimalRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);
        StuffedAnimal entity = getEntityById(id);

        // アクセス制御: 自分のデータかADMINのみ更新可
        checkOwnerOrAdmin(entity, user);

        entity.setName(request.getName());
        entity.setCharacter(request.getCharacter());
        entity.setPurchaseDate(request.getPurchaseDate());
        entity.setPurchasePlace(request.getPurchasePlace());
        entity.setImageUrl(request.getImageUrl());
        entity.setNotes(request.getNotes());
        entity.setUpdatedBy(user);

        if (request.getSeriesId() != null) {
            Series series = seriesRepository.findById(request.getSeriesId())
                    .orElseThrow(() -> new IllegalArgumentException("指定されたシリーズが見つかりません"));
            entity.setSeries(series);
        } else {
            entity.setSeries(null);
        }

        return StuffedAnimalResponse.from(stuffedAnimalRepository.save(entity));
    }

    /**
     * ぬいぐるみを削除する。
     * 自分が登録したデータかADMINのみ削除可能。
     *
     * @throws AccessDeniedException 権限がない場合
     */
    @Transactional
    public void delete(Long id, String userEmail) {
        User user = getUserByEmail(userEmail);
        StuffedAnimal entity = getEntityById(id);

        // アクセス制御: 自分のデータかADMINのみ削除可
        checkOwnerOrAdmin(entity, user);

        stuffedAnimalRepository.delete(entity);
    }

    /**
     * ダブりチェックを行う。
     * 名前・シリーズ・キャラクターが一致するものを返す。
     */
    public List<StuffedAnimalResponse> checkDuplicate(String name, Long seriesId, String character) {
        return stuffedAnimalRepository.findDuplicates(name, seriesId, character)
                .stream()
                .map(StuffedAnimalResponse::from)
                .toList();
    }

    /**
     * IDでエンティティを取得する共通処理。
     */
    private StuffedAnimal getEntityById(Long id) {
        return stuffedAnimalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("指定されたぬいぐるみが見つかりません"));
    }

    /**
     * メールアドレスでユーザーを取得する共通処理。
     */
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("ユーザーが見つかりません"));
    }

    /**
     * 自分のデータかADMINかチェックする共通処理。
     */
    private void checkOwnerOrAdmin(StuffedAnimal entity, User user) {
        boolean isOwner = entity.getCreatedBy().getId().equals(user.getId());
        boolean isAdmin = "ADMIN".equals(user.getRole());
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("このデータを操作する権限がありません");
        }
    }
}