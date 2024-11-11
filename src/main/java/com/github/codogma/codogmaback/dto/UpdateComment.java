package com.github.codogma.codogmaback.dto;

import com.github.codogma.codogmaback.model.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Update Comment")
public class UpdateComment {

  @Schema(description = "Status of the article")
  private Status status;
  @Schema(description = "Content of the comment", example = "This is the updated content of the comment", minLength = 2, maxLength = 1000)
  private String content;
}
