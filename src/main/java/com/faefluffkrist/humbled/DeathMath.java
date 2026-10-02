package com.faefluffkrist.humbled;

public final class DeathMath {
 private DeathMath() { }
 public static double clampFraction(double value) { return Math.max(0,Math.min(1,value)); }
 public static int protectedDamage(int raw,double protection) { return Math.min(raw,(int)Math.ceil(raw*(1-clampFraction(protection)))); }
 public static double retained(double levels, double loss) { return levels * (1 - loss); }
 public static double recovered(double levels, double loss, double recovery) { return levels * loss * recovery; }
 public static int totalPoints(int level, float progress, int nextLevelCost) {
  double base = level <= 16 ? (double)level * level + 6.0 * level
    : level <= 31 ? 2.5 * level * level - 40.5 * level + 360
    : 4.5 * level * level - 162.5 * level + 2220;
  return (int)Math.min(Integer.MAX_VALUE, Math.max(0, base + Math.floor(progress * nextLevelCost)));
 }
 public static boolean breaks(int max, int damage, double threshold) { return max - damage <= max * threshold; }
}
