package com.example.stamboot.letterformatting.preformat.implementations;

import com.example.stamboot.letterformatting.DocumentType;
import com.example.stamboot.letterformatting.preformat.BaseLetterPreFormatter;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy(false)
public class InternalDocumentPreFormatter extends BaseLetterPreFormatter {

    public InternalDocumentPreFormatter() {
        super(DocumentType.INTERNAL.toString());
    }

    @Override
    protected String doPreFormat(String doc) {
        return "Dear employees,\n" + doc;
    }
}
