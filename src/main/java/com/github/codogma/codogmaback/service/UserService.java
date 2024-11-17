package com.github.codogma.codogmaback.service;

import com.github.codogma.codogmaback.dto.GetCategory;
import com.github.codogma.codogmaback.dto.GetUser;
import com.github.codogma.codogmaback.dto.UpdateUser;
import com.github.codogma.codogmaback.dto.UserRole;
import com.github.codogma.codogmaback.exception.SubscriptionAlreadyExistsException;
import com.github.codogma.codogmaback.interceptor.localization.LocalizationContext;
import com.github.codogma.codogmaback.model.CategoryModel;
import com.github.codogma.codogmaback.model.Language;
import com.github.codogma.codogmaback.model.SubscriptionModel;
import com.github.codogma.codogmaback.model.UserModel;
import com.github.codogma.codogmaback.repository.CategoryRepository;
import com.github.codogma.codogmaback.repository.SubscriptionRepository;
import com.github.codogma.codogmaback.repository.UserRepository;
import com.github.codogma.codogmaback.repository.specifications.UserSpecifications;
import com.github.codogma.codogmaback.util.FileUploadUtil;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

@Service
@RequiredArgsConstructor
public class UserService {

  private final EntityManager entityManager;
  private final UserRepository userRepository;
  private final CategoryRepository categoryRepository;
  private final SubscriptionRepository subscriptionRepository;
  private final FileUploadUtil fileUploadUtil;
  private final PasswordEncoder passwordEncoder;
  private final LocalizationContext localizationContext;

  @Value("${search.results.limit}")
  private int searchResultsLimit;

  @Transactional
  public List<GetUser> getAllUsers(Long categoryId, UserRole role, String tag, String info,
      int page, int size, String sort, String order) {
    Sort.Direction sortDirection = Sort.Direction.fromString(order);
    Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
    List<Long> usersIds = null;
    if (info != null && !info.isEmpty()) {
      SearchSession searchSession = Search.session(entityManager);
      usersIds = searchSession.search(UserModel.class)
          .where(f -> f.match().fields("username", "firstName", "lastName", "shortInfo", "bio")
              .matching(info).fuzzy(1))
          .fetchHits(searchResultsLimit).stream().map(UserModel::getId).toList();
    }
    Specification<UserModel> spec = UserSpecifications.buildSpecification(categoryId, role, tag,
        usersIds);
    return userRepository.findAll(spec, pageable).stream().map(this::convertUserModelToDto)
        .toList();
  }

  // TODO delete this code after testing
//  @Transactional
//  public List<GetUser> getAllUsersOld() {
//    return userRepository.findAllByRoleIsNot(Role.ROLE_ADMIN).stream()
//        .map(this::convertUserModelToDto).toList();
//  }
//
//  @Transactional
//  public List<GetUser> getAllByRole(Role role) {
//    return userRepository.findAllByRoleAndRoleIsNot(role, Role.ROLE_ADMIN).stream()
//        .map(this::convertUserModelToDto).toList();
//  }
//
//  @Transactional
//  public List<GetUser> getAllAuthorsByCategoryId(Long categoryId) {
//    return userRepository.findAuthorsByCategoryId(categoryId).stream()
//        .map(this::convertUserModelToDto).toList();
//  }

  @Transactional
  public Optional<GetUser> getUserByUsername(String username) {
    return userRepository.findByUsername(username).map(this::convertUserModelToDto);
  }

  @Transactional
  public GetUser updateUser(UpdateUser updateUser, UserDetails userDetails,
      BindingResult bindingResult) {
    UserModel user = userRepository.findByUsername(userDetails.getUsername())
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    if (updateUser.getUsername() != null && !updateUser.getUsername().equals(user.getUsername())) {
      boolean isExistsUser = userRepository.existsByUsername(updateUser.getUsername());
      if (isExistsUser) {
        bindingResult.rejectValue("username", "username.exists", "This username is already taken");
      } else {
        user.setUsername(updateUser.getUsername());
      }
    }
    if (updateUser.getFirstName() != null) {
      user.setFirstName(updateUser.getFirstName());
    }
    if (updateUser.getLastName() != null) {
      user.setLastName(updateUser.getLastName());
    }
    if (updateUser.getShortInfo() != null) {
      user.setShortInfo(updateUser.getShortInfo());
    }
    if (updateUser.getBio() != null) {
      user.setBio(updateUser.getBio());
    }
    if (updateUser.getNewEmail() != null && !updateUser.getNewEmail().equals(user.getEmail())) {
      boolean isExistsEmail = userRepository.existsByEmail(updateUser.getNewEmail());
      if (isExistsEmail) {
        bindingResult.rejectValue("newEmail", "email.exists", "This email is already taken");
      } else {
        user.setEmail(updateUser.getNewEmail());
      }
    }
    if (updateUser.getNewPassword() != null && !updateUser.getNewPassword().isEmpty()) {
      if (passwordEncoder.matches(updateUser.getCurrentPassword(), user.getPassword())) {
        user.setPassword(passwordEncoder.encode(updateUser.getNewPassword()));
      } else {
        bindingResult.rejectValue("currentPassword", "password.incorrect",
            "Current password is incorrect");
      }
    }
    if (updateUser.getAvatar() != null && !updateUser.getAvatar().isEmpty()) {
      String avatarUrl = fileUploadUtil.uploadUserAvatar(updateUser.getAvatar(), user.getId());
      user.setAvatarUrl(avatarUrl);
    }
    return convertUserModelToDto(userRepository.save(user));
  }

