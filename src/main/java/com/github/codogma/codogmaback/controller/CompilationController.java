package com.github.codogma.codogmaback.controller;

import com.github.codogma.codogmaback.dto.CreateCompilation;
import com.github.codogma.codogmaback.dto.GetCompilation;
import com.github.codogma.codogmaback.model.UserModel;
import com.github.codogma.codogmaback.service.CompilationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/compilations")
@Tag(name = "Compilations", description = "API for compilations")
public class CompilationController {

  private final CompilationService compilationService;

  @GetMapping
  @Operation(summary = "Get all compilations")
  @Parameters({@Parameter(name = "tag", description = "Tag to filter compilations"),
      @Parameter(name = "info", description = "Information to filter compilations"),
      @Parameter(name = "isFollowing", description = "Get user's following compilations"),
      @Parameter(name = "page", description = "Page number to retrieve"),
      @Parameter(name = "size", description = "Number of compilations per page"),
      @Parameter(name = "sort", description = "Field to sort by"),
      @Parameter(name = "order", description = "Order direction, either 'asc' or 'desc'")})
  public ResponseEntity<Page<GetCompilation>> getCompilations(
      @RequestParam(required = false) String tag, @RequestParam(required = false) String info,
      @RequestParam(required = false) Boolean isFollowing,
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "name") String sort,
      @RequestParam(defaultValue = "desc") String order,
      @AuthenticationPrincipal UserModel userModel) {
    Page<GetCompilation> compilations = compilationService.getCompilations(tag, info, isFollowing,
        page, size, sort, order, userModel);
    return ResponseEntity.ok(compilations);
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "Create a new compilation")
  @SecurityRequirement(name = "bearerAuth")
  @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_AUTHOR')")
  public ResponseEntity<GetCompilation> createCompilation(
      @Valid @ModelAttribute CreateCompilation createCompilationDTO,
      @AuthenticationPrincipal UserModel userModel) {
    GetCompilation createdCompilation = compilationService.createCompilation(createCompilationDTO,
        userModel);
    return new ResponseEntity<>(createdCompilation, HttpStatus.CREATED);
  }
}
