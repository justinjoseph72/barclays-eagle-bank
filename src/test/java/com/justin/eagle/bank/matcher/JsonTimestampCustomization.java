package com.justin.eagle.bank.matcher;

import org.skyscreamer.jsonassert.Customization;

public class JsonTimestampCustomization {

    public static Customization forPath(String fieldPath) {
        return new Customization(fieldPath, new TimestampValueMatcher());
    }

}
