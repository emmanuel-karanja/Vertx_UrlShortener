package com.example.urlshortener.util;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ShortCodeGenerator implements IShortCodeGenerator{

    @Override
    public String generate(String longUrl) {

        try {

            MessageDigest md =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    md.digest(
                            longUrl.getBytes(StandardCharsets.UTF_8));

            long value =
                    Math.abs(ByteBuffer.wrap(hash).getLong());

            String code = Base62.encode(value);

            if (code.length() >= 7) {
                return code.substring(0, 7);
            }

            return "0".repeat(7 - code.length()) + code;

        } catch (NoSuchAlgorithmException e) {

            throw new RuntimeException(e);

        }

    }
}