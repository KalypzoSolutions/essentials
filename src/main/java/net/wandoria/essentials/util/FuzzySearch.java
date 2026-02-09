package net.wandoria.essentials.util;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Small, self-contained fuzzy search utility for ranking string candidates by similarity to a query.
 *
 * Behavior summary:
 * - Case-insensitive
 * - Returns an empty list for null/empty queries or non-positive limits
 * - Uses normalized Levenshtein distance as a baseline and gives small bonuses for prefix/substring matches
 */
public final class FuzzySearch {
    private FuzzySearch() {
        // utility
    }

    /**
     * Compute a similarity score between 0.0 and 1.0 (higher means better match).
     */
    public static double score(String query, String candidate) {
        if (query == null || candidate == null) return 0.0;
        String q = query.toLowerCase(Locale.ROOT).trim();
        String c = candidate.toLowerCase(Locale.ROOT).trim();
        if (q.isEmpty() || c.isEmpty()) return 0.0;
        if (q.equals(c)) return 1.0;

        int lev = levenshtein(q, c);
        int max = Math.max(q.length(), c.length());
        double base = 1.0 - ((double) lev / (double) max);

        // cheap heuristics to favour prefix and substring matches
        if (c.startsWith(q)) base = Math.min(1.0, base + 0.15);
        else if (c.contains(q)) base = Math.min(1.0, base + 0.05);

        if (base < 0.0) base = 0.0;
        if (base > 1.0) base = 1.0;
        return base;
    }

    /**
     * Return up to {@code limit} candidates from the given collection that best match {@code query}.
     */
    public static List<String> suggest(String query, Collection<String> candidates, int limit) {
        if (query == null || candidates == null || limit <= 0) return Collections.emptyList();
        String q = query.trim();
        if (q.isEmpty()) return Collections.emptyList();

        return candidates.stream()
                .filter(Objects::nonNull)
                .distinct()
                .map(name -> new AbstractMap.SimpleEntry<>(name, score(q, name)))
                .filter(e -> e.getValue() > 0.0)
                .sorted((a, b) -> {
                    int cmp = Double.compare(b.getValue(), a.getValue());
                    if (cmp != 0) return cmp;
                    return a.getKey().compareToIgnoreCase(b.getKey());
                })
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    // Classic Levenshtein distance (iterative, O(m*n) time, O(min(m,n)) space)
    private static int levenshtein(String a, String b) {
        if (a == null) return (b == null) ? 0 : b.length();
        if (b == null) return a.length();
        int la = a.length();
        int lb = b.length();
        if (la == 0) return lb;
        if (lb == 0) return la;

        // ensure a is the shorter to use less memory
        if (la > lb) {
            String tmp = a; a = b; b = tmp;
            int tmpL = la; la = lb; lb = tmpL;
        }

        int[] prev = new int[la + 1];
        int[] cur = new int[la + 1];
        for (int i = 0; i <= la; i++) prev[i] = i;

        for (int j = 1; j <= lb; j++) {
            cur[0] = j;
            char bj = b.charAt(j - 1);
            for (int i = 1; i <= la; i++) {
                int cost = (a.charAt(i - 1) == bj) ? 0 : 1;
                cur[i] = Math.min(
                        Math.min(cur[i - 1] + 1, prev[i] + 1),
                        prev[i - 1] + cost
                );
            }
            // swap
            int[] tmp = prev; prev = cur; cur = tmp;
        }
        return prev[la];
    }
}

