package com.faefluffkrist.humbled;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;

public final class DeathRules {
 private DeathRules() { }
 public static boolean environmental(DamageSource source,Config c) {
  return environmental(source,c,null);
 }
 private static boolean environmental(DamageSource source,Config c,ServerPlayer target) {
  if (source==null) return false;
  if (c.hostileMagicRegular && SplashDamage.regularPenalty(source,target)) return false;
  String id=source.typeHolder().unwrapKey().map(k->k.identifier().toString()).orElse("");
  if (c.environmentalExcluded.contains(id)) return false;
  return c.environmentalTypes.contains(id) || (c.environmentalTag && source.is(DamageTypeTags.BYPASSES_ARMOR));
 }
 public static void apply(ServerPlayer p, ServerLevel level) {
  Config c=Humbled.config;
  DamageSnapshot state=(DamageSnapshot)p;
  double difficulty=c.difficulty(level.getDifficulty().getId());
  double armorRate=c.penalty(c.armorLoss,difficulty,c.scaleArmor);
  double threshold=c.penalty(c.breakAt,difficulty,c.scaleThreshold);
  if (environmental(state.humbled$deathSource(),c,p)) {
   armorRate *= switch(c.environmentalMode) {
    case REGULAR -> 1;
    case NONE -> 0;
    case REDUCED -> c.penalty(c.environmentalMultiplier,difficulty,c.scaleEnvironmental);
   };
  }
  // Disabling death wear leaves normal lethal-hit wear intact. Enabled replaces it.
  if (c.armorEnabled) state.humbled$restoreArmorBeforeLethalHit();
  var inv=p.getInventory();
  for (int slot=0;slot<inv.getContainerSize();slot++) {
   ItemStack stack=inv.getItem(slot);
   if (stack.isEmpty()) continue;
   boolean binding=EnchantmentHelper.has(stack,EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE);
   if ((c.honorVanishing && EnchantmentHelper.has(stack,EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP))
       || (binding && c.destroyNonDurableBindingItems && stack.getMaxDamage()<=0)) {
    inv.setItem(slot,ItemStack.EMPTY); continue;
   }
   if (c.armorEnabled && armorRate>0 && slot>=36 && slot<=39 && stack.isDamageableItem()) {
    int raw=(int)Math.ceil(stack.getMaxDamage()*armorRate);
    int penalty=raw;
    if (c.unbreakingEnabled) {
     if (c.unbreakingMode==Config.UnbreakingMode.VANILLA && !(c.difficultyEnabled && c.scaleUnbreaking)) {
      penalty=Math.max(0,Math.min(raw,EnchantmentHelper.processDurabilityChange(level,stack,raw)));
     } else {
      int unbreaking=0;
      for (var enchantment:stack.getEnchantments().keySet())
       if (enchantment.is(Enchantments.UNBREAKING)) unbreaking=stack.getEnchantments().getLevel(enchantment);
      double protection;
      if (c.unbreakingMode==Config.UnbreakingMode.CUSTOM) protection=c.unbreakingProtection(unbreaking);
      else {
       // Vanilla armor/non-armor binomial chance; deterministic when difficulty protection is scaled.
       boolean armorTag=stack.is(net.minecraft.tags.ItemTags.ARMOR_ENCHANTABLE);
       protection=unbreaking<=0 ? 0 : armorTag ? (2.0*unbreaking)/(5.0*unbreaking+5) : (double)unbreaking/(unbreaking+1);
      }
      protection=c.protection(protection,difficulty,c.scaleUnbreaking && c.difficultyEnabled);
      penalty=DeathMath.protectedDamage(raw,protection);
     }
    }
    int damage=(int)Math.min((long)stack.getMaxDamage(),(long)stack.getDamageValue()+penalty);
    if (damage>=stack.getMaxDamage() || (c.thresholdEnabled && DeathMath.breaks(stack.getMaxDamage(),damage,threshold))) {
     inv.setItem(slot,ItemStack.EMPTY); state.humbled$armorBroke(); continue;
    }
    stack.setDamageValue(damage);
   }
   boolean keep=slot<9 ? c.keepHotbar : slot<36 ? c.keepMainInventory : slot==40 ? c.keepOffhand : c.keepArmor;
   if (binding && c.keepBindingItems) keep=true;
   if (!keep) {
    ItemEntity dropped=new ItemEntity(level,p.getX(),p.getY(),p.getZ(),stack.copy());
    dropped.setDefaultPickUpDelay();
    if (level.addFreshEntity(dropped)) inv.setItem(slot,ItemStack.EMPTY);
   }
  }
  double before=p.experienceLevel+(double)p.experienceProgress;
  double lost=c.experienceLost(before,difficulty);
  double recoverable=c.recoveryEnabled ? lost*c.protection(c.recovery,difficulty,c.difficultyEnabled && c.scaleRecovery) : 0;
  double retained=before-lost;
  if (recoverable>0 && !Recovery.spawn(p,level,recoverable)) retained+=recoverable;
  // No loss means no conversion or rounding of existing XP.
  if (lost>0) setLevels(p,retained);
  inv.setChanged();
 }
 public static void setLevels(ServerPlayer p,double levels) {
  levels=Math.max(0,Math.min(Integer.MAX_VALUE-1.0,levels));
  p.experienceLevel=(int)Math.floor(levels);
  p.experienceProgress=(float)(levels-p.experienceLevel);
  p.totalExperience=DeathMath.totalPoints(p.experienceLevel,p.experienceProgress,p.getXpNeededForNextLevel());
  ((DamageSnapshot)p).humbled$syncExperience();
 }
}
