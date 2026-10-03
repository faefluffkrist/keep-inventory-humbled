package com.faefluffkrist.humbled.mixin;

import com.faefluffkrist.humbled.EffectOrigin;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ThrownSplashPotion.class)
public abstract class SplashPotionMixin {
 @Redirect(method="onHitAsPotion",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z"))
 private boolean humbled$splashOrigin(LivingEntity target,MobEffectInstance effect,Entity source) {
  ((EffectOrigin)(Object)effect).humbled$fromSplash(true);
  return target.addEffect(effect,source);
 }
}
