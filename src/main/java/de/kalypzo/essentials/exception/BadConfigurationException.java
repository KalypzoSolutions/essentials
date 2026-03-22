package de.kalypzo.essentials.exception;

/**
 * Thrown if a configured value leads to an exception
 */
public class BadConfigurationException extends Exception {
    public BadConfigurationException(String field, String configFile, Throwable cause) {
        super("The configured value for '" + field + "' is invalid in " + configFile, cause);
    }
}
