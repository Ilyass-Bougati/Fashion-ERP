package com.sefault.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.sefault.server.exception.NotFoundException;
import com.sefault.server.image.dto.projection.ImageProjection;
import com.sefault.server.image.dto.record.ImageRecord;
import com.sefault.server.image.dto.record.ImageUrlRecord;
import com.sefault.server.image.entity.Image;
import com.sefault.server.image.mapper.ImageMapper;
import com.sefault.server.image.repository.ImageRepository;
import com.sefault.server.image.service.ImageServiceImpl;
import com.sefault.server.minio.MinioProperties;
import com.sefault.server.minio.MinioService;
import io.minio.errors.MinioException;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
@DisplayName("ImageServiceImpl Tests")
@TestPropertySource(properties = {"minio.endpoint=http://192.168.11.200:9000"})
class ImageServiceImplTest {

    @Mock
    private MinioService minioService;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private MinioProperties minioProperties;

    @Mock
    private ImageMapper imageMapper;

    @Mock
    private MultipartFile multipartFile;

    // We instantiate manually because the constructor calls minioService.setBucketName()
    private ImageServiceImpl imageService;

    private static final String BUCKET_NAME = "images-bucket";
    private static final String CONTENT_TYPE = "image/png";
    private static final String OBJECT_KEY = "some-object-key";
    private static final String PERMANENT_URL = "http://192.168.11.200/images/some-object-key";

