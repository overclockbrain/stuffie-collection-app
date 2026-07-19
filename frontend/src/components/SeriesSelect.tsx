import { useState, useEffect } from 'react';
import { fetchSeries, createSeries, deleteSeries, type Series } from '../api/series';
import '../styles/SeriesSelect.css';

interface Props {
    /** 選択中のシリーズID（未選択はnull） */
    value: number | null;
    /** 選択変更時のコールバック */
    onChange: (seriesId: number | null) => void;
}

/** プルダウンの特殊値：新規シリーズ追加モード */
const ADD_NEW_VALUE = '__ADD_NEW__';

export default function SeriesSelect({ value, onChange }: Props) {
    const [seriesList, setSeriesList] = useState<Series[]>([]);
    const [loading, setLoading] = useState(false);
    const [isAdding, setIsAdding] = useState(false);
    const [newSeriesName, setNewSeriesName] = useState('');
    const [error, setError] = useState('');

    /** シリーズ一覧を取得する */
    const loadSeries = async () => {
        setLoading(true);
        try {
            const data = await fetchSeries();
            setSeriesList(data);
        } catch {
            setError('シリーズ一覧の取得に失敗しました');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadSeries();
    }, []);

    /** プルダウン選択時の処理 */
    const handleSelectChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        const selected = e.target.value;
        if (selected === ADD_NEW_VALUE) {
            setIsAdding(true);
            return;
        }
        onChange(selected === '' ? null : Number(selected));
    };

    /** 新しいシリーズを登録する */
    const handleAddSeries = async () => {
        if (!newSeriesName.trim()) return;
        setError('');
        try {
            const created = await createSeries({ name: newSeriesName.trim() });
            await loadSeries();
            onChange(created.id);
            setNewSeriesName('');
            setIsAdding(false);
        } catch {
            setError('このシリーズ名は既に使われているかもしれません');
        }
    };

    /** 選択中のシリーズを削除する */
    const handleDeleteSeries = async () => {
        if (value === null) return;
        const target = seriesList.find((s) => s.id === value);
        if (!target) return;
        if (!confirm(`シリーズ「${target.name}」を削除してええ？\n※このシリーズを使ってるぬいぐるみはシリーズ未設定になるで`)) return;

        try {
            await deleteSeries(value);
            onChange(null);
            await loadSeries();
        } catch {
            alert('削除に失敗しました（権限がないかもしれません）');
        }
    };

    return (
        <div className="series-select-wrapper">
            {!isAdding ? (
                <div className="series-select-row">
                    <select
                        className="input series-select"
                        value={value ?? ''}
                        onChange={handleSelectChange}
                        disabled={loading}
                    >
                        <option value="">シリーズなし</option>
                        {seriesList.map((s) => (
                            <option key={s.id} value={s.id}>
                                {s.name}
                            </option>
                        ))}
                        <option value={ADD_NEW_VALUE}>＋ 新しいシリーズを追加</option>
                    </select>

                    {value !== null && (
                        <button
                            type="button"
                            className="series-delete-btn"
                            onClick={handleDeleteSeries}
                            title="このシリーズを削除"
                        >
                            🗑
                        </button>
                    )}
                </div>
            ) : (
                <div className="series-add-row">
                    <input
                        className="input"
                        value={newSeriesName}
                        onChange={(e) => setNewSeriesName(e.target.value)}
                        placeholder="新しいシリーズ名"
                        autoFocus
                    />
                    <button type="button" className="btn-secondary" onClick={handleAddSeries}>
                        追加
                    </button>
                    <button
                        type="button"
                        className="btn-secondary"
                        onClick={() => {
                            setIsAdding(false);
                            setNewSeriesName('');
                        }}
                    >
                        やめる
                    </button>
                </div>
            )}

            {error && <p className="error-text">{error}</p>}
        </div>
    );
}