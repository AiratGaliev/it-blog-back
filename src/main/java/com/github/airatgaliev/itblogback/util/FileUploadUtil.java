package com.github.airatgaliev.itblogback.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileUploadUtil {

  @Value("${user.avatar.upload-dir}")
  private String avatarUploadDir;

  @Value("${article.image.upload-dir}")
  private String articleImageUploadDir;

  public String uploadUserAvatar(MultipartFile file, String username) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("File is empty or null");
    }

    String filename = username + "_avatar" + getFileExtension(file);
    saveFile(file, avatarUploadDir, filename);
    return filename;
  }

  public String uploadArticleImage(MultipartFile file, Long articleId) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("File is empty or null");
    }

    String filename = articleId + "_" + UUID.randomUUID() + getFileExtension(file);
    saveFile(file, articleImageUploadDir, filename);
    return filename;
  }

  public String uploadCategoryAvatar(MultipartFile file, Long categoryId) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("File is empty or null");
    }

    String filename = categoryId + "_image" + getFileExtension(file);
    saveFile(file, avatarUploadDir, filename);
    return filename;
  }

  private void saveFile(MultipartFile file, String dir, String filename) {
    try {
      Path path = Paths.get(dir).resolve(filename);
      Files.createDirectories(path.getParent());
      Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new RuntimeException("Failed to store file", e);
    }
  }

  private String getFileExtension(MultipartFile file) {
    String filename = file.getOriginalFilename();
    if (filename != null && filename.contains(".")) {
      return filename.substring(filename.lastIndexOf("."));
    }
    return "";
  }
}
