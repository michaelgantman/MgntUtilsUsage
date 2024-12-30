package com.example.stamboot.letterformatting.format.implementations;

import com.example.stamboot.letterformatting.DocumentType;
import com.example.stamboot.letterformatting.format.BaseLetterFormatter;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy(false)
public class FinancialDocumentFormatter extends BaseLetterFormatter {

    public FinancialDocumentFormatter() {
        super((DocumentType.FINANCE.toString()));
    }

    @Override
    public String formatDocument(String doc) {
        return doc + "\n\nCFO and Financial Department";
    }
}
