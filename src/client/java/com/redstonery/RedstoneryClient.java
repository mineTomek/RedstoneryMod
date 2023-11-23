package com.redstonery;

import java.awt.Color;

import me.x150.renderer.event.RenderEvents;
import me.x150.renderer.render.Renderer3d;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public class RedstoneryClient implements ClientModInitializer {
	Vec3d pos1 = null;
	Vec3d pos2 = null;

	double cornersSizeMargin = 1 * Math.pow(10, -3);
	double mainSelectionSizeMargin = .5 * Math.pow(10, -2);

	@Override
	public void onInitializeClient() {
		RenderEvents.WORLD.register(matrixStack -> {
			MinecraftClient client = MinecraftClient.getInstance();

			if (client.player.isSneaking()) {
				Renderer3d.renderThroughWalls();
			}

			if (pos1 != null && pos2 != null) {
				Renderer3d.renderOutline(matrixStack, Color.WHITE,
						new Vec3d(Math.min(pos1.x, pos2.x), Math.min(pos1.y, pos2.y), Math.min(pos1.z, pos2.z))
								.subtract(mainSelectionSizeMargin / 2, mainSelectionSizeMargin / 2,
										mainSelectionSizeMargin / 2),
						new Vec3d(Math.abs(pos1.getX() - pos2.getX()), Math.abs(pos1.getY() - pos2.getY()),
								Math.abs(pos1.getZ() - pos2.getZ()))
								.add(1 + mainSelectionSizeMargin, 1 + mainSelectionSizeMargin,
										1 + mainSelectionSizeMargin));
			}

			if (pos1 != null) {
				Renderer3d.renderEdged(matrixStack, Renderer3d.modifyColor(Color.BLUE, -1, -1, -1, 64), Color.BLUE,
						pos1.subtract(cornersSizeMargin / 2, cornersSizeMargin / 2, cornersSizeMargin / 2),
						new Vec3d(1 + cornersSizeMargin, 1 + cornersSizeMargin, 1 + cornersSizeMargin));
			}

			if (pos2 != null) {
				Renderer3d.renderEdged(matrixStack, Renderer3d.modifyColor(Color.RED, -1, -1, -1, 64), Color.RED,
						pos2.subtract(cornersSizeMargin / 2, cornersSizeMargin / 2, cornersSizeMargin / 2),
						new Vec3d(1 + cornersSizeMargin, 1 + cornersSizeMargin, 1 + cornersSizeMargin));
			}

			if (client.player.isSneaking()) {
				Renderer3d.stopRenderThroughWalls();
			}
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) {
				return;
			}

			ItemStack stack = client.player.getStackInHand(Hand.MAIN_HAND);

			if (stack.getItem() == Redstonery.REDSTONE_SELECTOR) {
				stack.getOrCreateNbt();

				if (stack.getNbt().contains("redstonery.pos1")) {
					int[] nbtPos1 = stack.getNbt().getIntArray("redstonery.pos1");

					pos1 = new Vec3d(nbtPos1[0], nbtPos1[1], nbtPos1[2]);
				}

				if (stack.getNbt().contains("redstonery.pos2")) {
					int[] nbtPos2 = stack.getNbt().getIntArray("redstonery.pos2");

					pos2 = new Vec3d(nbtPos2[0], nbtPos2[1], nbtPos2[2]);
				}
			} else {
				pos1 = null;
				pos2 = null;
			}
		});

		ModelPredicateProviderRegistry.register(Redstonery.REDSTONE_SELECTOR.asItem(), new Identifier("on-cooldown"),
				(itemStack, clientWorld, livingEntity, mysteriousInteger) -> {
					if (livingEntity == null) {
						return 0.0F;
					}
					return livingEntity.getMainHandStack() == itemStack
							&& ((PlayerEntity) livingEntity).getItemCooldownManager().isCoolingDown(itemStack.getItem())
									? 1.0F
									: 0.0F;
				});

	}
}
