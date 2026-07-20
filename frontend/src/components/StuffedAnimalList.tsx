import { type StuffedAnimal, deleteStuffedAnimal } from '../api/stuffedAnimal';
import '../styles/StuffedAnimalList.css';

interface Props {
    /** 表示するぬいぐるみ一覧 */
    animals: StuffedAnimal[];
    /** 削除後に一覧を再取得するコールバック */
    onChanged: () => void;
    /** 編集ボタン押下時に呼ばれるコールバック（編集対象を親に渡す） */
    onEdit: (animal: StuffedAnimal) => void;
}

export default function StuffedAnimalList({ animals, onChanged, onEdit }: Props) {
    const handleDelete = async (id: number, name: string) => {
        if (!confirm(`「${name}」を削除していいですか？`)) return;
        try {
            await deleteStuffedAnimal(id);
            onChanged();
        } catch {
            alert('削除に失敗しました（権限がないかもしれません）');
        }
    };

    if (animals.length === 0) {
        return (
            <div className="animal-empty">
                <p>まだぬいぐるみが登録されていません。可愛いぬいぐるみを登録してね！</p>
            </div>
        );
    }

    return (
        <div className="animal-grid">
            {animals.map((animal) => (
                <div key={animal.id} className="animal-card">
                    {/* 画像 */}
                    <div className="animal-image-area">
                        {animal.imageUrl ? (
                            <img src={animal.imageUrl} alt={animal.name} className="animal-image" />
                        ) : (
                            <div className="animal-no-image">🧸</div>
                        )}
                    </div>

                    {/* 情報 */}
                    <div className="animal-info">
                        <h3 className="animal-name">{animal.name}</h3>

                        {animal.seriesName && (
                            <span className="animal-badge">{animal.seriesName}</span>
                        )}

                        {animal.character && (
                            <p className="animal-detail">キャラ: {animal.character}</p>
                        )}

                        {animal.purchasePlace && (
                            <p className="animal-detail">📍 {animal.purchasePlace}</p>
                        )}

                        {animal.purchaseDate && (
                            <p className="animal-detail">📅 {animal.purchaseDate}</p>
                        )}

                        {animal.notes && (
                            <p className="animal-notes">{animal.notes}</p>
                        )}
                    </div>

                    {/* 編集・削除ボタン */}
                    <div className="animal-actions">
                        <button className="btn-edit" onClick={() => onEdit(animal)}>
                            編集
                        </button>
                        <button className="btn-delete" onClick={() => handleDelete(animal.id, animal.name)}>
                            削除
                        </button>
                    </div>
                </div>
            ))}
        </div>
    );
}