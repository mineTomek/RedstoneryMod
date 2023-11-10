package com.redstonery;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redstonery.item.RedstoneSelector;

public class Redstonery implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("redstonery");

	public static final RedstoneSelector REDSTONE_SELECTOR = new RedstoneSelector(
			new FabricItemSettings().rarity(Rarity.EPIC).maxCount(1));

	@Override
	public void onInitialize() {
		LOGGER.info("Hello, Fabric world from Redstonery!");

		Registry.register(Registries.ITEM, new Identifier("redstonery", "redstone_selector"), REDSTONE_SELECTOR);

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(content -> {
			content.add(REDSTONE_SELECTOR);
		});
	}
}