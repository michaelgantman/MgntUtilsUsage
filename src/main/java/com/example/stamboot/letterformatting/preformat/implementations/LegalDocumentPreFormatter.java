package com.example.stamboot.letterformatting.preformat.implementations;

import com.example.stamboot.letterformatting.DocumentType;
import com.example.stamboot.letterformatting.preformat.BaseLetterPreFormatter;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy(false)
public class LegalDocumentPreFormatter extends BaseLetterPreFormatter {

    public LegalDocumentPreFormatter() {
        super(DocumentType.LEGAL.toString());
    }

    @Override
    protected String doPreFormat(String doc) {
        return "Legal Entity to whom it may concern,\n" + doc;
    }
}
