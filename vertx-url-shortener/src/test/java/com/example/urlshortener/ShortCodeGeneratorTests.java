package com.example.urlshortener;

import com.example.urlshortener.util.ShortCodeGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ShortCodeGeneratorTests {

    public static final int SHORT_CODE_LENGTH = 7;

    @Test
    void shouldGenerateShortCodesDeterministically(){

        String source="https://www.google.com";

        String shortCode=new ShortCodeGenerator().generate(source);

        String shortCodeNext=new ShortCodeGenerator().generate(source);

        assertEquals(shortCode,shortCodeNext);
    }

    @Test
    void shouldGenerateShortCodesOfEqualLength(){
        String source1="https://www.example1.com";
        String source2="https://www.example2.com";

        String shortCode1=new ShortCodeGenerator().generate(source1);
        String shortCode2=new ShortCodeGenerator().generate(source2);

        assertTrue(shortCode1.length()==shortCode2.length());
    }

    @Test
    void shouldGenerateDifferentShortCodesForDifferentUrls(){
        String source1="https://www.example1.com";
        String source2="https://www.example2.com";

        String shortCode1=new ShortCodeGenerator().generate(source1);
        String shortCode2=new ShortCodeGenerator().generate(source2);

        assertTrue(!shortCode1.equals(shortCode2));
    }

    @Test
    void shouldGenerateShortCodesOfLengthOfSHORT_CODE_LENGTH(){

        String source1="https://www.example1.com";
        String source2="https://www.example2.com";

        String shortCode1=new ShortCodeGenerator().generate(source1);
        String shortCode2=new ShortCodeGenerator().generate(source2);

        assertEquals(shortCode1.length(),SHORT_CODE_LENGTH);
        assertEquals(shortCode2.length(),SHORT_CODE_LENGTH);
    }
}
