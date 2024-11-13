package com.github.codogma.codogmaback.service;

import com.github.codogma.codogmaback.util.FileUploadUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ImageUploadService {

  private final FileUploadUtil fileUploadUtil;

  public String uploadArticleImage(MultipartFile file) {
    return fileUploadUtil.uploadArticleImage(file);
  }
}
