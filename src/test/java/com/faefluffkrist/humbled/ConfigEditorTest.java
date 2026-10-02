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
  if (config.armorLoss!=.20 || config.xpLoss!=.50 || config.recovery!=.10) throw new AssertionError("GUI percent conversion");
  if (ConfigFields.ALL.stream().filter(f->f.group()==ConfigFields.Group.DIFFICULTY).count()<10) throw new AssertionError("difficulty missing");
  var path=Files.createTempDirectory("humbled-editor-test").resolve("config.properties");
  defaults.setProperty("futureOption","retained"); ConfigStore.save(path,defaults);
  Properties saved=ConfigStore.read(path); if (!saved.getProperty("futureOption").equals("retained")) throw new AssertionError("unknown key loss");
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
