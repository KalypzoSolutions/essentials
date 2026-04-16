package de.kalypzo.essentials;

import de.kalypzo.essentials.util.Text;
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslationStore;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 1. Stores the locale files from the resources into the plugin directory if they not exist.
 * 2. Syncs missing keys from bundled resources into existing on-disk locale files.
 * 3. Loads the locale files from the plugins directory and registers them in the {@link GlobalTranslator}.
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

        // 2. Save default locale files from resources to plugin directory (and sync missing keys)
        saveDefaultLocaleFiles(langDir);

        // 3. Load locale files from the plugin directory
        MiniMessageTranslationStore translationStore = MiniMessageTranslationStore.create(
                new NamespacedKey(plugin, "messages"),
                Text.getMiniMessage()
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
     * If a file already exists, missing keys from the bundled resource are appended to it.
     *
     * @param langDir the lang directory in the plugin's data folder
     */
    private void saveDefaultLocaleFiles(File langDir) {
        String[] defaultLocaleFiles = {
                "lang/messages_de_DE.properties",
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
            } else {
                syncMissingKeys(resourcePath, targetFile);
            }
        }
    }

    /**
     * Reads the bundled resource properties and appends any keys that are absent in the
     * on-disk file, preserving all existing user-customized values.
     *
     * @param resourcePath path inside the jar (e.g. {@code "lang/messages_de_DE.properties"})
     * @param targetFile   the existing on-disk file to sync into
     */
    private void syncMissingKeys(String resourcePath, File targetFile) {
        InputStream resourceStream = plugin.getResource(resourcePath);
        if (resourceStream == null) {
            plugin.getSLF4JLogger().warn("Bundled resource not found, cannot sync: {}", resourcePath);
            return;
        }

        // Load bundled keys in order (Properties loses order, so we parse manually)
        List<String> bundledLines;
        Properties bundledProps = new Properties();
        try (InputStreamReader reader = new InputStreamReader(resourceStream, StandardCharsets.UTF_8)) {
            bundledLines = readLines(reader);
            bundledProps.load(new StringReader(String.join("\n", bundledLines)));
        } catch (IOException e) {
            plugin.getSLF4JLogger().warn("Could not read bundled resource: {}", resourcePath, e);
            return;
        }

        // Load existing on-disk keys
        Properties existingProps = new Properties();
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(targetFile), StandardCharsets.UTF_8)) {
            existingProps.load(reader);
        } catch (IOException e) {
            plugin.getSLF4JLogger().warn("Could not read locale file for sync: {}", targetFile.getName(), e);
            return;
        }

        // Collect missing keys (maintaining bundled order)
        List<String> missingLines = new ArrayList<>();
        for (String line : bundledLines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            int eqIdx = trimmed.indexOf('=');
            if (eqIdx < 0) continue;
            String key = trimmed.substring(0, eqIdx).trim();
            if (!existingProps.containsKey(key)) {
                missingLines.add(line);
            }
        }

        if (missingLines.isEmpty()) {
            return;
        }

        // Append missing keys to the on-disk file
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(targetFile, true), StandardCharsets.UTF_8))) {
            writer.newLine();
            writer.write("# --- Keys added automatically by plugin update ---");
            writer.newLine();
            for (String line : missingLines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            plugin.getSLF4JLogger().warn("Could not write missing keys to: {}", targetFile.getName(), e);
            return;
        }

        plugin.getSLF4JLogger().info("Synced {} missing translation key(s) into {}", missingLines.size(), targetFile.getName());
    }

    /**
     * Reads all lines from a Reader into a list, preserving order.
     */
    private List<String> readLines(Reader reader) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(reader)) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
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
