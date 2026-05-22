package com.marketplace.backend.service;

import com.marketplace.backend.dto.PageResponseDto;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    // ── constants ────────────────────────────────────────────────────────────
    private static final String TEST_SECRET =
            "fc6b7015f4c48b13e4780016c5d83f3229fe9b62f5fb5d913b86ae11640f9f88";

    // ── mocks ────────────────────────────────────────────────────────────────
    @Mock
    UserRepository userRepository;
    @Mock
    ProductRepository productRepository;
    @Mock
    AdminActionRepository adminActionRepository;
    @Mock
    JwtService jwtService;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    CartService cartService;
    @Mock
    MinioService minioService;
    @Mock
    ModerationPolicy moderationPolicy;
    @Mock
    UserMapper userMapper;
    @Mock
    ProductMapper productMapper;
    @Mock
    PageMapper pageMapper;

    @InjectMocks
    UserService userService;

    /**
     * Real JwtService used only to generate valid tokens/claims for test data.
     */
    private JwtService tokenHelper;

    @BeforeEach
    void setUp() {
        tokenHelper = new JwtService();
        ReflectionTestUtils.setField(tokenHelper, "jwtSecret", TEST_SECRET);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private User buildUser(String id) {
        return User.builder()
                .id(id)
                .username("user_" + id)
                .email(id + "@test.com")
                .password("encodedPwd")
                .roles(Set.of(Role.ROLE_USER))
                .wishlist(new ArrayList<>())
                .userStatus(UserStatus.ACTIVE)
                .build();
    }

    // ── findAll ──────────────────────────────────────────────────────────────

    @Test
    void findAll_returnsMappedPage() {
        User user = buildUser("u-1");
        Page<User> page = new PageImpl<>(List.of(user));

        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(minioService.buildUrlImage(nullable(String.class))).thenReturn(null);
        when(userMapper.mapToDto(any(), any())).thenReturn(new UserDto());
        when(pageMapper.mapToDto(anyInt(), anyInt(), any())).thenReturn(new PageResponseDto<>());

        PageResponseDto<UserDto> result = userService.findAll(0, 5);
        assertThat(result).isNotNull();
    }

    // ── findUserById ──────────────────────────────────────────────────────────

    @Test
    void findUserById_found_returnsDto() {
        User user = buildUser("u-1");
        UserDto dto = UserDto.builder().id("u-1").build();

        when(userRepository.findById("u-1")).thenReturn(Optional.of(user));
        when(minioService.buildUrlImage(nullable(String.class))).thenReturn(null);
        when(userMapper.mapToDto(any(), any())).thenReturn(dto);

        UserDto result = userService.findUserById("u-1");
        assertThat(result.getId()).isEqualTo("u-1");
    }

    @Test
    void findUserById_notFound_throwsNotFoundException() {
        when(userRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findUserById("missing"))
                .isInstanceOf(NotFoundException.class);
    }

    // ── existByUsername / existByEmail ────────────────────────────────────────

    @Test
    void existByUsername_returnsTrue() {
        when(userRepository.existsByUsername("alice")).thenReturn(true);
        assertThat(userService.existByUsername("alice")).isTrue();
    }

    @Test
    void existByEmail_returnsFalse() {
        when(userRepository.existsByEmail("alice@test.com")).thenReturn(false);
        assertThat(userService.existByEmail("alice@test.com")).isFalse();
    }

    // ── createUser ────────────────────────────────────────────────────────────

    @Test
    void createUser_validRequest_savesAndReturnsDto() {
        CreateUserRequest req = CreateUserRequest.builder()
                .username("newuser").email("new@test.com").password("password123").build();
        User user = buildUser("u-new");
        UserDto dto = UserDto.builder().id("u-new").build();

        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPwd");
        when(userRepository.save(any())).thenReturn(user);
        when(minioService.buildUrlImage(nullable(String.class))).thenReturn(null);
        when(userMapper.mapToDto(any(), any())).thenReturn(dto);
        when(cartService.create(any())).thenReturn(null);

        UserDto result = userService.createUser(req);
        assertThat(result.getId()).isEqualTo("u-new");
    }

    @Test
    void createUser_emailExists_throwsConflict() {
        CreateUserRequest req = CreateUserRequest.builder()
                .username("newuser").email("existing@test.com").password("password123").build();

        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(req))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void createUser_usernameExists_throwsConflict() {
        CreateUserRequest req = CreateUserRequest.builder()
                .username("taken").email("new@test.com").password("password123").build();

        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(userRepository.existsByUsername("taken")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(req))
                .isInstanceOf(ConflictException.class);
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    void update_changeUsername_updatesAndReturns() {
        User user = buildUser("u-1");
        UserDto dto = UserDto.builder().id("u-1").build();
        UpdateUserRequest req = UpdateUserRequest.builder().username("newname").build();

        when(userRepository.findById("u-1")).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("newname")).thenReturn(false);
        when(minioService.buildUrlImage(nullable(String.class))).thenReturn(null);
        when(userMapper.mapToDto(any(), any())).thenReturn(dto);

        UserDto result = userService.update("u-1", req);
        assertThat(result).isNotNull();
        assertThat(user.getUsername()).isEqualTo("newname");
    }

    @Test
    void update_sameUsername_doesNotThrow() {
        User user = buildUser("u-1");           // username = "user_u-1"
        UserDto dto = UserDto.builder().id("u-1").build();
        UpdateUserRequest req = UpdateUserRequest.builder().username("user_u-1").build();

        when(userRepository.findById("u-1")).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("user_u-1")).thenReturn(true);
        when(minioService.buildUrlImage(nullable(String.class))).thenReturn(null);
        when(userMapper.mapToDto(any(), any())).thenReturn(dto);

        assertThatCode(() -> userService.update("u-1", req)).doesNotThrowAnyException();
    }

    @Test
    void update_removeAvatar_deletesOldAvatar() {
        User user = buildUser("u-1");
        user.setAvatar("old-avatar.jpg");
        UpdateUserRequest req = UpdateUserRequest.builder().removeAvatar(true).build();
        UserDto dto = UserDto.builder().id("u-1").build();

        when(userRepository.findById("u-1")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(minioService.buildUrlImage(nullable(String.class))).thenReturn(null);
        when(userMapper.mapToDto(any(), any())).thenReturn(dto);

        userService.update("u-1", req);

        verify(minioService).delete("old-avatar.jpg");
        assertThat(user.getAvatar()).isNull();
    }

    @Test
    void update_uploadAvatar_uploadsNewAndDeletesOld() {
        User user = buildUser("u-1");
        user.setAvatar("old-avatar.jpg");
        MockMultipartFile avatarFile =
                new MockMultipartFile("avatar", "avatar.jpg", "image/jpeg", new byte[]{1, 2, 3});
        UpdateUserRequest req = UpdateUserRequest.builder().avatar(avatarFile).build();
        UserDto dto = UserDto.builder().id("u-1").build();

        when(userRepository.findById("u-1")).thenReturn(Optional.of(user));
        when(minioService.upload(any(MultipartFile.class))).thenReturn("new-avatar.jpg");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(minioService.buildUrlImage(nullable(String.class))).thenReturn(null);
        when(userMapper.mapToDto(any(), any())).thenReturn(dto);

        userService.update("u-1", req);

        verify(minioService).upload(avatarFile);
        verify(minioService).delete("old-avatar.jpg");
        assertThat(user.getAvatar()).isEqualTo("new-avatar.jpg");
    }

    @Test
    void update_usernameAlreadyTakenByOther_throwsBadRequest() {
        User user = buildUser("u-1");
        UpdateUserRequest req = UpdateUserRequest.builder().username("takenname").build();

        when(userRepository.findById("u-1")).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("takenname")).thenReturn(true);

        assertThatThrownBy(() -> userService.update("u-1", req))
                .isInstanceOf(BadRequestException.class);
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    void delete_ownAccount_deletesSuccessfully() {
        userService.delete("u-1", "u-1");
        verify(userRepository).deleteById("u-1");
    }

    @Test
    void delete_differentUser_throwsAccessDenied() {
        assertThatThrownBy(() -> userService.delete("u-1", "u-2"))
                .isInstanceOf(AccessDeniedException.class);
    }

    // ── signIn ────────────────────────────────────────────────────────────────

    @Test
    void signIn_validCredentials_returnsTokenPair() throws AuthenticationException {
        User user = buildUser("u-1");
        AuthRequest req = AuthRequest.builder().email("u-1@test.com").password("rawPwd").build();
        JwtAuthenticationDto tokens = JwtAuthenticationDto.builder()
                .token("access").refreshToken("refresh").build();

        when(userRepository.findByEmail("u-1@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("rawPwd", "encodedPwd")).thenReturn(true);
        doNothing().when(moderationPolicy).validateUserAccess(user);
        when(jwtService.generateTokenPair(any(), any(), any())).thenReturn(tokens);

        JwtAuthenticationDto result = userService.signIn(req);
        assertThat(result.getToken()).isEqualTo("access");
    }

    @Test
    void signIn_wrongPassword_throwsAuthenticationException() {
        User user = buildUser("u-1");
        AuthRequest req = AuthRequest.builder().email("u-1@test.com").password("wrong").build();

        when(userRepository.findByEmail("u-1@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encodedPwd")).thenReturn(false);

        assertThatThrownBy(() -> userService.signIn(req))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void signIn_bannedUser_throwsAuthenticationException() throws AuthenticationException {
        User user = buildUser("u-1");
        AuthRequest req = AuthRequest.builder().email("u-1@test.com").password("rawPwd").build();

        when(userRepository.findByEmail("u-1@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("rawPwd", "encodedPwd")).thenReturn(true);
        doThrow(new AuthenticationException("banned"))
                .when(moderationPolicy).validateUserAccess(user);

        assertThatThrownBy(() -> userService.signIn(req))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("banned");
    }

    // ── refreshToken ──────────────────────────────────────────────────────────

    @Test
    void refreshToken_validRefreshToken_returnsNewTokenPair() {
        User user = buildUser("u-1");
        // Use real JwtService to produce a valid refresh token and real Claims
        String rawRefresh = tokenHelper.generateRefreshToken(
                "u-1@test.com", "u-1", Set.of(Role.ROLE_USER));
        Claims claims = tokenHelper.extractAllClaims(rawRefresh);

        RefreshTokenDto dto = RefreshTokenDto.builder().refreshToken(rawRefresh).build();
        JwtAuthenticationDto tokens = JwtAuthenticationDto.builder()
                .token("new-access").refreshToken("new-refresh").build();

        when(jwtService.validateAndExtract(rawRefresh)).thenReturn(Optional.of(claims));
        when(userRepository.findByEmail("u-1@test.com")).thenReturn(Optional.of(user));
        when(jwtService.generateTokenPair(any(), any(), any())).thenReturn(tokens);

        JwtAuthenticationDto result = userService.refreshToken(dto);
        assertThat(result.getToken()).isEqualTo("new-access");
    }

    @Test
    void refreshToken_missingToken_throwsAuthenticationException() {
        RefreshTokenDto dto = RefreshTokenDto.builder().refreshToken(null).build();

        assertThatThrownBy(() -> userService.refreshToken(dto))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("missing");
    }

    @Test
    void refreshToken_invalidToken_throwsAuthenticationException() {
        RefreshTokenDto dto = RefreshTokenDto.builder().refreshToken("bad-token").build();
        when(jwtService.validateAndExtract("bad-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.refreshToken(dto))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void refreshToken_wrongTokenType_throwsAuthenticationException() {
        // Generate a real ACCESS token (wrong type for refresh endpoint)
        String accessToken = tokenHelper.generateToken(
                "u-1@test.com", "u-1", Set.of(Role.ROLE_USER));
        Claims claims = tokenHelper.extractAllClaims(accessToken);

        RefreshTokenDto dto = RefreshTokenDto.builder().refreshToken(accessToken).build();
        when(jwtService.validateAndExtract(accessToken)).thenReturn(Optional.of(claims));

        assertThatThrownBy(() -> userService.refreshToken(dto))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Invalid token type");
    }

    // ── addProductToWishList ──────────────────────────────────────────────────

    @Test
    void addProductToWishList_validProduct_addsToWishlist() {
        User user = buildUser("u-1");
        UserDto dto = UserDto.builder().id("u-1").build();

        when(userRepository.findById("u-1")).thenReturn(Optional.of(user));
        when(productRepository.existsById("p-1")).thenReturn(true);
        when(adminActionRepository.existsByTargetId("p-1")).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(minioService.buildUrlImage(nullable(String.class))).thenReturn(null);
        when(userMapper.mapToDto(any(), any())).thenReturn(dto);

        userService.addProductToWishList("p-1", "u-1");

        assertThat(user.getWishlist()).contains("p-1");
    }

    @Test
    void addProductToWishList_alreadyInWishlist_doesNotDuplicate() {
        User user = buildUser("u-1");
        user.getWishlist().add("p-1");
        UserDto dto = UserDto.builder().id("u-1").build();

        when(userRepository.findById("u-1")).thenReturn(Optional.of(user));
        when(productRepository.existsById("p-1")).thenReturn(true);
        when(adminActionRepository.existsByTargetId("p-1")).thenReturn(false);
        when(minioService.buildUrlImage(nullable(String.class))).thenReturn(null);
        when(userMapper.mapToDto(any(), any())).thenReturn(dto);

        userService.addProductToWishList("p-1", "u-1");

        assertThat(user.getWishlist()).hasSize(1);
        verify(userRepository, never()).save(any());
    }

    @Test
    void addProductToWishList_productNotFound_throwsNotFoundException() {
        when(userRepository.findById("u-1")).thenReturn(Optional.of(buildUser("u-1")));
        when(productRepository.existsById("p-missing")).thenReturn(false);

        assertThatThrownBy(() -> userService.addProductToWishList("p-missing", "u-1"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void addProductToWishList_bannedProduct_throwsForbidden() {
        when(userRepository.findById("u-1")).thenReturn(Optional.of(buildUser("u-1")));
        when(productRepository.existsById("p-1")).thenReturn(true);
        when(adminActionRepository.existsByTargetId("p-1")).thenReturn(true);

        assertThatThrownBy(() -> userService.addProductToWishList("p-1", "u-1"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void addProductToWishList_nullUserId_throwsBadRequest() {
        assertThatThrownBy(() -> userService.addProductToWishList("p-1", null))
                .isInstanceOf(BadRequestException.class);
    }

    // ── deleteProductFromWishList ─────────────────────────────────────────────

    @Test
    void deleteProductFromWishList_productInList_removes() {
        User user = buildUser("u-1");
        user.getWishlist().add("p-1");

        when(userRepository.findById("u-1")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        userService.deleteProductFromWishList("p-1", "u-1");

        assertThat(user.getWishlist()).doesNotContain("p-1");
        verify(userRepository).save(user);
    }

    @Test
    void deleteProductFromWishList_productNotInList_doesNotSave() {
        User user = buildUser("u-1");

        when(userRepository.findById("u-1")).thenReturn(Optional.of(user));

        userService.deleteProductFromWishList("p-not-in-list", "u-1");

        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteProductFromWishList_nullProductId_throwsBadRequest() {
        assertThatThrownBy(() -> userService.deleteProductFromWishList(null, "u-1"))
                .isInstanceOf(BadRequestException.class);
    }

    // ── findMeById ────────────────────────────────────────────────────────────

    @Test
    void findMeById_withEmptyWishlist_returnsDto() {
        User user = buildUser("u-1");
        UserDto dto = UserDto.builder().id("u-1").build();

        when(userRepository.findById("u-1")).thenReturn(Optional.of(user));
        when(minioService.buildUrlImage(nullable(String.class))).thenReturn(null);
        when(userMapper.mapMeToDto(any(), any(), any())).thenReturn(dto);

        UserDto result = userService.findMeById("u-1");
        assertThat(result).isNotNull();
    }

    // ── findByEmail / findByUsername ───────────────────────────────────────────

    @Test
    void findByEmail_notFound_throwsNotFoundException() {
        when(userRepository.findByEmail("none@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByEmail("none@test.com"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findByUsername_notFound_throwsNotFoundException() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByUsername("ghost"))
                .isInstanceOf(NotFoundException.class);
    }
}
