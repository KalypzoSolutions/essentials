package net.wandoria.essentials.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Datenklasse für eine serverübergreifende Nachricht.
 * Es empfiehlt sich die Verwendung von {@link ChatMessage#create(Component)} oder {@link ChatMessage#create(Component, List)} um eine Instanz zu erstellen.
 *
 * @param recipients            null, wenn alle Spieler erreicht werden sollen oder eine Liste von UUIDs, die die Empfänger darstellen
 * @param serializedMiniMessage der Inhalt der Nachricht als serialisierter MiniMessage String
 */
public record ChatMessage(@Nullable List<UUID> recipients, @NotNull String serializedMiniMessage) {

    private static final MiniMessage SERIALIZER = MiniMessage.miniMessage();

    public static ChatMessage create(@NotNull Component component) {
        return new ChatMessage(null, SERIALIZER.serialize(component));
    }

    public static ChatMessage create(@NotNull Component component, @Nullable List<UUID> recipients) {
        return new ChatMessage(recipients, SERIALIZER.serialize(component));
    }

    /**
     * Erstellt eine neue Instanz von GlobalMessage mit den gegebenen Empfängern.
     *
     * @param recipients Liste von UUIDs, die die Empfänger darstellen
     * @return eine neue Instanz von GlobalMessage mit den gegebenen Empfängern
     */
    @NotNull
    public ChatMessage recipients(List<UUID> recipients) {
        return new ChatMessage(recipients, serializedMiniMessage);
    }

    /**
     * @return der Inhalt der Nachricht als Adventure-Component
     */
    @NotNull
    public Component getContent() {
        return SERIALIZER.deserialize(serializedMiniMessage);
    }

    /**
     * Versende die Nachricht serverübergreifend.
     *
     * @param system das ChatSystem, das die Nachricht versenden soll.
     */
    public void deliver(ChatSystem system) {
        system.publishNetworkChatMessage(this);
    }
}
