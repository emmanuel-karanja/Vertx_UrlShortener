package com.example.urlshortener.util;

public final class Base62 {

    private static final String CHARACTERS =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private Base62() {}

    public static String encode(long value) {

        if (value == 0) {
            return "0";
        }

        StringBuilder result = new StringBuilder();

        while (value > 0) {
            int remainder = (int)(value % 62);
            result.append(CHARACTERS.charAt(remainder));
            value = value / 62;
        }

        return result.reverse().toString();
    }
}