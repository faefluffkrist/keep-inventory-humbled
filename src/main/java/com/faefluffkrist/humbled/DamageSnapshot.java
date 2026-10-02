package com.faefluffkrist.humbled;
import net.minecraft.world.damagesource.DamageSource;
public interface DamageSnapshot {
 void humbled$restoreArmorBeforeLethalHit();
 void humbled$syncExperience();
 DamageSource humbled$deathSource();
 void humbled$armorBroke();
}
