package com.justin.eagle.bank.matcher;

import java.util.stream.Stream;

import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.CustomComparator;

public class TimestampComparator extends CustomComparator {

    private TimestampComparator(JSONCompareMode mode, Customization... customizations) {
        super(mode, customizations);
    }

    public static TimestampComparator forPaths(String... fieldPath) {
        final Customization[] customizations = Stream.of(fieldPath)
                .map(JsonTimestampCustomization::forPath)
                .toList().toArray(new Customization[]{});
        return new TimestampComparator(JSONCompareMode.LENIENT, customizations);
    }
}
