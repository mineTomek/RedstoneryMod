package com.redstonery.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.redstonery.Redstonery;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
	@Inject(at = @At("HEAD"), method = "isBlockBreakingRestricted", cancellable = true)
	private void isBlockBreakingRestrictedInject(CallbackInfoReturnable<Boolean> info) {
		if (((PlayerEntity) (Object) this).getStackInHand(Hand.MAIN_HAND).getItem() == Redstonery.REDSTONE_SELECTOR) {
			info.setReturnValue(true);
		}
	}

	@Inject(at = @At("HEAD"), method = "shouldCancelInteraction", cancellable = true)
	private void shouldCancelInteractionInject(CallbackInfoReturnable<Boolean> info) {
		if (((PlayerEntity) (Object) this).getStackInHand(Hand.MAIN_HAND).getItem() == Redstonery.REDSTONE_SELECTOR) {
			info.setReturnValue(true);
		}
	}
}