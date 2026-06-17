package de.kalypzo.essentials.chat;

import io.lettuce.core.api.sync.RedisCommands;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TeamChatService {

    private final RedisCommands<String, String> redis;

    private final Map<UUID, Boolean> toggled = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> silent = new ConcurrentHashMap<>();

    public TeamChatService(RedisCommands<String, String> redis) {
        this.redis = redis;
    }

    public boolean isToggled(UUID uuid) {
        return toggled.getOrDefault(uuid, false);
    }

    public void setToggled(UUID uuid, boolean state) {
        toggled.put(uuid, state);

        if (state) {
            redis.set("teamchat:toggle:" + uuid, "true");
        } else {
            redis.del("teamchat:toggle:" + uuid);
        }
    }

    public boolean isSilent(UUID uuid) {
        return silent.getOrDefault(uuid, false);
    }

    public void setSilent(UUID uuid, boolean state) {
        silent.put(uuid, state);

        if (state) {
            redis.set("teamchat:silent:" + uuid, "true");
        } else {
            redis.del("teamchat:silent:" + uuid);
        }
    }

    public void loadPlayer(UUID uuid) {
        toggled.put(uuid,
                Boolean.parseBoolean(redis.get("teamchat:toggle:" + uuid)));

        silent.put(uuid,
                Boolean.parseBoolean(redis.get("teamchat:silent:" + uuid)));
    }

    public void unloadPlayer(UUID uuid) {
        toggled.remove(uuid);
        silent.remove(uuid);
    }
}