package org.example.data;

import java.io.Serializable;

public enum UnitOfMeasure implements Serializable {
    CENTIMETERS,
    LITERS,
    GRAMS;

    public static String getValues() {
        StringBuilder sb = new StringBuilder();
        for (UnitOfMeasure unit : values()) {
            sb.append(unit.name()).append(", ");
        }
        return sb.substring(0, sb.length() - 2);
    }
}