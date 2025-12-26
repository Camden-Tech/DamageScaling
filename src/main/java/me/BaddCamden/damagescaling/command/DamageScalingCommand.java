package me.BaddCamden.damagescaling.command;

import me.BaddCamden.damagescaling.DamageScalingPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Handles the {@code /damagescaling} administrative command, allowing operators to toggle scaling
 * on and off at runtime.
 */
public class DamageScalingCommand implements CommandExecutor, TabCompleter {

    private final DamageScalingPlugin plugin;

    /**
     * Creates a new command handler bound to the plugin instance so settings can be updated and
     * players refreshed.
     *
     * @param plugin owning plugin instance
     */
    public DamageScalingCommand(DamageScalingPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Processes execution of the command, validating permissions and toggling damage scaling based
     * on the provided argument.
     *
     * @param sender  command invoker
     * @param command executed command
     * @param label   alias used
     * @param args    command arguments
     * @return {@code true} to indicate the command was handled
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("damagescaling.admin")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <enable|disable>");
            return true;
        }

        String action = args[0].toLowerCase();
        if (action.equals("enable")) {
            plugin.setScalingEnabled(true);
            plugin.refreshAllPlayers();
            sender.sendMessage(ChatColor.GREEN + "Damage scaling is now enabled.");
            return true;
        }

        if (action.equals("disable")) {
            plugin.setScalingEnabled(false);
            plugin.restoreRealHealth();
            sender.sendMessage(ChatColor.YELLOW + "Damage scaling is now disabled.");
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <enable|disable>");
        return true;
    }

    /**
     * Provides tab-completion for the command, offering enable/disable suggestions when typing the
     * first argument.
     *
     * @param sender source requesting completions
     * @param command command being completed
     * @param alias alias used
     * @param args current arguments supplied by the sender
     * @return list of suggestions or an empty list when none are available
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("enable", "disable");
        }
        return Collections.emptyList();
    }
}
