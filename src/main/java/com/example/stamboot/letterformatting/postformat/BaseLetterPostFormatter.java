package com.example.stamboot.letterformatting.postformat;

import com.mgnt.lifecycle.management.BaseEntity;

public abstract class BaseLetterPostFormatter extends BaseEntity<BaseLetterPostFormatter> implements LetterPostFormatter {

    private static final String FACTORY_TYPE = BaseLetterPostFormatter.class.getSimpleName();
    static {
        init(FACTORY_TYPE, LetterPostFormatterFactory.getFactoryInstance());
    }

    public BaseLetterPostFormatter() {
        super(FACTORY_TYPE);
    }

    public BaseLetterPostFormatter(String customName) {
        super(FACTORY_TYPE, customName);
    }
}
