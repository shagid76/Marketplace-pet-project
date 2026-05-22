package com.marketplace.backend.service;

import com.marketplace.backend.exception.ImageProcessingException;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MinioServiceTest {

	@Mock MinioClient minioClient;

	@InjectMocks MinioService minioService;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(minioService, "endpoint", "http://localhost:9000");
		ReflectionTestUtils.setField(minioService, "bucketName", "marketplace-images");
		ReflectionTestUtils.setField(minioService, "defaultAvatarPath",
				"http://localhost:9000/marketplace-images/default-avatar.png");
	}

	@Test
	void upload_singleFile_returnsGeneratedName() throws Exception {
		MultipartFile file = new MockMultipartFile(
				"file", "cat.png", "image/png", new byte[]{1, 2, 3});

		String name = minioService.upload(file);

		assertThat(name).endsWith(".png");
		verify(minioClient).putObject(any(PutObjectArgs.class));
	}

	@Test
	void upload_multipleFiles_returnsAllNames() throws Exception {
		MultipartFile a = new MockMultipartFile("a", "a.png", "image/png", new byte[]{1});
		MultipartFile b = new MockMultipartFile("b", "b.jpg", "image/jpeg", new byte[]{2});

		List<String> names = minioService.upload(List.of(a, b));

		assertThat(names).hasSize(2);
		assertThat(names.get(0)).endsWith(".png");
		assertThat(names.get(1)).endsWith(".jpg");
	}

	@Test
	void upload_withExistingPaths_prependsThem() throws Exception {
		MultipartFile a = new MockMultipartFile("a", "a.png", "image/png", new byte[]{1});

		List<String> result = minioService.upload(List.of(a), List.of("kept-1.png", "kept-2.png"));

		assertThat(result).hasSize(3);
		assertThat(result.get(0)).isEqualTo("kept-1.png");
		assertThat(result.get(1)).isEqualTo("kept-2.png");
		assertThat(result.get(2)).endsWith(".png");
	}

	@Test
	void upload_nullFilesList_returnsEmpty() {
		List<String> result = minioService.upload((List<MultipartFile>) null);
		assertThat(result).isEmpty();
	}

	@Test
	void upload_clientThrows_wrapsInImageProcessingException() throws Exception {
		MultipartFile file = new MockMultipartFile("f", "f.png", "image/png", new byte[]{1});
		doThrow(new RuntimeException("boom")).when(minioClient).putObject(any(PutObjectArgs.class));

		assertThatThrownBy(() -> minioService.upload(file))
				.isInstanceOf(ImageProcessingException.class)
				.hasMessageContaining("Failed to upload file");
	}

	@Test
	void delete_callsClient() throws Exception {
		minioService.delete("file-1.png");
		verify(minioClient).removeObject(any(RemoveObjectArgs.class));
	}

	@Test
	void delete_clientThrows_wrapsInImageProcessingException() throws Exception {
		doThrow(new RuntimeException("boom")).when(minioClient).removeObject(any(RemoveObjectArgs.class));
		assertThatThrownBy(() -> minioService.delete("file-1.png"))
				.isInstanceOf(ImageProcessingException.class)
				.hasMessageContaining("Failed to delete file");
	}

	@Test
	void buildUrlImage_blank_returnsDefaultAvatar() {
		assertThat(minioService.buildUrlImage((String) null))
				.isEqualTo("http://localhost:9000/marketplace-images/default-avatar.png");
		assertThat(minioService.buildUrlImage(""))
				.isEqualTo("http://localhost:9000/marketplace-images/default-avatar.png");
	}

	@Test
	void buildUrlImage_fileName_concatenatesEndpoint() {
		assertThat(minioService.buildUrlImage("xyz.png"))
				.isEqualTo("http://localhost:9000/marketplace-images/xyz.png");
	}

	@Test
	void buildUrlImage_trailingSlashesStripped() {
		ReflectionTestUtils.setField(minioService, "endpoint", "http://localhost:9000///");
		assertThat(minioService.buildUrlImage("xyz.png"))
				.isEqualTo("http://localhost:9000/marketplace-images/xyz.png");
	}

	@Test
	void buildUrlImage_list_returnsAll() {
		List<String> urls = minioService.buildUrlImage(List.of("a.png", "b.png"));
		assertThat(urls).containsExactly(
				"http://localhost:9000/marketplace-images/a.png",
				"http://localhost:9000/marketplace-images/b.png"
		);
	}

	@Test
	void buildUrlImage_nullList_returnsEmpty() {
		assertThat(minioService.buildUrlImage((List<String>) null)).isEmpty();
	}
}
