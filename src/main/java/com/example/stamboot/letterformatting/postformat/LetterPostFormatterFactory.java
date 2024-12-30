package com.example.stamboot.letterformatting.postformat;

import com.mgnt.lifecycle.management.BaseEntityFactory;

import java.util.Collection;

public class LetterPostFormatterFactory extends BaseEntityFactory<LetterPostFormatter> {
    private static LetterPostFormatterFactory FACTORY = new LetterPostFormatterFactory();

    private LetterPostFormatterFactory() {
    }

    /*
     * This method is not for external use, It is used by the abstract parent class to get the factory to register
     * it in the infrastructure
     */
    public static LetterPostFormatterFactory getFactoryInstance() {
        return FACTORY;
    }

    /*
     * This is the method to access concrete implementation by its name from the factory
     */
    public static LetterPostFormatter getInstance(String key) {
        return FACTORY.getEntity(key);
    }

    /*
     * This is convenience method that allows you to get all the available implementations from this factory
     */
    public static Collection<LetterPostFormatter> getAllInstances() {
        return FACTORY.getAllEntities();
    }
}
