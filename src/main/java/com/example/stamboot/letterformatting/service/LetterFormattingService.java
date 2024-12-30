package com.example.stamboot.letterformatting.service;

import com.example.stamboot.letterformatting.DocumentType;
import com.example.stamboot.letterformatting.bl.LetterFormattingManager;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LetterFormattingService {

    private final static List<String> DOC_TYPE_VALUES;

    static {
        DOC_TYPE_VALUES = Arrays.stream(DocumentType.values()).map(DocumentType::toString).collect(Collectors.toList());
    }

    @Resource
    private LetterFormattingManager letterFormattingManager;

    public String formatLetter(String content, String type) {
        DocumentType documentType = getDocumentType(type);
        String formattedLetter = letterFormattingManager.handleLetterFormatting(content, documentType);
        return formattedLetter;
    }

    private DocumentType getDocumentType(String type) {
        DocumentType documentType;
        try {
            documentType = DocumentType.valueOf(type);
        } catch (NullPointerException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Requested document type '" + type + "' is not supported\n" +
                    "Supported document types: " + DOC_TYPE_VALUES, e);
        }
        return documentType;
    }
}
