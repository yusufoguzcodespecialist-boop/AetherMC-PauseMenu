package net.aethermc.pausemenu.mixin;

import net.aethermc.pausemenu.AetherPauseMenuMod;
import net.aethermc.pausemenu.RankReader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.util.Identifier;
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
    private static final int PANEL_GAP = 12;
    private static final int PANEL_H = 270;
    private static final int PANEL_W = 240;
    private static final int PLAYER_W = 240;
    private static final int BTN_W = 210;
    private static final int BTN_H = 27;
    private static final int BTN_GAP = 8;

    private static final int C_OVERLAY = 0x00050A12;
    private static final int C_PANEL = 0xF0161D29;
    private static final int C_PANEL_INNER = 0xF00D131E;
    private static final int C_ACCENT = 0xFF35BFFF;
    private static final int C_ACCENT_LIGHT = 0xFF8BE4FF;
    private static final int C_ACCENT_DARK = 0xFF1764C0;
    private static final int C_WHITE = 0xFFF5F7FB;
    private static final int C_MUTED = 0xFFAAAEB8;

    private boolean aether$playerPreviewFailed;

    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    private int panelWidth() {
        return Math.min(PANEL_W, Math.max(170, (this.width - 36) / 2 - PANEL_GAP / 2));
    }

    private int playerWidth() {
        return panelWidth();
    }

    private int panelHeight() {
        return Math.min(PANEL_H, Math.max(210, this.height - 28));
    }

    private int totalWidth() {
        return panelWidth() + PANEL_GAP + playerWidth();
    }

    private int leftPanelX() {
        return Math.max(8, (this.width - totalWidth()) / 2);
    }

    private int panelY() {
        return Math.max(8, (this.height - panelHeight()) / 2);
    }

    private int buttonX() {
        return leftPanelX() + (panelWidth() - Math.min(BTN_W, panelWidth() - 24)) / 2;
    }

    private int buttonWidth() {
        return Math.min(BTN_W, panelWidth() - 24);
    }

    private int buttonY(int index) {
        return panelY() + 86 + index * (BTN_H + BTN_GAP);
    }

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void aether$init(CallbackInfo ci) {
        RankReader.update();
        this.clearChildren();
        int bx = buttonX();
        int bw = buttonWidth();

        addDrawableChild(ButtonWidget.builder(Text.literal("Back to Game"), b -> this.client.setScreen(null))
            .dimensions(bx, buttonY(0), bw, BTN_H).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("AetherMC Settings"), b ->
                this.client.setScreen(new OptionsScreen(this, this.client.options)))
            .dimensions(bx, buttonY(1), bw, BTN_H).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Disconnect"), b -> {
                MinecraftClient client = this.client;
                if (client == null) return;
                if (client.getNetworkHandler() != null) {
                    client.getNetworkHandler().getConnection().disconnect(Text.literal("Disconnected"));
                }
                client.setScreen(new TitleScreen());
            }).dimensions(bx, buttonY(2), bw, BTN_H).build());
        ci.cancel();
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void aether$render(DrawContext ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.client == null) {
            ci.cancel();
            return;
        }

        int pw = panelWidth();
        int ph = panelHeight();
        int px = leftPanelX();
        int py = panelY();
        int rx = px + pw + PANEL_GAP;
        int rw = playerWidth();
        int bottom = py + ph;

        ctx.fill(0, 0, this.width, this.height, C_OVERLAY);
        drawPanel(ctx, rx, py, rw, ph);

        // Clean text wordmark: avoids displaying the broken two-block texture.
        int logoCenterX = px + pw / 2;
        ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("AETHERMC"),
            logoCenterX, py + 14, C_ACCENT_LIGHT);
        ctx.fill(logoCenterX - 52, py + 34, logoCenterX + 52, py + 35, C_ACCENT_DARK);
        ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("MINECRAFT NETWORK"),
            logoCenterX, py + 40, C_ACCENT);

        super.render(ctx, mouseX, mouseY, delta);

        String name = this.client.getSession().getUsername();
        ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal(name),
            rx + rw / 2, py + 18, C_WHITE);
        ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("PLAYER PROFILE"),
            rx + rw / 2, py + 34, C_MUTED);

        String rankName = AetherPauseMenuMod.currentRankName;
        int rankColor = 0xFF000000 | AetherPauseMenuMod.currentRankColor;
        int rankTextWidth = this.textRenderer.getWidth(rankName);
        int badgeWidth = Math.min(rankTextWidth + 18, rw - 24);
        int badgeX = rx + (rw - badgeWidth) / 2;
        int badgeY = py + 48;
        ctx.fill(badgeX, badgeY, badgeX + badgeWidth, badgeY + 16, 0xFF080B10);
        ctx.fill(badgeX + 1, badgeY + 1, badgeX + badgeWidth - 1, badgeY + 15, 0xFF222936);
        ctx.fill(badgeX, badgeY, badgeX + badgeWidth, badgeY + 2, rankColor);
        ctx.fill(badgeX, badgeY + 14, badgeX + badgeWidth, badgeY + 16, rankColor);
        ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal(rankName),
            rx + rw / 2, badgeY + 4, rankColor);

        AbstractClientPlayerEntity player = this.client.player;
        if (player != null && !this.aether$playerPreviewFailed) {
            int centerX = rx + rw / 2;
            int centerY = py + Math.min(190, ph - 52);
            try {
                InventoryScreen.drawEntity(ctx, centerX, centerY, centerX, centerY,
                    Math.min(58, Math.max(36, ph / 5)), 0.0625F, mouseX, mouseY, player);
            } catch (RuntimeException | LinkageError exception) {
                this.aether$playerPreviewFailed = true;
                AetherPauseMenuMod.LOGGER.error("AetherMC player preview failed; disabling it for this screen.", exception);
            }
        }

        ctx.fill(rx + 16, bottom - 31, rx + rw - 16, bottom - 30, 0xFF343B48);
        ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("AETHERMC NETWORK"),
            rx + rw / 2, bottom - 22, C_ACCENT_LIGHT);
        ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Have a great game!"),
            rx + rw / 2, bottom - 11, C_MUTED);
        ci.cancel();
    }

    private void drawPanel(DrawContext ctx, int x, int y, int w, int h) {
        ctx.fill(x, y, x + w, y + h, 0xFF080B11);
        ctx.fill(x + 1, y + 1, x + w - 1, y + h - 1, C_PANEL);
        ctx.fill(x + 2, y + 2, x + w - 2, y + h - 2, C_PANEL_INNER);
        ctx.fill(x + 2, y + 2, x + w - 2, y + 3, C_ACCENT);
        ctx.fill(x + 2, y + h - 3, x + w - 2, y + h - 2, C_ACCENT_DARK);
        ctx.fill(x + 2, y + 2, x + 3, y + h - 2, C_ACCENT_DARK);
        ctx.fill(x + w - 3, y + 2, x + w - 2, y + h - 2, C_ACCENT_DARK);
    }
}
