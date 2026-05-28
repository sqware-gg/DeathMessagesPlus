package dev.deathmessagesplus.command;

import dev.deathmessagesplus.DeathMessagesPlusPlugin;
import dev.deathmessagesplus.config.DeathMessagesPlusConfig;
import dev.deathmessagesplus.death.DeathContext;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public final class DeathMessagesPlusCommand implements CommandExecutor, TabCompleter {
    private final DeathMessagesPlusPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public DeathMessagesPlusCommand(DeathMessagesPlusPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("deathmessagesplus.admin")) {
            message(sender, "<#ED4245>No permission.</#ED4245>");
            return true;
        }

        if (args.length == 0 || "status".equalsIgnoreCase(args[0])) {
            status(sender);
            return true;
        }

        if ("reload".equalsIgnoreCase(args[0])) {
            plugin.reloadPlugin();
            message(sender, "<#57F287>DeathMessagesPlus reloaded.</#57F287>");
            return true;
        }

        if ("preview".equalsIgnoreCase(args[0])) {
            preview(sender);
            return true;
        }

        usage(sender, label);
        return true;
    }

    private void status(CommandSender sender) {
        DeathMessagesPlusConfig config = plugin.deathConfig();
        message(sender, "<gray>Version:</gray> <white>" + plugin.getPluginMeta().getVersion() + "</white>");
        message(sender, "<gray>Enabled:</gray> <white>" + enabledText(config.enabled()) + "</white>");
        message(sender, "<gray>Broadcast:</gray> <white>" + enabledText(config.broadcast().enabled())
                + "</white> <dark_gray>(" + config.broadcast().audience() + ")</dark_gray>");
        message(sender, "<gray>Personal message:</gray> <white>"
                + enabledText(config.personalMessage().enabled()) + "</white>");
        message(sender, "<gray>Format engine:</gray> <white>" + config.format().engine() + "</white>");
    }

    private void preview(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            message(sender, "<#ED4245>Only players can preview death messages.</#ED4245>");
            return;
        }

        DeathContext context = DeathContext.preview(player, plugin.deathConfig());
        message(sender, "<gray>Broadcast preview:</gray>");
        sender.sendMessage(plugin.renderer().renderBroadcast(context));
        message(sender, "<gray>Personal preview:</gray>");
        sender.sendMessage(plugin.renderer().renderPersonal(context));
    }

    private String enabledText(boolean enabled) {
        return enabled ? "enabled" : "disabled";
    }

    private void usage(CommandSender sender, String label) {
        message(sender, "<gray>Usage:</gray> <#ff4f6d>/" + label + " <status|reload|preview></#ff4f6d>");
    }

    private void message(CommandSender sender, String body) {
        sender.sendMessage(miniMessage.deserialize("<#ff4f6d>DeathMessagesPlus</#ff4f6d> <dark_gray>></dark_gray> " + body));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("deathmessagesplus.admin")) {
            return List.of();
        }
        if (args.length == 1) {
            return filter(List.of("status", "reload", "preview"), args[0]);
        }
        return List.of();
    }

    private List<String> filter(List<String> values, String prefix) {
        String normalizedPrefix = prefix.toLowerCase(Locale.ROOT);
        return values.stream()
                .filter(value -> value.toLowerCase(Locale.ROOT).startsWith(normalizedPrefix))
                .toList();
    }
}
