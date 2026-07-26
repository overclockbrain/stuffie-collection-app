import { useState } from 'react';
import { login } from '../api/auth';
import '../styles/LoginPage.css';
import '../styles/common.css';

interface Props {
    onLoginSuccess: () => void;
    sessionExpired?: boolean;
}

export default function LoginPage({ onLoginSuccess, sessionExpired }: Props) {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    // props由来の初期値をローカルstateにコピーし、ログイン試行時に消せるようにする
    const [showSessionExpired, setShowSessionExpired] = useState(!!sessionExpired);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');
        // 新しくログインを試みた時点で「セッションが切れたで」の通知は消す
        setShowSessionExpired(false);
        setLoading(true);

        try {
            const res = await login({ email, password });
            localStorage.setItem('accessToken', res.accessToken);
            onLoginSuccess();
        } catch {
            setError('メールアドレスかパスワードが違います。');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="login-container">
            <div className="login-card">
                <h1 className="login-title">🧸 stuffie-collection</h1>
                <h2 className="login-subtitle">ログイン</h2>

                {showSessionExpired && (
                    <p className="session-expired-notice">
                        セッションが切れました。再度ログインしてください。
                    </p>
                )}

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