import { useState } from 'react';
import { createStuffedAnimal, checkDuplicate, type StuffedAnimalRequest, type StuffedAnimal } from '../api/stuffedAnimal';
import { uploadAnimalImage } from '../api/image';
import { isSessionExpiredError } from '../api/client';
import SeriesSelect from './SeriesSelect';
import ImageSelectPreview from './ImageSelectPreview';
import '../styles/StuffedAnimalForm.css';
import '../styles/common.css';

interface Props {
    // 登録（＋画像アップロードがあればそれも完了した後）に呼ばれる。
    // 一覧再読み込みのためのコールバックとして親から渡される。
    onSuccess: () => void;
    onCancel: () => void;
}

export default function StuffedAnimalForm({ onSuccess, onCancel }: Props) {
    const [name, setName] = useState('');
    const [seriesId, setSeriesId] = useState<number | null>(null);
    const [character, setCharacter] = useState('');
    const [purchasePlace, setPurchasePlace] = useState('');
    const [purchaseDate, setPurchaseDate] = useState('');
    const [notes, setNotes] = useState('');
    // 登録ボタンを押すまでは実際にはアップロードせず、選んだファイルだけ保持しておく
    const [selectedImage, setSelectedImage] = useState<File | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [duplicates, setDuplicates] = useState<StuffedAnimal[]>([]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');
        setDuplicates([]);
        setLoading(true);

        try {
            const dupes = await checkDuplicate({
                name,
                seriesId: seriesId ?? undefined,
                character: character || undefined,
            });
            if (dupes.length > 0) {
                setDuplicates(dupes);
                setLoading(false);
                return;
            }
            await register();
        } catch (err) {
            if (isSessionExpiredError(err)) return;
            setError('登録に失敗しました');
        } finally {
            setLoading(false);
        }
    };

    const handleForceRegister = async () => {
        setLoading(true);
        setDuplicates([]);
        try {
            await register();
        } catch (err) {
            if (isSessionExpiredError(err)) return;
            setError('登録に失敗しました');
        } finally {
            setLoading(false);
        }
    };

    const register = async () => {
        const request: StuffedAnimalRequest = {
            name,
            seriesId: seriesId,
            character: character || undefined,
            purchasePlace: purchasePlace || undefined,
            purchaseDate: purchaseDate || undefined,
            notes: notes || undefined,
        };

        // ① まずテキスト情報を登録し、作成されたIDを取得する
        const created = await createStuffedAnimal(request);

        // ② 画像が選ばれていれば、確定したIDを使って続けてアップロードする
        //    （画像は任意項目のため、選ばれていなければこのステップはスキップする）
        if (selectedImage) {
            try {
                await uploadAnimalImage(created.id, selectedImage);
            } catch {
                // 登録自体は成功しているので、画像だけ失敗した場合はエラーを致命的にはしない。
                // ユーザーには一覧・編集画面から後で再アップロードしてもらう。
                setError('登録は完了しましたが、画像のアップロードに失敗しました。編集画面から再度お試しください。');
            }
        }

        onSuccess();
    };

    return (
        <div className="form-container">
            <h2 className="form-title">🧸 ぬいぐるみを登録する</h2>

            <form onSubmit={handleSubmit} className="form-body">
                <div className="field">
                    <label>画像</label>
                    <ImageSelectPreview onFileSelected={setSelectedImage} />
                </div>

                <div className="field">
                    <label>名前 *</label>
                    <input className="input" value={name} onChange={(e) => setName(e.target.value)} placeholder="例: くまのプーさん" required />
                </div>

                <div className="field">
                    <label>シリーズ</label>
                    <SeriesSelect value={seriesId} onChange={setSeriesId} />
                </div>

                <div className="field">
                    <label>キャラクター</label>
                    <input className="input" value={character} onChange={(e) => setCharacter(e.target.value)} placeholder="例: プーさん" />
                </div>

                <div className="field">
                    <label>購入場所</label>
                    <input className="input" value={purchasePlace} onChange={(e) => setPurchasePlace(e.target.value)} placeholder="例: 東京ディズニーランド" />
                </div>

                <div className="field">
                    <label>購入日</label>
                    <input className="input" type="date" value={purchaseDate} onChange={(e) => setPurchaseDate(e.target.value)} />
                </div>

                <div className="field">
                    <label>メモ</label>
                    <textarea className="input" value={notes} onChange={(e) => setNotes(e.target.value)} placeholder="例: 誕生日プレゼントでもらった" />
                </div>

                {error && <p className="error-text">{error}</p>}

                {duplicates.length > 0 && (
                    <div className="duplicate-warning">
                        <p className="duplicate-warning-title">⚠️ 似たぬいぐるみが {duplicates.length} 件あるで！</p>
                        {duplicates.map((d) => (
                            <p key={d.id} className="duplicate-warning-item">・{d.name} ({d.purchasePlace ?? '購入場所不明'})</p>
                        ))}
                        <div className="duplicate-warning-actions">
                            <button type="button" className="btn-cancel" onClick={() => setDuplicates([])}>キャンセル</button>
                            <button type="button" className="btn-force" onClick={handleForceRegister}>それでも登録する</button>
                        </div>
                    </div>
                )}

                {duplicates.length === 0 && (
                    <div className="edit-form-actions">
                        <button type="button" className="btn-cancel" onClick={onCancel}>
                            キャンセル
                        </button>
                        <button type="submit" className="btn-primary" disabled={loading}>
                            {loading ? '登録中...' : '登録する'}
                        </button>
                    </div>
                )}
            </form>
        </div>
    );
}