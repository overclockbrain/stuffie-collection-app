import { useState, useEffect } from 'react';
import { fetchStuffedAnimals, type StuffedAnimal } from '../api/stuffedAnimal';
import StuffedAnimalList from '../components/StuffedAnimalList';
import StuffedAnimalForm from '../components/StuffedAnimalForm';
import '../styles/StuffedAnimalPage.css';

export default function StuffedAnimalPage() {
    const [animals, setAnimals] = useState<StuffedAnimal[]>([]);
    const [loading, setLoading] = useState(false);
    const [showForm, setShowForm] = useState(false);
    const [searchName, setSearchName] = useState('');

    /** ぬいぐるみ一覧を取得する */
    const loadAnimals = async () => {
        setLoading(true);
        try {
            const data = await fetchStuffedAnimals({
                name: searchName || undefined,
            });
            setAnimals(data);
        } catch {
            alert('一覧の取得に失敗しました');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadAnimals();
    }, []);

    return (
        <div className="page-container">
            {/* ヘッダー */}
            <div className="page-header">
                <h1 className="page-title">🧸 stuffie-collection</h1>
                <button className="btn-add" onClick={() => setShowForm(!showForm)}>
                    {showForm ? '閉じる' : '＋ 追加'}
                </button>
            </div>

            {/* 登録フォーム */}
            {showForm && (
                <StuffedAnimalForm
                    onSuccess={() => {
                        setShowForm(false);
                        loadAnimals();
                    }}
                />
            )}

            {/* 検索バー */}
            <div className="search-bar">
                <input
                    className="search-input"
                    value={searchName}
                    onChange={(e) => setSearchName(e.target.value)}
                    placeholder="名前で検索..."
                />
                <button className="btn-search" onClick={loadAnimals}>
                    検索
                </button>
            </div>

            {/* 件数 */}
            <p className="result-count">{animals.length} 件</p>

            {/* 一覧 */}
            {loading ? (
                <p className="loading-text">読み込み中...</p>
            ) : (
                <StuffedAnimalList animals={animals} onDeleted={loadAnimals} />
            )}
        </div>
    );
}