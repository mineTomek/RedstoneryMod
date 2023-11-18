package com.redstonery;

import java.awt.Color;

import me.x150.renderer.event.RenderEvents;
import me.x150.renderer.render.Renderer3d;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class RedstoneryClient implements ClientModInitializer {
	Vec3d pos1 = null;
	Vec3d pos2 = null;

	@Override
	public void onInitializeClient() {
		RenderEvents.WORLD.register(matrixStack -> {
			if (pos1 != null && pos2 != null) {
				Renderer3d.renderOutline(matrixStack, Color.WHITE,
						new Vec3d(Math.min(pos1.x, pos2.x), Math.min(pos1.y, pos2.y), Math.min(pos1.z, pos2.z)),
						new Vec3d(Math.abs(pos1.getX() - pos2.getX()), Math.abs(pos1.getY() - pos2.getY()),
								Math.abs(pos1.getZ() - pos2.getZ())));
			}
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) {
				return;
			}

			ItemStack stack = client.player.getStackInHand(Hand.MAIN_HAND);

			if (stack.getItem() == Redstonery.REDSTONE_SELECTOR) {
				stack.getOrCreateNbt();

				if (stack.getNbt().contains("redstonery.pos1") && stack.getNbt().contains("redstonery.pos2")) {
					int[] nbtPos1 = stack.getNbt().getIntArray("redstonery.pos1");
					int[] nbtPos2 = stack.getNbt().getIntArray("redstonery.pos2");

					pos1 = new Vec3d(nbtPos1[0], nbtPos1[1], nbtPos1[2]);
					pos2 = new Vec3d(nbtPos2[0], nbtPos2[1], nbtPos2[2]);
				}
			}
		});
	}
}
