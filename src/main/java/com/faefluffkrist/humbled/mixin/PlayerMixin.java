package com.faefluffkrist.humbled.mixin;
import com.faefluffkrist.humbled.DeathRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Player.class)
public abstract class PlayerMixin {
 @Unique private boolean humbled$deathProcessed;
 @Inject(method="dropEquipment", at=@At("HEAD"), cancellable=true)
 private void humbled$equipment(ServerLevel level, CallbackInfo ci) {
  if ((Object)this instanceof ServerPlayer p && !p.isSpectator()) {
   ci.cancel();
   if (!humbled$deathProcessed) { humbled$deathProcessed = true; DeathRules.apply(p, level); }
  }
 }
 @Inject(method="getBaseExperienceReward", at=@At("HEAD"), cancellable=true)
 private void humbled$noOrbs(ServerLevel level, CallbackInfoReturnable<Integer> cir) {
  if ((Object)this instanceof ServerPlayer) cir.setReturnValue(0);
 }
}
