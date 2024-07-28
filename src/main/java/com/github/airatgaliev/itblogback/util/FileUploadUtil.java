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
    Path path = Paths.get(avatarUploadDir).resolve(filename);
    saveFile(file, path);
    return filename;
  }

  public String uploadArticleImage(MultipartFile file, Long articleId) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("File is empty or null");
    }

    String filename = articleId + "_" + UUID.randomUUID() + getFileExtension(file);
    Path path = Paths.get(articleImageUploadDir).resolve(filename);
    saveFile(file, path);
    return filename;
  }

  private void saveFile(MultipartFile file, Path path) {
    try {
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
