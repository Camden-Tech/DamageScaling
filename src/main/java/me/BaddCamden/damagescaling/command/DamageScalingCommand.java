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

public class DamageScalingCommand implements CommandExecutor, TabCompleter {

    private final DamageScalingPlugin plugin;

    public DamageScalingCommand(DamageScalingPlugin plugin) {
        this.plugin = plugin;
    }

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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("enable", "disable");
        }
        return Collections.emptyList();
    }
}