  @Transactional
  public void deleteUser(String username) {
    userRepository.deleteByUsername(username);
  }

  @Transactional
  public void subscribe(String subscriberUsername, String targetUsername) {
    if (subscriberUsername.equals(targetUsername)) {
      throw new IllegalArgumentException("User cannot subscribe to themselves.");
    }
    UserModel subscriber = userRepository.findByUsername(subscriberUsername)
        .orElseThrow(() -> new UsernameNotFoundException("Subscriber not found"));
    UserModel targetUser = userRepository.findByUsername(targetUsername)
        .orElseThrow(() -> new UsernameNotFoundException("Target user not found"));
    boolean subscribedExists = subscriptionRepository.existsBySubscriberAndUser(subscriber,
        targetUser);
    if (subscribedExists) {
      throw new SubscriptionAlreadyExistsException("User already subscribed");
    }
    SubscriptionModel subscription = SubscriptionModel.builder().subscriber(subscriber)
        .user(targetUser).build();
    subscriptionRepository.save(subscription);
  }

  @Transactional
  public boolean isSubscribed(String subscriberUsername, String targetUsername) {
    UserModel subscriber = userRepository.findByUsername(subscriberUsername)
        .orElseThrow(() -> new UsernameNotFoundException("Subscriber not found"));
    UserModel targetUser = userRepository.findByUsername(targetUsername)
        .orElseThrow(() -> new UsernameNotFoundException("Target user not found"));
    return subscriptionRepository.existsBySubscriberAndUser(subscriber, targetUser);
  }

  @Transactional
  public void unsubscribe(String subscriberUsername, String targetUsername) {
    UserModel subscriber = userRepository.findByUsername(subscriberUsername)
        .orElseThrow(() -> new UsernameNotFoundException("Subscriber not found"));
    UserModel targetUser = userRepository.findByUsername(targetUsername)
        .orElseThrow(() -> new UsernameNotFoundException("Target user not found"));
    subscriptionRepository.deleteBySubscriberAndUser(subscriber, targetUser);
  }

  private GetUser convertUserModelToDto(UserModel userModel) {
    List<CategoryModel> categories = categoryRepository.findCategoriesByUserId(userModel.getId());
    Language interfaceLanguage = Language.valueOf(localizationContext.getLocale().toUpperCase());
    return GetUser.builder().username(userModel.getUsername()).email(userModel.getEmail())
        .firstName(userModel.getFirstName()).lastName(userModel.getLastName())
        .shortInfo(userModel.getShortInfo()).bio(userModel.getBio())
        .avatarUrl(userModel.getAvatarUrl()).categories(categories.stream().map(category -> {
          String localizedCategoryName = category.getName()
              .getOrDefault(interfaceLanguage, category.getName().get(Language.EN));
          return GetCategory.builder().id(category.getId()).name(localizedCategoryName).build();
        }).toList()).role(userModel.getRole()).subscriptions(userModel.getSubscriptions().stream()
            .map(sub -> GetUser.builder().username(sub.getUser().getUsername())
                .firstName(sub.getUser().getFirstName()).lastName(sub.getUser().getLastName())
                .avatarUrl(sub.getUser().getAvatarUrl()).shortInfo(sub.getUser().getShortInfo())
                .build()).toList()).subscribers(userModel.getSubscribers().stream().map(
            sub -> GetUser.builder().username(sub.getSubscriber().getUsername())
                .firstName(sub.getSubscriber().getFirstName())
                .lastName(sub.getSubscriber().getLastName())
                .avatarUrl(sub.getSubscriber().getAvatarUrl())
                .shortInfo(sub.getSubscriber().getShortInfo()).build()).toList()).build();
  }
}
