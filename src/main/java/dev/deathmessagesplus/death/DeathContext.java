package dev.deathmessagesplus.death;

import dev.deathmessagesplus.config.DeathMessagesPlusConfig;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Nameable;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public record DeathContext(
        Player player,
        Component displayName,
        String playerName,
        Location location,
        String world,
        int x,
        int y,
        int z,
        Component killerDisplayName,
        String killerName,
        String killerType,
        boolean playerKiller,
        String damageType,
        String damagePath,
        String cause,
        String vanilla,
        String vanillaWithoutPlayer,
        int droppedItems,
        int itemDespawnSeconds,
        int itemDespawnMinutes
) {
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    public static DeathContext from(PlayerDeathEvent event, DeathMessagesPlusConfig config) {
        Player player = event.getPlayer();
        Location location = player.getLocation();
        World world = location.getWorld();
        DamageSource damageSource = event.getDamageSource();
        DamageDetails damageDetails = damageDetails(player, damageSource);
        Entity killer = killer(player, damageSource);
        KillerDetails killerDetails = killerDetails(killer);
        String vanilla = vanillaMessage(event);
        int itemDespawnSeconds = config.personalMessage().itemDespawnSeconds();

        return new DeathContext(
                player,
                player.displayName(),
                player.getName(),
                location,
                world == null ? "world" : world.getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ(),
                killerDetails.displayName(),
                killerDetails.name(),
                killerDetails.type(),
                killer instanceof Player,
                damageDetails.damageType(),
                damageDetails.damagePath(),
                damageDetails.cause(),
                vanilla,
                withoutPlayerName(vanilla, player),
                event.getDrops().size(),
                itemDespawnSeconds,
                Math.max(1, Math.round(itemDespawnSeconds / 60.0F))
        );
    }

    public static DeathContext preview(Player player, DeathMessagesPlusConfig config) {
        Location location = player.getLocation();
        World world = location.getWorld();
        int itemDespawnSeconds = config.personalMessage().itemDespawnSeconds();
        String vanilla = player.getName() + " was slain by Zombie";
        return new DeathContext(
                player,
                player.displayName(),
                player.getName(),
                location,
                world == null ? "world" : world.getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ(),
                Component.text("Zombie"),
                "Zombie",
                "zombie",
                false,
                "minecraft:mob_attack",
                "mob_attack",
                "entity_attack",
                vanilla,
                "was slain by Zombie",
                3,
                itemDespawnSeconds,
                Math.max(1, Math.round(itemDespawnSeconds / 60.0F))
        );
    }

    private static DamageDetails damageDetails(Player player, DamageSource damageSource) {
        String damageType = "unknown";
        String damagePath = "unknown";
        if (damageSource != null && damageSource.getDamageType() != null) {
            NamespacedKey key = damageSource.getDamageType().getKey();
            damageType = key.toString();
            damagePath = key.getKey();
        }

        EntityDamageEvent lastDamageCause = player.getLastDamageCause();
        String cause = lastDamageCause == null
                ? damagePath
                : lastDamageCause.getCause().name().toLowerCase(Locale.ROOT);
        return new DamageDetails(damageType, damagePath, cause);
    }

    private static Entity killer(Player player, DamageSource damageSource) {
        if (damageSource == null) {
            return null;
        }
        Entity causingEntity = damageSource.getCausingEntity();
        if (causingEntity != null && !causingEntity.getUniqueId().equals(player.getUniqueId())) {
            return causingEntity;
        }
        Entity directEntity = damageSource.getDirectEntity();
        if (directEntity != null && !directEntity.getUniqueId().equals(player.getUniqueId())) {
            return directEntity;
        }
        return null;
    }

    private static KillerDetails killerDetails(Entity killer) {
        if (killer == null) {
            return new KillerDetails(Component.text("something"), "something", "unknown");
        }
        if (killer instanceof Player player) {
            return new KillerDetails(player.displayName(), player.getName(), "player");
        }
        if (killer instanceof Nameable nameable && nameable.customName() != null
                && !PLAIN.serialize(nameable.customName()).isBlank()) {
            String plainName = PLAIN.serialize(nameable.customName());
            return new KillerDetails(nameable.customName(), plainName, killer.getType().getKey().getKey());
        }
        String type = killer.getType().getKey().getKey();
        return new KillerDetails(Component.translatable(killer.getType().translationKey()), humanize(type), type);
    }

    private static String vanillaMessage(PlayerDeathEvent event) {
        Component deathMessage = event.deathMessage();
        if (deathMessage != null) {
            String plain = PLAIN.serialize(deathMessage).trim();
            if (!plain.isBlank()) {
                return normalizeSpaces(plain);
            }
        }
        return event.getPlayer().getName() + " died";
    }

    private static String withoutPlayerName(String message, Player player) {
        String normalized = normalizeSpaces(message);
        for (String candidate : playerNameCandidates(player)) {
            if (candidate.isBlank()) {
                continue;
            }
            if (normalized.equals(candidate)) {
                return "died";
            }
            if (normalized.startsWith(candidate + " ")) {
                return normalized.substring(candidate.length() + 1).trim();
            }
        }
        return normalized;
    }

    private static String[] playerNameCandidates(Player player) {
        return new String[] {
                normalizeSpaces(player.getName()),
                normalizeSpaces(PLAIN.serialize(player.displayName()))
        };
    }

    private static String normalizeSpaces(String value) {
        return value == null ? "" : value.replaceAll("\\s+", " ").trim();
    }

    private static String humanize(String value) {
        String normalized = value == null ? "" : value.replace('_', ' ').trim().toLowerCase(Locale.ROOT);
        if (normalized.isBlank()) {
            return "Unknown";
        }
        StringBuilder result = new StringBuilder();
        for (String part : normalized.split("\\s+")) {
            if (!result.isEmpty()) {
                result.append(' ');
            }
            result.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                result.append(part.substring(1));
            }
        }
        return result.toString();
    }

    private record DamageDetails(String damageType, String damagePath, String cause) {
    }

    private record KillerDetails(Component displayName, String name, String type) {
    }
}
