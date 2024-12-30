package com.example.stamboot.letterformatting.preformat.implementations;

import com.example.stamboot.letterformatting.DocumentType;
import com.example.stamboot.letterformatting.preformat.BaseLetterPreFormatter;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy(false)
public class CustomerLetterPreFormatter extends BaseLetterPreFormatter {

    public CustomerLetterPreFormatter() {
        super(DocumentType.CUSTOMER.toString());
    }

    @Override
    protected String doPreFormat(String doc) {
        return "Dear customer,\n" + doc;
    }
}
