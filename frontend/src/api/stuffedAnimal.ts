import { api } from './client';

export interface StuffedAnimal {
    id: number;
    name: string;
    seriesId: number | null;
    seriesName: string | null;
    character: string | null;
    purchaseDate: string | null;
    purchasePlace: string | null;
    imageUrl: string | null;
    notes: string | null;
    createdBy: number;
    updatedBy: number;
    createdAt: string;
    updatedAt: string;
}

export interface StuffedAnimalRequest {
    name: string;
    seriesId?: number | null;
    character?: string;
    purchaseDate?: string;
    purchasePlace?: string;
    imageUrl?: string;
    notes?: string;
}

/** ぬいぐるみ一覧取得 */
export const fetchStuffedAnimals = async (params?: {
    name?: string;
    seriesId?: number;
    character?: string;
}): Promise<StuffedAnimal[]> => {
    const res = await api.get<StuffedAnimal[]>('/stuffed-animals', { params });
    return res.data;
};

/** ぬいぐるみ1件取得 */
export const fetchStuffedAnimalById = async (id: number): Promise<StuffedAnimal> => {
    const res = await api.get<StuffedAnimal>(`/stuffed-animals/${id}`);
    return res.data;
};

/** ぬいぐるみ登録 */
export const createStuffedAnimal = async (data: StuffedAnimalRequest): Promise<StuffedAnimal> => {
    const res = await api.post<StuffedAnimal>('/stuffed-animals', data);
    return res.data;
};

/** ぬいぐるみ更新 */
export const updateStuffedAnimal = async (id: number, data: StuffedAnimalRequest): Promise<StuffedAnimal> => {
    const res = await api.put<StuffedAnimal>(`/stuffed-animals/${id}`, data);
    return res.data;
};

/** ぬいぐるみ削除 */
export const deleteStuffedAnimal = async (id: number): Promise<void> => {
    await api.delete(`/stuffed-animals/${id}`);
};

/** ダブりチェック */
export const checkDuplicate = async (params: {
    name: string;
    seriesId?: number;
    character?: string;
}): Promise<StuffedAnimal[]> => {
    const res = await api.get<StuffedAnimal[]>('/stuffed-animals/duplicate-check', { params });
    return res.data;
};