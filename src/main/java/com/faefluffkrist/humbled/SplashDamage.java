package com.faefluffkrist.humbled;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;

/** Scoped to the actual effect tick; unrelated damage while poisoned is never reclassified. */
public final class SplashDamage {
 private SplashDamage() { }
 private static final ThreadLocal<LivingEntity> TICK_TARGET=new ThreadLocal<>();
 public static LivingEntity tickTarget() { return TICK_TARGET.get(); }
 public static void tickTarget(LivingEntity target) {
  if (target==null) TICK_TARGET.remove(); else TICK_TARGET.set(target);
 }
 public static boolean regularPenalty(DamageSource source,LivingEntity target) {
  return source.getDirectEntity() instanceof EvokerFangs
   || source.getDirectEntity() instanceof ThrownSplashPotion
   || (target!=null && TICK_TARGET.get()==target
       && (source.is(DamageTypes.MAGIC) || source.is(DamageTypes.WITHER) || source.is(DamageTypes.INDIRECT_MAGIC)));
 }
}
