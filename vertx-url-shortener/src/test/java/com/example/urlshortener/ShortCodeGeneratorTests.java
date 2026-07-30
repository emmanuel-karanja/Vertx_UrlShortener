package com.example.urlshortener;

import com.example.urlshortener.util.ShortCodeGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ShortCodeGeneratorTests {

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
    void shouldGenerateShortCodesOfLength7(){

        String source1="https://www.example1.com";
        String source2="https://www.example2.com";

        String shortCode1=new ShortCodeGenerator().generate(source1);
        String shortCode2=new ShortCodeGenerator().generate(source2);

        assertTrue(shortCode1.length()==7);
        assertTrue(shortCode2.length()==7);

    }
}
