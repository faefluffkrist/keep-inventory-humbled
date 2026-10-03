package com.faefluffkrist.humbled.mixin;

import com.faefluffkrist.humbled.SplashDamage;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.EvokerFangs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Ownerless/command-summoned fangs otherwise lose their identity in vanilla magic damage. */
@Mixin(EvokerFangs.class)
public abstract class EvokerFangsMixin {
 @Redirect(method="dealDamageTo",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)V"))
 private void humbled$ownerlessFangs(LivingEntity target,DamageSource source,float amount) {
  LivingEntity previous=SplashDamage.tickTarget();
  SplashDamage.tickTarget(target);
  try { target.hurt(source,amount); }
  finally { SplashDamage.tickTarget(previous); }
 }
}
