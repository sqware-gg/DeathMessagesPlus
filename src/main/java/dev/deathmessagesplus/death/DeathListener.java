package dev.deathmessagesplus.death;

import dev.deathmessagesplus.DeathMessagesPlusPlugin;
import dev.deathmessagesplus.config.DeathMessagesPlusConfig;
import java.util.Collection;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public final class DeathListener implements Listener {
    private final DeathMessagesPlusPlugin plugin;

    public DeathListener(DeathMessagesPlusPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        DeathMessagesPlusConfig config = plugin.deathConfig();
        if (!config.enabled()) {
            return;
        }

        DeathContext context = DeathContext.from(event, config);
        if (config.filter().ignoresWorld(context.world())
                || config.filter().ignoresDamageType(context.damageType(), context.damagePath(), context.cause())) {
            return;
        }

        Component message = plugin.renderer().renderBroadcast(context);
        event.deathMessage(message);
        event.setShowDeathMessages(false);

        broadcast(context, message, config.broadcast());
        sendPersonalMessage(context, config.personalMessage());
    }

    private void broadcast(DeathContext context, Component message, DeathMessagesPlusConfig.BroadcastSettings settings) {
        if (!settings.enabled() || "none".equalsIgnoreCase(settings.audience())) {
            return;
        }

        for (Player recipient : recipients(context.player(), settings)) {
            recipient.sendMessage(message);
        }

        if (settings.console()) {
            Bukkit.getConsoleSender().sendMessage(message);
        }
    }

    private Collection<? extends Player> recipients(Player player, DeathMessagesPlusConfig.BroadcastSettings settings) {
        return switch (settings.audience().toLowerCase(Locale.ROOT)) {
            case "world" -> player.getWorld().getPlayers();
            case "permission" -> Bukkit.getOnlinePlayers().stream()
                    .filter(online -> online.hasPermission(settings.permission()))
                    .toList();
            case "self" -> java.util.List.of(player);
            default -> Bukkit.getOnlinePlayers();
        };
    }

    private void sendPersonalMessage(DeathContext context, DeathMessagesPlusConfig.PersonalMessageSettings settings) {
        if (!settings.enabled()) {
            return;
        }
        if (settings.onlyIfItemsDropped() && context.droppedItems() <= 0) {
            return;
        }

        Runnable send = () -> {
            if (context.player().isOnline()) {
                context.player().sendMessage(plugin.renderer().renderPersonal(context));
            }
        };

        if (settings.delayTicks() <= 0L) {
            send.run();
            return;
        }
        Bukkit.getScheduler().runTaskLater(plugin, send, settings.delayTicks());
    }
}
