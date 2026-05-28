package dev.deathmessagesplus.config;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class DeathMessagesPlusConfig {
    private final JavaPlugin plugin;

    private boolean enabled;
    private BroadcastSettings broadcast;
    private PersonalMessageSettings personalMessage;
    private FilterSettings filter;
    private FormatSettings format;

    public DeathMessagesPlusConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();

        enabled = bool("enabled", true);
        broadcast = new BroadcastSettings(
                bool("broadcast.enabled", true),
                string("broadcast.audience", "all"),
                string("broadcast.permission", "deathmessagesplus.see"),
                bool("broadcast.console", true),
                string("broadcast.template",
                        "<#ff4f6d>Death</#ff4f6d> <dark_gray>></dark_gray> <white><display_name></white> <gray><summary></gray>")
        );

        int itemDespawnSeconds = Math.max(1, plugin.getConfig().getInt("personal-message.item-despawn-seconds", 300));
        personalMessage = new PersonalMessageSettings(
                bool("personal-message.enabled", true),
                Math.max(0L, plugin.getConfig().getLong("personal-message.delay-ticks", 1L)),
                bool("personal-message.only-if-items-dropped", false),
                itemDespawnSeconds,
                string("personal-message.template",
                        "<#ff4f6d>Death</#ff4f6d> <dark_gray>></dark_gray> <gray>Quickly return to <#ffcc66><x> <y> <z></#ffcc66> in <white><world></white>, or your items could despawn.</gray>")
        );

        filter = new FilterSettings(
                stringList("filter.ignored-worlds", List.of()),
                stringList("filter.ignored-damage-types", List.of())
        );

        format = new FormatSettings(
                string("format.engine", "minimessage"),
                readSummaryTemplates()
        );
    }

    private Map<String, String> readSummaryTemplates() {
        Map<String, String> templates = new HashMap<>();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("summary-templates");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                String value = section.getString(key);
                if (value != null) {
                    templates.put(normalize(key), value.trim());
                }
            }
        }
        templates.putIfAbsent("default", "<vanilla_without_player>");
        return Map.copyOf(templates);
    }

    private boolean bool(String path, boolean fallback) {
        return plugin.getConfig().getBoolean(path, fallback);
    }

    private String string(String path, String fallback) {
        FileConfiguration config = plugin.getConfig();
        String value = config.getString(path, fallback);
        if (value == null) {
            return fallback;
        }
        return value.trim();
    }

    private List<String> stringList(String path, List<String> fallback) {
        FileConfiguration config = plugin.getConfig();
        if (!config.isList(path)) {
            return fallback;
        }
        return config.getStringList(path).stream()
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    public boolean enabled() {
        return enabled;
    }

    public BroadcastSettings broadcast() {
        return broadcast;
    }

    public PersonalMessageSettings personalMessage() {
        return personalMessage;
    }

    public FilterSettings filter() {
        return filter;
    }

    public FormatSettings format() {
        return format;
    }

    public record BroadcastSettings(boolean enabled, String audience, String permission, boolean console,
                                    String template) {
    }

    public record PersonalMessageSettings(boolean enabled, long delayTicks, boolean onlyIfItemsDropped,
                                          int itemDespawnSeconds, String template) {
        public int itemDespawnMinutes() {
            return Math.max(1, Math.round(itemDespawnSeconds / 60.0F));
        }
    }

    public record FilterSettings(List<String> ignoredWorlds, List<String> ignoredDamageTypes) {
        public boolean ignoresWorld(String world) {
            return matches(ignoredWorlds, world);
        }

        public boolean ignoresDamageType(String damageType, String damagePath, String cause) {
            return matches(ignoredDamageTypes, damageType)
                    || matches(ignoredDamageTypes, damagePath)
                    || matches(ignoredDamageTypes, cause);
        }

        private static boolean matches(List<String> values, String candidate) {
            if (candidate == null || candidate.isBlank()) {
                return false;
            }
            String normalized = candidate.toLowerCase(Locale.ROOT);
            return values.stream()
                    .map(value -> value.toLowerCase(Locale.ROOT))
                    .anyMatch(value -> value.equals("*") || value.equals(normalized));
        }
    }

    public record FormatSettings(String engine, Map<String, String> summaryTemplates) {
        public String summaryTemplate(String damageType, String damagePath) {
            String fullKey = normalize(damageType);
            String pathKey = normalize(damagePath);
            if (summaryTemplates.containsKey(fullKey)) {
                return summaryTemplates.get(fullKey);
            }
            if (summaryTemplates.containsKey(pathKey)) {
                return summaryTemplates.get(pathKey);
            }
            return summaryTemplates.getOrDefault("default", "<vanilla_without_player>");
        }
    }
}
