package de.kalypzo.essentials.chat;

import de.kalypzo.essentials.EssentialsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Datenklasse für eine serverübergreifende Nachricht.
 * Es empfiehlt sich die Verwendung von {@link ChatMessage#create(Component)} oder {@link ChatMessage#create(Component, List)} um eine Instanz zu erstellen.
 *
 * @param recipients        null, wenn alle Spieler erreicht werden sollen oder eine Liste von UUIDs, die die Empfänger darstellen
 * @param serializedMessage der Inhalt der Nachricht als serialisierter  String
 * @param sender            UUID des Spielers, der die Nachricht gesendet hat (nullable für System-Nachrichten)
 * @param originatingServer Name des Servers, von dem die Nachricht stammt (nullable für Abwärtskompatibilität)
 * @param permissionScope   Optionaler String, der die Berechtigungsgruppe angibt, die die Nachricht empfangen soll (nullable für keine Einschränkung)
 */
public record ChatMessage(@Nullable List<UUID> recipients, @NotNull String serializedMessage,
                          @Nullable UUID sender, @Nullable String originatingServer, @Nullable String permissionScope) {


    public ChatMessage(@Nullable List<UUID> recipients, @NotNull String serialized) {
        this(recipients, serialized, null, null, null);
    }

    public ChatMessage(@Nullable List<UUID> recipients, @NotNull String serialized, @Nullable UUID sender) {
        this(recipients, serialized, sender, null, null);
    }

    private static final ComponentSerializer<Component, Component, String> SERIALIZER = GsonComponentSerializer.gson();

    public static ChatMessage create(@NotNull Component component) {
        return new ChatMessage(null, SERIALIZER.serialize(component), null, null, null);
    }

    public static ChatMessage create(@NotNull Component component, UUID sender) {
        return new ChatMessage(null, SERIALIZER.serialize(component), sender, null, null);
    }

    public static ChatMessage create(@NotNull Component component, UUID sender, String originatingServer) {
        return new ChatMessage(null, SERIALIZER.serialize(component), sender, originatingServer, null);
    }

    public static ChatMessage create(@NotNull Component component, @Nullable List<UUID> recipients) {
        return new ChatMessage(recipients, SERIALIZER.serialize(component), null, null, null);
    }

    public static ChatMessage create(@NotNull Component component, @Nullable List<UUID> recipients, UUID sender, String originatingServer) {
        return new ChatMessage(recipients, SERIALIZER.serialize(component), sender, originatingServer, null);
    }

    public static ChatMessage createPermissionScoped(@NotNull Component component, String permissionScope) {
        return new ChatMessage(null, SERIALIZER.serialize(component), null, null, permissionScope);
    }

    /**
     * Erstellt eine neue Instanz von GlobalMessage mit den gegebenen Empfängern.
     *
     * @param recipients Liste von UUIDs, die die Empfänger darstellen
     * @return eine neue Instanz von GlobalMessage mit den gegebenen Empfängern
     */
    @NotNull
    public ChatMessage recipients(List<UUID> recipients) {
        return new ChatMessage(recipients, serializedMessage, sender, originatingServer, permissionScope);
    }


    /**
     * @return der Inhalt der Nachricht als Adventure-Component
     */
    @NotNull
    public Component getContent() {
        return SERIALIZER.deserialize(serializedMessage);
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
