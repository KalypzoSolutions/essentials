package de.kalypzo.essentials.user;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeEqualityPredicate;
import net.luckperms.api.util.Tristate;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;


/**
 * Use {@link #of(UUID)} to create a new instance.
 */
public class UserSettings {
    private final UUID uuid;

    private UserSettings(UUID uuid) {
        this.uuid = uuid;
    }

    public static UserSettings of(UUID uuid) {
        return new UserSettings(uuid);
    }

    public boolean disabledPingSound() {
        return getLuckPermsUser().data().contains(Settings.DISABLED_PING_SOUND, NodeEqualityPredicate.IGNORE_EXPIRY_TIME).equals(Tristate.FALSE);
    }

    public CompletableFuture<Void> disabledPingSound(boolean isDisabled) {
        return Settings.LUCKPERMS.getUserManager().modifyUser(uuid, (user) -> {
            if (isDisabled) user.data().add(Settings.DISABLED_PING_SOUND);
            else user.data().remove(Settings.DISABLED_PING_SOUND);
        });

    }

    public User getLuckPermsUser() {
        return Settings.LUCKPERMS.getUserManager().getUser(uuid);
    }

    /**
     * Lazy class
     */
    private static class Settings {
        private static final LuckPerms LUCKPERMS = LuckPermsProvider.get();
        private static final Node DISABLED_PING_SOUND = Node.builder("essentials.settings.pings").value(false).build();
    }

}
