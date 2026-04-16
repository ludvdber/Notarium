package be.freenote.service;

import be.freenote.exception.FileStorageException;
import be.freenote.service.impl.MinioServiceImpl;
import io.minio.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MinioServiceImplTest {

    @Mock private MinioClient minioClient;

    private MinioServiceImpl minioService;

    @BeforeEach
    void setUp() {
        minioService = new MinioServiceImpl(minioClient);
        ReflectionTestUtils.setField(minioService, "bucket", "test-bucket");
    }

    @Test
    void initBucket_shouldCreateBucketWhenNotExists() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);

        minioService.initBucket();

        verify(minioClient).makeBucket(any(MakeBucketArgs.class));
    }

    @Test
    void initBucket_shouldSkipWhenBucketExists() throws Exception {
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        minioService.initBucket();

        verify(minioClient, never()).makeBucket(any());
    }

    @Test
    void upload_shouldReturnObjectKey() throws Exception {
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(null);

        String result = minioService.upload("docs/test.pdf",
                new ByteArrayInputStream(new byte[0]), 0, "application/pdf");

        assertThat(result).isEqualTo("docs/test.pdf");
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    void upload_shouldThrowFileStorageExceptionOnError() throws Exception {
        when(minioClient.putObject(any(PutObjectArgs.class)))
                .thenThrow(new RuntimeException("connection refused"));

        assertThatThrownBy(() -> minioService.upload("key", new ByteArrayInputStream(new byte[0]), 0, "application/pdf"))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("Failed to upload");
    }

    @Test
    void download_shouldReturnBytes() throws Exception {
        byte[] content = "PDF content".getBytes();
        GetObjectResponse mockResponse = mock(GetObjectResponse.class);
        when(mockResponse.readAllBytes()).thenReturn(content);
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(mockResponse);

        byte[] result = minioService.download("docs/test.pdf");

        assertThat(result).isEqualTo(content);
    }

    @Test
    void delete_shouldCallRemoveObject() throws Exception {
        minioService.delete("docs/test.pdf");

        verify(minioClient).removeObject(any(RemoveObjectArgs.class));
    }
}
