package com.ishttt.diwali.commands;

import com.ishttt.diwali.DiwaliPlugin;
import com.ishttt.diwali.items.DiwaliItemType;
import com.ishttt.diwali.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DiwaliCommand implements CommandExecutor, TabCompleter {

    private final DiwaliPlugin plugin;

    public DiwaliCommand(DiwaliPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        // /diwali shop
        if (sub.equals("shop")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.getConfigManager().getMessage("general.player-only"));
                return true;
            }
            if (!player.hasPermission("diwali.shop")) {
                player.sendMessage(plugin.getConfigManager().getMessage("general.no-permission"));
                return true;
            }
            plugin.getShopGUI().openShop(player);
            return true;
        }

        // /diwali reload
        if (sub.equals("reload")) {
            if (!sender.hasPermission("diwali.admin")) {
                sender.sendMessage(plugin.getConfigManager().getMessage("general.no-permission"));
                return true;
            }
            plugin.getConfigManager().loadConfigs();
            sender.sendMessage(plugin.getConfigManager().getMessage("general.reloaded"));
            return true;
        }

        // /diwali give <player> <item> [amount]
        if (sub.equals("give")) {
            if (!sender.hasPermission("diwali.admin")) {
                sender.sendMessage(plugin.getConfigManager().getMessage("general.no-permission"));
                return true;
            }
            if (args.length < 3) {
                sender.sendMessage(ChatUtils.color("&cUsage: /diwali give <player> <item> [amount]"));
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(ChatUtils.color("&cPlayer not found: " + args[1]));
                return true;
            }

            DiwaliItemType type = DiwaliItemType.fromId(args[2]);
            if (type == null) {
                sender.sendMessage(ChatUtils.color("&cInvalid Diwali item: " + args[2]));
                return true;
            }

            int amount = 1;
            if (args.length >= 4) {
                try {
                    amount = Integer.parseInt(args[3]);
                } catch (NumberFormatException ignored) {}
            }

            ItemStack item = plugin.getItemManager().createItem(type, amount);
            target.getInventory().addItem(item);
            sender.sendMessage(ChatUtils.color("&aGave &e" + amount + "x " + type.getDisplayName() + " &ato &e" + target.getName()));
            target.sendMessage(ChatUtils.color("&aYou received &e" + amount + "x " + type.getDisplayName() + "&a!"));
            return true;
        }

        sendHelp(sender);
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatUtils.color("&6&m--------------------------------------------------"));
        sender.sendMessage(ChatUtils.color("           &e&l🪔 DIWALI FESTIVAL COMMANDS &r"));
        sender.sendMessage(ChatUtils.color(" &e/diwali shop &7- Open the Diwali Festival Shop"));
        if (sender.hasPermission("diwali.admin")) {
            sender.sendMessage(ChatUtils.color(" &e/diwali give <player> <item> [amount] &7- Give festival items"));
            sender.sendMessage(ChatUtils.color(" &e/diwali reload &7- Reload plugin configuration"));
        }
        sender.sendMessage(ChatUtils.color("&6&m--------------------------------------------------"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            List<String> subs = new ArrayList<>(Arrays.asList("shop", "help"));
            if (sender.hasPermission("diwali.admin")) {
                subs.add("give");
                subs.add("reload");
            }
            return filter(subs, args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("give") && sender.hasPermission("diwali.admin")) {
            return filter(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()), args[1]);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("give") && sender.hasPermission("diwali.admin")) {
            return filter(Arrays.stream(DiwaliItemType.values()).map(DiwaliItemType::getId).collect(Collectors.toList()), args[2]);
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("give") && sender.hasPermission("diwali.admin")) {
            return Arrays.asList("1", "4", "16", "64");
        }

        return completions;
    }

    private List<String> filter(List<String> list, String query) {
        return list.stream()
                .filter(s -> s.toLowerCase().startsWith(query.toLowerCase()))
                .collect(Collectors.toList());
    }
}
