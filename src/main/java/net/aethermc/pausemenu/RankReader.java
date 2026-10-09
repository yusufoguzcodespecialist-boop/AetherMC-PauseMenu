package net.aethermc.pausemenu;

import net.minecraft.client.MinecraftClient;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreHolder;

public final class RankReader {

    private RankReader() {
    }

    public static void update() {
        AetherPauseMenuMod.currentRank = 0;
        AetherPauseMenuMod.currentRankName = AetherPauseMenuMod.RANK_NAMES[0];
        AetherPauseMenuMod.currentRankColor = AetherPauseMenuMod.RANK_COLORS[0];

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            return;
        }

        Scoreboard scoreboard = client.world.getScoreboard();
        ScoreboardObjective objective = scoreboard.getNullableObjective("aetherrank");
        if (objective == null) {
            return;
        }

        ScoreHolder holder = ScoreHolder.fromName(client.player.getNameForScoreboard());
        var scores = scoreboard.getScoreHolderObjectives(holder);
        if (scores == null) {
            return;
        }

        var score = scores.get(objective);
        if (score == null) {
            return;
        }

        int rankIndex = Math.max(0, Math.min(score.getScore(),
            AetherPauseMenuMod.RANK_NAMES.length - 1));
        AetherPauseMenuMod.currentRank = rankIndex;
        AetherPauseMenuMod.currentRankName = AetherPauseMenuMod.RANK_NAMES[rankIndex];
        AetherPauseMenuMod.currentRankColor = AetherPauseMenuMod.RANK_COLORS[rankIndex];
    }
}
