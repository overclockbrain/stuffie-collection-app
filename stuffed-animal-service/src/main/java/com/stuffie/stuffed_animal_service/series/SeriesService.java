package com.stuffie.stuffed_animal_service.series;
import com.stuffie.stuffed_animal_service.auth.User;
import com.stuffie.stuffed_animal_service.auth.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * シリーズ管理のビジネスロジックを担うサービスクラス。
 * ぬいぐるみと同様、自分が登録したデータかADMINのみ更新・削除できる。
 */
@Service
@RequiredArgsConstructor
public class SeriesService {

    private final SeriesRepository seriesRepository;
    private final UserRepository userRepository;

    /**
     * シリーズ一覧を取得する（プルダウン表示用）。
     * 全ユーザーが閲覧可能。
     */
    public List<SeriesResponse> findAll() {
        return seriesRepository.findAll()
                .stream()
                .map(SeriesResponse::from)
                .toList();
    }

    /**
     * シリーズを新規登録する。
     *
     * @throws IllegalArgumentException 同名のシリーズが既に存在する場合
     */
    @Transactional
    public SeriesResponse create(SeriesRequest request, String userEmail) {
        if (seriesRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("このシリーズは既に登録されています");
        }

        User user = getUserByEmail(userEmail);

        Series entity = new Series();
        entity.setName(request.getName());
        entity.setCreatedBy(user);

        return SeriesResponse.from(seriesRepository.save(entity));
    }

    /**
     * シリーズを更新する。
     * 自分が登録したデータかADMINのみ更新可能。
     *
     * @throws AccessDeniedException 権限がない場合
     */
    @Transactional
    public SeriesResponse update(Long id, SeriesRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);
        Series entity = getEntityById(id);

        checkOwnerOrAdmin(entity, user);

        // 自分自身以外で同名のシリーズがあれば重複エラー
        seriesRepository.findByName(request.getName())
                .filter(s -> !s.getId().equals(id))
                .ifPresent(s -> {
                    throw new IllegalArgumentException("このシリーズは既に登録されています");
                });

        entity.setName(request.getName());
        return SeriesResponse.from(seriesRepository.save(entity));
    }

    /**
     * シリーズを削除する。
     * 自分が登録したデータかADMINのみ削除可能。
     *
     * @throws AccessDeniedException 権限がない場合
     */
    @Transactional
    public void delete(Long id, String userEmail) {
        User user = getUserByEmail(userEmail);
        Series entity = getEntityById(id);

        checkOwnerOrAdmin(entity, user);

        seriesRepository.delete(entity);
    }

    /**
     * IDでエンティティを取得する共通処理。
     */
    private Series getEntityById(Long id) {
        return seriesRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("指定されたシリーズが見つかりません"));
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
     * StuffedAnimalServiceと同じアクセス制御ルールを適用する。
     */
    private void checkOwnerOrAdmin(Series entity, User user) {
        boolean isOwner = entity.getCreatedBy().getId().equals(user.getId());
        boolean isAdmin = "ADMIN".equals(user.getRole());
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("このデータを操作する権限がありません");
        }
    }
}