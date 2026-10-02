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
  help.put("armorDurabilityLossEnabled","Apply a death armor penalty instead of normal wear from the lethal hit. Disabled: normal hit wear remains.");
  help.put("armorDeathLoss","Percent of maximum armor durability lost on death, before Unbreaking.");
  help.put("armorBreakAtRemaining","Armor breaks at or below this remaining durability after an enabled death penalty.");
  help.put("unbreakingMode","Vanilla preserves the real random enchantment effect. Custom uses the protection percentages below.");
  help.put("unbreakingProtectionLevel1","Custom mode: percent of the death penalty prevented by Unbreaking I.");
  help.put("unbreakingProtectionLevel2","Custom mode: percent prevented by Unbreaking II. Must be at least level I's protection.");
  help.put("unbreakingProtectionLevel3","Custom mode: percent prevented by Unbreaking III. Must be at least level II's protection.");
  help.put("unbreakingProtectionPerExtraLevel","Custom mode: extra percentage points of protection per level beyond III, capped at 100%.");
  help.put("environmentalArmorMode","Regular: full penalty. Reduced: multiply the penalty by the setting below. None: no death wear or threshold breaking.");
  help.put("environmentalArmorMultiplier","Percent of the ordinary penalty: 25% of a 20% penalty means 5% durability lost.");
  help.put("environmentalDamageTypes","Comma-separated damage IDs. Explicit exclusions take priority over this list and the vanilla tag.");
  help.put("environmentalExcludedDamageTypes","Comma-separated damage IDs that should receive the regular penalty. Empty is allowed.");
  help.put("environmentalUseVanillaBypassesArmorTag","Include non-armor damage types such as fall, fire, drowning, freezing, void and /kill.");
  help.put("experienceLossMode","Percentage: lose a fraction of levels plus bar. Fixed: lose a set number of levels plus a bar fraction.");
  help.put("experienceDeathLoss","Percentage mode: percent of the combined levels + bar value lost. Default 50%.");
  help.put("experienceFixedBarLost","Fixed mode: percent of one experience bar added to the fixed level loss.");
  help.put("lostExperienceRecoverable","Percent of the actual loss that can be recovered. Default 10%.");
  help.put("experienceRecoveryItem","Display item ID, such as minecraft:experience_bottle. Unknown items fall back to the bottle.");
  help.put("experienceRecoveryLifetimeTicks","Loaded ticks until expiry. 0 means no despawn. 1200 ticks is one minute.");
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
   label=switch(key) {case "armorDeathLoss"->"Durability lost";case "armorBreakAtRemaining"->"Break at remaining";case "environmentalArmorMultiplier"->"Reduced penalty";case "experienceDeathLoss"->"Experience lost";case "lostExperienceRecoverable"->"Lost experience recoverable";default->label;};
   if(kind==Kind.PERCENT) label+=" (%)";
   String fallback=kind==Kind.PERCENT ? "Enter a percentage from 0 to 100." : kind==Kind.NUMBER ? "Enter a nonnegative number. Hover other fields for mode-specific details." : "Toggle or edit this option. Changes apply after reopening your world.";
   fields.add(new Field(key,label,help.getOrDefault(key,fallback),group,kind,choices.getOrDefault(key,List.of())));
  }
  return List.copyOf(fields);
 }
}
