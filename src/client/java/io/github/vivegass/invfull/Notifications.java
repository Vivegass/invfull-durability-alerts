package io.github.vivegass.invfull;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Small vanilla-tooltip-styled notices stacked in the bottom-right corner. */
public final class Notifications {
	private static final long SLIDE_MS = 250;
	private static final int MARGIN = 6;
	private static final int ICON = 16;
	private static final int GAP = 6;
	private static final int SPACING = 8; // between stacked notices
	private static final int MAX_TEXT_W = 160;

	private static final class Notice {
		final String key;
		ItemStack icon;
		Component title, subtitle;
		int titleColor;
		long shownAt, hideAt;

		Notice(String key) { this.key = key; }
	}

	private static final List<Notice> notices = new ArrayList<>();

	private Notifications() {}

	/** Show (or refresh) a notice. A notice with the same key is replaced, not duplicated. */
	public static void show(String key, ItemStack icon, Component title, Component subtitle, int titleColor) {
		long now = Util.getMillis();
		Notice n = find(key);
		if (n == null) {
			n = new Notice(key);
			n.shownAt = now;
			notices.add(n);
		} else if (now > n.hideAt) {
			n.shownAt = now; // was sliding out — slide in again
		}
		n.icon = icon.copy();
		n.title = title;
		n.subtitle = subtitle;
		n.titleColor = titleColor;
		n.hideAt = Math.max(now, n.shownAt + SLIDE_MS) + InvFullConfig.get().toastSeconds * 1000L;
	}

	/** Slide a notice out early (condition no longer true). */
	public static void hide(String key) {
		Notice n = find(key);
		if (n == null) return;
		long now = Util.getMillis();
		if (now < n.hideAt) n.hideAt = Math.max(now, n.shownAt + SLIDE_MS);
	}

	public static void clear() {
		notices.clear();
	}

	public static void playSound(InvFullConfig.Alert alert) {
		if (!alert.playSound || alert.sound == null || alert.sound.isBlank()) return;
		Identifier id = Identifier.tryParse(alert.sound.trim());
		if (id == null) {
			InvFullClient.LOGGER.warn("Invalid sound id in config: {}", alert.sound);
			return;
		}
		SoundEvent event = SoundEvent.createVariableRangeEvent(id);
		Minecraft.getInstance().getSoundManager().play(
				SimpleSoundInstance.forUI(event, Mth.clamp(alert.pitch, 0.5f, 2f), Mth.clamp(alert.volume, 0f, 1f)));
	}

	private static Notice find(String key) {
		for (Notice n : notices) if (n.key.equals(key)) return n;
		return null;
	}

	static void extract(GuiGraphicsExtractor g, DeltaTracker delta) {
		if (notices.isEmpty()) return;
		long now = Util.getMillis();
		notices.removeIf(n -> now >= n.hideAt + SLIDE_MS);

		Font font = Minecraft.getInstance().font;
		int padX = TooltipRenderUtil.PADDING_LEFT;
		int padY = TooltipRenderUtil.PADDING_TOP;
		int h = font.lineHeight * 2 + 1;
		int bottom = g.guiHeight() - MARGIN - padY;

		for (Notice n : notices) {
			float t;
			if (now < n.shownAt + SLIDE_MS) t = (now - n.shownAt) / (float) SLIDE_MS;
			else if (now > n.hideAt) t = 1f - (now - n.hideAt) / (float) SLIDE_MS;
			else t = 1f;
			t = Mth.clamp(t, 0f, 1f);
			float eased = 1f - (1f - t) * (1f - t) * (1f - t);

			String sub = font.plainSubstrByWidth(n.subtitle.getString(), MAX_TEXT_W);
			int textW = Math.min(MAX_TEXT_W, Math.max(font.width(n.title), font.width(sub)));
			int w = ICON + GAP + textW;

			int restX = g.guiWidth() - MARGIN - padX - w;
			int hiddenX = g.guiWidth() + padX + 2;
			int x = Math.round(Mth.lerp(eased, hiddenX, restX));
			int y = bottom - h;

			TooltipRenderUtil.extractTooltipBackground(g, x, y, w, h, null);
			g.item(n.icon, x, y + (h - ICON) / 2);
			int tx = x + ICON + GAP;
			g.text(font, n.title, tx, y, n.titleColor, true);
			g.text(font, sub, tx, y + font.lineHeight + 1, 0xFFA8A8A8, true);

			// Next notice goes above this one; the gap collapses smoothly while this one slides out.
			bottom -= Math.round((h + padY * 2 + SPACING) * Math.min(1f, eased * 1.5f));
		}
	}
}
