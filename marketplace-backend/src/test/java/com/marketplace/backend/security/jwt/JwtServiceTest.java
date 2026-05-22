package com.marketplace.backend.security.jwt;

import com.marketplace.backend.dto.jwt.JwtAuthenticationDto;
import com.marketplace.backend.model.User.Role;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

	private static final String TEST_SECRET_HEX =
			"fc6b7015f4c48b13e4780016c5d83f3229fe9b62f5fb5d913b86ae11640f9f88";

	private JwtService jwtService;

	@BeforeEach
	void setUp() {
		jwtService = new JwtService();
		ReflectionTestUtils.setField(jwtService, "jwtSecret", TEST_SECRET_HEX);
	}

	@Test
	void generateTokenPair_producesBothTokens() {
		JwtAuthenticationDto pair = jwtService.generateTokenPair(
				"alice@example.com", "user-1", Set.of(Role.ROLE_USER));

		assertThat(pair.getToken()).isNotBlank();
		assertThat(pair.getRefreshToken()).isNotBlank();
		assertThat(pair.getToken()).isNotEqualTo(pair.getRefreshToken());
	}

	@Test
	void validateAndExtract_validAccessToken_returnsClaims() {
		String token = jwtService.generateToken(
				"alice@example.com", "user-1", Set.of(Role.ROLE_USER));

		Optional<Claims> claims = jwtService.validateAndExtract(token);

		assertThat(claims).isPresent();
		assertThat(claims.get().getSubject()).isEqualTo("alice@example.com");
		assertThat(claims.get().get("userId", String.class)).isEqualTo("user-1");
		assertThat(claims.get().get("type", String.class)).isEqualTo("ACCESS");
	}

	@Test
	void validateAndExtract_validRefreshToken_returnsClaims() {
		String token = jwtService.generateRefreshToken(
				"alice@example.com", "user-1", Set.of(Role.ROLE_USER));

		Optional<Claims> claims = jwtService.validateAndExtract(token);

		assertThat(claims).isPresent();
		assertThat(claims.get().get("type", String.class)).isEqualTo("REFRESH");
	}

	@Test
	void validateAndExtract_malformedToken_returnsEmpty() {
		Optional<Claims> claims = jwtService.validateAndExtract("definitely.not.a.jwt");
		assertThat(claims).isEmpty();
	}

	@Test
	void validateAndExtract_tokenWithWrongSignature_returnsEmpty() {
		// Sign with our secret, then mess with the payload by swapping the secret
		String token = jwtService.generateToken(
				"alice@example.com", "user-1", Set.of(Role.ROLE_USER));

		// Use a different secret to sign — should fail signature check
		JwtService rogue = new JwtService();
		ReflectionTestUtils.setField(rogue, "jwtSecret",
				"deadbeef".repeat(8)); // different 64-char hex

		Optional<Claims> claims = rogue.validateAndExtract(token);
		assertThat(claims).isEmpty();
	}

	@Test
	void getEmailFromToken_returnsSubject() {
		String token = jwtService.generateToken(
				"alice@example.com", "user-1", Set.of(Role.ROLE_USER));
		assertThat(jwtService.getEmailFromToken(token)).isEqualTo("alice@example.com");
	}

	@Test
	void getUserIdFromToken_returnsUserIdClaim() {
		String token = jwtService.generateToken(
				"alice@example.com", "user-42", Set.of(Role.ROLE_USER));
		assertThat(jwtService.getUserIdFromToken(token)).isEqualTo("user-42");
	}

	@Test
	void getTokenType_returnsAccessOrRefresh() {
		String access = jwtService.generateToken("a@b.c", "u", Set.of(Role.ROLE_USER));
		String refresh = jwtService.generateRefreshToken("a@b.c", "u", Set.of(Role.ROLE_USER));

		assertThat(jwtService.getTokenType(access)).isEqualTo("ACCESS");
		assertThat(jwtService.getTokenType(refresh)).isEqualTo("REFRESH");
	}
}
