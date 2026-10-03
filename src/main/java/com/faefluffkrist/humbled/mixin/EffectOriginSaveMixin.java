package com.faefluffkrist.humbled.mixin;

import com.faefluffkrist.humbled.EffectOrigin;
import com.mojang.serialization.Codec;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Saves the origin alongside vanilla effects, without changing their codec or behavior. */
@Mixin(LivingEntity.class)
public abstract class EffectOriginSaveMixin {
 @Unique private static final Codec<Map<String,List<Boolean>>> humbled$originCodec=Codec.unboundedMap(Codec.STRING,Codec.BOOL.listOf());
 @Inject(method="addAdditionalSaveData",at=@At("TAIL"))
 private void humbled$saveOrigins(ValueOutput output,CallbackInfo ci) {
  Map<String,List<Boolean>> origins=new LinkedHashMap<>();
  for(var effect:((LivingEntity)(Object)this).getActiveEffects()) {
   List<Boolean> layers=((EffectOrigin)(Object)effect).humbled$origins();
   if(layers.contains(true)) origins.put(BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value()).toString(),layers);
  }
  if(!origins.isEmpty()) output.store("humbled_splash_origins",humbled$originCodec,origins);
 }
 @Inject(method="readAdditionalSaveData",at=@At("TAIL"))
 private void humbled$loadOrigins(ValueInput input,CallbackInfo ci) {
  var origins=input.read("humbled_splash_origins",humbled$originCodec).orElse(Map.of());
  for(var effect:((LivingEntity)(Object)this).getActiveEffects()) {
   String id=BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value()).toString();
   ((EffectOrigin)(Object)effect).humbled$origins(origins.getOrDefault(id,List.of()));
  }
 }
}
