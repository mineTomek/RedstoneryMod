package com.redstonery.item;

import java.util.List;

import com.redstonery.Redstonery;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RedstoneSelector extends Item {
    public RedstoneSelector(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (hand != Hand.MAIN_HAND) {
            return TypedActionResult.fail(player.getStackInHand(hand));
        }

        ItemStack handStack = player.getStackInHand(hand);
        if (player.isSneaking()) {
            handStack.getOrCreateNbt();

            if (handStack.getNbt().contains("redstonery.pos1")) {
                int[] pos1 = handStack.getNbt().getIntArray("redstonery.pos1");
                player.sendMessage(Text.of(String.join(", ", ((Integer) pos1[0]).toString(),
                        ((Integer) pos1[1]).toString(), ((Integer) pos1[2]).toString())));
            }
            if (handStack.getNbt().contains("redstonery.pos2")) {
                int[] pos2 = handStack.getNbt().getIntArray("redstonery.pos2");
                player.sendMessage(Text.of(String.join(", ", ((Integer) pos2[0]).toString(),
                        ((Integer) pos2[1]).toString(), ((Integer) pos2[2]).toString())));
            }

            return TypedActionResult.success(handStack, false);
        }

        player.playSound(SoundEvents.BLOCK_WOOL_BREAK, 1.0F, 1.0F);

        handStack.getOrCreateNbt().putIntArray("redstonery.pos1",
                new int[] { player.getBlockPos().getX(), player.getBlockPos().getY(), player.getBlockPos().getZ() });

        return TypedActionResult.success(handStack, false);
    }

    @Override
    public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
        Redstonery.LOGGER.info("canMine()!");

        miner.getStackInHand(Hand.MAIN_HAND).getOrCreateNbt().putIntArray("redstonery.pos2",
                new int[] { miner.getBlockPos().getX(), miner.getBlockPos().getY(), miner.getBlockPos().getZ() });

        return true;
    }
}
