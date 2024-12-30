package com.example.stamboot.letterformatting.preformat;

import com.mgnt.lifecycle.management.BaseEntityFactory;
import java.util.Collection;

public class LetterPreFormatterFactory extends BaseEntityFactory<LetterPreFormatter> {
    private static LetterPreFormatterFactory FACTORY = new LetterPreFormatterFactory();

    private LetterPreFormatterFactory() {
    }

    /*
     * This method is not for external use, It is used by the abstract parent class to get the factory to register
     * it in the infrastructure
     */
    public static LetterPreFormatterFactory getFactoryInstance() {
        return FACTORY;
    }

    /*
     * This is the method to access concrete implementation by its name from the factory
     */
    public static LetterPreFormatter getInstance(String key) {
        return FACTORY.getEntity(key);
    }

    /*
     * This is convenience method that allows you to get all the available implementations from this factory
     */
    public static Collection<LetterPreFormatter> getAllInstances() {
        return FACTORY.getAllEntities();
    }
}
