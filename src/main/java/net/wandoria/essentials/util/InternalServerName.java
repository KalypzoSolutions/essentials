package net.wandoria.essentials.util;

import org.bukkit.Bukkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;


/**
 * Detects the server name most likely used in velocity.
 * Priorities:
 * <ol>
 *     <li>INTERNAL_SERVER_NAME environment variable</li>
 *     <li>getServerName() method</li>
 * </ol>
 *
 * @author Johannes / EinJOJO
 * @version 2
 */
public class InternalServerName {
    private static final Logger log = LoggerFactory.getLogger(InternalServerName.class);
    private final String serverName;

    private InternalServerName() {
        String name = (System.getenv("INTERNAL_SERVER_NAME"));
        if (name == null) {
            name = "unknown";
            try {
                var method = Bukkit.getServer().getClass().getDeclaredMethod("getServerName");
                name = (String) method.invoke(Bukkit.getServer());
            } catch (NoSuchMethodException e) {
                log.warn("This server version does not support getServerName() in Server-API. Consider using a PURPUR-fork");
            } catch (InvocationTargetException | IllegalAccessException e) {
                log.error("Could not invoke server name from Bukkit");
            }
        }

        log.info("Detected server name: {}", name);
        this.serverName = name;
    }

    private static class LazyHolder {
        private static final InternalServerName INSTANCE = new InternalServerName();
    }

    public static String get() {
        return LazyHolder.INSTANCE.serverName;
    }

    @Override
    public String toString() {
        return serverName;
    }
}