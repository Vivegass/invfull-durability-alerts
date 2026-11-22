package io.github.vivegass.invfull;

import com.mojang.brigadier.Command;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
//? if >=26.1 {
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
//?} else {
/*import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
*///?}
//? if >=1.21.6 {
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
//?} else {
/*import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
*///?}
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvFullClient implements ClientModInitializer {
	public static final String MOD_ID = "invfull";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final String KEY_FULL = "full";
	private static final int FULL_COLOR = 0xFFFFFFFF;
	private static final int DURABILITY_COLOR = 0xFFFFAA55;

	/** Main inventory state on the previous tick; null = unknown (just joined a world). */
	private Boolean wasFull = null;
	private final HandWatch mainHand = new HandWatch("main");
	private final HandWatch offHand = new HandWatch("off");

	@Override
	public void onInitializeClient() {
		InvFullConfig.load();
		//? if >=1.21.6 {
		HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(MOD_ID, "notifications"), Notifications::extract);
		//?} else {
		/*HudRenderCallback.EVENT.register(Notifications::extract);
		*///?}
		ClientTickEvents.END_CLIENT_TICK.register(this::tick);
		registerCommands();
	}

	private void tick(Minecraft mc) {
		LocalPlayer player = mc.player;
		if (player == null) {
			reset();
			Notifications.clear();
			return;
		}
		// Creative/spectator: nothing fills up or breaks, no point in nagging.
		if (player.isCreative() || player.isSpectator()) {
			reset();
			return;
		}
		InvFullConfig cfg = InvFullConfig.get();
		tickInventory(player, cfg);
		//? if >=1.21.5 {
		int selected = player.getInventory().getSelectedSlot();
		//?} else {
		/*int selected = player.getInventory().selected;
		*///?}
		mainHand.tick(selected, player.getMainHandItem(), cfg);
		if (cfg.checkOffhand) offHand.tick(-1, player.getOffhandItem(), cfg);
	}

	private void reset() {
		wasFull = null;
		mainHand.reset();
		offHand.reset();
	}

	private void tickInventory(LocalPlayer player, InvFullConfig cfg) {
		boolean full = player.getInventory().getFreeSlot() == -1;
		if (wasFull == null) { // first tick in the world: remember silently
			wasFull = full;
			return;
		}
		if (full && !wasFull && cfg.inventoryFull.enabled) {
			alertFull(cfg);
		} else if (!full && wasFull) {
			Notifications.hide(KEY_FULL);
		}
		wasFull = full;
	}

	static void alertFull(InvFullConfig cfg) {
		if (cfg.inventoryFull.showToast) {
			Notifications.show(KEY_FULL, new ItemStack(Items.CHEST),
					Component.translatable("invfull.toast.title"),
					Component.translatable("invfull.toast.subtitle"), FULL_COLOR);
		}
		Notifications.playSound(cfg.inventoryFull);
	}

	static void alertDurability(String key, ItemStack stack, InvFullConfig cfg) {
		int left = stack.getMaxDamage() - stack.getDamageValue();
		if (cfg.durability.showToast) {
			Notifications.show(key, stack,
					Component.translatable("invfull.durability.title"),
					Component.translatable("invfull.durability.subtitle", stack.getHoverName(), left, stack.getMaxDamage()),
					DURABILITY_COLOR);
		}
		Notifications.playSound(cfg.durability);
	}

	static boolean isLow(ItemStack stack, InvFullConfig cfg) {
		if (stack.isEmpty() || !stack.isDamageableItem() || stack.getMaxDamage() <= 0) return false;
		int left = stack.getMaxDamage() - stack.getDamageValue();
		int threshold = Math.max(1, (int) Math.ceil(stack.getMaxDamage() * cfg.durabilityPercent / 100.0));
		return left <= threshold;
	}

	/**
	 * Warns once when the item held in a hand wears down past the low-durability threshold.
	 * Switching to an already worn item does not warn — only wearing it down does.
	 */
	private static final class HandWatch {
		private final String key;
		private int slot = Integer.MIN_VALUE;
		private Item item;
		private boolean low;

		HandWatch(String hand) { this.key = "durability_" + hand; }

		void reset() {
			slot = Integer.MIN_VALUE;
			item = null;
			low = false;
		}

		void tick(int currentSlot, ItemStack stack, InvFullConfig cfg) {
			boolean nowLow = isLow(stack, cfg);
			boolean sameItem = currentSlot == slot && item != null && stack.getItem() == item;
			if (sameItem && nowLow && !low && cfg.durability.enabled) {
				alertDurability(key, stack, cfg);
			}
			if (!sameItem || !nowLow) Notifications.hide(key);
			slot = currentSlot;
			item = stack.isEmpty() ? null : stack.getItem();
			low = nowLow;
		}
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> literal(String name) {
		//? if >=26.1 {
		return ClientCommands.literal(name);
		//?} else {
		/*return ClientCommandManager.literal(name);
		*///?}
	}

	private void registerCommands() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, ctx) -> dispatcher.register(
				literal("invfull")
						.then(literal("reload").executes(c -> {
							InvFullConfig.load();
							c.getSource().sendFeedback(Component.translatable("invfull.command.reloaded"));
							return Command.SINGLE_SUCCESS;
						}))
						.then(literal("test")
								.then(literal("full").executes(c -> {
									alertFull(InvFullConfig.get());
									return Command.SINGLE_SUCCESS;
								}))
								.then(literal("durability").executes(c -> {
									ItemStack held = c.getSource().getPlayer().getMainHandItem();
									ItemStack demo = held.isDamageableItem() ? held.copy() : new ItemStack(Items.DIAMOND_PICKAXE);
									if (!isLow(demo, InvFullConfig.get())) demo.setDamageValue(demo.getMaxDamage() * 95 / 100);
									alertDurability("durability_test", demo, InvFullConfig.get());
									return Command.SINGLE_SUCCESS;
								})))));
	}
}
