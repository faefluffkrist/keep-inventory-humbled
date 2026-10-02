package com.faefluffkrist.humbled;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.AtomicMoveNotSupportedException;
import java.util.Properties;
import java.util.TreeSet;

/** Editing/saving never mutates the active world's settings. */
public final class ConfigStore {
 private ConfigStore() { }
 public static Properties defaults() {
  Properties p=new Properties();
  try { p.load(new StringReader(Config.TEMPLATE)); } catch (IOException e) { throw new AssertionError(e); }
  return p;
 }
 public static Properties read(Path path) throws IOException {
  Properties p=defaults();
  if (Files.exists(path)) try(var reader=Files.newBufferedReader(path,StandardCharsets.UTF_8)) { p.load(reader); }
  return p;
 }
 public static void save(Path path,Properties values) throws IOException {
  // Validate before any write. Unknown settings survive a save.
  new Config(values);
  Properties all=read(path); all.putAll(values);
  StringBuilder text=new StringBuilder();
  Properties defaults=defaults();
  for (String line:Config.TEMPLATE.split("\\R")) {
   if (line.isBlank() || line.startsWith("#")) { text.append(line).append('\n'); continue; }
   String key=line.substring(0,line.indexOf('='));
   String value=all.getProperty(key,defaults.getProperty(key));
   // Inputs are single-line; prohibit creating extra property entries.
   if (value.indexOf('\n')>=0 || value.indexOf('\r')>=0) throw new IllegalArgumentException("Invalid multiline value for "+key);
   text.append(key).append('=').append(value).append('\n');
  }
  // Preserve any extension keys with standard Properties escaping.
  Properties extra=new Properties();
  for(String key:new TreeSet<>(all.stringPropertyNames())) if (!defaults.containsKey(key)) extra.setProperty(key,all.getProperty(key));
  if (!extra.isEmpty()) {
   java.io.StringWriter writer=new java.io.StringWriter(); extra.store(writer,"Additional settings");
   text.append('\n').append(writer);
  }
  Files.createDirectories(path.getParent());
  Path temp=Files.createTempFile(path.getParent(),"humbled-config-",".tmp");
  try {
   Files.writeString(temp,text,StandardCharsets.UTF_8);
   try { Files.move(temp,path,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE); }
   catch (AtomicMoveNotSupportedException e) { Files.move(temp,path,StandardCopyOption.REPLACE_EXISTING); }
  } finally { Files.deleteIfExists(temp); }
 }
}
