package com.faefluffkrist.humbled.mixin;
import com.faefluffkrist.humbled.Recovery;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
 @Inject(method="tick",at=@At("HEAD"),cancellable=true)
 private void humbled$lifetime(CallbackInfo ci) {
  ItemEntity self=(ItemEntity)(Object)this;
  if (self.level().isClientSide()) return;
  var data=Recovery.data(self.getItem());
  if (data==null) return;
  int lifetime=data.getIntOr("humbled_lifetime",0);
  if (lifetime<=0) return;
  int elapsed=data.getIntOr("humbled_elapsed",0)+1;
  if (elapsed>=lifetime) { self.discard(); ci.cancel(); return; }
  data.putInt("humbled_elapsed",elapsed);
  self.getItem().set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.of(data));
 }
 @Inject(method="playerTouch", at=@At("HEAD"), cancellable=true)
 private void humbled$pickup(Player p, CallbackInfo ci) {
  ItemEntity self = (ItemEntity)(Object)this;
  var data = Recovery.data(self.getItem());
  if (data == null) return;
  ci.cancel();
  if (self.isAlive() && p instanceof ServerPlayer serverPlayer) Recovery.collect(self, serverPlayer, data);
 }
}
