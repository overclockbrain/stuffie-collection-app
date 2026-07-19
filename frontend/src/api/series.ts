import axios from 'axios';

const api = axios.create({
    baseURL: '/api',
    headers: {
        'Content-Type': 'application/json',
    },
});

// リクエスト時にトークンを自動付与するインターセプター
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

export interface Series {
    id: number;
    name: string;
    createdBy: number;
    createdAt: string;
}

export interface SeriesRequest {
    name: string;
}

/** シリーズ一覧取得 */
export const fetchSeries = async (): Promise<Series[]> => {
    const res = await api.get<Series[]>('/series');
    return res.data;
};

/** シリーズ登録 */
export const createSeries = async (data: SeriesRequest): Promise<Series> => {
    const res = await api.post<Series>('/series', data);
    return res.data;
};

/** シリーズ更新 */
export const updateSeries = async (id: number, data: SeriesRequest): Promise<Series> => {
    const res = await api.put<Series>(`/series/${id}`, data);
    return res.data;
};

/** シリーズ削除 */
export const deleteSeries = async (id: number): Promise<void> => {
    await api.delete(`/series/${id}`);
};