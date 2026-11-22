package io.github.vivegass.invfull;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** Cloth Config screen, opened from Mod Menu. */
final class ConfigScreen {
	private ConfigScreen() {}

	static Screen create(Screen parent) {
		InvFullConfig cfg = InvFullConfig.get();
		ConfigBuilder builder = ConfigBuilder.create()
				.setParentScreen(parent)
				.setTitle(Component.translatable("invfull.config.title"))
				.setSavingRunnable(InvFullConfig::save);
		ConfigEntryBuilder eb = builder.entryBuilder();
		ConfigCategory general = builder.getOrCreateCategory(Component.translatable("invfull.config.general"));

		List<String> sounds = BuiltInRegistries.SOUND_EVENT.keySet().stream()
				.map(Identifier::toString).sorted().toList();

		general.addEntry(eb.startIntSlider(Component.translatable("invfull.config.toast_seconds"), cfg.toastSeconds, 1, 15)
				.setDefaultValue(4)
				.setTextGetter(v -> Component.translatable("invfull.config.seconds", v))
				.setSaveConsumer(v -> cfg.toastSeconds = v).build());

		general.addEntry(alertSection(eb, "invfull.config.full", cfg.inventoryFull, sounds,
				InvFullConfig.DEFAULT_FULL_SOUND, InvFullConfig.DEFAULT_FULL_VOLUME, InvFullConfig.DEFAULT_FULL_PITCH, null).build());
		general.addEntry(alertSection(eb, "invfull.config.durability", cfg.durability, sounds,
				InvFullConfig.DEFAULT_DURABILITY_SOUND, InvFullConfig.DEFAULT_DURABILITY_VOLUME, InvFullConfig.DEFAULT_DURABILITY_PITCH, cfg).build());

		return builder.build();
	}

	private static SubCategoryBuilder alertSection(ConfigEntryBuilder eb, String key, InvFullConfig.Alert a,
			List<String> sounds, String defSound, float defVol, float defPitch, InvFullConfig durabilityCfg) {
		SubCategoryBuilder s = eb.startSubCategory(Component.translatable(key)).setExpanded(true);
		s.add(eb.startBooleanToggle(Component.translatable("invfull.config.enabled"), a.enabled)
				.setDefaultValue(true).setSaveConsumer(v -> a.enabled = v).build());
		if (durabilityCfg != null) {
			s.add(eb.startIntSlider(Component.translatable("invfull.config.durability_percent"), durabilityCfg.durabilityPercent, 1, 50)
					.setDefaultValue(10)
					.setTextGetter(v -> Component.literal(v + "%"))
					.setSaveConsumer(v -> durabilityCfg.durabilityPercent = v).build());
			s.add(eb.startBooleanToggle(Component.translatable("invfull.config.offhand"), durabilityCfg.checkOffhand)
					.setDefaultValue(true).setSaveConsumer(v -> durabilityCfg.checkOffhand = v).build());
		}
		s.add(eb.startBooleanToggle(Component.translatable("invfull.config.show_toast"), a.showToast)
				.setDefaultValue(true).setSaveConsumer(v -> a.showToast = v).build());
		s.add(eb.startBooleanToggle(Component.translatable("invfull.config.play_sound"), a.playSound)
				.setDefaultValue(true).setSaveConsumer(v -> a.playSound = v).build());
		s.add(eb.startStringDropdownMenu(Component.translatable("invfull.config.sound"), a.sound)
				.setSelections(sounds)
				.setSuggestionMode(true) // also accepts ids not in the list (resource-pack sounds)
				.setDefaultValue(defSound)
				.setTooltip(Component.translatable("invfull.config.sound.tooltip"))
				.setErrorSupplier(v -> Identifier.tryParse(v.trim()) == null
						? Optional.of(Component.translatable("invfull.config.sound.invalid"))
						: Optional.empty())
				.setSaveConsumer(v -> a.sound = v.trim()).build());
		s.add(eb.startIntSlider(Component.translatable("invfull.config.volume"), Math.round(a.volume * 100), 0, 100)
				.setDefaultValue(Math.round(defVol * 100))
				.setTextGetter(v -> Component.literal(v + "%"))
				.setSaveConsumer(v -> a.volume = v / 100f).build());
		s.add(eb.startIntSlider(Component.translatable("invfull.config.pitch"), Math.round(a.pitch * 100), 50, 200)
				.setDefaultValue(Math.round(defPitch * 100))
				.setTextGetter(v -> Component.literal(String.format(Locale.ROOT, "%.2f", v / 100f)))
				.setSaveConsumer(v -> a.pitch = v / 100f).build());
		return s;
	}
}
