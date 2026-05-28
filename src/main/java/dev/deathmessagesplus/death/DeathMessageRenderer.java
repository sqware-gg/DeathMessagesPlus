package dev.deathmessagesplus.death;

import dev.deathmessagesplus.config.DeathMessagesPlusConfig;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.plugin.java.JavaPlugin;

public final class DeathMessageRenderer {
    private final JavaPlugin plugin;
    private final DeathMessagesPlusConfig config;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final LegacyComponentSerializer legacy = LegacyComponentSerializer.legacyAmpersand();
    private final PlainTextComponentSerializer plain = PlainTextComponentSerializer.plainText();
    private final Set<String> warnedTemplates = ConcurrentHashMap.newKeySet();

    public DeathMessageRenderer(JavaPlugin plugin, DeathMessagesPlusConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    public Component renderBroadcast(DeathContext context) {
        Component summary = renderSummary(context);
        String template = config.broadcast().template();
        if (legacyEngine()) {
            String summaryText = plain.serialize(summary);
            return legacy.deserialize(applyLegacyPlaceholders(template, context, summaryText));
        }
        return renderMiniMessage(template, context, summary);
    }

    public Component renderPersonal(DeathContext context) {
        String template = config.personalMessage().template();
        if (legacyEngine()) {
            return legacy.deserialize(applyLegacyPlaceholders(template, context, ""));
        }
        return renderMiniMessage(template, context, Component.empty());
    }

    private Component renderSummary(DeathContext context) {
        String template = config.format().summaryTemplate(context.damageType(), context.damagePath());
        if (legacyEngine()) {
            return legacy.deserialize(applyLegacyPlaceholders(template, context, ""));
        }
        return renderMiniMessage(template, context, Component.empty());
    }

    private Component renderMiniMessage(String template, DeathContext context, Component summary) {
        try {
            return miniMessage.deserialize(template, resolver(context, summary));
        } catch (RuntimeException e) {
            warnInvalidTemplate(template, e);
            return Component.text(applyLegacyPlaceholders(template, context, plain.serialize(summary)));
        }
    }

    private TagResolver resolver(DeathContext context, Component summary) {
        return TagResolver.resolver(
                Placeholder.component("player", Component.text(context.playerName())),
                Placeholder.component("display_name", context.displayName()),
                Placeholder.component("killer", context.killerDisplayName()),
                Placeholder.component("summary", summary),
                Placeholder.unparsed("killer_name", context.killerName()),
                Placeholder.unparsed("killer_type", context.killerType()),
                Placeholder.unparsed("damage_type", context.damageType()),
                Placeholder.unparsed("damage_path", context.damagePath()),
                Placeholder.unparsed("cause", context.cause()),
                Placeholder.unparsed("vanilla", context.vanilla()),
                Placeholder.unparsed("vanilla_without_player", context.vanillaWithoutPlayer()),
                Placeholder.unparsed("world", context.world()),
                Placeholder.unparsed("x", Integer.toString(context.x())),
                Placeholder.unparsed("y", Integer.toString(context.y())),
                Placeholder.unparsed("z", Integer.toString(context.z())),
                Placeholder.unparsed("dropped_items", Integer.toString(context.droppedItems())),
                Placeholder.unparsed("item_despawn_seconds", Integer.toString(context.itemDespawnSeconds())),
                Placeholder.unparsed("item_despawn_minutes", Integer.toString(context.itemDespawnMinutes()))
        );
    }

    private String applyLegacyPlaceholders(String template, DeathContext context, String summary) {
        String rendered = template == null ? "" : template;
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", context.playerName());
        placeholders.put("display_name", plain.serialize(context.displayName()));
        placeholders.put("killer", plain.serialize(context.killerDisplayName()));
        placeholders.put("killer_name", context.killerName());
        placeholders.put("killer_type", context.killerType());
        placeholders.put("summary", summary);
        placeholders.put("damage_type", context.damageType());
        placeholders.put("damage_path", context.damagePath());
        placeholders.put("cause", context.cause());
        placeholders.put("vanilla", context.vanilla());
        placeholders.put("vanilla_without_player", context.vanillaWithoutPlayer());
        placeholders.put("world", context.world());
        placeholders.put("x", Integer.toString(context.x()));
        placeholders.put("y", Integer.toString(context.y()));
        placeholders.put("z", Integer.toString(context.z()));
        placeholders.put("dropped_items", Integer.toString(context.droppedItems()));
        placeholders.put("item_despawn_seconds", Integer.toString(context.itemDespawnSeconds()));
        placeholders.put("item_despawn_minutes", Integer.toString(context.itemDespawnMinutes()));

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            rendered = rendered.replace("{" + entry.getKey() + "}", entry.getValue());
            rendered = rendered.replace("<" + entry.getKey() + ">", entry.getValue());
        }
        return rendered;
    }

    private boolean legacyEngine() {
        return "legacy".equalsIgnoreCase(config.format().engine());
    }

    private void warnInvalidTemplate(String template, RuntimeException e) {
        String key = template.toLowerCase(Locale.ROOT);
        if (warnedTemplates.add(key)) {
            plugin.getLogger().warning("Invalid MiniMessage template in config: " + template);
            plugin.getLogger().warning("MiniMessage error: " + e.getMessage());
        }
    }
}
