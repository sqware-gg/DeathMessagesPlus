package dev.deathmessagesplus;

import dev.deathmessagesplus.command.DeathMessagesPlusCommand;
import dev.deathmessagesplus.config.ConfigReferenceWriter;
import dev.deathmessagesplus.config.DeathMessagesPlusConfig;
import dev.deathmessagesplus.death.DeathListener;
import dev.deathmessagesplus.death.DeathMessageRenderer;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class DeathMessagesPlusPlugin extends JavaPlugin {
    private DeathMessagesPlusConfig deathConfig;
    private DeathMessageRenderer renderer;

    @Override
    public void onEnable() {
        ConfigReferenceWriter.saveDefaultAndReferenceIfNeeded(this);
        deathConfig = new DeathMessagesPlusConfig(this);
        renderer = new DeathMessageRenderer(this, deathConfig);

        getServer().getPluginManager().registerEvents(new DeathListener(this), this);

        DeathMessagesPlusCommand command = new DeathMessagesPlusCommand(this);
        PluginCommand pluginCommand = getCommand("deathmessagesplus");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(command);
            pluginCommand.setTabCompleter(command);
        }

        getLogger().info("Custom death messages are active.");
    }

    public void reloadPlugin() {
        ConfigReferenceWriter.saveDefaultAndReferenceIfNeeded(this);
        deathConfig.reload();
        renderer = new DeathMessageRenderer(this, deathConfig);
    }

    public DeathMessagesPlusConfig deathConfig() {
        return deathConfig;
    }

    public DeathMessageRenderer renderer() {
        return renderer;
    }
}
