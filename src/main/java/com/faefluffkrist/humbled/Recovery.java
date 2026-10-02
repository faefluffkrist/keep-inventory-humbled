package com.faefluffkrist.humbled;

import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.component.CustomData;

public final class Recovery {
 private static final String KEY = "humbled_recovery";
 private Recovery() { }
 private static final java.util.Set<String> warnedItems=new java.util.HashSet<>();
 public static CompoundTag data(ItemStack stack) {
  CustomData data = stack.get(DataComponents.CUSTOM_DATA);
  if (data == null) return null;
  CompoundTag tag = data.copyTag();
  return tag.getDoubleOr(KEY, 0) > 0 ? tag : null;
 }
 public static boolean spawn(ServerPlayer p, ServerLevel level, double amount) {
  Config c=Humbled.config;
  var configured=BuiltInRegistries.ITEM.getOptional(Identifier.parse(c.recoveryItem)).filter(item->item!=Items.AIR);
  if (configured.isEmpty() && warnedItems.add(c.recoveryItem))
   System.getLogger("keepinventoryhumbled").log(System.Logger.Level.WARNING,"Unknown recovery item "+c.recoveryItem+"; using minecraft:experience_bottle.");
  var display=configured.orElse(Items.EXPERIENCE_BOTTLE);
  ItemStack token = new ItemStack(display);
  CompoundTag tag = new CompoundTag();
  tag.putInt("humbled_lifetime", c.recoveryLifetime);
  tag.putDouble(KEY, amount); tag.putString("owner", p.getUUID().toString());
  tag.putString("death_id", UUID.randomUUID().toString());
  token.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
  token.set(DataComponents.CUSTOM_NAME, Component.literal("Lost experience — " + p.getName().getString()));
  ItemEntity entity = new ItemEntity(level, p.getX(), Math.max(p.getY(), level.getMinY() + 2), p.getZ(), token);
  entity.setTarget(p.getUUID()); entity.setNoPickUpDelay(); entity.setUnlimitedLifetime();
  entity.setInvulnerable(c.recoveryInvulnerable); entity.setNoGravity(true); entity.setDeltaMovement(0, 0, 0);
  entity.setGlowingTag(c.recoveryGlowing);
  return level.addFreshEntity(entity);
 }
 public static void collect(ItemEntity entity, ServerPlayer player, CompoundTag tag) {
  if (!player.isAlive() || entity.hasPickUpDelay() || !player.getUUID().toString().equals(tag.getStringOr("owner", ""))) return;
  double amount = tag.getDoubleOr(KEY, 0);
  if (!Double.isFinite(amount) || amount <= 0) return;
  // Remove before awarding, preventing repeat touches from paying out twice.
  entity.discard();
  DeathRules.setLevels(player, player.experienceLevel + (double)player.experienceProgress + amount);
  player.sendSystemMessage(Component.literal(String.format(java.util.Locale.ROOT, "Recovered %.2f experience levels.", amount)));
 }
}
