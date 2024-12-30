package com.example.stamboot.letterformatting.format;

import com.mgnt.lifecycle.management.BaseEntity;

public abstract class BaseLetterFormatter extends BaseEntity<BaseLetterFormatter> implements LetterFormatter {

    private static final String FACTORY_TYPE = BaseLetterFormatter.class.getSimpleName();
    static {
        init(FACTORY_TYPE, LetterFormatterFactory.getFactoryInstance());
    }

    public BaseLetterFormatter() {
        super(FACTORY_TYPE);
    }

    public BaseLetterFormatter(String customName) {
        super(FACTORY_TYPE, customName);
    }
}
