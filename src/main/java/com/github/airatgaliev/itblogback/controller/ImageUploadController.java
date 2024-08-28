package com.github.airatgaliev.itblogback.controller;

import com.github.airatgaliev.itblogback.service.ImageUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/images")
@Tag(name = "Upload images", description = "API for uploading images")
public class ImageUploadController {

  private final ImageUploadService imageUploadService;

  @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Upload image")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAuthority('ROLE_AUTHOR')")
  public ResponseEntity<String> upload(@RequestParam("image") MultipartFile image) {
    String imageUrl = imageUploadService.uploadArticleImage(image);
    return ResponseEntity.ok(imageUrl);
  }
}
