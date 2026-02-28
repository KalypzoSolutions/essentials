package de.kalypzo.essentials.chat;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * <p>Immutable set of possible name aliases for a player, used for chat mentions or player input features.</p>
 * <p>Thread-safe and memory-efficient through immutability and precompiled regex patterns.</p>
 *
 * <h2>Alias Generation Rules (case-insensitive, high-performance, memory-optimized)</h2>
 * <ol>
 *   <li><strong>Exact Name (lowercase):</strong> Full username converted to lowercase. Example: {@code Ein_Jojo → einjojo}</li>
 *   <li><strong>No Underscores:</strong> Remove all underscores and convert to lowercase. Example: {@code Til_Boy → tilboy}</li>
 *   <li><strong>Consolidated Duplicates:</strong> Reduce consecutive duplicate characters (3+ occurrences) to 2. Example: {@code SHEEEEEEPMAN → sheepman}</li>
 *   <li><strong>Without Numbers:</strong> Strip trailing digits and convert to lowercase (if resulting length >= 3). Example: {@code Fireking1410 → fireking}</li>
 *   <li><strong>Word Initials:</strong> Extract first letter of each CamelCase word (if name has no underscores and length > 4). Example: {@code BigMelonMasters → bmm}</li>
 * </ol>
 *
 * <h2>Unsuitable Names (not aliased)</h2>
 * <ul>
 *   <li>Names that are too short (&lt;3 characters)</li>
 *   <li>Names that become too short after transformations</li>
 *   <li>Duplicate aliases (only first unique variant is kept)</li>
 * </ul>
 */
public final class PlayerNameAliasSet {

    private static final int MIN_ALIAS_LENGTH = 3;

    private final Set<String> aliases;  // Immutable
    private final Pattern mentionPattern;  // Precompiled regex for efficient matching

    public PlayerNameAliasSet(@NotNull Player player) {
        this(player.getName());
    }

    /**
     * Constructor for testing or direct string input.
     * Creates an immutable alias set with a precompiled mention pattern.
     *
     * @param playerName the player's username
     */
    public PlayerNameAliasSet(@NotNull String playerName) {
        Set<String> computed = new LinkedHashSet<>();

        if (playerName.trim().isEmpty()) {
            this.aliases = Collections.emptySet();
            this.mentionPattern = null;
            return;
        }

        // Rule 1: Exact name (lowercase)
        String exact = playerName.toLowerCase();
        computed.add(exact);

        // Rule 2: No underscores
        if (playerName.contains("_")) {
            String noUnderscores = playerName.replace("_", "").toLowerCase();
            addIfValid(computed, noUnderscores);

            // Rule 4: Without numbers (applied after underscores removed)
            String withoutNumbers = stripTrailingNumbers(noUnderscores);
            addIfValid(computed, withoutNumbers);
        }

        // Rule 3: Consolidated duplicates
        String consolidated = consolidateDuplicates(exact);
        addIfValid(computed, consolidated);

        // Rule 4: Without numbers (on exact name)
        String withoutNumbers = stripTrailingNumbers(exact);
        addIfValid(computed, withoutNumbers);

        // Rule 5: Word initials (only for camelCase, multi-word names)
        if (!playerName.contains("_") && playerName.length() > 4 && hasMultipleWords(playerName)) {
            String initials = extractWordInitials(playerName);
            addIfValid(computed, initials);
        }

        // Store as immutable set
        this.aliases = Collections.unmodifiableSet(computed);

        // Precompile regex pattern for efficient mention matching: @(alias1|alias2|...)
        this.mentionPattern = compileMentionPattern(aliases);
    }

    /**
     * Compiles a regex pattern for efficient mention detection.
     * Pattern: @(alias1|alias2|alias3)
     */
    private Pattern compileMentionPattern(Set<String> aliasSet) {
        if (aliasSet.isEmpty()) {
            return null;
        }

        String pattern = "@(" +
            aliasSet.stream()
                .map(Pattern::quote)
                .collect(Collectors.joining("|"))
            + ")";

        return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
    }

