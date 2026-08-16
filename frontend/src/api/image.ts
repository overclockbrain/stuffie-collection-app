import { api } from './client';

/**
 * 画像をアップロードする。
 * multipart/form-data形式で送信する必要があるが、
 * 共通の api インスタンスは全リクエストに Content-Type: application/json を
 * 固定で付与する設定になっているため、この呼び出しだけ明示的に上書きする。
 * headers を undefined にすることで、axiosがFormDataを見て
 * 自動的に "multipart/form-data; boundary=..." を設定してくれるようになる。
 */
export const uploadAnimalImage = async (animalId: number, file: File): Promise<string> => {
    const formData = new FormData();
    formData.append('file', file);

    const res = await api.post<{ imageUrl: string }>(
        `/stuffed-animals/${animalId}/image`,
        formData,
        {
            headers: {
                'Content-Type': undefined,
            },
        }
    );

    return res.data.imageUrl;
};