package net.wandoria.essentials.environment.name;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;

import java.lang.reflect.InvocationTargetException;

@Slf4j
@Getter
public class DefaultServerNameProvider implements ServerNameProvider {
    private final String serverName;

    public DefaultServerNameProvider() {
        String name = "unknown-server";
        try {
            var method = Bukkit.getServer().getClass().getDeclaredMethod("getServerName");
            name = (String) method.invoke(Bukkit.getServer());
        } catch (NoSuchMethodException e) {
            log.warn("This server version does not support getServerName() in Server-API. Consider using a PURPUR-fork");
        } catch (InvocationTargetException | IllegalAccessException e) {
            log.error("Could not invoke server name from Bukkit");
        }
        log.info("Detected server name: {}", name);
        this.serverName = name;
    }

}