    /**
     * Checks if any of this player's aliases are mentioned in the message.
     * Uses precompiled regex for efficiency.
     *
     * @param message the message to check
     * @return true if any alias is mentioned with '@' prefix
     */
    public boolean isMentioned(@NotNull String message) {
        if (mentionPattern == null || message.isEmpty()) {
            return false;
        }
        return mentionPattern.matcher(message).find();
    }

    /**
     * Finds the first mentioned alias in a message.
     *
     * @param message the message to search in
     * @return the matched alias (without @), or null if not found
     */
    public String getFirstMention(@NotNull String message) {
        if (mentionPattern == null || message.isEmpty()) {
            return null;
        }

        var matcher = mentionPattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1);  // Return matched alias without @
        }
        return null;
    }

    /**
     * Returns an iterator over the immutable alias set.
     * Aliases can be iterated but not modified.
     */
    public Iterator<String> iterator() {
        return aliases.iterator();
    }

    /**
     * Checks if a specific alias is in this set.
     */
    public boolean contains(String alias) {
        return aliases.contains(alias);
    }

    /**
     * Returns the number of aliases in this set.
     */
    public int size() {
        return aliases.size();
    }

    /**
     * Returns true if this set has no aliases.
     */
    public boolean isEmpty() {
        return aliases.isEmpty();
    }

    /**
     * Adds an alias if it meets the validity criteria.
     * Ensures no duplicates and maintains minimum length requirements.
     */
    private void addIfValid(Set<String> aliasSet, String alias) {
        if (alias != null && alias.length() >= MIN_ALIAS_LENGTH && !aliasSet.contains(alias)) {
            aliasSet.add(alias);
        }
    }
    private String consolidateDuplicates(String name) {
        if (name == null || name.length() < 3) {
            return name;
        }

        StringBuilder result = new StringBuilder();
        int consecutiveCount = 1;
        char previousChar = name.charAt(0);
        result.append(previousChar);

        for (int i = 1; i < name.length(); i++) {
            char currentChar = name.charAt(i);
            if (currentChar == previousChar) {
                consecutiveCount++;
                // Keep only up to 2 consecutive duplicates
                if (consecutiveCount <= 2) {
                    result.append(currentChar);
                }
            } else {
                result.append(currentChar);
                previousChar = currentChar;
                consecutiveCount = 1;
            }
        }

        return result.toString();
    }

    /**
     * Strips trailing digits from a name if the resulting name has at least MIN_ALIAS_LENGTH characters.
     * Example: "fireking1410" → "fireking"
     */
    private String stripTrailingNumbers(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }

        int endIndex = name.length() - 1;
        while (endIndex >= 0 && Character.isDigit(name.charAt(endIndex))) {
            endIndex--;
        }

        String stripped = name.substring(0, endIndex + 1);
        return stripped.length() >= MIN_ALIAS_LENGTH ? stripped : null;
    }

    /**
     * Checks if a name has multiple words (CamelCase or underscore-separated).
     * Examples: "BigMelonMasters" (CamelCase), "Mr_Schneemann" (underscore)
     */
    private boolean hasMultipleWords(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }

        // Check for CamelCase (at least one uppercase letter after lowercase)
        boolean hasCamelCase = false;
        boolean hadLowercase = false;

        for (char c : name.toCharArray()) {
            if (Character.isLowerCase(c)) {
                hadLowercase = true;
            } else if (Character.isUpperCase(c) && hadLowercase) {
                hasCamelCase = true;
                break;
            }
        }

        return hasCamelCase || name.contains("_");
    }

    /**
     * Extracts the first letter of each word (CamelCase words).
     * Example: "BigMelonMasters" → "bmm"
     */
    private String extractWordInitials(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }

        StringBuilder initials = new StringBuilder();

        // Handle underscores as word separators first
        if (name.contains("_")) {
            String[] parts = name.split("_");
            for (String part : parts) {
                if (!part.isEmpty()) {
                    initials.append(Character.toLowerCase(part.charAt(0)));
                }
            }
        } else {
            // Handle CamelCase
            initials.append(Character.toLowerCase(name.charAt(0)));
            for (int i = 1; i < name.length(); i++) {
                if (Character.isUpperCase(name.charAt(i))) {
                    initials.append(Character.toLowerCase(name.charAt(i)));
                }
            }
        }

        return initials.length() >= MIN_ALIAS_LENGTH ? initials.toString() : null;
    }
}
