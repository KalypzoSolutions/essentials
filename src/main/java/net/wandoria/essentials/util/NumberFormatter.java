package net.wandoria.essentials.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class NumberFormatter {
    public static final DecimalFormatSymbols SYMBOLS = new DecimalFormatSymbols(Locale.GERMAN);
    public static final DecimalFormat FORMATTER = new DecimalFormat("###,###", SYMBOLS);

    /**
     * Formats a double into a human-readable string.
     * Decimal values are truncated (not rounded).
     * Uses a dot as a thousands separator.
     * For example, 100.0 becomes "100", and 123456.78 becomes "123.456".
     *
     * @param number The double to format.
     * @return A human-readable string representation of the integer part of the number.
     */
    public static String doubleToHumanReadable(double number) {
        long longValue = (long) number;
        return FORMATTER.format(longValue);
    }

}