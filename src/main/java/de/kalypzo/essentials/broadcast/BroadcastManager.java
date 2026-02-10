package de.kalypzo.essentials.broadcast;

import de.kalypzo.essentials.chat.ChatMessage;
import de.kalypzo.essentials.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
public class BroadcastManager {
    private final JavaPlugin plugin;
    private final List<BukkitTask> tasks = new ArrayList<>();

    public BroadcastManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        stop();
        scheduleFromConfig();
    }

    public void reload() {
        start();
    }

    public void stop() {
        for (BukkitTask task : tasks) {
            task.cancel();
        }
        tasks.clear();
    }

    public void broadcast(String message) {
        if (message == null || message.isBlank()) {
            return;
        }
        for (String line : message.split("\\R")) {
            if (!line.isBlank()) {
                Bukkit.broadcast(Text.deserialize(line));
            }
        }
    }

    public void broadcastNetwork(String message) {
        if (message == null || message.isBlank()) {
            return;
        }
        for (String line : message.split("\\R")) {
            if (!line.isBlank()) {
                ChatMessage.create(Text.deserialize(line)).deliver();
            }
        }
    }

    private void scheduleFromConfig() {
        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("broadcast.enabled", false)) {
            return;
        }
        scheduleFromBroadcasts(config);
    }

    private boolean scheduleFromBroadcasts(FileConfiguration config) {
        var section = config.getConfigurationSection("broadcast.broadcasts");
        if (section == null) {
            return false;
        }
        boolean scheduled = false;
        for (String key : section.getKeys(false)) {
            var broadcastSection = section.getConfigurationSection(key);
            if (broadcastSection == null) {
                continue;
            }
            if (!broadcastSection.getBoolean("enabled", true)) {
                continue;
            }
            long intervalSeconds = broadcastSection.getLong("interval-seconds", 0L);
            Object messagesValue = broadcastSection.get("messages");
            Object messageValue = broadcastSection.get("message");
            List<String> messages = parseMessages(messagesValue, messageValue);
            scheduled |= scheduleMessages(messages, intervalSeconds);
        }
        return scheduled;
    }

    private boolean scheduleMessages(List<String> messages, long intervalSeconds) {
        if (messages == null || messages.isEmpty()) {
            return false;
        }
        if (intervalSeconds <= 0) {
            return false;
        }
        long intervalTicks = intervalSeconds * 20L;
        BroadcastSchedule schedule = new BroadcastSchedule(messages);
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, schedule::broadcastNext, intervalTicks, intervalTicks);
        tasks.add(task);
        return true;
    }

    private List<String> parseMessages(Object messagesValue, Object messageValue) {
        if (messagesValue instanceof List<?> list) {
            List<String> messages = new ArrayList<>();
            for (Object value : list) {
                if (value != null) {
                    messages.add(value.toString());
                }
            }
            return messages;
        }
        if (messageValue != null) {
            return Collections.singletonList(messageValue.toString());
        }
        return Collections.emptyList();
    }

    private class BroadcastSchedule {
        private final List<String> messages;
        private int nextIndex;

        private BroadcastSchedule(List<String> messages) {
            this.messages = messages;
        }

        private void broadcastNext() {
            if (messages.isEmpty()) {
                return;
            }
            if (nextIndex >= messages.size()) {
                nextIndex = 0;
            }
            String message = messages.get(nextIndex++);
            broadcast(message);
        }
    }
}
