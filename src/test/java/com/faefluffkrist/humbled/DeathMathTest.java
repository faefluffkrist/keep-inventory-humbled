package com.faefluffkrist.humbled;
public final class DeathMathTest {
 private static void close(double actual, double expected) {
  if (Math.abs(actual-expected) > .000001) throw new AssertionError(actual + " != " + expected);
 }
 public static void main(String[] args) throws Exception {
  close(DeathMath.retained(60.5, .5), 30.25);
  close(DeathMath.recovered(60, .5, .1), 3);
  close(DeathMath.retained(61.5, .5), 30.75);
  close(DeathMath.retained(.5, .5), .25);
  close(DeathMath.recovered(0, .5, .1), 0);
  close(DeathMath.retained(60.5, .5) + DeathMath.recovered(60.5, .5, .1), 33.275);
  close(DeathMath.retained(60, 0), 60);
  if (!DeathMath.breaks(100,90,.1) || DeathMath.breaks(100,89,.1)) throw new AssertionError("threshold");
  if (DeathMath.totalPoints(16,0,42)!=352 || DeathMath.totalPoints(17,0,47)!=394
      || DeathMath.totalPoints(31,0,121)!=1507 || DeathMath.totalPoints(32,0,130)!=1628)
   throw new AssertionError("XP curve boundaries");
  if (DeathMath.totalPoints(Integer.MAX_VALUE/2,0,1)!=Integer.MAX_VALUE) throw new AssertionError("overflow");
  Config defaults=Config.defaults();
  close(defaults.armorLoss,.20); close(defaults.breakAt,.10);
  close(defaults.experienceLost(60.5,1),30.25);
  close(defaults.armorLoss*defaults.environmentalMultiplier,.05);
  if (defaults.difficultyEnabled || !defaults.unbreakingEnabled || defaults.unbreakingMode!=Config.UnbreakingMode.VANILLA)
   throw new AssertionError("default modes");
  if (!defaults.recoveryItem.equals("minecraft:experience_bottle")) throw new AssertionError("default pickup");
  java.util.Properties props=new java.util.Properties();
  props.setProperty("experienceLossEnabled","false"); close(new Config(props).experienceLost(60.5,1),0);
  props.clear(); props.setProperty("experienceLossMode","FIXED");
  props.setProperty("experienceFixedLevelsLost","5"); props.setProperty("experienceFixedBarLost",".25");
  close(new Config(props).experienceLost(60.5,1),5.25); close(new Config(props).experienceLost(2,1),2);
  props.clear(); props.setProperty("difficultyScalingEnabled","true"); Config scaled=new Config(props);
  close(scaled.difficulty(0),.05); close(scaled.difficulty(1),.5); close(scaled.difficulty(2),1); close(scaled.difficulty(3),1.5);
  close(scaled.experienceLost(60,scaled.difficulty(3)),45);
  close(scaled.penalty(.8,1.5,true),1); close(scaled.protection(.1,0,true),1);
  for (int level=0;level<=255;level++) {
   double protection=defaults.unbreakingProtection(level);
   if (protection<0 || protection>1) throw new AssertionError("unsafe Unbreaking");
   for (int raw=0;raw<=100;raw++) if (DeathMath.protectedDamage(raw,protection)>raw) throw new AssertionError("Unbreaking punishment");
  }
  props.clear(); props.setProperty("unbreakingProtectionLevel1","-.2");
  try { new Config(props); throw new AssertionError("negative protection accepted"); } catch (IllegalArgumentException expected) { }
  props.clear(); props.setProperty("unbreakingProtectionLevel2",".1");
  try { new Config(props); throw new AssertionError("backwards protection accepted"); } catch (IllegalArgumentException expected) { }
  java.nio.file.Path temp=java.nio.file.Files.createTempDirectory("humbled-config-test");
  java.nio.file.Path config=temp.resolve("keep-inventory-humbled.properties");
  java.nio.file.Files.writeString(config,"# Existing user setting\narmorDeathLoss=.35\n");
  close(Config.load(config).armorLoss,.35);
  java.util.Properties migrated=new java.util.Properties();
  try (var in=java.nio.file.Files.newInputStream(config)) { migrated.load(in); }
  if (!migrated.containsKey("experienceRecoveryItem") || !migrated.containsKey("hardMultiplier")) throw new AssertionError("missing config migration");
  String once=java.nio.file.Files.readString(config); Config.load(config);
  if (!once.equals(java.nio.file.Files.readString(config))) throw new AssertionError("migration repeated");
  java.nio.file.Files.delete(config); java.nio.file.Files.delete(temp);
  ConfigEditorTest.run();
  System.out.println("Passed XP math, defaults, fixed loss, difficulty scaling, Unbreaking bounds, validation, config migration, and threshold checks.");
 }
}
