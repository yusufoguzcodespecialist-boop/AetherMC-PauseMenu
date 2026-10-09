package net.aethermc.pausemenu.mixin;

import net.aethermc.pausemenu.AetherPauseMenuMod;
import net.aethermc.pausemenu.RankReader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {

    private static final int PANEL_W = 170;
    private static final int PANEL_H = 220;
    private static final int BTN_W = 150;
    private static final int BTN_H = 20;
    private static final int BTN_GAP = 4;
    private static final int PLAYER_W = 160;
    private static final int PLAYER_H = 220;
    private static final int PANEL_GAP = 16;

    private static final int C_BG = 0xD0050810;
    private static final int C_PANEL = 0xE0080F1E;
    private static final int C_BORDER_HI = 0xFF63C0FF;
    private static final int C_BORDER_LO = 0xFF1860D0;
    private static final int C_INNER = 0xFF091830;
    private static final int C_TITLE_TOP = 0xFF7FE9FF;
    private static final int C_WHITE = 0xFFFFFFFF;
    private static final int C_GRAY = 0xFF888888;

    // Keep a bad preview render from taking down the entire game screen.
    private boolean aether$playerPreviewFailed;

    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    private int leftPanelX() {
        return (this.width - (PANEL_W + PANEL_GAP + PLAYER_W)) / 2;
    }

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void aether$init(CallbackInfo ci) {
        RankReader.update();
        this.clearChildren();

        int px = leftPanelX();
        int py = (this.height - PANEL_H) / 2;
        int bx = px + (PANEL_W - BTN_W) / 2;
        int by = py + 70;

        addDrawableChild(ButtonWidget.builder(
            Text.literal("Back to Game"),
            b -> this.client.setScreen(null))
            .dimensions(bx, by, BTN_W, BTN_H).build());
        by += BTN_H + BTN_GAP;

        addDrawableChild(ButtonWidget.builder(
            Text.literal("Options..."),
            b -> this.client.setScreen(
                new OptionsScreen(this, this.client.options)))
            .dimensions(bx, by, BTN_W, BTN_H).build());
        by += BTN_H + BTN_GAP;

        addDrawableChild(ButtonWidget.builder(
            Text.literal("Disconnect"),
            b -> {
                MinecraftClient client = this.client;
                if (client == null) return;

                if (client.getNetworkHandler() != null) {
                    client.getNetworkHandler().getConnection().disconnect(Text.literal("Disconnected"));
                }
                client.setScreen(new TitleScreen());
            })
            .dimensions(bx, by, BTN_W, BTN_H).build());

        ci.cancel();
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void aether$render(DrawContext ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.client == null) {
            ci.cancel();
            return;
        }

        int px = leftPanelX();
        int py = (this.height - PANEL_H) / 2;
        int rx = px + PANEL_W + PANEL_GAP;
        int ry = (this.height - PLAYER_H) / 2;

        // Do not call Screen.renderBackground here: in 1.21.11 it triggers
        // the frame blur pass, and the game can throw "Can only blur once per frame"
        // when another screen/render hook already performed that pass.
        // A translucent fill gives the pause menu its dark overlay without blurring.
        ctx.fill(0, 0, this.width, this.height, C_BG);

        drawPanel(ctx, px, py, PANEL_W, PANEL_H);
        drawTitleBanner(ctx, px + 10, py + 8, PANEL_W - 20, 36);
        ctx.drawCenteredTextWithShadow(this.textRenderer,
            Text.literal("AetherMC"), px + PANEL_W / 2, py + 20, C_TITLE_TOP);

        drawPanel(ctx, rx, ry, PLAYER_W, PLAYER_H);

        String name = this.client.getSession().getUsername();
        ctx.drawCenteredTextWithShadow(this.textRenderer,
            Text.literal(name), rx + PLAYER_W / 2, ry + 8, C_WHITE);

        int rankColor = 0xFF000000 | AetherPauseMenuMod.currentRankColor;
        String rankName = AetherPauseMenuMod.currentRankName;
        int rw = Math.min(this.textRenderer.getWidth(rankName) + 10, PLAYER_W - 12);
        int rby = ry + 20;
        int rbx = rx + (PLAYER_W - rw) / 2;
        ctx.fill(rbx - 1, rby - 1, rbx + rw + 1, rby + 11, 0xFF000000);
        ctx.fill(rbx, rby, rbx + rw, rby + 10, 0xFF111520);
        ctx.fill(rbx, rby, rbx + rw, rby + 1, rankColor);
        ctx.fill(rbx, rby + 9, rbx + rw, rby + 10, rankColor);
        ctx.fill(rbx, rby, rbx + 1, rby + 10, rankColor);
        ctx.fill(rbx + rw - 1, rby, rbx + rw, rby + 10, rankColor);
        ctx.drawCenteredTextWithShadow(this.textRenderer,
            Text.literal(rankName), rx + PLAYER_W / 2, rby + 2, rankColor);

        AbstractClientPlayerEntity player = this.client.player;
        if (player != null && !this.aether$playerPreviewFailed) {
            // Use vanilla's 1.21.11 entity-in-GUI renderer. Keep preview failures
            // isolated so a renderer error does not crash the entire pause screen.
            int playerCenterX = rx + PLAYER_W / 2;
            int playerCenterY = ry + 132;
            try {
                InventoryScreen.drawEntity(
                    ctx,
                    playerCenterX, playerCenterY,
                    playerCenterX, playerCenterY,
                    42, 0.0625F, mouseX, mouseY, player
                );
            } catch (RuntimeException | LinkageError exception) {
                this.aether$playerPreviewFailed = true;
                AetherPauseMenuMod.LOGGER.error(
                    "Player preview failed in the AetherMC pause menu; disabling the preview for this screen.",
                    exception
                );
            }
        }

        ctx.drawCenteredTextWithShadow(this.textRenderer,
            Text.literal("AetherMC wishes you a good game!"),
            rx + PLAYER_W / 2, ry + PLAYER_H - 14, C_GRAY);

        super.render(ctx, mouseX, mouseY, delta);
        ci.cancel();
    }

    private void drawPanel(DrawContext ctx, int x, int y, int w, int h) {
        ctx.fill(x, y, x + w, y + h, 0xFF000000);
        ctx.fill(x + 1, y + 1, x + w - 1, y + 2, C_BORDER_HI);
        ctx.fill(x + 1, y + 1, x + 2, y + h - 1, C_BORDER_HI);
        ctx.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, C_BORDER_LO);
        ctx.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, C_BORDER_LO);
        ctx.fill(x + 2, y + 2, x + w - 2, y + h - 2, C_INNER);
        ctx.fill(x + 3, y + 3, x + w - 3, y + h - 3, C_PANEL);
        ctx.fill(x + 3, y + 3, x + w - 3, y + 4, 0xFF1A3060);
        drawGem(ctx, x, y + h / 2, 4);
        drawGem(ctx, x + w - 1, y + h / 2, 4);
        drawGem(ctx, x + w / 2, y, 4);
        drawGem(ctx, x + w / 2, y + h - 1, 4);
    }

    private void drawGem(DrawContext ctx, int cx, int cy, int r) {
        for (int dy = -r; dy <= r; dy++) {
            for (int dx = -r; dx <= r; dx++) {
                if (Math.abs(dx) + Math.abs(dy) <= r) {
                    float t = (dy + r) / (float) (2 * r);
                    boolean border = Math.abs(dx) + Math.abs(dy) == r;
                    int color = border ? 0xFF000000 : lerpColor(C_BORDER_HI, C_BORDER_LO, t);
                    ctx.fill(cx + dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
                }
            }
        }
    }

    private void drawTitleBanner(DrawContext ctx, int x, int y, int w, int h) {
        ctx.fill(x, y, x + w, y + h, 0xFF000000);
        ctx.fill(x + 1, y + 1, x + w - 1, y + h - 1, C_PANEL);
        ctx.fill(x + 1, y + 1, x + w - 1, y + 2, C_BORDER_HI);
        ctx.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, C_BORDER_LO);
        ctx.fill(x + 1, y + 1, x + 2, y + h - 1, C_BORDER_HI);
        ctx.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, C_BORDER_LO);
        drawGem(ctx, x + w / 2, y, 4);
        drawGem(ctx, x + w / 2, y + h - 1, 4);
        drawGem(ctx, x, y + h / 2, 4);
        drawGem(ctx, x + w - 1, y + h / 2, 4);
    }

    private static int lerpColor(int a, int b, float t) {
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        int r = (int) (ar + (br - ar) * t);
        int g = (int) (ag + (bg - ag) * t);
        int bl = (int) (ab + (bb - ab) * t);
        return 0xFF000000 | (r << 16) | (g << 8) | bl;
    }

}
