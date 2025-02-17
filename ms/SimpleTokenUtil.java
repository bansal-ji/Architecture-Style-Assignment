import java.util.Base64;

public class SimpleTokenUtil {
    // The secret key; choose a sufficiently long random string in a real system.
    private static final String SECRET_KEY = System.getenv("SECRET_KEY");

    /**
     * Generates a token for the given username that remains valid for validityMillis milliseconds.
     * The token embeds the username and an expiration timestamp.
     */
    public static String generateToken(String username, long validityMillis) {
        long expiration = System.currentTimeMillis() + validityMillis;
        // Create a payload in the format "username:expiration"
        String payload = username + ":" + expiration;
        // Encrypt the payload using a simple XOR-based algorithm.
        String encryptedPayload = xorEncrypt(payload, SECRET_KEY);
        // Encode the result in Base64 so it can be transmitted as a string.
        return Base64.getEncoder().encodeToString(encryptedPayload.getBytes());
    }

    /**
     * Verifies the token by decoding, decrypting, and checking that it is well-formed and not expired.
     */
    public static boolean verifyToken(String token) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(token);
            String encryptedPayload = new String(decodedBytes);
            String payload = xorEncrypt(encryptedPayload, SECRET_KEY); // XOR is symmetric
            String[] parts = payload.split(":");
            if (parts.length != 2) {
                return false;
            }
            long expiration = Long.parseLong(parts[1]);
            return System.currentTimeMillis() < expiration;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * A simple XOR-based encryption/decryption method.
     * Since XOR is symmetric, applying it twice with the same key recovers the original data.
     */
    private static String xorEncrypt(String data, String key) {
        char[] keyChars = key.toCharArray();
        char[] dataChars = data.toCharArray();
        char[] result = new char[dataChars.length];
        for (int i = 0; i < dataChars.length; i++) {
            result[i] = (char)(dataChars[i] ^ keyChars[i % keyChars.length]);
        }
        return new String(result);
    }
    
    // (Optional) Extract username from a valid token.
    public static String extractUsername(String token) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(token);
            String encryptedPayload = new String(decodedBytes);
            String payload = xorEncrypt(encryptedPayload, SECRET_KEY);
            String[] parts = payload.split(":");
            if (parts.length != 2) {
                return null;
            }
            return parts[0];
        } catch(Exception e) {
            return null;
        }
    }
    
    // (Optional) Demonstration main.
    public static void main(String[] args) {
        String token = generateToken("alice", 3600000);
        System.out.println("Generated token: " + token);
        System.out.println("Token valid? " + verifyToken(token));
        System.out.println("Username: " + extractUsername(token));
    }
}
