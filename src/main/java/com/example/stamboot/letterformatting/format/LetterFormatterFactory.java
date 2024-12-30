package com.example.stamboot.letterformatting.format;

import com.mgnt.lifecycle.management.BaseEntityFactory;

import java.util.Collection;

public class LetterFormatterFactory extends BaseEntityFactory<LetterFormatter> {
    private static LetterFormatterFactory FACTORY = new LetterFormatterFactory();

    private LetterFormatterFactory() {
    }

    /*
     * This method is not for external use, It is used by the abstract parent class to get the factory to register
     * it in the infrastructure
     */
    public static LetterFormatterFactory getFactoryInstance() {
        return FACTORY;
    }

    /*
     * This is the method to access concrete implementation by its name from the factory
     */
    public static LetterFormatter getInstance(String key) {
        return FACTORY.getEntity(key);
    }

    /*
     * This is convenience method that allows you to get all the available implementations from this factory
     */
    public static Collection<LetterFormatter> getAllInstances() {
        return FACTORY.getAllEntities();
    }
}
