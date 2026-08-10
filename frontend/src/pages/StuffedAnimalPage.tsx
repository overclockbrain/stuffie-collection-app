import { useState, useEffect } from 'react';
import { fetchStuffedAnimals, type StuffedAnimal } from '../api/stuffedAnimal';
import { logout, isSessionExpiredError } from '../api/client';
import StuffedAnimalList from '../components/StuffedAnimalList';
import StuffedAnimalForm from '../components/StuffedAnimalForm';
import StuffedAnimalEditForm from '../components/StuffedAnimalEditForm';
import '../styles/StuffedAnimalPage.css';

export default function StuffedAnimalPage() {
    const [animals, setAnimals] = useState<StuffedAnimal[]>([]);
    const [loading, setLoading] = useState(false);
    const [searchName, setSearchName] = useState('');
    const [isAdding, setIsAdding] = useState(false);
    const [editingAnimal, setEditingAnimal] = useState<StuffedAnimal | null>(null);

    const loadAnimals = async () => {
        setLoading(true);
        try {
            const data = await fetchStuffedAnimals({
                name: searchName || undefined,
            });
            setAnimals(data);
        } catch (err) {
            // セッション切れの場合はApp.tsxが自動でログイン画面に戻すのでalertは出さない
            if (isSessionExpiredError(err)) return;
            alert('一覧の取得に失敗しました');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadAnimals();
    }, []);

    const renderHeaderTitle = () => (
        <div className="page-header">
            <h1 className="page-title">🧸 stuffie-collection</h1>
            <button className="btn-logout" onClick={logout}>
                ログアウト
            </button>
        </div>
    );

    if (isAdding) {
        return (
            <div className="page-container">
                <div className="page-content">
                    {renderHeaderTitle()}
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

    if (editingAnimal) {
        return (
            <div className="page-container">
                <div className="page-content">
                    {renderHeaderTitle()}
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
                <div className="page-header">
                    <h1 className="page-title">🧸 stuffie-collection</h1>
                    <div className="page-header-actions">
                        <button className="btn-add" onClick={() => setIsAdding(true)}>
                            ＋ 追加
                        </button>
                        <button className="btn-logout" onClick={logout}>
                            ログアウト
                        </button>
                    </div>
                </div>

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

                <p className="result-count">{animals.length} 件</p>

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