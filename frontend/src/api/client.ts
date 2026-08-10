import axios from 'axios';

/**
 * 全APIファイルで共有するaxiosインスタンス。
 * - リクエスト時にトークンを自動付与する
 * - 401（認証エラー）を検知したら自動でセッションを終了させる
 *   （ただしログイン・登録エンドポイント自体の401は対象外）
 */
export const api = axios.create({
    baseURL: '/api',
    headers: {
        'Content-Type': 'application/json',
    },
});

/** App.tsxがこのイベントを購読して画面をログイン画面に戻す */
export const SESSION_ENDED_EVENT = 'auth:session-ended';

export interface SessionEndedDetail {
    reason: 'expired' | 'manual';
}

function dispatchSessionEnded(reason: 'expired' | 'manual') {
    localStorage.removeItem('accessToken');
    window.dispatchEvent(
        new CustomEvent<SessionEndedDetail>(SESSION_ENDED_EVENT, { detail: { reason } })
    );
}

/** ログイン・登録エンドポイントかどうかを判定する（このエンドポイントの401は認証失敗であり、セッション切れではない） */
function isAuthEndpoint(url?: string): boolean {
    if (!url) return false;
    return url.includes('/auth/login') || url.includes('/auth/register');
}

// リクエスト時にトークンを自動付与する
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// 401が返ってきたらトークン切れとみなしてセッションを終了する
// ただしログイン・登録自体の401（＝認証失敗）は対象外にする
api.interceptors.response.use(
    (response) => response,
    (error) => {
        const isUnauthorized = error.response?.status === 401;
        const isAuthRequest = isAuthEndpoint(error.config?.url);

        if (isUnauthorized && !isAuthRequest) {
            dispatchSessionEnded('expired');
        }
        return Promise.reject(error);
    }
);

/** ユーザー操作による手動ログアウト。ヘッダーのログアウトボタンから呼ばれる。 */
export const logout = (): void => {
    dispatchSessionEnded('manual');
};

/**
 * エラーが「セッション切れ（401、ただしログイン・登録以外）」によるものかどうかを判定する。
 * 401のときはApp.tsxが自動でログイン画面に戻す処理を行うため、
 * 各画面側でalert等の追加のエラー表示を行わないようにするために使う。
 */
export const isSessionExpiredError = (err: unknown): boolean => {
    if (!axios.isAxiosError(err)) return false;
    return err.response?.status === 401 && !isAuthEndpoint(err.config?.url);
};