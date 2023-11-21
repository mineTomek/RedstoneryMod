package com.redstonery;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redstonery.command.RedstoneryCommand;
import com.redstonery.item.RedstoneSelector;

public class Redstonery implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("redstonery");

	public static final RedstoneSelector REDSTONE_SELECTOR = new RedstoneSelector(
			new FabricItemSettings().rarity(Rarity.EPIC).maxCount(1));

	@Override
	public void onInitialize() {
		Registry.register(Registries.ITEM, new Identifier("redstonery", "redstone_selector"), REDSTONE_SELECTOR);

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(content -> {
			content.add(REDSTONE_SELECTOR);
		});

		AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
			if (hand != Hand.MAIN_HAND
					|| player.getStackInHand(hand).getItem() != Redstonery.REDSTONE_SELECTOR
					|| player.getItemCooldownManager().isCoolingDown(REDSTONE_SELECTOR)) {
				return ActionResult.PASS;
			}

			LOGGER.info("Block " + world.getBlockState(pos).getBlock().getName() + " attacked by " + player.getName());

			((RedstoneSelector) player.getStackInHand(hand).getItem()).onSelect(player, pos, world);

			player.getStackInHand(hand).getOrCreateNbt().putIntArray("redstonery.pos2",
					new int[] { pos.getX(), pos.getY(), pos.getZ() });

			return ActionResult.FAIL;
		});

		CommandRegistrationCallback.EVENT
				.register((dispatcher, registryAccess, environment) -> RedstoneryCommand.register(dispatcher));
	}
}