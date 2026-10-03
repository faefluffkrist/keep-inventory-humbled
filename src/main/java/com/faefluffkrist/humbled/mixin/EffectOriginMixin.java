package com.faefluffkrist.humbled.mixin;

import com.faefluffkrist.humbled.EffectOrigin;
import com.faefluffkrist.humbled.SplashDamage;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEffectInstance.class)
public abstract class EffectOriginMixin implements EffectOrigin {
 @Shadow private MobEffectInstance hiddenEffect;
 @Unique private boolean humbled$splash;
 @Unique private boolean humbled$replaceOrigin;
 @Override public boolean humbled$fromSplash() { return humbled$splash; }
 @Override public void humbled$fromSplash(boolean value) { humbled$splash=value; }
 @Override public List<Boolean> humbled$origins() {
  List<Boolean> values=new ArrayList<>(); values.add(humbled$splash);
  if(hiddenEffect!=null) values.addAll(((EffectOrigin)(Object)hiddenEffect).humbled$origins());
  return values;
 }
 @Override public void humbled$origins(List<Boolean> values) {
  humbled$splash=!values.isEmpty() && values.getFirst();
  if(hiddenEffect!=null) ((EffectOrigin)(Object)hiddenEffect).humbled$origins(values.size()>1?values.subList(1,values.size()):List.of());
 }
 @Inject(method="setDetailsFrom",at=@At("TAIL"))
 private void humbled$copyOrigin(MobEffectInstance other,CallbackInfo ci) {
  humbled$splash=((EffectOrigin)(Object)other).humbled$fromSplash();
 }
 @Inject(method="update",at=@At("HEAD"))
 private void humbled$beforeUpdate(MobEffectInstance other,CallbackInfoReturnable<Boolean> cir) {
  MobEffectInstance self=(MobEffectInstance)(Object)this;
  humbled$replaceOrigin=other.getAmplifier()>self.getAmplifier()
   || (other.getAmplifier()==self.getAmplifier() && !self.isInfiniteDuration()
       && (other.isInfiniteDuration() || other.getDuration()>self.getDuration()));
 }
 @Inject(method="update",at=@At("RETURN"))
 private void humbled$afterUpdate(MobEffectInstance other,CallbackInfoReturnable<Boolean> cir) {
  if(humbled$replaceOrigin) humbled$splash=((EffectOrigin)(Object)other).humbled$fromSplash();
 }
 @Redirect(method="tickServer",at=@At(value="INVOKE",target="Lnet/minecraft/world/effect/MobEffect;applyEffectTick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;I)Z"))
 private boolean humbled$effectDamage(MobEffect effect,ServerLevel level,LivingEntity target,int amplifier) {
  LivingEntity previous=SplashDamage.tickTarget();
  SplashDamage.tickTarget(humbled$splash?target:null);
  try { return effect.applyEffectTick(level,target,amplifier); }
  finally { SplashDamage.tickTarget(previous); }
 }
}
