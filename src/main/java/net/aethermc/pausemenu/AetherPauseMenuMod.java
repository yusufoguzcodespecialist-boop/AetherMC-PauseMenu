package net.aethermc.pausemenu;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class AetherPauseMenuMod implements ClientModInitializer {
    public static final String MOD_ID = "aethermc_pausemenu";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Rank system: server sends rank via scoreboard objective "aetherrank"
    // Value 0=DEFAULT,1=JUNIOR_HELPER,2=HELPER,3=JUNIOR_MOD,4=MOD,5=BUILDER,
    //       6=DEVELOPER,7=MEDIA,8=VIP,9=MVP,10=CO-OWNER,11=OWNER
    public static int currentRank = 0;
    public static String currentRankName = "DEFAULT";
    public static int currentRankColor = 0xAAAAAA;

    public static final int[] RANK_COLORS = {
        0xAAAAAA, // DEFAULT
        0x1F9A2F, // JUNIOR HELPER
        0x7DFF6B, // HELPER
        0xD96A0A, // JUNIOR MOD
        0xFF9A1F, // MODERATOR
        0x4A8CFF, // BUILDER
        0x3DDC4A, // DEVELOPER
        0xFF3B3B, // MEDIA
        0xFFE04A, // VIP
        0xFFE04A, // MVP
        0xFF8A1F, // CO-OWNER
        0xFF3B3B, // OWNER
    };
    public static final String[] RANK_NAMES = {
        "DEFAULT","JUNIOR HELPER","HELPER","JUNIOR MOD",
        "MODERATOR","BUILDER","DEVELOPER","MEDIA",
        "VIP","MVP","CO-OWNER","OWNER"
    };

    @Override
    public void onInitializeClient() {
        LOGGER.info("AetherMC Pause Menu loaded!");
    }
}
