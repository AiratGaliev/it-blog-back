package com.github.codogma.codogmaback.repository.specifications;

import com.github.codogma.codogmaback.model.CompilationModel;
import com.github.codogma.codogmaback.model.UserModel;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class CompilationSpecifications {

  public static Specification<CompilationModel> buildSpecification(String tag,
      List<Long> categoryIds, Boolean isFollowing, UserModel foundUser) {
    return null;
  }
}
