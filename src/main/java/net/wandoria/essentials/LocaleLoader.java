package net.wandoria.essentials;

import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslationStore;
import net.kyori.adventure.translation.GlobalTranslator;
import net.wandoria.essentials.util.Text;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * 1. Stores the locale files from the resources into the plugin directory if they not exist.
 * 2. Loads the locale files from the plugins directory and registers them in the {@link GlobalTranslator}.
 */
public class LocaleLoader {
    private final JavaPlugin plugin;

    /**
     * Creates a new LocaleLoader.
     *
     * @param plugin            the plugin instance, needed for the NamespacedKey
     * @param pluginClassLoader because getClassLoader is protected in JavaPlugin. Please pass plugin.getClassLoader() here.
     */
    public LocaleLoader(JavaPlugin plugin, ClassLoader pluginClassLoader) {
        this.plugin = plugin;
    }

    public void loadLocales() {
        // 1. Create lang directory in plugin's data folder
        File langDir = new File(plugin.getDataFolder(), "lang");
        if (!langDir.exists()) {
            if (langDir.mkdirs()) {
                plugin.getSLF4JLogger().info("Created lang directory at {}", langDir.getAbsolutePath());
            }
        }

        // 2. Save default locale files from resources to plugin directory
        saveDefaultLocaleFiles(langDir);

        // 3. Load locale files from the plugin directory
        MiniMessageTranslationStore translationStore = MiniMessageTranslationStore.create(
                new NamespacedKey(plugin, "messages"),
                Text.MINI_MESSAGE
        );

        // 4. Discover and load all locale files dynamically
        File[] localeFiles = langDir.listFiles((dir, name) ->
                name.startsWith("messages_") && name.endsWith(".properties")
        );

        if (localeFiles != null && localeFiles.length > 0) {
            plugin.getSLF4JLogger().info("Loading {} locale file(s) from {}", localeFiles.length, langDir.getAbsolutePath());
            for (File file : localeFiles) {
                try {
                    Locale locale = parseLocaleFromFilename(file.getName());
                    ResourceBundle bundle = new PropertyResourceBundle(new FileInputStream(file));
                    translationStore.registerAll(locale, bundle, false);
                    plugin.getSLF4JLogger().info("Loaded locale file: {} ({})", file.getName(), locale);
                } catch (IOException e) {
                    plugin.getSLF4JLogger().warn("Failed to load locale file: {}", file.getName(), e);
                } catch (IllegalArgumentException e) {
                    plugin.getSLF4JLogger().warn("Invalid locale filename format: {}", file.getName(), e);
                }
            }
        } else {
            plugin.getSLF4JLogger().warn("No locale files found in {}", langDir.getAbsolutePath());
        }

        // 5. Set default locale and register with GlobalTranslator
        translationStore.defaultLocale(Locale.GERMANY);
        GlobalTranslator.translator().addSource(translationStore);
    }

    /**
     * Saves default locale files from the plugin's resources to the plugin directory.
     * Only saves if the files don't already exist (preserves user customizations).
     *
     * @param langDir the lang directory in the plugin's data folder
     */
    private void saveDefaultLocaleFiles(File langDir) {
        String[] defaultLocaleFiles = {
                "lang/messages_de_DE.properties",
                "lang/messages_en_US.properties"
        };

        for (String resourcePath : defaultLocaleFiles) {
            String fileName = resourcePath.substring(resourcePath.lastIndexOf('/') + 1);
            File targetFile = new File(langDir, fileName);

            if (!targetFile.exists()) {
                try {
                    plugin.saveResource(resourcePath, false);
                    plugin.getSLF4JLogger().info("Saved default locale file: {}", fileName);
                } catch (IllegalArgumentException e) {
                    plugin.getSLF4JLogger().warn("Could not find resource: {}", resourcePath, e);
                }
            }
        }
    }

    /**
     * Parses a Locale from a filename in the format "messages_&lt;locale&gt;.properties".
     * Examples:
     * - "messages_de_DE.properties" → Locale.GERMANY
     * - "messages_en_US.properties" → Locale.US
     * - "messages_fr_FR.properties" → Locale.FRANCE
     *
     * @param filename the locale file name
     * @return the parsed Locale
     * @throws IllegalArgumentException if the filename format is invalid
     */
    private Locale parseLocaleFromFilename(String filename) throws IllegalArgumentException {
        if (!filename.startsWith("messages_") || !filename.endsWith(".properties")) {
            throw new IllegalArgumentException("Invalid locale filename format: " + filename);
        }

        // Extract locale string: "messages_de_DE.properties" → "de_DE"
        String localeString = filename.substring(9, filename.length() - 11);

        // Split by underscore to get language and country
        String[] parts = localeString.split("_");
        if (parts.length < 1) {
            throw new IllegalArgumentException("Invalid locale format in filename: " + filename);
        }

        String language = parts[0].toLowerCase();
        String country = parts.length > 1 ? parts[1].toUpperCase() : "";
        String variant = parts.length > 2 ? parts[2] : "";

        Locale.Builder builder = new Locale.Builder();
        builder.setLanguage(language);

        if (!country.isEmpty()) {
            builder.setRegion(country);
        }

        if (!variant.isEmpty()) {
            builder.setVariant(variant);
        }

        return builder.build();
    }
}
