package com.faefluffkrist.humbled.client;

import com.faefluffkrist.humbled.Config;
import com.faefluffkrist.humbled.ConfigStore;
import com.faefluffkrist.humbled.Humbled;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class HumbledConfigScreen extends Screen {
 private final Screen parent;
 private final Map<String,String> draft=new LinkedHashMap<>();
 private final List<Row> visible=new ArrayList<>();
 private ConfigFields.Group group=ConfigFields.Group.INVENTORY;
 private int page,rowsPerPage=1;
 private String status="";
 private int statusColor=0xFFAAAAAA;
 private record Row(ConfigFields.Field field,int y) { }
 public HumbledConfigScreen(Screen parent) {
  super(Component.literal("Keep Inventory Humbled")); this.parent=parent;
  try { setDraft(ConfigStore.read(Humbled.configPath())); }
  catch(Exception e) { setDraft(ConfigStore.defaults()); status="Could not read settings. Defaults shown; Cancel preserves the file."; statusColor=0xFFFF5555; }
 }
 private void setDraft(Properties values) {
  for(var field:ConfigFields.ALL) {
   String value=values.getProperty(field.key(),ConfigStore.defaults().getProperty(field.key()));
   try { draft.put(field.key(),field.display(value)); }
   catch(NumberFormatException e) { draft.put(field.key(),value); }
  }
 }
 @Override protected void init() {
  visible.clear();
  int contentWidth=Math.min(560,width-20),x=(width-contentWidth)/2;
  var groups=ConfigFields.Group.values();
  int tabWidth=contentWidth/4;
  for(int i=0;i<groups.length;i++) {
   var target=groups[i];
   Button tab=Button.builder(Component.literal(target.label),b->{group=target;page=0;rebuild();})
    .bounds(x+(i%4)*tabWidth,38+(i/4)*23,tabWidth-3,20).build();
   tab.active=target!=group; addRenderableWidget(tab);
  }
  rowsPerPage=Math.max(1,(height-180)/26);
  List<ConfigFields.Field> fields=ConfigFields.ALL.stream().filter(f->f.group()==group).toList();
  int pages=Math.max(1,(fields.size()+rowsPerPage-1)/rowsPerPage);
  page=Math.min(page,pages-1);
  int valueWidth=Math.min(220,contentWidth/2),valueX=x+contentWidth-valueWidth;
  for(int i=page*rowsPerPage,row=0;i<Math.min(fields.size(),(page+1)*rowsPerPage);i++,row++) {
   var field=fields.get(i); int y=91+row*26;
   visible.add(new Row(field,y));
   Tooltip tooltip=Tooltip.create(Component.literal(field.label()+"\n"+field.description()));
   if(field.kind()==ConfigFields.Kind.BOOLEAN || field.kind()==ConfigFields.Kind.CHOICE) {
    Button button=Button.builder(Component.literal(buttonText(field)),b->{
     if(field.kind()==ConfigFields.Kind.BOOLEAN) draft.put(field.key(),Boolean.toString(!Boolean.parseBoolean(draft.get(field.key()))));
     else { var choices=field.choices(); int at=choices.indexOf(draft.get(field.key())); draft.put(field.key(),choices.get((at+1)%choices.size())); }
     b.setMessage(Component.literal(buttonText(field))); edited();
    }).bounds(valueX,y,valueWidth,20).build();
    button.setTooltip(tooltip); addRenderableWidget(button);
   } else {
    EditBox edit=new EditBox(font,valueX,y,valueWidth,20,Component.literal(field.label()));
    edit.setMaxLength(field.kind()==ConfigFields.Kind.TEXT?4096:32);
    edit.setValue(draft.get(field.key())); edit.setTooltip(tooltip);
    edit.setResponder(v->{draft.put(field.key(),v);edited();}); addRenderableWidget(edit);
   }
  }
  int pagerY=height-78;
  Button previous=Button.builder(Component.literal("Previous"),b->{page--;rebuild();}).bounds(x,pagerY,80,20).build();
  previous.active=page>0;addRenderableWidget(previous);
  Button next=Button.builder(Component.literal("Next"),b->{page++;rebuild();}).bounds(x+contentWidth-80,pagerY,80,20).build();
  next.active=page+1<pages;addRenderableWidget(next);
  int bottom=height-27;
  int bw=(contentWidth-9)/4;
  addRenderableWidget(Button.builder(Component.literal("Defaults"),b->{
   Properties defaults=ConfigStore.defaults();
   for(var field:ConfigFields.ALL) if(field.group()==group) draft.put(field.key(),field.display(defaults.getProperty(field.key())));
   edited();rebuild();
  }).bounds(x,bottom,bw,20).tooltip(Tooltip.create(Component.literal("Reset this category to defaults. Save to keep the changes."))).build());
  addRenderableWidget(Button.builder(Component.literal("Save"),b->save(false)).bounds(x+bw+3,bottom,bw,20).build());
  addRenderableWidget(Button.builder(Component.literal("Save & Close"),b->save(true)).bounds(x+2*(bw+3),bottom,bw,20).build());
  addRenderableWidget(Button.builder(Component.literal("Cancel"),b->onClose()).bounds(x+3*(bw+3),bottom,bw,20).build());
 }
 private String buttonText(ConfigFields.Field field) {
  String value=draft.get(field.key());
  return field.kind()==ConfigFields.Kind.BOOLEAN ? Boolean.parseBoolean(value)?"ON":"OFF" : value;
 }
 private void edited() { status="Unsaved changes.";statusColor=0xFFFFFF55; }
 private void rebuild() { rebuildWidgets(); }
 private void save(boolean close) {
  try {
   Properties values=new Properties();
   for(var field:ConfigFields.ALL) values.setProperty(field.key(),field.stored(draft.get(field.key())));
   new Config(values); ConfigStore.save(Humbled.configPath(),values);
   status="Saved. Leave and reopen your world to apply changes.";statusColor=0xFF55FF55;
   if(close) onClose();
  } catch(Exception e) { status=e.getMessage()==null?"Could not save settings.":e.getMessage();statusColor=0xFFFF5555; }
 }
 @Override public void onClose() { minecraft.gui.setScreen(parent); }
 @Override public void extractRenderState(GuiGraphicsExtractor graphics,int mouseX,int mouseY,float delta) {
  super.extractRenderState(graphics,mouseX,mouseY,delta);
  int contentWidth=Math.min(560,width-20),x=(width-contentWidth)/2;
  graphics.centeredText(font,title,width/2,9,0xFFFFFFFF);
  String note=minecraft.getCurrentServer()!=null && minecraft.getSingleplayerServer()==null
   ? "Multiplayer uses server settings; this screen edits local worlds."
   : "Save changes, then leave and reopen your world to apply them.";
  graphics.centeredText(font,Component.literal(font.plainSubstrByWidth(note,width-16)),width/2,23,0xFFAAAAAA);
  int labelWidth=contentWidth-Math.min(220,contentWidth/2)-8;
  for(Row row:visible) graphics.text(font,Component.literal(font.plainSubstrByWidth(row.field().label(),labelWidth)),x,row.y()+6,0xFFFFFFFF,false);
  int count=(int)ConfigFields.ALL.stream().filter(f->f.group()==group).count();
  int pages=Math.max(1,(count+rowsPerPage-1)/rowsPerPage);
  graphics.centeredText(font,Component.literal((page+1)+" / "+pages),width/2,height-72,0xFFAAAAAA);
  graphics.centeredText(font,Component.literal(font.plainSubstrByWidth(status,width-16)),width/2,height-47,statusColor);
  graphics.centeredText(font,Component.literal("Hover an option for details. Percentage fields use 0–100."),width/2,height-36,0xFFAAAAAA);
 }
}
