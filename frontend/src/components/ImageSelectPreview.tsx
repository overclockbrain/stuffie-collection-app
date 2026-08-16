import { useRef, useState } from 'react';
import '../styles/ImageUpload.css';

interface Props {
    /** 選択されたファイルが変わるたびに呼ばれる（未選択に戻す場合は null） */
    onFileSelected: (file: File | null) => void;
}

/** ブラウザ側でのリサイズ後の最大幅。サーバー側でも別途圧縮するため、ここでは通信量削減が主目的 */
const CLIENT_MAX_WIDTH = 1600;
const CLIENT_JPEG_QUALITY = 0.85;

/**
 * 新規登録フォーム専用の画像選択コンポーネント。
 *
 * 既存のImageUploadとの違い:
 * ImageUploadは「animalIdが確定済み」の編集画面専用で、選んだら即アップロードする。
 * こちらは新規登録時、まだIDが存在しない段階で使うため、
 * 「選んで軽量化・プレビューするだけ」に留め、実際のアップロードは
 * 親コンポーネント（登録が完了してIDが確定した後）に任せる。
 */
export default function ImageSelectPreview({ onFileSelected }: Props) {
    const [previewUrl, setPreviewUrl] = useState<string | null>(null);
    const [processing, setProcessing] = useState(false);
    const [error, setError] = useState('');
    const fileInputRef = useRef<HTMLInputElement>(null);

    /** ImageUploadと同じロジックで、選択直後にブラウザ側で軽くリサイズ・圧縮する */
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
        setProcessing(true);
        try {
            const resized = await resizeImage(file);
            setPreviewUrl(URL.createObjectURL(resized));
            onFileSelected(resized);
        } catch {
            setError('画像の読み込みに失敗しました');
            onFileSelected(null);
        } finally {
            setProcessing(false);
        }
    };

    return (
        <div className="image-upload">
            <div className="image-upload-preview">
                {previewUrl ? (
                    <img src={previewUrl} alt="選択した画像のプレビュー" className="image-upload-img" />
                ) : (
                    <div className="image-upload-placeholder">🧸</div>
                )}
                {processing && <div className="image-upload-overlay">処理中...</div>}
            </div>

            <input
                ref={fileInputRef}
                type="file"
                accept="image/*"
                onChange={handleFileChange}
                disabled={processing}
                className="image-upload-input"
                id="image-select-input"
            />
            <label htmlFor="image-select-input" className="btn-secondary image-upload-label">
                {previewUrl ? '画像を変更' : '画像を選択（任意）'}
            </label>

            {error && <p className="error-text">{error}</p>}
        </div>
    );
}