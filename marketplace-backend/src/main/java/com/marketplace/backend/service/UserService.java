package com.marketplace.backend.service;

import com.marketplace.backend.dto.PageResponseDto;
import com.marketplace.backend.dto.ProductDto;
import com.marketplace.backend.dto.UserDto;
import com.marketplace.backend.dto.jwt.JwtAuthenticationDto;
import com.marketplace.backend.dto.jwt.RefreshTokenDto;
import com.marketplace.backend.exception.*;
import com.marketplace.backend.mapper.PageMapper;
import com.marketplace.backend.mapper.ProductMapper;
import com.marketplace.backend.mapper.UserMapper;
import com.marketplace.backend.model.User.*;
import com.marketplace.backend.repository.AdminActionRepository;
import com.marketplace.backend.repository.ProductRepository;
import com.marketplace.backend.repository.UserRepository;
import com.marketplace.backend.security.jwt.JwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final AdminActionRepository adminActionRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final CartService cartService;
    private final MinioService minioService;
    private final ModerationPolicy moderationPolicy;
    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final PageMapper pageMapper;

    private Page<UserDto> findPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable)
                .map(user -> userMapper.mapToDto(user, minioService.buildUrlImage(user.getAvatar())));
    }

    public PageResponseDto<UserDto> findAll(int page, int size) {
        Page<UserDto> pageDto = findPage(page, size);
        return pageMapper.mapToDto(page, size, pageDto);
    }

    private User findById(String id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("Not found with id: " + id));
    }

    public boolean existByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    private User create(CreateUserRequest createUserRequest) {
        validateUser(createUserRequest);
        return userRepository.save(User.builder()
                .id(UUID.randomUUID().toString())
                .username(createUserRequest.getUsername())
                .email(createUserRequest.getEmail())
                .password(passwordEncoder.encode(createUserRequest.getPassword()))
                .roles(Set.of(Role.ROLE_USER))
                .createdAt(LocalDateTime.now())
                .wishlist(new ArrayList<>())
                .userStatus(UserStatus.ACTIVE)
                .build());
    }

    private void validateUser(CreateUserRequest createUserRequest) {
        if (existByEmail(createUserRequest.getEmail())) {
            throw new ConflictException("Email already exists");
        }
        if (existByUsername(createUserRequest.getUsername())) {
            throw new ConflictException("Username already exists");
        }
    }

    public List<UserDto> searchByUsername(String username) {
        return userRepository
                .findTop10ByUsernameContainingIgnoreCase(username)
                .stream()
                .map(user -> userMapper.mapToDto(user, minioService.buildUrlImage(user.getAvatar())))
                .toList();
    }

    @Transactional
    public UserDto createUser(CreateUserRequest createUserRequest) {
        User user = create(createUserRequest);
        cartService.create(user.getId());
        log.info("User registered: id={}, email={}", user.getId(), user.getEmail());
        return userMapper.mapToDto(user, minioService.buildUrlImage(user.getAvatar()));
    }

    @Transactional
    public UserDto update(String userId, UpdateUserRequest request) {
        User user = findById(userId);
        String oldAvatar = user.getAvatar();

        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            validateUsername(user, request.getUsername());
            user.setUsername(request.getUsername());
        }

        if (Boolean.TRUE.equals(request.getRemoveAvatar())) {
            user.setAvatar(null);
            if (oldAvatar != null) {
                minioService.delete(oldAvatar);
            }

        } else if (request.getAvatar() != null
                && !request.getAvatar().isEmpty()) {
            String newAvatarPath = minioService.upload(request.getAvatar());
            user.setAvatar(newAvatarPath);
            if (oldAvatar != null) {
                minioService.delete(oldAvatar);
            }
        }
        userRepository.save(user);
        return userMapper.mapToDto(user, minioService.buildUrlImage(user.getAvatar()));
    }

    private void validateUsername(User currentUser, String newUsername) {
        if (existByUsername(newUsername) && !currentUser.getUsername().equals(newUsername)) {
            throw new BadRequestException("This nickname is already used by another user");
        }
    }

    public UserDto findUserById(String id) {
        User user = findById(id);
        return userMapper.mapToDto(user, minioService.buildUrlImage(user.getAvatar()));
    }

    public UserDto findMeById(String currentUserId) {
        User user = findById(currentUserId);
        String avatarUrl = minioService.buildUrlImage(user.getAvatar());
        List<String> wishlistIds = Optional.ofNullable(user.getWishlist())
                .orElse(Collections.emptyList());

        List<ProductDto> wishlist = wishlistIds.isEmpty()
                ? Collections.emptyList()
                : productRepository.findAllById(wishlistIds).stream()
                        .map(product -> productMapper.mapToDto(product, minioService.buildUrlImage(product.getImages())))
                        .toList();
        return userMapper.mapMeToDto(user, avatarUrl, wishlist);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found by Email: " + email));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found by username: " + username));
    }

    public void delete(String userId, String currentUserId) {
        if (!userId.equals(currentUserId)) {
            throw new AccessDeniedException("You can delete only your account");
        }
        userRepository.deleteById(userId);
    }

    public JwtAuthenticationDto signIn(AuthRequest authRequest) throws AuthenticationException {
        User user = findByCredentials(authRequest);
        log.info("User signed in: id={}", user.getId());
        return jwtService.generateTokenPair(user.getEmail(), user.getId(), user.getRoles());
    }

    private User findByCredentials(AuthRequest authRequest) throws AuthenticationException {
        User user = findByEmail(authRequest.getEmail());
        validateAuthRequest(authRequest, user);
        moderationPolicy.validateUserAccess(user);
        return user;
    }

    private void validateAuthRequest(AuthRequest authRequest, User user) {
        if (!passwordEncoder.matches(authRequest.getPassword(), user.getPassword())) {
            log.warn("Failed login attempt for user id={}", user.getId());
            throw new AuthenticationException("Email or password incorrect!");
        }
    }

    public JwtAuthenticationDto refreshToken(RefreshTokenDto refreshTokenDto) {
        String refreshToken = refreshTokenDto.getRefreshToken();
        if (refreshToken == null) {
            throw new AuthenticationException("Refresh token is missing");
        }

        Claims claims = jwtService.validateAndExtract(refreshToken)
                .orElseThrow(() -> new AuthenticationException("Invalid refresh token"));

        if (!JwtService.TokenType.REFRESH.name().equals(claims.get("type", String.class))) {
            throw new AuthenticationException("Invalid token type");
        }

        User user = findByEmail(claims.getSubject());
        return jwtService.generateTokenPair(user.getEmail(), user.getId(), user.getRoles());
    }

    public UserDto addProductToWishList(String productId, String currentUserId) {
        if (currentUserId == null || productId == null) {
            throw new BadRequestException("Missing required fields");
        }
        User user = findById(currentUserId);
        validateAddedProduct(productId);
        if (user.getWishlist().contains(productId)) {
            return userMapper.mapToDto(user, minioService.buildUrlImage(user.getAvatar()));
        }
        user.getWishlist().add(productId);
        userRepository.save(user);
        return userMapper.mapToDto(user, minioService.buildUrlImage(user.getAvatar()));
    }

    private void validateAddedProduct(String productId) {
        if (!productRepository.existsById(productId)) {
            throw new NotFoundException("Product not found");
        }
        if (adminActionRepository.existsByTargetId(productId)) {
            throw new ForbiddenException("Action impossible: product is banned.");
        }
    }

    public void deleteProductFromWishList(String productId, String currentUserId) {
        if (currentUserId == null || productId == null) {
            throw new BadRequestException("Missing required fields");
        }
        User user = findById(currentUserId);
        boolean removed = user.getWishlist().remove(productId);
        if (!removed) {
            return;
        }
        userRepository.save(user);
    }
}