package com.broja.simplespawn;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SimpleSpawn extends JavaPlugin implements Listener {
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("SimpleSpawn enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("SimpleSpawn disabled");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players may use this command.");
            return true;
        }
        Player p = (Player) sender;
        if (command.getName().equalsIgnoreCase("setspawn")) {
            if (!p.hasPermission("simplespawn.set")) {
                p.sendMessage(ChatColor.RED + "No permission.");
                return true;
            }
            Location loc = p.getLocation();
            getConfig().set("spawn.world", loc.getWorld().getName());
            getConfig().set("spawn.x", loc.getX());
            getConfig().set("spawn.y", loc.getY());
            getConfig().set("spawn.z", loc.getZ());
            getConfig().set("spawn.yaw", loc.getYaw());
            getConfig().set("spawn.pitch", loc.getPitch());
            saveConfig();
            p.sendMessage(ChatColor.GREEN + "Spawn set.");
            return true;
        } else if (command.getName().equalsIgnoreCase("spawn")) {
            int cooldown = getConfig().getInt("cooldownSeconds", 10);
            UUID id = p.getUniqueId();
            long now = System.currentTimeMillis();
            if (cooldowns.containsKey(id)) {
                long last = cooldowns.get(id);
                long diff = (now - last) / 1000;
                if (diff < cooldown) {
                    p.sendMessage(ChatColor.YELLOW + "You must wait " + (cooldown - diff) + " seconds to use /spawn again.");
                    return true;
                }
            }
            if (!getConfig().contains("spawn.world")) {
                p.sendMessage(ChatColor.RED + "Spawn not set. Ask an admin to run /setspawn.");
                return true;
            }
            String worldName = getConfig().getString("spawn.world");
            World w = getServer().getWorld(worldName);
            if (w == null) {
                p.sendMessage(ChatColor.RED + "Spawn world not found on this server.");
                return true;
            }
            double x = getConfig().getDouble("spawn.x");
            double y = getConfig().getDouble("spawn.y");
            double z = getConfig().getDouble("spawn.z");
            float yaw = (float) getConfig().getDouble("spawn.yaw");
            float pitch = (float) getConfig().getDouble("spawn.pitch");
            Location spawn = new Location(w, x, y, z, yaw, pitch);
            p.teleport(spawn);
            p.sendMessage(ChatColor.GREEN + "Teleported to spawn.");
            cooldowns.put(id, now);
            return true;
        }
        return false;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        String msg = getConfig().getString("welcome.message", "&aWelcome to the server, %player%!");
        msg = msg.replace("%player%", p.getName());
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
        boolean title = getConfig().getBoolean("welcome.title.enabled", false);
        if (title) {
            String titleText = ChatColor.translateAlternateColorCodes('&', getConfig().getString("welcome.title.title", "&6Welcome"));
            String subtitle = ChatColor.translateAlternateColorCodes('&', getConfig().getString("welcome.title.subtitle", "&7Have fun!"));
            p.sendTitle(titleText, subtitle, 10, 70, 20);
        }
    }
}
