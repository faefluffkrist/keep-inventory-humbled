package com.faefluffkrist.humbled.client;

import com.faefluffkrist.humbled.Config;
import com.faefluffkrist.humbled.ConfigStore;
import com.faefluffkrist.humbled.Humbled;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class HumbledConfigScreen extends ScrollingConfigScreen {
 private final Screen parent;
 private final Map<String,String> draft=new LinkedHashMap<>();
 private ConfigFields.Group group=ConfigFields.Group.INVENTORY;
 private List<ConfigFields.Field> fields=List.of();
 private final java.util.List<Integer> rowOffsets=new java.util.ArrayList<>(),rowHeights=new java.util.ArrayList<>();
 private String status="";
 private int statusColor=0xFFAAAAAA;
 public HumbledConfigScreen(Screen parent) {
  super(Component.literal("Keep Inventory Humbled"));this.parent=parent;
  try { setDraft(ConfigStore.read(Humbled.configPath())); }
  catch(Exception e) { setDraft(ConfigStore.defaults());status="Could not read settings. Back preserves the file.";statusColor=0xFFFF5555; }
 }
 private void setDraft(Properties values) {
  Properties defaults=ConfigStore.defaults();
  for(var field:ConfigFields.ALL) {
   String value=values.getProperty(field.key(),defaults.getProperty(field.key()));
   try { draft.put(field.key(),field.display(value)); }
   catch(NumberFormatException e) { draft.put(field.key(),value); }
  }
 }
 @Override protected void init() {
  int contentWidth=Math.min(500,width-48),x=(width-contentWidth)/2;
  var groups=ConfigFields.Group.values();int tabWidth=contentWidth/4;
  for(int i=0;i<groups.length;i++) {
   var target=groups[i];int row=i/4,count=Math.min(4,groups.length-row*4);
   int start=width/2-count*tabWidth/2;
   Button tab=Button.builder(Component.literal(target.label),b->{group=target;scrollPixels=0;rebuildWidgets();})
    .bounds(start+(i%4)*tabWidth,37+row*23,tabWidth-3,20).build();
   tab.active=target!=group;addRenderableWidget(tab);
  }
  fields=ConfigFields.ALL.stream().filter(f->f.group()==group).toList();
  rowOffsets.clear();rowHeights.clear();int total=0;
  for(var field:fields){
   int lines=font.split(Component.literal(field.label()),contentWidth-82).size()+font.split(Component.literal(secondary(field)),contentWidth-82).size();
   int step=Math.max(38,lines*font.lineHeight+18);rowOffsets.add(total);rowHeights.add(step);total+=step;
  }
  layoutContent(total,101,height-67,500);
  for(int i=0;i<fields.size();i++) {
   var field=fields.get(i);int y=listTop+rowOffsets.get(i)-scrollPixels+(rowHeights.get(i)-26)/2;
   if(!controlVisible(y,20))continue;
   Tooltip tooltip=Tooltip.create(Component.literal(field.label()+"\n"+field.description()+"\nCurrent: "+summary(field)));
   if(field.kind()==ConfigFields.Kind.BOOLEAN) {
    Button toggle=Button.builder(toggleText(field),b->{
     draft.put(field.key(),Boolean.toString(!Boolean.parseBoolean(draft.get(field.key()))));
     b.setMessage(toggleText(field));edited();
    }).bounds(listX+listWidth-60,y,60,20).tooltip(tooltip).build();addRenderableWidget(toggle);
   } else {
    addRenderableWidget(Button.builder(Component.literal("Edit"),b->minecraft.gui.setScreen(new FieldEditor(field)))
     .bounds(listX+listWidth-60,y,60,20).tooltip(tooltip).build());
   }
  }
  addRenderableWidget(Button.builder(Component.literal("Defaults"),b->{
   Properties defaults=ConfigStore.defaults();
   for(var field:fields)draft.put(field.key(),field.display(defaults.getProperty(field.key())));
   edited();rebuildWidgets();
  }).bounds(width/2-144,height-28,92,20).tooltip(Tooltip.create(Component.literal("Reset this category. Save to apply."))).build());
  addRenderableWidget(Button.builder(Component.literal("Save"),b->save()).bounds(width/2-46,height-28,92,20).build());
  addRenderableWidget(Button.builder(Component.literal("Back"),b->onClose()).bounds(width/2+52,height-28,92,20)
   .tooltip(Tooltip.create(Component.literal("Discard unsaved changes and return."))).build());
 }
 private Component toggleText(ConfigFields.Field field) {
  boolean on=Boolean.parseBoolean(draft.get(field.key()));
  return Component.literal(on?"ON":"OFF").withStyle(on?ChatFormatting.GREEN:ChatFormatting.RED);
 }
 private String summary(ConfigFields.Field field) {
  String value=draft.get(field.key());
  if(value.isEmpty())return "None configured";
  if(field.kind()==ConfigFields.Kind.PERCENT)return value+"%";
  if(field.kind()==ConfigFields.Kind.CHOICE)return field.choiceLabel(value);
  if(field.kind()==ConfigFields.Kind.TEXT)return java.util.Arrays.stream(value.split(",")).map(v->pretty(v.trim().substring(v.trim().indexOf(':')+1))).collect(java.util.stream.Collectors.joining(", "));
  return value;
 }
 private String secondary(ConfigFields.Field field){return field.kind()==ConfigFields.Kind.BOOLEAN?field.description():"Current: "+summary(field);}
 private static String pretty(String value) {
  String lower=value.toLowerCase(java.util.Locale.ROOT).replace('_',' ');
  return lower.isEmpty()?lower:Character.toUpperCase(lower.charAt(0))+lower.substring(1);
 }
 private void edited() {status="Unsaved changes • Back discards";statusColor=0xFFFFFF55;}
 private boolean save() {
  try {
   Properties values=new Properties();
   for(var field:ConfigFields.ALL)values.setProperty(field.key(),field.stored(draft.get(field.key())));
   new Config(values);ConfigStore.save(Humbled.configPath(),values);
   status="Saved • Reopen your world to apply changes";statusColor=0xFF55FF55;return true;
  } catch(Exception e) {status=e.getMessage()==null?"Could not save settings":e.getMessage();statusColor=0xFFFF5555;return false;}
 }
 @Override public void onClose() {minecraft.gui.setScreen(parent);}
 private static void background(Screen screen,GuiGraphicsExtractor graphics) {
  Screen.extractMenuBackgroundTexture(graphics,Identifier.withDefaultNamespace("textures/block/deepslate.png"),0,0,0,0,screen.width,screen.height);
  graphics.fill(0,0,screen.width,screen.height,0xB0101418);
 }
 @Override public void extractBackground(GuiGraphicsExtractor graphics,int mx,int my,float delta) {background(this,graphics);}
 @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta) {
  super.extractRenderState(g,mx,my,delta);
  g.centeredText(font,title,width/2,10,0xFFFFFFFF);
  String note=minecraft.getCurrentServer()!=null&&minecraft.getSingleplayerServer()==null
   ?"Multiplayer uses server settings; this edits local worlds":"Save changes, then reopen your world";
  g.centeredText(font,Component.literal(font.plainSubstrByWidth(note,width-20)),width/2,24,0xFFB7C7CC);
  g.centeredText(font,Component.literal(group.label+" settings"),width/2,87,0xFFFFE0A1);
  g.enableScissor(listX,listTop,listX+listWidth,listBottom);
  for(int i=0;i<fields.size();i++) {
   var field=fields.get(i);int y=listTop+rowOffsets.get(i)-scrollPixels,step=rowHeights.get(i);
   if(y+step<=listTop||y>=listBottom)continue;
   g.fill(listX,y,listX+listWidth-67,y+step-5,0x80333C42);
   int textWidth=listWidth-82;
   g.textWithWordWrap(font,Component.literal(field.label()),listX+7,y+5,textWidth,0xFFFFE0A1);
   int descriptionY=y+8+font.split(Component.literal(field.label()),textWidth).size()*font.lineHeight;
   g.textWithWordWrap(font,Component.literal(secondary(field)),listX+7,descriptionY,textWidth,0xFFB7C7CC);
  }
  g.disableScissor();
  drawScrollbar(g);
  g.centeredText(font,Component.literal(font.plainSubstrByWidth(status,width-20)),width/2,height-54,statusColor);
  g.centeredText(font,Component.literal("Back discards unsaved changes"),width/2,height-41,0xFFAAAAAA);
 }
 private final class FieldEditor extends ScrollingConfigScreen {
  private final ConfigFields.Field field;
  private String pending;
  private String error="";
  FieldEditor(ConfigFields.Field field) {super(Component.literal(field.label()));this.field=field;pending=draft.get(field.key());}
  @Override protected void init() {
   int fieldWidth=Math.min(360,width-48),x=width/2-fieldWidth/2;
   layoutContent(inputY()-35+26,35,height-80,420);
   int y=inputY()-scrollPixels;
   if(controlVisible(y,20)){
   if(field.kind()==ConfigFields.Kind.CHOICE) {
    int count=field.choices().size(),choiceWidth=(fieldWidth-(count-1)*4)/count;
    for(int i=0;i<count;i++){
     String choice=field.choices().get(i);
     Button option=Button.builder(Component.literal(field.choiceLabel(choice)),b->{pending=choice;error="";rebuildWidgets();})
      .bounds(x+i*(choiceWidth+4),y,choiceWidth,20).build();
     option.active=!choice.equals(pending);addRenderableWidget(option);
    }
   } else {
    EditBox box=new EditBox(font,x,y,fieldWidth,20,Component.literal(field.label()));
    box.setMaxLength(field.kind()==ConfigFields.Kind.TEXT?4096:32);box.setValue(pending);box.setResponder(v->pending=v);
    box.setTooltip(Tooltip.create(Component.literal(field.kind()==ConfigFields.Kind.PERCENT?"Enter a percentage from 0 to 100":field.description())));
    addRenderableWidget(box);setInitialFocus(box);
   }
   }
   addRenderableWidget(Button.builder(Component.literal("Save"),b->{
    String before=draft.get(field.key());draft.put(field.key(),pending);
    if(save())minecraft.gui.setScreen(HumbledConfigScreen.this);
    else {draft.put(field.key(),before);error=status;}
   }).bounds(width/2-98,height-28,94,20).build());
   addRenderableWidget(Button.builder(Component.literal("Back"),b->onClose()).bounds(width/2+4,height-28,94,20).build());
  }
  private int inputY() {
   int textWidth=Math.min(420,width-48);
   int descriptionBottom=35+font.split(Component.literal(field.description()),textWidth).size()*font.lineHeight;
   return Math.max(descriptionBottom+24,height/2-10);
  }
  @Override public void onClose() {minecraft.gui.setScreen(HumbledConfigScreen.this);}
  @Override public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float delta) {background(this,g);}
  @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta) {
   super.extractRenderState(g,mx,my,delta);g.centeredText(font,title,width/2,14,0xFFFFE0A1);
   int textWidth=Math.min(420,width-48);
   g.enableScissor(listX,listTop,listX+listWidth,listBottom);
   g.textWithWordWrap(font,Component.literal(field.description()),width/2-textWidth/2,35-scrollPixels,textWidth,0xFFB7C7CC);
   g.centeredText(font,Component.literal(field.kind()==ConfigFields.Kind.CHOICE?"Choose a mode":field.kind()==ConfigFields.Kind.PERCENT?"Value (%)":"Value"),width/2,inputY()-scrollPixels-14,0xFFFFFFFF);
   g.disableScissor();drawScrollbar(g);
   g.centeredText(font,Component.literal(font.plainSubstrByWidth(error,width-20)),width/2,height-65,0xFFFF5555);
   g.centeredText(font,Component.literal("Save applies • Back discards this entry"),width/2,height-50,0xFFAAAAAA);
   g.centeredText(font,Component.literal("Reopen the world after saving"),width/2,height-39,0xFFAAAAAA);
  }
 }
}
