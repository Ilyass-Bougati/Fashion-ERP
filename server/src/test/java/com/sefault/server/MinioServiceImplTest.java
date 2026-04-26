package com.sefault.server;

import com.sefault.server.minio.MinioProperties;
import com.sefault.server.minio.MinioServiceImpl;
import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.errors.ServerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MinioServiceImplTest {

    @Mock
    private MinioClient minioClient;

    @Mock
    private MinioProperties minioProperties;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private MinioServiceImpl minioService;

    private static final String DEFAULT_BUCKET = "default";
    private static final String OBJECT_NAME = "test-object.jpg";
    private static final String ENDPOINT = "http://localhost:9000";

    @BeforeEach
    void setUp() {
        minioService.setBucketName(DEFAULT_BUCKET);
    }

    @Test
    void uploadFile_whenBucketExists_shouldNotCreateBucket() throws Exception {
        InputStream inputStream = new ByteArrayInputStream("data".getBytes());
        when(multipartFile.getInputStream()).thenReturn(inputStream);
        when(multipartFile.getSize()).thenReturn(4L);
        when(multipartFile.getContentType()).thenReturn("image/jpeg");
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        minioService.uploadFile(OBJECT_NAME, multipartFile);

        verify(minioClient, never()).makeBucket(any(MakeBucketArgs.class));
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    void uploadFile_whenBucketDoesNotExist_shouldCreateBucketThenUpload() throws Exception {
        InputStream inputStream = new ByteArrayInputStream("data".getBytes());
        when(multipartFile.getInputStream()).thenReturn(inputStream);
        when(multipartFile.getSize()).thenReturn(4L);
        when(multipartFile.getContentType()).thenReturn("image/jpeg");
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);

        minioService.uploadFile(OBJECT_NAME, multipartFile);

        verify(minioClient).makeBucket(any(MakeBucketArgs.class));
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    void uploadFile_shouldUseCorrctBucketAndObjectName() throws Exception {
        InputStream inputStream = new ByteArrayInputStream("data".getBytes());
        when(multipartFile.getInputStream()).thenReturn(inputStream);
        when(multipartFile.getSize()).thenReturn(4L);
        when(multipartFile.getContentType()).thenReturn("text/plain");
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        ArgumentCaptor<PutObjectArgs> captor = ArgumentCaptor.forClass(PutObjectArgs.class);

        minioService.uploadFile(OBJECT_NAME, multipartFile);

        verify(minioClient).putObject(captor.capture());
        PutObjectArgs args = captor.getValue();
        assertThat(args.bucket()).isEqualTo(DEFAULT_BUCKET);
        assertThat(args.object()).isEqualTo(OBJECT_NAME);
        assertThat(args.contentType().toString()).isEqualTo("text/plain");
    }

    @Test
    void uploadFile_whenGetInputStreamThrows_shouldPropagateIOException() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);
        when(multipartFile.getInputStream()).thenThrow(new IOException("stream error"));

        assertThatThrownBy(() -> minioService.uploadFile(OBJECT_NAME, multipartFile))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("stream error");
    }

    @Test
    void uploadFile_whenMinioClientThrows_shouldPropagateMinioException() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class)))
                .thenThrow(new ServerException("minio error", 500, ""));

        assertThatThrownBy(() -> minioService.uploadFile(OBJECT_NAME, multipartFile))
                .isInstanceOf(MinioException.class);
    }

    // --- getFileUrl ---

    @Test
    void getFileUrl_shouldReturnPresignedUrl() throws Exception {
        String expectedUrl = "http://localhost:9000/default/test-object.jpg?signature=abc";
        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn(expectedUrl);

        String result = minioService.getFileUrl(OBJECT_NAME, 3600);

        assertThat(result).isEqualTo(expectedUrl);
    }

    @Test
    void getFileUrl_shouldPassCorrectArgsToClient() throws Exception {
        ArgumentCaptor<GetPresignedObjectUrlArgs> captor =
                ArgumentCaptor.forClass(GetPresignedObjectUrlArgs.class);
        when(minioClient.getPresignedObjectUrl(any())).thenReturn("http://url");

        minioService.getFileUrl(OBJECT_NAME, 7200);

        verify(minioClient).getPresignedObjectUrl(captor.capture());
        GetPresignedObjectUrlArgs args = captor.getValue();
        assertThat(args.bucket()).isEqualTo(DEFAULT_BUCKET);
        assertThat(args.object()).isEqualTo(OBJECT_NAME);
        assertThat(args.expiry()).isEqualTo(7200);
        assertThat(args.method()).isEqualTo(Http.Method.GET);
    }

    @Test
    void getFileUrl_whenMinioClientThrows_shouldPropagateException() throws Exception {
        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenThrow(new ServerException("presign error", 500, ""));

        assertThatThrownBy(() -> minioService.getFileUrl(OBJECT_NAME, 3600))
                .isInstanceOf(MinioException.class);
    }

    // --- deleteFile ---

    @Test
    void deleteFile_shouldCallRemoveObjectWithCorrectArgs() throws Exception {
        ArgumentCaptor<RemoveObjectArgs> captor = ArgumentCaptor.forClass(RemoveObjectArgs.class);

        minioService.deleteFile(OBJECT_NAME);

        verify(minioClient).removeObject(captor.capture());
        RemoveObjectArgs args = captor.getValue();
        assertThat(args.bucket()).isEqualTo(DEFAULT_BUCKET);
        assertThat(args.object()).isEqualTo(OBJECT_NAME);
    }

    @Test
    void deleteFile_whenMinioClientThrows_shouldPropagateException() throws Exception {
        doThrow(new ServerException("delete error", 500, ""))
                .when(minioClient).removeObject(any(RemoveObjectArgs.class));

        assertThatThrownBy(() -> minioService.deleteFile(OBJECT_NAME))
                .isInstanceOf(MinioException.class);
    }

    // --- getPermanentFileUrl ---

    @Test
    void getPermanentFileUrl_shouldReturnComposedUrl() {
        when(minioProperties.endpoint()).thenReturn(ENDPOINT);

        String result = minioService.getPermanentFileUrl(OBJECT_NAME);

        assertThat(result).isEqualTo(ENDPOINT + "/" + DEFAULT_BUCKET + "/" + OBJECT_NAME);
    }

    @Test
    void getPermanentFileUrl_shouldRespectCustomBucketName() {
        minioService.setBucketName("my-bucket");
        when(minioProperties.endpoint()).thenReturn(ENDPOINT);

        String result = minioService.getPermanentFileUrl(OBJECT_NAME);

        assertThat(result).isEqualTo(ENDPOINT + "/my-bucket/" + OBJECT_NAME);
    }

    // --- setBucketName ---

    @Test
    void setBucketName_shouldAffectSubsequentOperations() throws Exception {
        minioService.setBucketName("custom-bucket");

        ArgumentCaptor<BucketExistsArgs> captor = ArgumentCaptor.forClass(BucketExistsArgs.class);
        when(minioClient.bucketExists(captor.capture())).thenReturn(true);
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(multipartFile.getContentType()).thenReturn("application/octet-stream");

        minioService.uploadFile(OBJECT_NAME, multipartFile);

        assertThat(captor.getValue().bucket()).isEqualTo("custom-bucket");
    }
}