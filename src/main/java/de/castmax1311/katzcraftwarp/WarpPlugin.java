package de.castmax1311.katzcraftwarp;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WarpPlugin extends JavaPlugin implements CommandExecutor, TabCompleter {

    private Map<String, Location> warps;
    private FileConfiguration warpsConfig;
    private File warpsFile;

    @Override
    public void onEnable() {
        warps = new HashMap<>();
        warpsFile = new File(getDataFolder(), "warps.yml");
        if (!warpsFile.exists()) {
            warpsFile.getParentFile().mkdirs();
            saveResource("warps.yml", false);
        }

        warpsConfig = YamlConfiguration.loadConfiguration(warpsFile);
        loadWarpsFromConfig();

        getCommand("addwarp").setExecutor(this);
        getCommand("deletewarp").setExecutor(this);
        getCommand("warp").setExecutor(this);
        getCommand("warplist").setExecutor(this);
        getCommand("warp").setTabCompleter(this);
        getCommand("deletewarp").setTabCompleter(this);
    }

    @Override
    public void onDisable() {
        saveWarpsToConfig();
    }

    public static String formatMessage(String message) {
        return "[" + ChatColor.BLUE + "KatzcraftWarp" + ChatColor.RESET + "] " + message;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("addwarp")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(WarpPlugin.formatMessage(ChatColor.RED + "This command can only be executed by players."));
                return true;
            }

            Player player = (Player) sender;

            if (!player.hasPermission("warpplugin.addwarp")) {
                player.sendMessage(WarpPlugin.formatMessage(ChatColor.RED + "You don't have permission to set warps"));
                return true;
            }

            if (args.length != 1) {
                player.sendMessage(WarpPlugin.formatMessage("Use: /addwarp <warp-Name>"));
                return true;
            }

            String warpName = args[0];
            Location warpLocation = player.getLocation();

            warps.put(warpName, warpLocation);
            player.sendMessage(WarpPlugin.formatMessage(ChatColor.GREEN + "Warp \"" + warpName + "\" has been set"));

            return true;
        }

        if (command.getName().equalsIgnoreCase("deletewarp")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(WarpPlugin.formatMessage(ChatColor.RED + "This command can only be executed by players."));
                return true;
            }

            Player player = (Player) sender;

            if (!player.hasPermission("warpplugin.deletewarp")) {
                player.sendMessage(WarpPlugin.formatMessage(ChatColor.RED + "You don't have permission to delete warps"));
                return true;
            }

            if (args.length != 1) {
                player.sendMessage(WarpPlugin.formatMessage("Use: /deletewarp <warp-Name>"));
                return true;
            }

            String warpName = args[0];

            if (warps.containsKey(warpName)) {
                warps.remove(warpName);
                player.sendMessage(WarpPlugin.formatMessage(ChatColor.GREEN + "Warp \"" + warpName + "\" has been deleted"));
            } else {
                player.sendMessage(WarpPlugin.formatMessage(ChatColor.RED + "Warp \"" + warpName + "\" doesn't exist"));
            }

            return true;
        }

        else if (command.getName().equalsIgnoreCase("warp")) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(WarpPlugin.formatMessage(ChatColor.RED + "This command can only be executed by players."));
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 1) {
            player.sendMessage(WarpPlugin.formatMessage("Use: /warp <Warp-Name>"));
            return true;
        }

        String warpName = args[0];
        Location warpLocation = warps.get(warpName);

        if (warpLocation == null) {
            player.sendMessage(WarpPlugin.formatMessage(ChatColor.RED + "Warp \"" + warpName + "\" doesn't exist"));
            return true;
        }

        player.teleport(warpLocation);
        player.sendMessage(WarpPlugin.formatMessage(ChatColor.GREEN + "You were teleported to \"" + warpName + "\""));

        return true;

    } else if (command.getName().equalsIgnoreCase("warplist")) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(WarpPlugin.formatMessage(ChatColor.RED + "This command can only be executed by players."));
            return true;
        }

        Player player = (Player) sender;

        player.sendMessage(WarpPlugin.formatMessage(ChatColor.GREEN + "Available warps:"));

        for (String warpName : warps.keySet()) {
            player.sendMessage("- " + warpName);
        }

        return true;
    }

        return false;
}

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("warp")) {
            if (args.length == 1) {
                for (String warpName : warps.keySet()) {
                    if (warpName.startsWith(args[0])) {
                        completions.add(warpName);
                    }
                }
            }
        } else if (command.getName().equalsIgnoreCase("deletewarp")) {
            if (args.length == 1) {
                for (String warpName : warps.keySet()) {
                    if (warpName.startsWith(args[0])) {
                        completions.add(warpName);
                    }
                }
            }
        }
        return completions;
    }

    private void loadWarpsFromConfig() {
        ConfigurationSection warpsSection = warpsConfig.getConfigurationSection("warps");
        if (warpsSection != null) {
            Set<String> warpNames = warpsSection.getKeys(false);
            for (String warpName : warpNames) {
                ConfigurationSection warpSection = warpsSection.getConfigurationSection(warpName);
                double x = warpSection.getDouble("x");
                double y = warpSection.getDouble("y");
                double z = warpSection.getDouble("z");
                float yaw = (float) warpSection.getDouble("yaw");
                float pitch = (float) warpSection.getDouble("pitch");
                Location location = new Location(getServer().getWorlds().get(0), x, y, z, yaw, pitch);
                warps.put(warpName, location);
            }
        }
    }

    private void saveWarpsToConfig() {
        ConfigurationSection warpsSection = warpsConfig.createSection("warps");
        for (Map.Entry<String, Location> entry : warps.entrySet()) {
            String warpName = entry.getKey();
            Location location = entry.getValue();
            ConfigurationSection warpSection = warpsSection.createSection(warpName);
            warpSection.set("x", location.getX());
            warpSection.set("y", location.getY());
            warpSection.set("z", location.getZ());
            warpSection.set("yaw", location.getYaw());
            warpSection.set("pitch", location.getPitch());
        }
        try {
            warpsConfig.save(warpsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
