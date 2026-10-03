package com.faefluffkrist.humbled;
import com.faefluffkrist.humbled.client.ConfigFields;
import java.nio.file.Files;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
public final class ConfigEditorTest {
 private ConfigEditorTest() { }
 public static void run() throws Exception {
  Properties defaults=ConfigStore.defaults();
  Set<String> fields=ConfigFields.ALL.stream().map(ConfigFields.Field::key).collect(Collectors.toSet());
  if (!fields.equals(defaults.stringPropertyNames()) || fields.size()!=ConfigFields.ALL.size()) throw new AssertionError("settings coverage");
  Properties roundtrip=new Properties();
  for(var field:ConfigFields.ALL) roundtrip.setProperty(field.key(),field.stored(field.display(defaults.getProperty(field.key()))));
  Config config=new Config(roundtrip);
  if(config.recoveryLifetime!=24000)throw new AssertionError("20-minute default");
  Properties unlimited=new Properties();unlimited.setProperty("experienceRecoveryLifetimeTicks","0");
  if(new Config(unlimited).recoveryLifetime!=0)throw new AssertionError("explicit unlimited value preserved");
  var mode=ConfigFields.ALL.stream().filter(f->f.key().equals("unbreakingMode")).findFirst().orElseThrow();
  if(!mode.choiceLabel("VANILLA").equals("Vanilla")||!mode.choiceLabel("CUSTOM").equals("Value"))throw new AssertionError("mode labels");
  for(String selected:mode.choices()){
   Properties values=ConfigStore.defaults();values.setProperty(mode.key(),mode.stored(selected));
   if(!new Config(values).unbreakingMode.name().equals(selected))throw new AssertionError("mode selection");
  }

  if (!config.hostileMagicRegular || !Config.defaults().hostileMagicRegular) throw new AssertionError("hostile magic default/migration");
  Properties magicOff=new Properties();magicOff.setProperty("hostileMagicUsesRegularArmorPenalty","false");
  if(new Config(magicOff).hostileMagicRegular) throw new AssertionError("hostile magic toggle");
  var magic=ConfigFields.ALL.stream().filter(f->f.key().equals("hostileMagicUsesRegularArmorPenalty")).findFirst().orElseThrow();
  if(magic.group()!=ConfigFields.Group.ENVIRONMENT || magic.kind()!=ConfigFields.Kind.BOOLEAN)throw new AssertionError("magic toggle placement");
  if (config.armorLoss!=.20 || config.xpLoss!=.50 || config.recovery!=.10) throw new AssertionError("GUI percent conversion");
  if (ConfigFields.ALL.stream().filter(f->f.group()==ConfigFields.Group.DIFFICULTY).count()<10) throw new AssertionError("difficulty missing");
  var path=Files.createTempDirectory("humbled-editor-test").resolve("config.properties");
  defaults.setProperty("futureOption","retained"); ConfigStore.save(path,defaults);
  Properties saved=ConfigStore.read(path); if (!saved.getProperty("futureOption").equals("retained")) throw new AssertionError("unknown key loss");
  Properties valueMode=ConfigStore.read(path);valueMode.setProperty("unbreakingMode",mode.stored("CUSTOM"));
  valueMode.setProperty("unbreakingProtectionLevel1",".50");valueMode.setProperty("unbreakingProtectionLevel2",".60");valueMode.setProperty("unbreakingProtectionLevel3",".70");
  ConfigStore.save(path,valueMode);Config chosen=Config.load(path);
  if(chosen.unbreakingMode!=Config.UnbreakingMode.CUSTOM||chosen.unbreakingProtection(2)!=.60)throw new AssertionError("Value mode save/application");
  Properties edited=ConfigStore.read(path); edited.setProperty("armorDeathLoss",".35"); ConfigStore.save(path,edited);
  if(Config.load(path).armorLoss!=.35) throw new AssertionError("world reload value");
  String good=Files.readString(path); edited.setProperty("armorDeathLoss","-1");
  try { ConfigStore.save(path,edited); throw new AssertionError("invalid value saved"); } catch(IllegalArgumentException expected) { }
  if(!good.equals(Files.readString(path))) throw new AssertionError("invalid save modified file");
  // Disk edits are pending; existing immutable world settings retain their values.
  if(config.armorLoss!=.20) throw new AssertionError("active settings mutated");
  Files.delete(path);Files.delete(path.getParent());
  System.out.println("Passed form coverage, percent conversion, safe save, unknown keys and reload checks.");
 }
}
