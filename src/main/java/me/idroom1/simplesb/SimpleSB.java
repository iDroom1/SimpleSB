package me.idroom1.simplesb;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.scoreboard.*;

import java.util.List;

public class SimpleSB extends JavaPlugin implements TabExecutor {

    private ScoreboardManager manager;
    private Scoreboard board;
    private Objective objective;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getCommand("sb").setExecutor(this);
        manager = Bukkit.getScoreboardManager();
        board = manager.getNewScoreboard();
        loadScoreboard();
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(manager.getNewScoreboard());
        }
    }

    private void loadScoreboard() {
        FileConfiguration config = getConfig();
        String title = ChatColor.translateAlternateColorCodes('&', config.getString("title", "&aSimpleSB"));

        objective = board.registerNewObjective("SimpleSB", "dummy", title);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        List<String> lines = config.getStringList("lines");
        for (int i = 0; i < lines.size(); i++) {
            String line = ChatColor.translateAlternateColorCodes('&', lines.get(i));
            line = PlaceholderAPI.setPlaceholders(null, line); // Support for PlaceholderAPI
            objective.getScore(line).setScore(lines.size() - i);
        }
    }

    private void reloadScoreboard() {
        for (String entry : board.getEntries()) {
            board.resetScores(entry);
        }
        objective.unregister();
        loadScoreboard();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("simplesb.admin")) {
                player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                return true;
            }

            if (args.length == 0) {
                if (player.getScoreboard().equals(board)) {
                    player.setScoreboard(manager.getNewScoreboard());
                    player.sendMessage(ChatColor.GREEN + "Scoreboard disabled.");
                } else {
                    player.setScoreboard(board);
                    player.sendMessage(ChatColor.GREEN + "Scoreboard enabled.");
                }
                return true;
            }

            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                reloadConfig();
                reloadScoreboard();
                player.sendMessage(ChatColor.GREEN + "Configuration reloaded.");
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("reload");
        }
        return null;
    }
}