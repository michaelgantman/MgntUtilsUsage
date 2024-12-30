package com.example.stamboot.letterformatting.postformat.implementations;

import com.example.stamboot.letterformatting.DocumentType;
import com.example.stamboot.letterformatting.postformat.BaseLetterPostFormatter;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Lazy(false)
public class CustomerLetterPostFormatter extends BaseLetterPostFormatter {

    public CustomerLetterPostFormatter() {
        super(DocumentType.CUSTOMER.toString());
    }

    @Override
    public String postFormatDocument(String doc) {
        return LocalDate.now().toString() + " Customer communication\n\n" + doc;
    }
}