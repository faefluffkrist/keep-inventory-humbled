package com.faefluffkrist.humbled;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.HashSet;

/** Server-authoritative configuration. All percentages are fractions from 0 through 1. */
public final class Config {
 public enum EnvironmentalMode { REGULAR, REDUCED, NONE }
 public enum UnbreakingMode { VANILLA, CUSTOM }
 public enum ExperienceMode { PERCENTAGE, FIXED }
 public static final String TEMPLATE = """
# Keep Inventory Humbled. Reopen the world after editing; dedicated servers must restart.
# Percentages use fractions: .20 = 20%, .50 = 50%, 1 = 100%.

# Inventory and curses. Vanilla slots; accessory-mod slots are outside this policy.
keepHotbar=true
keepOffhand=true
keepArmor=true
keepMainInventory=false
honorVanishing=true
keepBindingItems=true
destroyNonDurableBindingItems=true

# Death armor penalty replaces ordinary armor wear from the lethal hit.
armorDurabilityLossEnabled=true
armorDeathLoss=.20
armorBreakThresholdEnabled=true
armorBreakAtRemaining=.10
playArmorBreakSound=true
armorBreakSoundVolume=1.0
armorBreakSoundPitch=1.0

# VANILLA preserves Minecraft's real Unbreaking effect, including its randomness.
# CUSTOM uses the protection fractions below, without random damage avoidance.
unbreakingReducesLoss=true
unbreakingMode=VANILLA
# CUSTOM: the fraction of the death penalty prevented at each enchantment level.
# Values must be nonnegative and cannot decrease as the enchantment level rises.
unbreakingProtectionLevel1=.20
unbreakingProtectionLevel2=.2666666667
unbreakingProtectionLevel3=.30
unbreakingProtectionPerExtraLevel=.025

# Environmental deaths: REGULAR, REDUCED, or NONE.
# REDUCED applies 25% of the usual penalty: .20 * .25 = .05 (5% durability).
environmentalArmorMode=REDUCED
environmentalArmorMultiplier=.25
# The vanilla bypasses_armor damage tag catches non-armor-wearing causes.
# An explicit ID exclusion takes priority, then explicit IDs, then the tag.
environmentalUseVanillaBypassesArmorTag=true
environmentalDamageTypes=minecraft:on_fire,minecraft:in_wall,minecraft:cramming,minecraft:outside_border,minecraft:drown,minecraft:fall,minecraft:ender_pearl,minecraft:fly_into_wall,minecraft:magic,minecraft:indirect_magic,minecraft:dragon_breath,minecraft:wither,minecraft:freeze
environmentalExcludedDamageTypes=

# PERCENTAGE halves the combined value "levels + XP bar fraction" by default.
# FIXED instead removes experienceFixedLevelsLost + experienceFixedBarLost.
experienceLossEnabled=true
experienceLossMode=PERCENTAGE
experienceDeathLoss=.50
experienceFixedLevelsLost=5
experienceFixedBarLost=0
experienceRecoveryEnabled=true
lostExperienceRecoverable=.10
experienceRecoveryItem=minecraft:experience_bottle
# 0 = no despawn. Otherwise ticks while the pickup's chunk is loaded.
experienceRecoveryLifetimeTicks=0
experienceRecoveryInvulnerable=true
experienceRecoveryGlowing=true

# Difficulty scaling is OFF by default. Hardcore uses its world difficulty (Hard).
difficultyScalingEnabled=false
peacefulMultiplier=.05
easyMultiplier=.50
normalMultiplier=1.0
hardMultiplier=1.50
# Scale penalties by the difficulty multiplier, capped at 100% for fractions.
difficultyScalesArmorLoss=true
difficultyScalesBreakThreshold=true
difficultyScalesExperienceLoss=true
# Optional: higher difficulty weakens recovery/Unbreaking by dividing by its multiplier.
difficultyScalesRecovery=false
difficultyScalesUnbreakingProtection=false
# Optional: scale the environmental multiplier like a penalty.
difficultyScalesEnvironmentalMultiplier=false
""";
 public final boolean keepHotbar, keepOffhand, keepArmor, keepMainInventory, honorVanishing,
  keepBindingItems, destroyNonDurableBindingItems, armorEnabled, thresholdEnabled, breakSound,
  unbreakingEnabled, environmentalTag, xpEnabled, recoveryEnabled, recoveryInvulnerable,
  recoveryGlowing, difficultyEnabled, scaleArmor, scaleThreshold, scaleXp, scaleRecovery,
  scaleUnbreaking, scaleEnvironmental;
 public final double armorLoss, breakAt, soundVolume, soundPitch, protection1, protection2,
  protection3, protectionExtra, environmentalMultiplier, xpLoss, fixedLevels, fixedBar,
  recovery, peaceful, easy, normal, hard;
 public final int recoveryLifetime;
 public final String recoveryItem;
 public final Set<String> environmentalTypes, environmentalExcluded;
 public final EnvironmentalMode environmentalMode;
 public final UnbreakingMode unbreakingMode;
 public final ExperienceMode xpMode;
 public Config(Properties p) {
  keepHotbar=bool(p,"keepHotbar"); keepOffhand=bool(p,"keepOffhand"); keepArmor=bool(p,"keepArmor");
  keepMainInventory=bool(p,"keepMainInventory"); honorVanishing=bool(p,"honorVanishing");
  keepBindingItems=bool(p,"keepBindingItems"); destroyNonDurableBindingItems=bool(p,"destroyNonDurableBindingItems");
  armorEnabled=bool(p,"armorDurabilityLossEnabled"); thresholdEnabled=bool(p,"armorBreakThresholdEnabled");
  armorLoss=fraction(p,"armorDeathLoss"); breakAt=fraction(p,"armorBreakAtRemaining");
  breakSound=bool(p,"playArmorBreakSound"); soundVolume=number(p,"armorBreakSoundVolume",0,10);
  soundPitch=number(p,"armorBreakSoundPitch",.5,2);
  unbreakingEnabled=bool(p,"unbreakingReducesLoss");
  unbreakingMode=choice(p,"unbreakingMode",UnbreakingMode.class);
  protection1=fraction(p,"unbreakingProtectionLevel1"); protection2=fraction(p,"unbreakingProtectionLevel2");
  protection3=fraction(p,"unbreakingProtectionLevel3"); protectionExtra=fraction(p,"unbreakingProtectionPerExtraLevel");
  if (protection2 < protection1 || protection3 < protection2) throw new IllegalArgumentException("Unbreaking protection must not decrease at higher levels");
  environmentalMode=choice(p,"environmentalArmorMode",EnvironmentalMode.class);
  environmentalMultiplier=fraction(p,"environmentalArmorMultiplier"); environmentalTag=bool(p,"environmentalUseVanillaBypassesArmorTag");
  environmentalTypes=ids(p,"environmentalDamageTypes"); environmentalExcluded=ids(p,"environmentalExcludedDamageTypes");
  xpEnabled=bool(p,"experienceLossEnabled"); xpMode=choice(p,"experienceLossMode",ExperienceMode.class);
  xpLoss=fraction(p,"experienceDeathLoss"); fixedLevels=number(p,"experienceFixedLevelsLost",0,1000000);
  fixedBar=fraction(p,"experienceFixedBarLost"); recoveryEnabled=bool(p,"experienceRecoveryEnabled");
  recovery=fraction(p,"lostExperienceRecoverable"); recoveryItem=value(p,"experienceRecoveryItem").trim();
  if (!validId(recoveryItem) || recoveryItem.equals("minecraft:air")) throw new IllegalArgumentException("experienceRecoveryItem must be a non-air namespaced item ID");
  double lifetime=number(p,"experienceRecoveryLifetimeTicks",0,Integer.MAX_VALUE);
  if (lifetime != Math.floor(lifetime)) throw new IllegalArgumentException("experienceRecoveryLifetimeTicks must be a whole number");
  recoveryLifetime=(int)lifetime;
  recoveryInvulnerable=bool(p,"experienceRecoveryInvulnerable"); recoveryGlowing=bool(p,"experienceRecoveryGlowing");
  difficultyEnabled=bool(p,"difficultyScalingEnabled");
  peaceful=number(p,"peacefulMultiplier",0,10); easy=number(p,"easyMultiplier",0,10);
  normal=number(p,"normalMultiplier",0,10); hard=number(p,"hardMultiplier",0,10);
  scaleArmor=bool(p,"difficultyScalesArmorLoss"); scaleThreshold=bool(p,"difficultyScalesBreakThreshold");
  scaleXp=bool(p,"difficultyScalesExperienceLoss"); scaleRecovery=bool(p,"difficultyScalesRecovery");
  scaleUnbreaking=bool(p,"difficultyScalesUnbreakingProtection"); scaleEnvironmental=bool(p,"difficultyScalesEnvironmentalMultiplier");
 }
 public static Config defaults() { return new Config(new Properties()); }
 private static Properties defaultsProperties() {
  Properties p=new Properties();
  try { p.load(new StringReader(TEMPLATE)); } catch (IOException e) { throw new AssertionError(e); }
  return p;
 }
 private static final Properties DEFAULTS=defaultsProperties();
 private static String value(Properties p,String key) { return p.getProperty(key,DEFAULTS.getProperty(key)); }
 private static boolean bool(Properties p,String key) {
  String v=value(p,key).trim();
  if (!v.equalsIgnoreCase("true") && !v.equalsIgnoreCase("false")) throw new IllegalArgumentException(key+" must be true or false");
  return Boolean.parseBoolean(v);
 }
 private static double fraction(Properties p,String key) { return number(p,key,0,1); }
 private static double number(Properties p,String key,double min,double max) {
  try { double v=Double.parseDouble(value(p,key).trim()); if (Double.isFinite(v) && v>=min && v<=max) return v; }
  catch (NumberFormatException ignored) { }
  throw new IllegalArgumentException(key+" must be between "+min+" and "+max);
 }
 private static <E extends Enum<E>> E choice(Properties p,String key,Class<E> type) {
  try { return Enum.valueOf(type,value(p,key).trim().toUpperCase(Locale.ROOT)); }
  catch (IllegalArgumentException e) { throw new IllegalArgumentException("Invalid "+key+": "+value(p,key),e); }
 }
 private static boolean validId(String id) { return id.matches("[a-z0-9_.-]+:[a-z0-9/._-]+"); }
 private static Set<String> ids(Properties p,String key) {
  Set<String> out=new HashSet<>();
  for (String raw:value(p,key).split(",")) {
   String id=raw.trim(); if (id.isEmpty()) continue;
   if (!validId(id)) throw new IllegalArgumentException(key+" contains an invalid damage ID: "+id);
   out.add(id);
  }
  return Set.copyOf(out);
 }
 public static Config load(Path path) throws IOException {
  Properties p=new Properties(); boolean existed=Files.exists(path);
  if (existed) { try (var in=Files.newInputStream(path)) { p.load(in); } }
  Config config=new Config(p);
  if (!existed) { Files.createDirectories(path.getParent()); Files.writeString(path,TEMPLATE); }
  else {
   // Add missing settings with comments while preserving existing settings and comments.
   StringBuilder addition=new StringBuilder();
   for (String line:TEMPLATE.split("\\R")) {
    if (line.isBlank() || line.startsWith("#")) { addition.append(line).append('\n'); continue; }
    int equals=line.indexOf('=');
    if (equals>0 && !p.containsKey(line.substring(0,equals))) addition.append(line).append('\n');
   }
   if (DEFAULTS.stringPropertyNames().stream().anyMatch(k->!p.containsKey(k)))
    Files.writeString(path,Files.readString(path)+"\n# Newly available settings (existing values preserved)\n"+addition);
  }
  return config;
 }
 public double difficulty(int id) {
  if (!difficultyEnabled) return 1;
  return switch(id) { case 0 -> peaceful; case 1 -> easy; case 3 -> hard; default -> normal; };
 }
 public double penalty(double fraction,double multiplier,boolean enabled) { return DeathMath.clampFraction(fraction*(enabled?multiplier:1)); }
 public double protection(double fraction,double multiplier,boolean enabled) {
  return !enabled ? fraction : multiplier==0 ? (fraction>0 ? 1 : 0) : DeathMath.clampFraction(fraction/multiplier);
 }
 public double unbreakingProtection(int level) {
  if (level<=0) return 0;
  return switch(level) { case 1 -> protection1; case 2 -> protection2; default -> DeathMath.clampFraction(protection3+Math.max(0,level-3)*protectionExtra); };
 }
 public double experienceLost(double before,double multiplier) {
  if (!xpEnabled) return 0;
  double base=xpMode==ExperienceMode.PERCENTAGE ? before*xpLoss : fixedLevels+fixedBar;
  return Math.min(before,base*(scaleXp?multiplier:1));
 }
}
