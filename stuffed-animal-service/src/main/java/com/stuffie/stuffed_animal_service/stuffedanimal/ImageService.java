package com.stuffie.stuffed_animal_service.stuffedanimal;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;

/**
 * 画像の圧縮・リサイズとMinIOへのアップロードを担うサービス。
 * アップロードされた画像は、幅の上限とJPEG品質を絞って保存することで
 * ストレージ容量と表示速度の両方を最適化する。
 */
@Service
@RequiredArgsConstructor
public class ImageService {

    /** リサイズ後の最大幅（これより大きい画像は縮小する） */
    private static final int MAX_WIDTH = 800;
    /** JPEG圧縮品質（0.0〜1.0、大きいほど高画質・大容量） */
    private static final float JPEG_QUALITY = 0.8f;

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.public-url-base}")
    private String publicUrlBase;

    /**
     * 画像データを圧縮・リサイズしてMinIOにアップロードし、公開URLを返す。
     *
     * @param animalId 紐づくぬいぐるみのID（保存パスの整理に使う）
     * @param rawImageData 元の画像バイナリデータ
     * @return ブラウザから直接アクセスできる公開URL
     */
    public String compressAndUpload(Long animalId, byte[] rawImageData) throws IOException {
        byte[] compressed = compress(rawImageData);

        String objectName = "animals/%d/%s.jpg".formatted(animalId, UUID.randomUUID());

        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(new ByteArrayInputStream(compressed), compressed.length, -1)
                            .contentType("image/jpeg")
                            .build()
            );
        } catch (Exception e) {
            // MinioClientの例外はチェック例外の寄せ集めなので、呼び出し側が扱いやすいIOExceptionにまとめる
            throw new IOException("MinIOへのアップロードに失敗しました", e);
        }

        return publicUrlBase + "/" + objectName;
    }

    /**
     * 画像を指定の最大幅にリサイズし、JPEGとして再圧縮する。
     * 元画像が既に MAX_WIDTH 以下の場合はリサイズをスキップする（拡大はしない）。
     */
    private byte[] compress(byte[] rawImageData) throws IOException {
        BufferedImage original = ImageIO.read(new ByteArrayInputStream(rawImageData));
        if (original == null) {
            throw new IOException("画像として読み込めない形式です");
        }

        BufferedImage resized = resizeIfNeeded(original);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        ImageWriter writer = writers.next();

        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(JPEG_QUALITY);

        try (MemoryCacheImageOutputStream ios = new MemoryCacheImageOutputStream(output)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(dropAlphaChannel(resized), null, null), param);
        } finally {
            writer.dispose();
        }

        return output.toByteArray();
    }

    private BufferedImage resizeIfNeeded(BufferedImage original) {
        int width = original.getWidth();
        int height = original.getHeight();

        if (width <= MAX_WIDTH) {
            return original;
        }

        int newWidth = MAX_WIDTH;
        int newHeight = (int) ((double) height / width * newWidth);

        BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(original, 0, 0, newWidth, newHeight, null);
        g.dispose();

        return resized;
    }

    /** JPEGはアルファチャンネル（透過）に対応していないため、PNG由来の画像は変換時に落とす必要がある */
    private BufferedImage dropAlphaChannel(BufferedImage image) {
        if (!image.getColorModel().hasAlpha()) {
            return image;
        }
        BufferedImage rgbImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgbImage.createGraphics();
        g.drawImage(image, 0, 0, Color.WHITE, null);
        g.dispose();
        return rgbImage;
    }
}