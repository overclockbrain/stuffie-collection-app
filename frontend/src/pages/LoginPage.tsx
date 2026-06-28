import { useState } from 'react';
import { login } from '../api/auth';
import '../styles/LoginPage.css';
import '../styles/common.css';

interface Props {
    /** ログイン成功時に呼ばれるコールバック */
    onLoginSuccess: () => void;
}

export default function LoginPage({ onLoginSuccess }: Props) {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            const res = await login({ email, password });
            localStorage.setItem('accessToken', res.accessToken);
            onLoginSuccess();
        } catch {
            // バックのエラー詳細は返さずフロント固定のメッセージにする（セキュリティ対策）
            setError('メールアドレスかパスワードが違うで！');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="login-container">
            <div className="login-card">
                <h1 className="login-title">🧸 stuffie-collection</h1>
                <h2 className="login-subtitle">ログイン</h2>

                <form onSubmit={handleSubmit} className="login-form">
                    <div className="field">
                        <label>メールアドレス</label>
                        <input
                            className="input"
                            type="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            placeholder="test@example.com"
                            required
                        />
                    </div>

                    <div className="field">
                        <label>パスワード</label>
                        <input
                            className="input"
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            placeholder="8文字以上"
                            required
                        />
                    </div>

                    {error && <p className="error-text">{error}</p>}

                    <button className="btn-primary" type="submit" disabled={loading}>
                        {loading ? 'ログイン中...' : 'ログイン'}
                    </button>
                </form>
            </div>
        </div>
    );
}