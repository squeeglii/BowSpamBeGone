package me.cg360.mod.bowspambegone.mixin;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BowItem.class)
public abstract class BowMixin extends Item {

    @Unique
    private boolean bowspambegone$hasGivenFullChargeFeedback = false;

    public BowMixin(Properties properties) {
        super(properties);
    }

    @Shadow public abstract int getUseDuration(ItemStack stack);

    @Inject(method = "releaseUsing(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V",
            at = @At("HEAD"),
            cancellable = true)
    public void forceBowShotsToBeFullPower(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged, CallbackInfo ci) {
        this.bowspambegone$hasGivenFullChargeFeedback = false;
        float power = BowItem.getPowerForTime(this.getUseDuration(stack) - timeCharged);

        if(power < 1.0f) {
            ci.cancel();
        }

    }


    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if(!level.isClientSide)
            return;

        float power = BowItem.getPowerForTime(this.getUseDuration(stack) - remainingUseDuration);

        if(power < 1.0f || this.bowspambegone$hasGivenFullChargeFeedback)
            return;

        this.bowspambegone$hasGivenFullChargeFeedback = true;

        level.playLocalSound(
                livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(),
                SoundEvents.NOTE_BLOCK_SNARE.value(), SoundSource.PLAYERS,
                0.1f, 1.2f, false);
    }
}
