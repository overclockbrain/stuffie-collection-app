import { useRef, useState } from 'react';
import { uploadAnimalImage } from '../api/image';
import { isSessionExpiredError } from '../api/client';
import '../styles/ImageUpload.css';

interface Props {
    /** アップロード対象のぬいぐるみID（新規未登録の場合はnull。その場合はアップロード不可） */
    animalId: number;
    /** 現在の画像URL（無ければnull） */
    currentImageUrl: string | null;
    /** アップロード成功時に呼ばれるコールバック（新しい画像URLを渡す） */
    onUploaded: (imageUrl: string) => void;
}

/** ブラウザ側でのリサイズ後の最大幅。サーバー側でも別途圧縮するため、ここでは通信量削減が主目的 */
const CLIENT_MAX_WIDTH = 1600;
const CLIENT_JPEG_QUALITY = 0.85;

export default function ImageUpload({ animalId, currentImageUrl, onUploaded }: Props) {
    const [previewUrl, setPreviewUrl] = useState<string | null>(currentImageUrl);
    const [uploading, setUploading] = useState(false);
    const [error, setError] = useState('');
    const fileInputRef = useRef<HTMLInputElement>(null);

    /** 選択直後にブラウザ側で軽くリサイズ・圧縮してからFileオブジェクトを作り直す */
    const resizeImage = (file: File): Promise<File> => {
        return new Promise((resolve, reject) => {
            const img = new Image();
            const objectUrl = URL.createObjectURL(file);

            img.onload = () => {
                URL.revokeObjectURL(objectUrl);

                const scale = Math.min(1, CLIENT_MAX_WIDTH / img.width);
                const canvas = document.createElement('canvas');
                canvas.width = img.width * scale;
                canvas.height = img.height * scale;

                const ctx = canvas.getContext('2d');
                if (!ctx) {
                    reject(new Error('canvas context を取得できませんでした'));
                    return;
                }
                ctx.drawImage(img, 0, 0, canvas.width, canvas.height);

                canvas.toBlob(
                    (blob) => {
                        if (!blob) {
                            reject(new Error('画像の変換に失敗しました'));
                            return;
                        }
                        resolve(new File([blob], 'image.jpg', { type: 'image/jpeg' }));
                    },
                    'image/jpeg',
                    CLIENT_JPEG_QUALITY
                );
            };

            img.onerror = () => {
                URL.revokeObjectURL(objectUrl);
                reject(new Error('画像の読み込みに失敗しました'));
            };

            img.src = objectUrl;
        });
    };

    const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (!file) return;

        setError('');

        // 選択直後、アップロード前にプレビューだけ先に表示する（体感速度のため）
        const localPreview = URL.createObjectURL(file);
        setPreviewUrl(localPreview);

        setUploading(true);
        try {
            const resized = await resizeImage(file);
            const imageUrl = await uploadAnimalImage(animalId, resized);
            setPreviewUrl(imageUrl);
            onUploaded(imageUrl);
        } catch (err) {
            if (isSessionExpiredError(err)) return;
            setError('画像のアップロードに失敗しました');
            // 失敗したので、元の画像に表示を戻す
            setPreviewUrl(currentImageUrl);
        } finally {
            setUploading(false);
            URL.revokeObjectURL(localPreview);
            // 同じファイルを連続で選び直したときも onChange が発火するようにリセットしておく
            if (fileInputRef.current) fileInputRef.current.value = '';
        }
    };

    return (
        <div className="image-upload">
            <div className="image-upload-preview">
                {previewUrl ? (
                    <img src={previewUrl} alt="ぬいぐるみの画像" className="image-upload-img" />
                ) : (
                    <div className="image-upload-placeholder">🧸</div>
                )}
                {uploading && <div className="image-upload-overlay">アップロード中...</div>}
            </div>

            <input
                ref={fileInputRef}
                type="file"
                accept="image/*"
                onChange={handleFileChange}
                disabled={uploading}
                className="image-upload-input"
                id="image-upload-input"
            />
            <label htmlFor="image-upload-input" className="btn-secondary image-upload-label">
                {previewUrl ? '画像を変更' : '画像を選択'}
            </label>

            {error && <p className="error-text">{error}</p>}
        </div>
    );
}