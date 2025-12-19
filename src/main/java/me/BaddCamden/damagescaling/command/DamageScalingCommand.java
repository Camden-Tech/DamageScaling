package me.BaddCamden.damagescaling.command;

import java.util.ArrayList;
import java.util.List;
import me.BaddCamden.damagescaling.ConfigManager;
import me.BaddCamden.damagescaling.PlayerHealthService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public class DamageScalingCommand implements CommandExecutor, TabCompleter {
    private final ConfigManager configManager;
    private final PlayerHealthService playerHealthService;

    public DamageScalingCommand(ConfigManager configManager, PlayerHealthService playerHealthService) {
        this.configManager = configManager;
        this.playerHealthService = playerHealthService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("damagescaling.admin")) {
            sender.sendMessage("You do not have permission to use this command.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage("Usage: /" + label + " <enable|disable>");
            return true;
        }

        String option = args[0].toLowerCase();
        switch (option) {
            case "enable" -> enable(sender);
            case "disable" -> disable(sender);
            default -> sender.sendMessage("Unknown option. Use enable or disable.");
        }
        return true;
    }

    private void enable(CommandSender sender) {
        configManager.reload();
        configManager.saveEnabledState(true);
        for (Player online : Bukkit.getOnlinePlayers()) {
            playerHealthService.applyHealthState(online);
        }
        sender.sendMessage("DamageScaling enabled.");
    }

    private void disable(CommandSender sender) {
        configManager.reload();
        configManager.saveEnabledState(false);
        for (Player online : Bukkit.getOnlinePlayers()) {
            playerHealthService.applyHealthState(online);
        }
        sender.sendMessage("DamageScaling disabled.");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], List.of("enable", "disable"), new ArrayList<>());
        }
        return List.of();
    }
}
