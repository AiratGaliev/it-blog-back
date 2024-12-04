package com.github.codogma.codogmaback.service;

import com.github.codogma.codogmaback.dto.GetCategory;
import com.github.codogma.codogmaback.dto.GetTag;
import com.github.codogma.codogmaback.dto.GetUser;
import com.github.codogma.codogmaback.dto.UpdateUser;
import com.github.codogma.codogmaback.dto.UserRole;
import com.github.codogma.codogmaback.exception.ExceptionFactory;
import com.github.codogma.codogmaback.interceptor.localization.LocalizationContext;
import com.github.codogma.codogmaback.model.CategoryModel;
import com.github.codogma.codogmaback.model.Language;
import com.github.codogma.codogmaback.model.SubscriptionModel;
import com.github.codogma.codogmaback.model.TagModel;
import com.github.codogma.codogmaback.model.UserModel;
import com.github.codogma.codogmaback.repository.CategoryRepository;
import com.github.codogma.codogmaback.repository.SubscriptionRepository;
import com.github.codogma.codogmaback.repository.TagRepository;
import com.github.codogma.codogmaback.repository.UserRepository;
import com.github.codogma.codogmaback.repository.specifications.UserSpecifications;
import com.github.codogma.codogmaback.util.FileUploadUtil;
import com.github.codogma.codogmaback.util.LocalizationUtil;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

@Service
@RequiredArgsConstructor
public class UserService {

  private final EntityManager entityManager;
  private final ExceptionFactory exceptionFactory;
  private final UserRepository userRepository;
  private final TagRepository tagRepository;
  private final CategoryRepository categoryRepository;
  private final SubscriptionRepository subscriptionRepository;
  private final FileUploadUtil fileUploadUtil;
  private final PasswordEncoder passwordEncoder;
  private final LocalizationContext localizationContext;
  private final LocalizationUtil localizationUtil;

  @Value("${search.results.limit}")
  private int searchResultsLimit;

  @Transactional
  public Page<GetUser> getUsers(Long categoryId, UserRole role, String tag, String info, int page,
      int size, String sort, String order, Boolean isSubscriptions, Boolean isSubscribers,
      UserModel userModel) {
    UserModel foundUser = userModel != null ? userRepository.findById(userModel.getId())
        .orElseThrow(() -> exceptionFactory.userNotFound(userModel.getUsername())) : null;
    Sort.Direction sortDirection = Sort.Direction.fromString(order);
    Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
    List<Long> usersIds = null;
    if (info != null && !info.isEmpty()) {
      SearchSession searchSession = Search.session(entityManager);
      usersIds = searchSession.search(UserModel.class).where(
              f -> f.match().fields("username", "firstName", "lastName", "shortInfo", "bio")
                  .matching(info).fuzzy(1)).fetchHits(searchResultsLimit).stream().map(UserModel::getId)
          .toList();
    }
    Specification<UserModel> spec = UserSpecifications.buildSpecification(categoryId, role, tag,
        usersIds, isSubscriptions, isSubscribers, foundUser);
    return userRepository.findAll(spec, pageable).map(this::convertUserModelToDto);
  }

  @Transactional
  public Optional<GetUser> getUserByUsername(String username) {
    return userRepository.findByUsername(username).map(this::convertUserModelToDto);
  }

