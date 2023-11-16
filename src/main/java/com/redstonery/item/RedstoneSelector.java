package com.redstonery.item;

import com.redstonery.Redstonery;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RedstoneSelector extends Item {
    public RedstoneSelector(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getHand() != Hand.MAIN_HAND) {
            return ActionResult.PASS;
        }

        ItemStack handStack = context.getStack();

        PlayerEntity player = context.getPlayer();

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

            return ActionResult.SUCCESS;
        }

        if (player.getItemCooldownManager().isCoolingDown(this)) {
            return ActionResult.PASS;
        }

        BlockPos targetBlock = context.getBlockPos();

        onSelect(player, targetBlock, context.getWorld());

        handStack.getOrCreateNbt().putIntArray("redstonery.pos1",
                new int[] { targetBlock.getX(), targetBlock.getY(), targetBlock.getZ() });

        return ActionResult.SUCCESS;
    }

    public void onSelect(PlayerEntity player, BlockPos pos, World world) {
        Redstonery.LOGGER.info("indicating selection");

        player.playSound(world.getBlockState(pos).getSoundGroup().getBreakSound(), 1.0F, 1.0F);

        player.getItemCooldownManager().set(this, 10);

        world.addParticle(ParticleTypes.CLOUD, pos.getX() + .5, pos.getY() + 1,
                pos.getZ() + .6, 0, .1,
                0);
    }

    // @Override
    // public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot,
    // ClickType clickType, PlayerEntity player,
    // StackReference cursorStackReference) {
    // Redstonery.LOGGER.info("onClicked: " + clickType.name());
    // return super.onClicked(stack, otherStack, slot, clickType, player,
    // cursorStackReference);
    // }
}
