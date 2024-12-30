package com.example.stamboot.letterformatting.bl;

import com.example.stamboot.letterformatting.DocumentType;
import com.example.stamboot.letterformatting.format.LetterFormatter;
import com.example.stamboot.letterformatting.format.LetterFormatterFactory;
import com.example.stamboot.letterformatting.postformat.LetterPostFormatter;
import com.example.stamboot.letterformatting.postformat.LetterPostFormatterFactory;
import com.example.stamboot.letterformatting.preformat.LetterPreFormatter;
import com.example.stamboot.letterformatting.preformat.LetterPreFormatterFactory;
import org.springframework.stereotype.Component;

@Component
public class LetterFormattingManager {

    public String handleLetterFormatting(String content, DocumentType documentType) {
        String preformattedContent = preformatLetter(content, documentType);
        String formattedContent = formatLetter(preformattedContent, documentType);
        String postFormattedString = postFormatLetter(formattedContent, documentType);
        return postFormattedString;
    }

    private String preformatLetter(String content, DocumentType documentType) {
        return getLetterPreFormatter(documentType).preFormatDocument(content);
    }

    private String formatLetter(String content, DocumentType documentType) {
        return getLetterFormatter(documentType).formatDocument(content);
    }

    private String postFormatLetter(String content, DocumentType documentType) {
        return getLetterPostFormatter(documentType).postFormatDocument(content);
    }

    private LetterPreFormatter getLetterPreFormatter(DocumentType documentType) {
        return LetterPreFormatterFactory.getInstance(documentType.toString());
    }

    private LetterFormatter getLetterFormatter(DocumentType documentType) {
        return LetterFormatterFactory.getInstance(documentType.toString());
    }

    private LetterPostFormatter getLetterPostFormatter(DocumentType documentType) {
        return LetterPostFormatterFactory.getInstance(documentType.toString());
    }
}
