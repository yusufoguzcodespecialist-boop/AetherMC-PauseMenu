package net.aethermc.pausemenu;

import net.minecraft.client.MinecraftClient;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreHolder;

public class RankReader {

    public static void update() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        Scoreboard sb = client.world.getScoreboard();
        ScoreboardObjective obj = sb.getNullableObjective("aetherrank");
        if (obj == null) return;

        ScoreHolder holder = ScoreHolder.fromName(client.player.getNameForScoreboard());
        var scores = sb.getScoreHolderObjectives(holder);
        if (scores == null || !scores.containsKey(obj)) return;

        int val = scores.get(obj).getScore();
        int idx = Math.max(0, Math.min(val, AetherPauseMenuMod.RANK_NAMES.length - 1));
        AetherPauseMenuMod.currentRank = idx;
        AetherPauseMenuMod.currentRankName = AetherPauseMenuMod.RANK_NAMES[idx];
        AetherPauseMenuMod.currentRankColor = AetherPauseMenuMod.RANK_COLORS[idx];
    }
}
