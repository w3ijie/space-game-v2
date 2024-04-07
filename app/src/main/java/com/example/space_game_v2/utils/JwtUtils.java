package com.example.space_game_v2.utils;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.space_game_v2.BuildConfig;

public class JwtUtils {
    public static String generateSignedToken(String token) {
        // make sure there is secret key in BuildConfig
        String secretKey = BuildConfig.SECRET_KEY;
        Algorithm algorithm = Algorithm.HMAC256(secretKey);

        return JWT.create()
                .withClaim("token", token)
                .sign(algorithm);
    }
}