    @BeforeEach
    void setUp() {
        when(minioProperties.imagesBucket()).thenReturn(BUCKET_NAME);
        imageService = new ImageServiceImpl(minioService, imageRepository, minioProperties, imageMapper);
    }

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("should call setBucketName with the images bucket on construction")
        void shouldSetBucketNameOnConstruction() {
            verify(minioService).setBucketName(BUCKET_NAME);
        }
    }

    @Nested
    @DisplayName("uploadImage()")
    class UploadImageTests {

        @Test
        @DisplayName("should upload file, persist image, and return ImageUrlRecord")
        void shouldUploadAndReturnRecord() throws IOException, MinioException {
            UUID savedId = UUID.randomUUID();
            Image savedImage = Image.builder()
                    .id(savedId)
                    .bucketName(BUCKET_NAME)
                    .contentType(CONTENT_TYPE)
                    .objectKey(OBJECT_KEY)
                    .build();

            when(multipartFile.getContentType()).thenReturn(CONTENT_TYPE);
            when(imageRepository.save(any(Image.class))).thenReturn(savedImage);
            when(minioService.getPermanentFileUrl(anyString())).thenReturn(PERMANENT_URL);

            ImageUrlRecord result = imageService.uploadImage(multipartFile);

            assertThat(result).isNotNull();
            assertThat(result.imageId()).isEqualTo(savedId);
            assertThat(result.url()).isEqualTo(PERMANENT_URL);
        }

        @Test
        @DisplayName("should save image with correct bucket name and content type")
        void shouldSaveImageWithCorrectFields() throws IOException, MinioException {
            Image savedImage = Image.builder()
                    .id(UUID.randomUUID())
                    .bucketName(BUCKET_NAME)
                    .contentType(CONTENT_TYPE)
                    .objectKey(OBJECT_KEY)
                    .build();

            when(multipartFile.getContentType()).thenReturn(CONTENT_TYPE);
            when(imageRepository.save(any(Image.class))).thenReturn(savedImage);
            when(minioService.getPermanentFileUrl(anyString())).thenReturn(PERMANENT_URL);

            imageService.uploadImage(multipartFile);

            ArgumentCaptor<Image> imageCaptor = ArgumentCaptor.forClass(Image.class);
            verify(imageRepository).save(imageCaptor.capture());

            Image captured = imageCaptor.getValue();
            assertThat(captured.getBucketName()).isEqualTo(BUCKET_NAME);
            assertThat(captured.getContentType()).isEqualTo(CONTENT_TYPE);
            assertThat(captured.getObjectKey()).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("should generate a unique UUID as object name for each upload")
        void shouldGenerateUniqueObjectName() throws IOException, MinioException {
            Image savedImage = Image.builder().id(UUID.randomUUID()).build();
            when(multipartFile.getContentType()).thenReturn(CONTENT_TYPE);
            when(imageRepository.save(any(Image.class))).thenReturn(savedImage);
            when(minioService.getPermanentFileUrl(anyString())).thenReturn(PERMANENT_URL);

            imageService.uploadImage(multipartFile);
            imageService.uploadImage(multipartFile);

            ArgumentCaptor<String> objectNameCaptor = ArgumentCaptor.forClass(String.class);
            verify(minioService, times(2)).uploadFile(objectNameCaptor.capture(), eq(multipartFile));

            assertThat(objectNameCaptor.getAllValues().get(0))
                    .isNotEqualTo(objectNameCaptor.getAllValues().get(1));
        }

        @Test
        @DisplayName("should call minioService.uploadFile with correct arguments")
        void shouldCallUploadFileWithCorrectArgs() throws IOException, MinioException {
            Image savedImage = Image.builder().id(UUID.randomUUID()).build();
            when(multipartFile.getContentType()).thenReturn(CONTENT_TYPE);
            when(imageRepository.save(any(Image.class))).thenReturn(savedImage);
            when(minioService.getPermanentFileUrl(anyString())).thenReturn(PERMANENT_URL);

            imageService.uploadImage(multipartFile);

            verify(minioService).uploadFile(anyString(), eq(multipartFile));
        }

        @Test
        @DisplayName("should propagate IOException thrown by minioService.uploadFile")
        void shouldPropagateIOException() throws IOException, MinioException {
            when(multipartFile.getContentType()).thenReturn(CONTENT_TYPE);
            doThrow(new IOException("Storage unavailable"))
                    .when(minioService)
                    .uploadFile(anyString(), any(MultipartFile.class));

            assertThatThrownBy(() -> imageService.uploadImage(multipartFile))
                    .isInstanceOf(IOException.class)
                    .hasMessage("Storage unavailable");
        }

        @Test
        @DisplayName("should propagate MinioException thrown by minioService.uploadFile")
        void shouldPropagateMinioException() throws IOException, MinioException {
            when(multipartFile.getContentType()).thenReturn(CONTENT_TYPE);
            doThrow(new MinioException("Minio error"))
                    .when(minioService)
                    .uploadFile(anyString(), any(MultipartFile.class));

            assertThatThrownBy(() -> imageService.uploadImage(multipartFile)).isInstanceOf(MinioException.class);
        }
    }

    @Nested
    @DisplayName("findImageById()")
    class FindImageByIdTests {

        @Test
        @DisplayName("should return mapped ImageRecord when image exists")
        void shouldReturnImageRecord() {
            UUID id = UUID.randomUUID();
            ImageProjection projection = mock(ImageProjection.class);
            ImageRecord expected = new ImageRecord(id, OBJECT_KEY, BUCKET_NAME, CONTENT_TYPE, null, null);

            when(imageRepository.getImageProjectionById(id)).thenReturn(Optional.of(projection));
            when(imageMapper.projectionToRecord(projection)).thenReturn(expected);

            ImageRecord result = imageService.findImageById(id);

            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("should throw NotFoundException when image does not exist")
        void shouldThrowNotFoundExceptionWhenMissing() {
            UUID id = UUID.randomUUID();
            when(imageRepository.getImageProjectionById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> imageService.findImageById(id))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(id.toString());
        }

        @Test
        @DisplayName("should delegate to imageMapper for record conversion")
        void shouldDelegateToMapper() {
            UUID id = UUID.randomUUID();
            ImageProjection projection = mock(ImageProjection.class);
            ImageRecord record = new ImageRecord(id, OBJECT_KEY, BUCKET_NAME, CONTENT_TYPE, null, null);

            when(imageRepository.getImageProjectionById(id)).thenReturn(Optional.of(projection));
            when(imageMapper.projectionToRecord(projection)).thenReturn(record);

            imageService.findImageById(id);

            verify(imageMapper).projectionToRecord(projection);
        }
    }

    @Nested
    @DisplayName("deleteImageById()")
    class DeleteImageByIdTests {

        @Test
        @DisplayName("should delete file from minio and remove record from repository")
        void shouldDeleteFileAndRecord() throws MinioException {
            UUID id = UUID.randomUUID();
            ImageProjection projection = mock(ImageProjection.class);
            ImageRecord record = new ImageRecord(id, OBJECT_KEY, BUCKET_NAME, CONTENT_TYPE, null, null);

            when(imageRepository.getImageProjectionById(id)).thenReturn(Optional.of(projection));
            when(imageMapper.projectionToRecord(projection)).thenReturn(record);

            imageService.deleteImageById(id);

            verify(minioService).deleteFile(OBJECT_KEY);
            verify(imageRepository).deleteById(id);
        }

        @Test
        @DisplayName("should throw NotFoundException when image does not exist")
        void shouldThrowNotFoundExceptionWhenMissing() {
            UUID id = UUID.randomUUID();
            when(imageRepository.getImageProjectionById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> imageService.deleteImageById(id))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(id.toString());

            verify(imageRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("should propagate MinioException thrown during file deletion")
        void shouldPropagateMinioException() throws MinioException {
            UUID id = UUID.randomUUID();
            ImageProjection projection = mock(ImageProjection.class);
            ImageRecord record = new ImageRecord(id, OBJECT_KEY, BUCKET_NAME, CONTENT_TYPE, null, null);

            when(imageRepository.getImageProjectionById(id)).thenReturn(Optional.of(projection));
            when(imageMapper.projectionToRecord(projection)).thenReturn(record);
            doThrow(new MinioException("Delete failed")).when(minioService).deleteFile(OBJECT_KEY);

            assertThatThrownBy(() -> imageService.deleteImageById(id)).isInstanceOf(MinioException.class);

            verify(imageRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("should use the objectKey from the found record when deleting from minio")
        void shouldUseCorrectObjectKey() throws MinioException {
            UUID id = UUID.randomUUID();
            String specificKey = "specific-object-key-123";
            ImageProjection projection = mock(ImageProjection.class);
            ImageRecord record = new ImageRecord(id, specificKey, BUCKET_NAME, CONTENT_TYPE, null, null);

            when(imageRepository.getImageProjectionById(id)).thenReturn(Optional.of(projection));
            when(imageMapper.projectionToRecord(projection)).thenReturn(record);

            imageService.deleteImageById(id);

            verify(minioService).deleteFile(specificKey);
        }
    }

    @Nested
    @DisplayName("getImageUrl()")
    class GetImageUrlTests {

        @Test
        @DisplayName("should return permanent URL for a valid image")
        void shouldReturnPermanentUrl() {
            UUID id = UUID.randomUUID();
            ImageProjection projection = mock(ImageProjection.class);
            ImageRecord record = new ImageRecord(id, OBJECT_KEY, BUCKET_NAME, CONTENT_TYPE, null, null);

            when(imageRepository.getImageProjectionById(id)).thenReturn(Optional.of(projection));
            when(imageMapper.projectionToRecord(projection)).thenReturn(record);
            when(minioService.getPermanentFileUrl(OBJECT_KEY)).thenReturn(PERMANENT_URL);

            String result = imageService.getImageUrl(id);

            assertThat(result).isEqualTo(PERMANENT_URL);
        }

        @Test
        @DisplayName("should call getPermanentFileUrl with the correct objectKey")
        void shouldPassCorrectObjectKeyToMinio() {
            UUID id = UUID.randomUUID();
            String specificKey = "my-specific-key";
            ImageProjection projection = mock(ImageProjection.class);
            ImageRecord record = new ImageRecord(id, specificKey, BUCKET_NAME, CONTENT_TYPE, null, null);

            when(imageRepository.getImageProjectionById(id)).thenReturn(Optional.of(projection));
            when(imageMapper.projectionToRecord(projection)).thenReturn(record);
            when(minioService.getPermanentFileUrl(specificKey)).thenReturn(PERMANENT_URL);

            imageService.getImageUrl(id);

            verify(minioService).getPermanentFileUrl(specificKey);
        }

        @Test
        @DisplayName("should throw NotFoundException when image does not exist")
        void shouldThrowNotFoundExceptionWhenMissing() {
            UUID id = UUID.randomUUID();
            when(imageRepository.getImageProjectionById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> imageService.getImageUrl(id))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining(id.toString());
        }
    }
}
