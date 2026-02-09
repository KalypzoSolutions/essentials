package de.kalypzo.essentials.exception;

import it.einjojo.economy.TransactionStatus;
import lombok.Getter;
import net.kyori.adventure.text.Component;

/**
 * This exception gets thrown when a transaction fails.
 */
@Getter
public class TransactionException extends RuntimeException {
    private final TransactionStatus status;

    public TransactionException(TransactionStatus status) {
        super(status.name());
        this.status = status;
    }


    public Component createUserErrorMessage() {
        return Component.text("Transaction failed: " + status.name());
    }
}
