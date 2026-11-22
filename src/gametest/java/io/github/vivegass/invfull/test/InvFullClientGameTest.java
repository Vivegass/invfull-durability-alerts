package io.github.vivegass.invfull.test;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * Visual smoke test: wears a pickaxe below the durability threshold and fills the inventory,
 * then takes a screenshot with both notifications on screen.
 * Run: ./gradlew :<version>:runClientGameTest  (screenshots land in the run directory)
 */
public class InvFullClientGameTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext ctx) {
		try (TestSingleplayerContext sp = ctx.worldBuilder().create()) {
			ctx.waitTicks(100);
			sp.getServer().runCommand("gamemode survival @a");
			ctx.waitTicks(20);
			// 1561 max durability, 10% threshold = 157 uses left -> damage 1300 is fine, 1500 is low
			sp.getServer().runCommand("item replace entity @a weapon.mainhand with minecraft:diamond_pickaxe[damage=1300]");
			ctx.waitTicks(20);
			sp.getServer().runCommand("item replace entity @a weapon.mainhand with minecraft:diamond_pickaxe[damage=1500]");
			ctx.waitTicks(10);
			sp.getServer().runCommand("give @a minecraft:dirt 2240");
			ctx.waitTicks(15);
			System.out.println("[invfull-test] screenshot: " + ctx.takeScreenshot("invfull-notifications"));
		}
	}
}
