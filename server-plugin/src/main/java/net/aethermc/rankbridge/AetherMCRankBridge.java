package net.aethermc.rankbridge;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public final class AetherMCRankBridge extends JavaPlugin {
    private LuckPerms luckPerms;
    private Objective objective;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        try {
            luckPerms = LuckPermsProvider.get();
        } catch (IllegalStateException exception) {
            getLogger().severe("LuckPerms was not found. Install LuckPerms and restart the server.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        Scoreboard main = Bukkit.getScoreboardManager().getMainScoreboard();
        objective = main.getObjective("aetherrank");
        if (objective == null) {
            objective = main.registerNewObjective("aetherrank", Criteria.DUMMY,
                ChatColor.AQUA + "AetherMC Rank");
        }

        Bukkit.getScheduler().runTaskTimer(this, this::updateOnlinePlayers, 1L, 40L);
        getLogger().info("LuckPerms rank bridge enabled; syncing ranks to scoreboard objective aetherrank.");
    }

    private void updateOnlinePlayers() {
        if (luckPerms == null || objective == null) return;
        for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
            var user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user == null) continue;

            String group = user.getPrimaryGroup().toLowerCase(Locale.ROOT);
            int rank = getConfig().getInt("groups." + group, getConfig().getInt("groups.default", 0));
            rank = Math.max(0, Math.min(11, rank));
            objective.getScore(player.getName()).setScore(rank);
        }
    }
}
