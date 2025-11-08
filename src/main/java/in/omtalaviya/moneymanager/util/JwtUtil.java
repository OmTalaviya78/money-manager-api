package in.omtalaviya.moneymanager.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    // Secret key for signing (must be at least 256 bits for HS256)
    private static final String SECRET_KEY = "6D5971337436763979244226452948404D635166546A576E5A72347537782141";

    // Token validity (e.g., 10 hours)
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 10;

    /**
     * Generate JWT token using email ID
     */
    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)                    // email is stored as "subject"
                .setIssuedAt(new Date(System.currentTimeMillis())) // issue time
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // expiry
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extract email (subject) from token
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Validate token against a given email
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String extractedEmail = extractEmail(token);
        return (extractedEmail.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Extract any specific claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from the token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Check if token is expired
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extract expiration date
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Decode the secret key (Base64)
     */
    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
