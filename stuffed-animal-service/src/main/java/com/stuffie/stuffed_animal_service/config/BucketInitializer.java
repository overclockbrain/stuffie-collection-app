package com.stuffie.stuffed_animal_service.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * アプリ起動時に画像保存用バケットが存在するか確認し、無ければ作成する。
 * 画像は「閲覧だけは誰でもできる（公開URLで直接表示するため）」設計にしたいので、
 * バケットに読み取り専用の公開ポリシーを設定する。
 * アップロード自体は引き続き認証済みユーザーしか実行できない（gRPC側で権限チェック済み）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BucketInitializer implements ApplicationRunner {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucketName).build()
        );

        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            log.info("MinIOバケット '{}' を新規作成しました", bucketName);
        } else {
            log.info("MinIOバケット '{}' は既に存在します", bucketName);
        }

        // 画像の閲覧（GET）だけを誰でも許可するポリシー。アップロード等の管理操作は含まない。
        String publicReadPolicy = """
                {
                  "Version": "2012-10-17",
                  "Statement": [
                    {
                      "Effect": "Allow",
                      "Principal": {"AWS": ["*"]},
                      "Action": ["s3:GetObject"],
                      "Resource": ["arn:aws:s3:::%s/*"]
                    }
                  ]
                }
                """.formatted(bucketName);

        minioClient.setBucketPolicy(
                SetBucketPolicyArgs.builder().bucket(bucketName).config(publicReadPolicy).build()
        );
        log.info("MinIOバケット '{}' に公開読み取りポリシーを設定しました", bucketName);
    }
}