import { type StuffedAnimal, deleteStuffedAnimal } from '../api/stuffedAnimal';
import { isSessionExpiredError } from '../api/client';
import '../styles/StuffedAnimalList.css';

interface Props {
    animals: StuffedAnimal[];
    onChanged: () => void;
    onEdit: (animal: StuffedAnimal) => void;
}

export default function StuffedAnimalList({ animals, onChanged, onEdit }: Props) {
    const handleDelete = async (id: number, name: string) => {
        if (!confirm(`「${name}」を削除してええ？`)) return;
        try {
            await deleteStuffedAnimal(id);
            onChanged();
        } catch (err) {
            if (isSessionExpiredError(err)) return;
            alert('削除に失敗しました（権限がないかもしれません）');
        }
    };

    if (animals.length === 0) {
        return (
            <div className="animal-empty">
                <p>まだぬいぐるみが登録されてへんで！</p>
            </div>
        );
    }

    return (
        <div className="animal-grid">
            {animals.map((animal) => (
                <div key={animal.id} className="animal-card">
                    <div className="animal-image-area">
                        {animal.imageUrl ? (
                            <img src={animal.imageUrl} alt={animal.name} className="animal-image" />
                        ) : (
                            <div className="animal-no-image">🧸</div>
                        )}
                    </div>

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