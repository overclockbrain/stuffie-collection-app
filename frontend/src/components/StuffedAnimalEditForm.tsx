import { useState } from 'react';
import { updateStuffedAnimal, type StuffedAnimalRequest, type StuffedAnimal } from '../api/stuffedAnimal';
import { isSessionExpiredError } from '../api/client';
import SeriesSelect from './SeriesSelect';
import '../styles/StuffedAnimalForm.css';
import '../styles/common.css';

interface Props {
    animal: StuffedAnimal;
    onSuccess: () => void;
    onCancel: () => void;
}

export default function StuffedAnimalEditForm({ animal, onSuccess, onCancel }: Props) {
    const [name, setName] = useState(animal.name);
    const [seriesId, setSeriesId] = useState<number | null>(animal.seriesId);
    const [character, setCharacter] = useState(animal.character ?? '');
    const [purchasePlace, setPurchasePlace] = useState(animal.purchasePlace ?? '');
    const [purchaseDate, setPurchaseDate] = useState(animal.purchaseDate ?? '');
    const [notes, setNotes] = useState(animal.notes ?? '');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            const request: StuffedAnimalRequest = {
                name,
                seriesId: seriesId,
                character: character || undefined,
                purchasePlace: purchasePlace || undefined,
                purchaseDate: purchaseDate || undefined,
                notes: notes || undefined,
            };
            await updateStuffedAnimal(animal.id, request);
            onSuccess();
        } catch (err) {
            if (isSessionExpiredError(err)) return;
            setError('更新に失敗しました（権限がないかもしれません）');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="form-container">
            <h2 className="form-title">✏️ ぬいぐるみを編集する</h2>

            <form onSubmit={handleSubmit} className="form-body">
                <div className="field">
                    <label>名前 *</label>
                    <input className="input" value={name} onChange={(e) => setName(e.target.value)} required />
                </div>

                <div className="field">
                    <label>シリーズ</label>
                    <SeriesSelect value={seriesId} onChange={setSeriesId} />
                </div>

                <div className="field">
                    <label>キャラクター</label>
                    <input className="input" value={character} onChange={(e) => setCharacter(e.target.value)} />
                </div>

                <div className="field">
                    <label>購入場所</label>
                    <input className="input" value={purchasePlace} onChange={(e) => setPurchasePlace(e.target.value)} />
                </div>

                <div className="field">
                    <label>購入日</label>
                    <input className="input" type="date" value={purchaseDate} onChange={(e) => setPurchaseDate(e.target.value)} />
                </div>

                <div className="field">
                    <label>メモ</label>
                    <textarea className="input" value={notes} onChange={(e) => setNotes(e.target.value)} />
                </div>

                {error && <p className="error-text">{error}</p>}

                <div className="edit-form-actions">
                    <button type="button" className="btn-cancel" onClick={onCancel}>
                        キャンセル
                    </button>
                    <button type="submit" className="btn-primary" disabled={loading}>
                        {loading ? '更新中...' : '更新する'}
                    </button>
                </div>
            </form>
        </div>
    );
}