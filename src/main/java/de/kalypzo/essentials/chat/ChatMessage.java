package de.kalypzo.essentials.chat;

import de.kalypzo.essentials.EssentialsPlugin;
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
 * @param sender                UUID des Spielers, der die Nachricht gesendet hat (nullable für System-Nachrichten)
 * @param originatingServer     Name des Servers, von dem die Nachricht stammt (nullable für Abwärtskompatibilität)
 */
public record ChatMessage(@Nullable List<UUID> recipients, @NotNull String serializedMiniMessage,
                          @Nullable UUID sender, @Nullable String originatingServer) {

    public ChatMessage(@Nullable List<UUID> recipients, @NotNull String serializedMiniMessage) {
        this(recipients, serializedMiniMessage, null, null);
    }

    public ChatMessage(@Nullable List<UUID> recipients, @NotNull String serializedMiniMessage, @Nullable UUID sender) {
        this(recipients, serializedMiniMessage, sender, null);
    }

    private static final MiniMessage SERIALIZER = MiniMessage.miniMessage();

    public static ChatMessage create(@NotNull Component component) {
        return new ChatMessage(null, SERIALIZER.serialize(component), null, null);
    }

    public static ChatMessage create(@NotNull Component component, UUID sender) {
        return new ChatMessage(null, SERIALIZER.serialize(component), sender, null);
    }

    public static ChatMessage create(@NotNull Component component, UUID sender, String originatingServer) {
        return new ChatMessage(null, SERIALIZER.serialize(component), sender, originatingServer);
    }

    public static ChatMessage create(@NotNull Component component, @Nullable List<UUID> recipients) {
        return new ChatMessage(recipients, SERIALIZER.serialize(component), null, null);
    }

    public static ChatMessage create(@NotNull Component component, @Nullable List<UUID> recipients, UUID sender, String originatingServer) {
        return new ChatMessage(recipients, SERIALIZER.serialize(component), sender, originatingServer);
    }

    /**
     * Erstellt eine neue Instanz von GlobalMessage mit den gegebenen Empfängern.
     *
     * @param recipients Liste von UUIDs, die die Empfänger darstellen
     * @return eine neue Instanz von GlobalMessage mit den gegebenen Empfängern
     */
    @NotNull
    public ChatMessage recipients(List<UUID> recipients) {
        return new ChatMessage(recipients, serializedMiniMessage, sender, originatingServer);
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

    /**
     * Delivers the message using the default ChatSystem instance.
     */
    public void deliver() {
        deliver(EssentialsPlugin.instance().getChatSystem());
    }
}
