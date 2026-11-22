package io.github.vivegass.invfull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** config/invfull.json — edited by hand or via Mod Menu (Cloth Config). */
public final class InvFullConfig {
	public static final class Alert {
		public boolean enabled = true;
		public boolean showToast = true;
		public boolean playSound = true;
		/** Any sound event id: vanilla, from a mod or from a resource pack. */
		public String sound;
		public float volume;
		public float pitch;

		Alert(String sound, float volume, float pitch) {
			this.sound = sound;
			this.volume = volume;
			this.pitch = pitch;
		}
	}

	public static final String DEFAULT_FULL_SOUND = "minecraft:block.note_block.pling";
	public static final float DEFAULT_FULL_VOLUME = 0.8f, DEFAULT_FULL_PITCH = 1.2f;
	public static final String DEFAULT_DURABILITY_SOUND = "minecraft:block.anvil.land";
	public static final float DEFAULT_DURABILITY_VOLUME = 0.45f, DEFAULT_DURABILITY_PITCH = 1.6f;

	public Alert inventoryFull = new Alert(DEFAULT_FULL_SOUND, DEFAULT_FULL_VOLUME, DEFAULT_FULL_PITCH);
	public Alert durability = new Alert(DEFAULT_DURABILITY_SOUND, DEFAULT_DURABILITY_VOLUME, DEFAULT_DURABILITY_PITCH);
	/** Warn when remaining durability drops to this percent or lower. */
	public int durabilityPercent = 10;
	public boolean checkOffhand = true;
	/** How long a notification stays on screen, seconds. */
	public int toastSeconds = 4;

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("invfull.json");
	private static InvFullConfig instance = new InvFullConfig();

	public static InvFullConfig get() {
		return instance;
	}

	public static void load() {
		if (Files.exists(PATH)) {
			try (Reader r = Files.newBufferedReader(PATH, StandardCharsets.UTF_8)) {
				InvFullConfig c = GSON.fromJson(r, InvFullConfig.class);
				if (c != null) instance = c.sanitize();
			} catch (Exception e) {
				InvFullClient.LOGGER.error("Failed to read {}, using defaults", PATH, e);
			}
		}
		save(); // write defaults / fill in fields added by newer versions
	}

	public static void save() {
		try {
			Files.createDirectories(PATH.getParent());
			try (Writer w = Files.newBufferedWriter(PATH, StandardCharsets.UTF_8)) {
				GSON.toJson(instance, w);
			}
		} catch (IOException e) {
			InvFullClient.LOGGER.error("Failed to write {}", PATH, e);
		}
	}

	private InvFullConfig sanitize() {
		if (inventoryFull == null) inventoryFull = new Alert(DEFAULT_FULL_SOUND, DEFAULT_FULL_VOLUME, DEFAULT_FULL_PITCH);
		if (durability == null) durability = new Alert(DEFAULT_DURABILITY_SOUND, DEFAULT_DURABILITY_VOLUME, DEFAULT_DURABILITY_PITCH);
		if (inventoryFull.sound == null) inventoryFull.sound = DEFAULT_FULL_SOUND;
		if (durability.sound == null) durability.sound = DEFAULT_DURABILITY_SOUND;
		durabilityPercent = Math.clamp(durabilityPercent, 1, 50);
		toastSeconds = Math.clamp(toastSeconds, 1, 15);
		return this;
	}
}
