package com.example.stamboot.letterformatting.preformat.implementations;

import com.example.stamboot.letterformatting.DocumentType;
import com.example.stamboot.letterformatting.preformat.BaseLetterPreFormatter;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy(false)
public class FinancialDocumentPreformatter extends BaseLetterPreFormatter{

    public FinancialDocumentPreformatter() {
        super(DocumentType.FINANCE.toString());
    }

    @Override
    protected String doPreFormat(String doc) {
        return "Dear Investors and Shareholders,\n" + doc;
    }
}
