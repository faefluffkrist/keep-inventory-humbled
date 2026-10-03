package com.faefluffkrist.humbled.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.math.BigDecimal;
import com.faefluffkrist.humbled.ConfigStore;

/** Shared, testable form metadata. Percentages display as 20, not .20. */
public final class ConfigFields {
 public enum Group {
  INVENTORY("Inventory"),ARMOR("Armor"),UNBREAKING("Unbreaking"),ENVIRONMENT("Environment"),
  EXPERIENCE("Experience"),RECOVERY("Recovery"),DIFFICULTY("Difficulty");
  public final String label; Group(String label) { this.label=label; }
 }
 public enum Kind { BOOLEAN,PERCENT,NUMBER,CHOICE,TEXT }
 public record Field(String key,String label,String description,Group group,Kind kind,List<String> choices) {
  public String choiceLabel(String value) {
   if(key.equals("unbreakingMode")&&value.equals("CUSTOM"))return "Value";
   String lower=value.toLowerCase(java.util.Locale.ROOT).replace('_',' ');
   return lower.isEmpty()?lower:Character.toUpperCase(lower.charAt(0))+lower.substring(1);
  }
  public String display(String stored) {
   return kind==Kind.PERCENT ? new BigDecimal(stored).multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString() : stored;
  }
  public String stored(String displayed) {
   try { return kind==Kind.PERCENT ? new BigDecimal(displayed.trim()).divide(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString() : displayed.trim(); }
   catch (NumberFormatException e) { throw new IllegalArgumentException(label+" must be a number",e); }
  }
 }
 private ConfigFields() { }
 public static final List<Field> ALL=create();
 private static List<Field> create() {
  List<Field> fields=new ArrayList<>();
  Map<String,List<String>> choices=Map.of("unbreakingMode",List.of("VANILLA","CUSTOM"),"environmentalArmorMode",List.of("REGULAR","REDUCED","NONE"),"experienceLossMode",List.of("PERCENTAGE","FIXED"));
  Properties defaults=ConfigStore.defaults();
  List<String> percent=List.of("armorDeathLoss","armorBreakAtRemaining","unbreakingProtectionLevel1","unbreakingProtectionLevel2","unbreakingProtectionLevel3","unbreakingProtectionPerExtraLevel","environmentalArmorMultiplier","experienceDeathLoss","experienceFixedBarLost","lostExperienceRecoverable");
  Map<String,String> help=new LinkedHashMap<>();
  help.put("keepHotbar","Keep items in the nine hotbar slots after death.");
  help.put("keepOffhand","Keep the item held in your offhand after death.");
  help.put("keepArmor","Keep equipped armor after death. Death durability penalties still apply when enabled.");
  help.put("keepMainInventory","Keep items in the main inventory. Disabled: these items drop at the death location.");
  help.put("honorVanishing","Delete items with Curse of Vanishing on death, including otherwise retained items.");
  help.put("keepBindingItems","Keep items with Curse of Binding, even when their inventory category would drop.");
  help.put("destroyNonDurableBindingItems","Destroy non-durable items with Curse of Binding, such as carved pumpkins, on death.");
  help.put("armorBreakThresholdEnabled","Break armor when its remaining durability reaches the threshold after a death penalty.");
  help.put("playArmorBreakSound","Play the item-break sound when armor breaks during death processing.");
  help.put("armorBreakSoundVolume","Armor-break sound volume, from 0 to 10. Default 1.");
  help.put("armorBreakSoundPitch","Armor-break sound pitch, from 0.5 to 2. Default 1.");
  help.put("unbreakingReducesLoss","Allow the Unbreaking enchantment to reduce death armor wear.");
  help.put("experienceLossEnabled","Remove experience on death using the selected loss mode.");
  help.put("experienceFixedLevelsLost","Fixed mode: number of levels removed, plus the configured bar fraction.");
  help.put("experienceRecoveryEnabled","Drop an experience recovery item at the death location for the recoverable share of your loss.");
  help.put("experienceRecoveryInvulnerable","Protect the recovery pickup from ordinary damage, including fire and explosions.");
  help.put("experienceRecoveryGlowing","Give the recovery pickup a glowing outline to help you find it.");
  help.put("peacefulMultiplier","Difficulty scaling: multiplier for Peaceful worlds. Default 0.05.");
  help.put("easyMultiplier","Difficulty scaling: multiplier for Easy worlds. Default 0.5.");
  help.put("normalMultiplier","Difficulty scaling: multiplier for Normal worlds. Default 1.");
  help.put("hardMultiplier","Difficulty scaling: multiplier for Hard and Hardcore worlds. Default 1.5.");
  help.put("difficultyScalesArmorLoss","Multiply death armor wear by the world's difficulty multiplier when scaling is enabled.");
  help.put("difficultyScalesBreakThreshold","Scale the armor break threshold by difficulty when scaling is enabled.");
  help.put("difficultyScalesExperienceLoss","Scale experience lost on death by difficulty when scaling is enabled.");
  help.put("difficultyScalesEnvironmentalMultiplier","Scale the reduced environmental armor penalty multiplier by difficulty.");
  help.put("armorDurabilityLossEnabled","Apply a death armor penalty instead of normal wear from the lethal hit. Disabled: normal hit wear remains.");
  help.put("armorDeathLoss","Percent of maximum armor durability lost on death, before Unbreaking.");
  help.put("armorBreakAtRemaining","Armor breaks at or below this remaining durability after an enabled death penalty.");
  help.put("unbreakingMode","Vanilla preserves the real random enchantment effect. Value uses the configured protection percentages for each Unbreaking level.");
  help.put("unbreakingProtectionLevel1","Value mode: percent of the death penalty prevented by Unbreaking I.");
  help.put("unbreakingProtectionLevel2","Value mode: percent prevented by Unbreaking II. Must be at least level I's protection.");
  help.put("unbreakingProtectionLevel3","Value mode: percent prevented by Unbreaking III. Must be at least level II's protection.");
  help.put("unbreakingProtectionPerExtraLevel","Value mode: extra percentage points of protection per level beyond III, capped at 100%.");
  help.put("environmentalArmorMode","Regular: full penalty. Reduced: multiply the penalty by the setting below. None: no death wear or threshold breaking.");
  help.put("environmentalArmorMultiplier","Percent of the ordinary penalty: 25% of a 20% penalty means 5% durability lost.");
  help.put("environmentalDamageTypes","Comma-separated damage IDs. Explicit exclusions take priority over this list and the vanilla tag.");
  help.put("environmentalExcludedDamageTypes","Comma-separated damage IDs that should receive the regular penalty. Empty is allowed.");
  help.put("environmentalUseVanillaBypassesArmorTag","Include non-armor damage types such as fall, fire, drowning, freezing, void and /kill.");
  help.put("hostileMagicUsesRegularArmorPenalty","Evoker fangs and damage caused by harmful splash potions use the regular death armor penalty, even when environmental wear is reduced or disabled. Food and animal poison are unaffected. This does not add wear on every hit.");
  help.put("experienceLossMode","Percentage: lose a fraction of levels plus bar. Fixed: lose a set number of levels plus a bar fraction.");
  help.put("experienceDeathLoss","Percentage mode: percent of the combined levels + bar value lost. Default 50%.");
  help.put("experienceFixedBarLost","Fixed mode: percent of one experience bar added to the fixed level loss.");
  help.put("lostExperienceRecoverable","Percent of the actual loss that can be recovered. Default 10%.");
  help.put("experienceRecoveryItem","Display item ID, such as minecraft:experience_bottle. Unknown items fall back to the bottle.");
  help.put("experienceRecoveryLifetimeTicks","Default 24000 loaded ticks is 20 minutes. Unloaded chunks pause the timer; 0 disables expiry.");
  help.put("difficultyScalingEnabled","Off by default. Normal uses 1x; Hard and Hardcore share the Hard multiplier.");
  help.put("difficultyScalesRecovery","Divide recovery protection by the difficulty multiplier. Higher difficulty gives less recovery.");
  help.put("difficultyScalesUnbreakingProtection","Divide Unbreaking protection by the difficulty multiplier. Vanilla mode then uses its expected protection.");
  Group group=Group.INVENTORY;
  for(String line:com.faefluffkrist.humbled.Config.TEMPLATE.split("\\R")) {
   if(line.isBlank() || line.startsWith("#")) continue;
   String key=line.substring(0,line.indexOf('='));
   if(key.equals("armorDurabilityLossEnabled")) group=Group.ARMOR;
   if(key.equals("unbreakingReducesLoss")) group=Group.UNBREAKING;
   if(key.equals("environmentalArmorMode")) group=Group.ENVIRONMENT;
   if(key.equals("experienceLossEnabled")) group=Group.EXPERIENCE;
   if(key.equals("experienceRecoveryEnabled")) group=Group.RECOVERY;
   if(key.equals("difficultyScalingEnabled")) group=Group.DIFFICULTY;
   String value=defaults.getProperty(key);
   Kind kind=value.equals("true") || value.equals("false") ? Kind.BOOLEAN : choices.containsKey(key) ? Kind.CHOICE : percent.contains(key) ? Kind.PERCENT : value.matches("[0-9.]+") ? Kind.NUMBER : Kind.TEXT;
   String label=key.replaceAll("([a-z])([A-Z])","$1 $2"); label=Character.toUpperCase(label.charAt(0))+label.substring(1);
   label=switch(key) {case "hostileMagicUsesRegularArmorPenalty"->"Fangs & splash potion penalty";case "armorDeathLoss"->"Durability lost";case "armorBreakAtRemaining"->"Break at remaining";case "environmentalArmorMultiplier"->"Reduced penalty";case "experienceDeathLoss"->"Experience lost";case "lostExperienceRecoverable"->"Lost experience recoverable";default->label;};
   if(kind==Kind.PERCENT) label+=" (%)";
   String fallback=kind==Kind.PERCENT ? "Enter a percentage from 0 to 100." : kind==Kind.NUMBER ? "Enter a nonnegative number. Hover other fields for mode-specific details." : "Toggle or edit this option. Changes apply after reopening your world.";
   fields.add(new Field(key,label,help.getOrDefault(key,fallback),group,kind,choices.getOrDefault(key,List.of())));
  }
  return List.copyOf(fields);
 }
}
