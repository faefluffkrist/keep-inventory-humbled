package com.faefluffkrist.humbled;
import java.io.IOException;
import java.nio.file.Path;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
public final class Humbled implements ModInitializer {
 public static volatile Config config=Config.defaults();
 public static Path configPath() { return FabricLoader.getInstance().getConfigDir().resolve("keep-inventory-humbled.properties"); }
 @Override public void onInitialize() { reloadForWorld(); }
 public static void reloadForWorld() {
  try { config=Config.load(configPath()); }
  catch(IOException e) { throw new IllegalStateException("Cannot load Keep Inventory Humbled configuration",e); }
 }
}
