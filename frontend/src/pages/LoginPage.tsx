import { useState } from 'react';
import { login } from '../api/auth';

export default function LoginPage() {
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
            // トークンをlocalStorageに保存
            localStorage.setItem('accessToken', res.accessToken);
            alert('ログイン成功！トークン取得できたで！');
        } catch (err: any) {
            setError('メールアドレスかパスワードが違います。');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={styles.container}>
            <div style={styles.card}>
                <h1 style={styles.title}>🧸 stuffie-collection</h1>
                <h2 style={styles.subtitle}>ログイン</h2>

                <form onSubmit={handleSubmit} style={styles.form}>
                    <div style={styles.field}>
                        <label style={styles.label}>メールアドレス</label>
                        <input
                            type="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            style={styles.input}
                            placeholder="test@example.com"
                            required
                        />
                    </div>

                    <div style={styles.field}>
                        <label style={styles.label}>パスワード</label>
                        <input
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            style={styles.input}
                            placeholder="8文字以上"
                            required
                        />
                    </div>

                    {error && <p style={styles.error}>{error}</p>}

                    <button type="submit" style={styles.button} disabled={loading}>
                        {loading ? 'ログイン中...' : 'ログイン'}
                    </button>
                </form>
            </div>
        </div>
    );
}

const styles: Record<string, React.CSSProperties> = {
    container: {
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: '#fdf6f0',
    },
    card: {
        backgroundColor: '#fff',
        borderRadius: '16px',
        padding: '40px',
        width: '100%',
        maxWidth: '400px',
        boxShadow: '0 4px 24px rgba(0,0,0,0.08)',
    },
    title: {
        textAlign: 'center',
        fontSize: '24px',
        marginBottom: '4px',
        color: '#333',
    },
    subtitle: {
        textAlign: 'center',
        fontSize: '16px',
        color: '#888',
        marginBottom: '32px',
        fontWeight: 'normal',
    },
    form: {
        display: 'flex',
        flexDirection: 'column',
        gap: '16px',
    },
    field: {
        display: 'flex',
        flexDirection: 'column',
        gap: '6px',
    },
    label: {
        fontSize: '14px',
        color: '#555',
        fontWeight: 'bold',
    },
    input: {
        padding: '10px 14px',
        borderRadius: '8px',
        border: '1px solid #ddd',
        fontSize: '14px',
        outline: 'none',
    },
    error: {
        color: '#e74c3c',
        fontSize: '13px',
        margin: '0',
    },
    button: {
        padding: '12px',
        borderRadius: '8px',
        border: 'none',
        backgroundColor: '#e8956d',
        color: '#fff',
        fontSize: '16px',
        fontWeight: 'bold',
        cursor: 'pointer',
        marginTop: '8px',
    },
};
