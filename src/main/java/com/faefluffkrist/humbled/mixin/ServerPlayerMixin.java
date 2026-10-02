package com.faefluffkrist.humbled.mixin;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import com.faefluffkrist.humbled.DamageSnapshot;
import com.faefluffkrist.humbled.Humbled;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin implements DamageSnapshot {
 @Shadow private int lastSentExp;
 @Unique private DamageSource humbled$deathDamage;
 @Unique private boolean humbled$brokeArmor;
 @Override public DamageSource humbled$deathSource() { return humbled$deathDamage; }
 @Override public void humbled$armorBroke() { humbled$brokeArmor=true; }
 @Inject(method="die",at=@At("HEAD"))
 private void humbled$deathStart(DamageSource source,CallbackInfo ci) {
  humbled$deathDamage=source; humbled$brokeArmor=false;
 }
 @Inject(method="die",at=@At("TAIL"))
 private void humbled$deathSound(DamageSource source,CallbackInfo ci) {
  if (!humbled$brokeArmor || !Humbled.config.breakSound) return;
  ServerPlayer self=(ServerPlayer)(Object)this;
  self.connection.send(new ClientboundSoundPacket(SoundEvents.ITEM_BREAK,SoundSource.PLAYERS,
   self.getX(),self.getY(),self.getZ(),(float)Humbled.config.soundVolume,(float)Humbled.config.soundPitch,self.getRandom().nextLong()));
 }

 @Override public void humbled$syncExperience() { lastSentExp = -1; }
 @Unique private final java.util.Deque<ItemStack[]> humbled$damageSnapshots = new java.util.ArrayDeque<>();
 @Inject(method="hurtServer", at=@At("HEAD"))
 private void humbled$beforeDamage(ServerLevel level, DamageSource source, float damage, CallbackInfoReturnable<Boolean> cir) {
  ServerPlayer self = (ServerPlayer)(Object)this;
  ItemStack[] armor = new ItemStack[4];
  for (int i=0; i<4; i++) armor[i] = self.getInventory().getItem(36+i).copy();
  humbled$damageSnapshots.push(armor);
 }
 @Inject(method="hurtServer", at=@At("RETURN"))
 private void humbled$afterDamage(ServerLevel level, DamageSource source, float damage, CallbackInfoReturnable<Boolean> cir) {
  if (!humbled$damageSnapshots.isEmpty()) humbled$damageSnapshots.pop();
 }
 @Override public void humbled$restoreArmorBeforeLethalHit() {
  if (humbled$damageSnapshots.isEmpty()) return;
  ServerPlayer self = (ServerPlayer)(Object)this;
  ItemStack[] armor = humbled$damageSnapshots.peek();
  for (int i=0; i<4; i++) self.getInventory().setItem(36+i, armor[i].copy());
 }

 @Inject(method="restoreFrom", at=@At("TAIL"))
 private void humbled$restore(ServerPlayer old, boolean restoreAll, CallbackInfo ci) {
  if (restoreAll || old.isSpectator() || old.isAlive()) return;
  ServerPlayer self = (ServerPlayer)(Object)this;
  self.getInventory().replaceWith(old.getInventory());
  self.experienceLevel = old.experienceLevel; self.experienceProgress = old.experienceProgress;
  self.totalExperience = old.totalExperience;
 }
}
