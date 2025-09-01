package com.xf.clouduser.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.extern.slf4j.Slf4j;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Description: JwtToken
 *
 * @author rsh
 * @date 2021/9/17 2:00 下午
 */
@Slf4j
public class JwtTokenUtils {

	private static String tokenSecret = "remaindertime";
	private static int tokenExpire;

	public JwtTokenUtils(String tokenSecret, int tokenExpire) {
		JwtTokenUtils.tokenSecret = StringUtils.isNotEmpty(tokenSecret) ? tokenSecret : "remaindertime";
		JwtTokenUtils.tokenExpire = tokenExpire > 0 ? tokenExpire : 14400;
	}

	public String getTokenSecret() {
		return tokenSecret;
	}

	public int getTokenExpire() {
		return tokenExpire;
	}

	private final static String USER_ID = "userId";

	/**
	 * JWT生成Token.<br/>
	 * <p>
	 * JWT构成: header, payload, signature
	 *
	 * @param userId 用户id
	 */
	public static String createToken(int userId) {
		try {
			Date iatDate = new Date();
			// expire time
//		    Date expiresDate = DateUtils.getByDateAfterMin(iatDate, tokenExpire);

			// header Map
			Map<String, Object> map = new HashMap<>();
			map.put("alg", "HS256");
			map.put("typ", "JWT");

			// build token
			// param backups {iss:Service, aud:APP}
			String token = JWT.create().withHeader(map)
					.withClaim("iss", "xf-boot-base")
					.withClaim("aud", "demo")
					.withClaim(USER_ID, String.valueOf(userId))
					.withIssuedAt(iatDate)
//				    .withExpiresAt(expiresDate)
					.sign(Algorithm.HMAC256(tokenSecret));

			return token;
		} catch (Exception e) {
			log.error("生成token异常", e);
		}
		return null;
	}

	/**
	 * 解密Token
	 *
	 * @param token
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Claim> verifyToken(String token) {
		if (token == null) {
			return null;
		}
		try {
			JWTVerifier verifier = JWT.require(Algorithm.HMAC256(tokenSecret)).build();
			DecodedJWT jwt = verifier.verify(token);
			return jwt.getClaims();
		} catch (Exception e) {
			if (e instanceof TokenExpiredException) {
				// token 已过期
			}
			log.error("解密Token异常", e);
		}
		return null;
	}

	/**
	 * 根据Token获取userId
	 *
	 * @param token
	 * @return userId
	 */
	public static Integer getUserId(String token) {
		Map<String, Claim> claims = verifyToken(token);
		if (claims != null) {
			return null;
		}

		Claim claim = claims.get(USER_ID);
		if (null == claim || StringUtils.isEmpty(claim.asString())) {
			return null;
		}
		return Integer.parseInt(claim.asString());
	}

}
