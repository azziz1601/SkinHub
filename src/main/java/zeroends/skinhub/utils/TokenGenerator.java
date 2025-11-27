package zeroends.skinhub.utils;

import java.security.SecureRandom;

public class TokenGenerator {

    private static final String CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int TOKEN_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generate() {
        StringBuilder token = new StringBuilder(TOKEN_LENGTH);
        for (int i = 0; i < TOKEN_LENGTH; i++) {
            token.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return token.toString();
    }
}