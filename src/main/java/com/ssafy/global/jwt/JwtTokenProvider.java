package com.ssafy.global.jwt;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ssafy.global.exception.CustomException;
import com.ssafy.global.exception.ErrorCode;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtTokenProvider {

	@Value("${jwt.private-key-path}")
	private String privateKeyPath;

	@Value("${jwt.public-key-path}")
	private String publicKeyPath;

	@Value("${jwt.access-expiration-time}")
	private long accessExpiration;

	@Value("${jwt.refresh-expiration-time}")
	private long refreshExpiration;

	private java.security.PrivateKey privateKey;
	private java.security.PublicKey publicKey;

	@PostConstruct
	public void init() {
		try {
			this.privateKey = loadPrivateKey(privateKeyPath);
			this.publicKey = loadPublicKey(publicKeyPath);
			log.info("RSA 공개키/개인키를 성공적으로 로드했습니다.");
		} catch (Exception e) {
			log.error("RSA 키 로드에 실패했습니다.", e);
			throw new RuntimeException("RSA 키 로드에 실패했습니다.", e);
		}
	}

	private java.security.PrivateKey loadPrivateKey(String path) throws Exception {
		String key = new String(new org.springframework.core.io.ClassPathResource(path).getInputStream().readAllBytes(),
				java.nio.charset.StandardCharsets.UTF_8);
		String privateKeyPEM = key
				.replace("-----BEGIN PRIVATE KEY-----", "")
				.replaceAll(System.lineSeparator(), "")
				.replace("-----END PRIVATE KEY-----", "")
				.replaceAll("\\s", "");

		byte[] encoded = java.util.Base64.getDecoder().decode(privateKeyPEM);
		java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance("RSA");
		java.security.spec.PKCS8EncodedKeySpec keySpec = new java.security.spec.PKCS8EncodedKeySpec(encoded);
		return keyFactory.generatePrivate(keySpec);
	}

	private java.security.PublicKey loadPublicKey(String path) throws Exception {
		String key = new String(new org.springframework.core.io.ClassPathResource(path).getInputStream().readAllBytes(),
				java.nio.charset.StandardCharsets.UTF_8);
		String publicKeyPEM = key
				.replace("-----BEGIN PUBLIC KEY-----", "")
				.replaceAll(System.lineSeparator(), "")
				.replace("-----END PUBLIC KEY-----", "")
				.replaceAll("\\s", "");

		byte[] encoded = java.util.Base64.getDecoder().decode(publicKeyPEM);
		java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance("RSA");
		java.security.spec.X509EncodedKeySpec keySpec = new java.security.spec.X509EncodedKeySpec(encoded);
		return keyFactory.generatePublic(keySpec);
	}

	public String createAccessToken(Long userId, String username, String email) {
		Date now = new Date();

		return Jwts.builder()
				.subject(username)
				.claim("userId", userId)
				.claim("email", email)
				.issuedAt(now)
				.expiration(new Date(now.getTime() + accessExpiration))
				.signWith(privateKey)
				.compact();
	}

	public String createRefreshToken(String username) {
		Date now = new Date();

		return Jwts.builder()
				.subject(username)
				.issuedAt(now)
				.expiration(new Date(now.getTime() + refreshExpiration))
				.signWith(privateKey)
				.compact();
	}

	public void validateToken(String token) {
		parseClaims(token);
	}

	public String getUsername(String token) {
		return parseClaims(token).getSubject();
	}

	public Long getUserIdFromToken(String token) {
		return parseClaims(token).get("userId", Long.class);
	}

	public String getEmailFromToken(String token) {
		return parseClaims(token).get("email", String.class);
	}

	private Claims parseClaims(String token) {
		try {
			return Jwts.parser()
					.verifyWith(publicKey)
					.build()
					.parseSignedClaims(token)
					.getPayload();
		} catch (ExpiredJwtException e) {
			log.info("만료된 토큰입니다. {}", e.getMessage());
			throw new CustomException(ErrorCode.EXPIRED_TOKEN);
		} catch (UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
			log.info("유효하지 않은 토큰입니다. {}", e.getMessage());
			throw new CustomException(ErrorCode.INVALID_TOKEN);
		}
	}
}