  @Transactional
  public GetUser updateUser(UpdateUser updateUser, UserModel userModel,
      BindingResult bindingResult) {
    if (updateUser.getUsername() != null && !updateUser.getUsername()
        .equals(userModel.getUsername())) {
      boolean isExistsUser = userRepository.existsByUsername(updateUser.getUsername());
      if (isExistsUser) {
        bindingResult.rejectValue("username", "username.exists",
            localizationUtil.getMessage("user.username.exists"));
      } else {
        userModel.setUsername(updateUser.getUsername());
      }
    }
    if (updateUser.getFirstName() != null) {
      userModel.setFirstName(updateUser.getFirstName());
    }
    if (updateUser.getLastName() != null) {
      userModel.setLastName(updateUser.getLastName());
    }
    if (updateUser.getShortInfo() != null) {
      userModel.setShortInfo(updateUser.getShortInfo());
    }
    if (updateUser.getBio() != null) {
      userModel.setBio(updateUser.getBio());
    }
    if (updateUser.getNewEmail() != null && !updateUser.getNewEmail()
        .equals(userModel.getEmail())) {
      boolean isExistsEmail = userRepository.existsByEmail(updateUser.getNewEmail());
      if (isExistsEmail) {
        bindingResult.rejectValue("newEmail", "email.exists",
            localizationUtil.getMessage("email.exists"));
      } else {
        userModel.setEmail(updateUser.getNewEmail());
      }
    }
    if (updateUser.getNewPassword() != null && !updateUser.getNewPassword().isEmpty()) {
      if (passwordEncoder.matches(updateUser.getCurrentPassword(), userModel.getPassword())) {
        userModel.setPassword(passwordEncoder.encode(updateUser.getNewPassword()));
      } else {
        bindingResult.rejectValue("currentPassword", "password.incorrect",
            localizationUtil.getMessage("user.password.incorrect"));
      }
    }
    if (updateUser.getAvatar() != null && !updateUser.getAvatar().isEmpty()) {
      String avatarUrl = fileUploadUtil.uploadUserAvatar(updateUser.getAvatar(), userModel.getId());
      userModel.setAvatarUrl(avatarUrl);
    }
    return convertUserModelToDto(userRepository.save(userModel));
  }

  @Transactional
  public void deleteUser(String username) {
    userRepository.deleteByUsername(username);
  }

  @Transactional
  public void subscribe(String targetUsername, UserModel subscriber) {
    if (subscriber.getUsername().equals(targetUsername)) {
      throw exceptionFactory.userCannotSubscribeToThemselves();
    }
    UserModel targetUser = userRepository.findByUsername(targetUsername)
        .orElseThrow(() -> exceptionFactory.targetUserNotFound(targetUsername));
    boolean subscribedExists = subscriptionRepository.existsBySubscriberAndUser(subscriber,
        targetUser);
    if (subscribedExists) {
      throw exceptionFactory.subscriptionAlreadyExists();
    }
    SubscriptionModel subscription = SubscriptionModel.builder().subscriber(subscriber)
        .user(targetUser).build();
    subscriptionRepository.save(subscription);
  }

  @Transactional
  public boolean isSubscribed(String targetUsername, UserModel subscriber) {
    UserModel targetUser = userRepository.findByUsername(targetUsername)
        .orElseThrow(() -> exceptionFactory.targetUserNotFound(targetUsername));
    return subscriptionRepository.existsBySubscriberAndUser(subscriber, targetUser);
  }

  @Transactional
  public void unsubscribe(String targetUsername, UserModel subscriber) {
    UserModel targetUser = userRepository.findByUsername(targetUsername)
        .orElseThrow(() -> exceptionFactory.targetUserNotFound(targetUsername));
    subscriptionRepository.deleteBySubscriberAndUser(subscriber, targetUser);
  }

  private GetUser convertUserModelToDto(UserModel userModel) {
    List<CategoryModel> categories = categoryRepository.findCategoriesByUserId(userModel.getId());
    Language interfaceLanguage = localizationContext.getLocale();
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
                .shortInfo(sub.getSubscriber().getShortInfo()).build()).toList())
        .favorites(userModel.getFavorites().stream().map(favorite -> {
          List<TagModel> topTags = tagRepository.findTop10TagsByCategoryId(
              favorite.getCategory().getId());
          String localizedCategoryName = favorite.getCategory().getName()
              .getOrDefault(interfaceLanguage, favorite.getCategory().getName().get(Language.EN));
          String localizedCategoryDescription = favorite.getCategory().getDescription()
              .getOrDefault(interfaceLanguage,
                  favorite.getCategory().getDescription().get(Language.EN));
          return GetCategory.builder().id(favorite.getCategory().getId())
              .name(localizedCategoryName).imageUrl(favorite.getCategory().getImageUrl())
              .description(localizedCategoryDescription).tags(topTags.stream().map(
                  tagModel -> GetTag.builder().id(tagModel.getId()).name(tagModel.getName())
                      .build()).toList()).build();
        }).toList()).build();
  }
}