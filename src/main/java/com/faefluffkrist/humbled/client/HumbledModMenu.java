package com.faefluffkrist.humbled.client;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
public final class HumbledModMenu implements ModMenuApi {
 @Override public ConfigScreenFactory<?> getModConfigScreenFactory() { return HumbledConfigScreen::new; }
}
