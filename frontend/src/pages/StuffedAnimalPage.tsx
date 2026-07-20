import { useState, useEffect } from 'react';
import { fetchStuffedAnimals, type StuffedAnimal } from '../api/stuffedAnimal';
import StuffedAnimalList from '../components/StuffedAnimalList';
import StuffedAnimalForm from '../components/StuffedAnimalForm';
import StuffedAnimalEditForm from '../components/StuffedAnimalEditForm';
import '../styles/StuffedAnimalPage.css';

export default function StuffedAnimalPage() {
    const [animals, setAnimals] = useState<StuffedAnimal[]>([]);
    const [loading, setLoading] = useState(false);
    const [searchName, setSearchName] = useState('');
    // 追加モードかどうか（trueのときは追加フォームだけ表示する）
    const [isAdding, setIsAdding] = useState(false);
    // 編集中のぬいぐるみ（編集してへんときはnull）
    const [editingAnimal, setEditingAnimal] = useState<StuffedAnimal | null>(null);

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

    // 追加モード中は追加フォームだけを表示する（編集モードと同じ見せ方に統一）
    if (isAdding) {
        return (
            <div className="page-container">
                <div className="page-content">
                    <div className="page-header">
                        <h1 className="page-title">🧸 stuffie-collection</h1>
                    </div>
                    <StuffedAnimalForm
                        onSuccess={() => {
                            setIsAdding(false);
                            loadAnimals();
                        }}
                        onCancel={() => setIsAdding(false)}
                    />
                </div>
            </div>
        );
    }

    // 編集モード中は編集フォームだけを表示する
    if (editingAnimal) {
        return (
            <div className="page-container">
                <div className="page-content">
                    <div className="page-header">
                        <h1 className="page-title">🧸 stuffie-collection</h1>
                    </div>
                    <StuffedAnimalEditForm
                        animal={editingAnimal}
                        onSuccess={() => {
                            setEditingAnimal(null);
                            loadAnimals();
                        }}
                        onCancel={() => setEditingAnimal(null)}
                    />
                </div>
            </div>
        );
    }

    return (
        <div className="page-container">
            <div className="page-content">
                {/* ヘッダー */}
                <div className="page-header">
                    <h1 className="page-title">🧸 stuffie-collection</h1>
                    <button className="btn-add" onClick={() => setIsAdding(true)}>
                        ＋ 追加
                    </button>
                </div>

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
                    <StuffedAnimalList
                        animals={animals}
                        onChanged={loadAnimals}
                        onEdit={setEditingAnimal}
                    />
                )}
            </div>
        </div>
    );
}