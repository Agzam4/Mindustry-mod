package agzam4;

import arc.ApplicationListener;
import arc.Core;
import arc.Events;
import arc.KeyBinds.Section;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.input.InputDevice.DeviceType;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.ui.Dialog;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.util.Log;
import arc.util.Strings;
import arc.util.Time;
import mindustry.Vars;
import mindustry.game.EventType.ClientServerConnectEvent;
import mindustry.game.EventType.PlayerChatEvent;
import mindustry.game.EventType.Trigger;
import mindustry.game.EventType.UnitDamageEvent;
import mindustry.gen.Call;
import mindustry.gen.Icon;
import mindustry.gen.Iconc;
import mindustry.graphics.Pal;
import mindustry.mod.Mod;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable;
import mindustry.world.meta.BuildVisibility;

//import java.awt.AWTException;
//import java.awt.Image;
//import java.awt.SystemTray;
//import java.awt.Toolkit;
//import java.awt.TrayIcon;
//import java.awt.TrayIcon.MessageType;
//import java.awt.image.BufferedImage;
//import java.util.concurrent.TimeUnit;

import agzam4.ModWork.KeyBinds;
import agzam4.debug.Debug;
import agzam4.utils.PlayerUtils;
import agzam4.utils.ProcessorGenerator;

public class AgzamMod extends Mod {

	public static boolean hideUnits;
	private static UnitTextures[] unitTextures;
	private static TextureRegion minelaser, minelaserEnd;
	private Cell<TextButton> unlockContent = null, unlockBlocks = null;

	private boolean isPaused = false;
	private long pauseStartTime = System.nanoTime();
	
	int pauseRandomNum = 0;
	String pingText = "@Agzam 000";

	private boolean afkAvalible;
	
	private boolean debug = false; // FIXME
	
	public static long updates = 0;
	
	@Override
	public void init() {
		try {
			Debug.init();
			CursorTracker.init();
			IndustryCalculator.init();
			PlayerUtils.build();
		try {
			try {
				MyTray.avalible = MyTray.avalible();
			} catch (Error e) {

			} 
		} catch (Throwable e) {
		}
		
		minelaser = Core.atlas.find("minelaser");
		minelaserEnd = Core.atlas.find("minelaser-end");
		unitTextures = new UnitTextures[Vars.content.units().size];
		for (int i = 0; i < unitTextures.length; i++) {
			unitTextures[i] = new UnitTextures(Vars.content.unit(i));
		}
		
		Core.scene.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, KeyCode keyCode) {
                if (ModWork.acceptKey()) {
                	if(keyCode.equals(KeyBinds.hideUnits.key)) {
                		hideUnits(!hideUnits);
                	}
                	if(keyCode.equals(KeyBinds.openUtils.key)) {
                		PlayerUtils.show();
                	}
                }

                return super.keyDown(event, keyCode);
            }
        });
		boolean needUpdate = UpdateInfo.needUpdate();
		
		Cons<SettingsTable> builder = settingsTable -> {
			settingsTable.defaults().left(); // .size(350f, 800f)
			
			Table table = new Table();
			
			if(needUpdate) {
				table.add(ModWork.bungle("need-update")).color(Color.red).colspan(4).pad(10).padBottom(4).row();
			}
			
			addCategory(table, "unlock");
            
			unlockContent = table.button(ModWork.bungle("settings.unlock-content"), Icon.lockOpen, Styles.defaultt, () -> {
				unlockDatabaseContent();
				if(unlockContent != null) unlockContent.disabled(true);
			}).growX().pad(10).padBottom(4);
			table.row();
			
			unlockBlocks = table.button(ModWork.bungle("settings.unlock-blocks"), Icon.lockOpen, Styles.defaultt, () -> {
				unlockBlocksContent();
				if(unlockBlocks != null) unlockBlocks.disabled(true);
			}).growX().pad(10).padBottom(4);
			table.row();

			addCategory(table, "cursors");
            addCheck(table, "cursors-tracking");

			addCategory(table, "units-and-buildings");
            addCheck(table, "show-turrets-range");
            addCheck(table, "show-build-health");
            addCheck(table, "show-units-health");
			addKeyBind(table, KeyBinds.hideUnits);
			addKeyBind(table, KeyBinds.slowMovement);

			addCategory(table, "calculations");
			addCheck(table, "show-blocks-tooltip");
			addCheck(table, "selection-calculations");
			addCheck(table, "buildplans-calculations");
			
			addKeyBind(table, KeyBinds.selection);
			addKeyBind(table, KeyBinds.clearSelection);
			
			addCategory(table, "afk");
			
			try {
				afkAvalible = true;
				if(MyTray.avalible && !Vars.mobile) {
					table.field(getCustomAfk(), t -> {
						Core.settings.put("agzam4mod.afk-start", t);
					}).tooltip(ModWork.bungle("afk.automessage-start-tooltip")).width(Core.scene.getWidth()/2f).row();
				} else {
					afkAvalible = false;
			        table.add(ModWork.bungle("afk-err")).color(Color.red).colspan(4).pad(10).padBottom(4).row();
				}
			} catch (Throwable e) {
				afkAvalible = false;
		        table.add(ModWork.bungle("afk-err")).color(Color.red).colspan(4).pad(10).padBottom(4).row();
			}
			
            settingsTable.add(table);
            settingsTable.row();
//
			settingsTable.name = ModWork.bungle("settings.name");
			settingsTable.visible = true;
			
			addCategory(table, "utils");
			addKeyBind(table, KeyBinds.openUtils);
			

			addCategory(table, "report-bugs");
			table.button(Iconc.github + " Github", Styles.defaultt, () -> {
	            if(!Core.app.openURI("https://github.com/Agzam4")){
	                Vars.ui.showErrorMessage("@linkfail");
	                Core.app.setClipboardText("https://github.com/Agzam4");
	            }
			}).growX().pad(20).padBottom(4);
			table.row();
			table.button(Iconc.play + " YouTube", Styles.defaultt, () -> {
	            if(!Core.app.openURI("https://www.youtube.com/@agzam4/")){
	            	Vars.ui.showErrorMessage("@linkfail");
	                Core.app.setClipboardText("https://www.youtube.com/@agzam4/");
	            }
			}).growX().pad(20).padBottom(4);
			table.row();
		};
		
		if(needUpdate) {
			Vars.ui.settings.addCategory(ModWork.bungle("settings.name") + " [red]" + Iconc.warning, Icon.wrench, builder);
		} else {
			Vars.ui.settings.addCategory(ModWork.bungle("settings.name"), Icon.wrench, builder);
		}
		
		Events.run(Trigger.update, () -> {
			updates++;
			IndustryCalculator.update();
			if(Core.input.keyDown(KeyBinds.slowMovement.key)) {
				if(Vars.player.unit() != null) {
					Vars.player.unit().vel.scl(.5f);
				}
			}
		});
		
		Events.run(Trigger.drawOver, () -> {
			CursorTracker.draw();
			DamageNumbers.draw();
			FireRange.draw();
			IndustryCalculator.draw();
			ProcessorGenerator.draw();
			Draw.color();
		});
		
		Events.on(UnitDamageEvent.class, e -> {
			DamageNumbers.unitDamageEvent(e);
		});
		
		Events.on(PlayerChatEvent.class, e -> {
			if(!afkAvalible) return;
			if(!MyTray.avalible) return;
			if(!ModWork.setting("afk-ping")) return;
			if(e.message == null) return;
			if(!isPaused) return;
			if(e.player.plainName().equals(Vars.player.plainName())) return;

			String stripName = ModWork.strip(Vars.player.name).replaceAll(" ", "_");
			String ruName = ModWork.toRus(stripName);
			
			long afk = getAfkTimeInSec();
			if(afk >= 10) {
				String msg = Strings.stripColors(e.message).toLowerCase();
				if(msg.startsWith(pingText)) {
					msg = msg.substring(pingText.length());
					createPingText(stripName);
					MyTray.message(Strings.stripColors(e.player.name()) + ": " + msg);
			        Call.sendChatMessage("[lightgray]" + ModWork.bungle("afk.message-send"));
					return;
				}
				if(msg.indexOf(ruName) != -1 || msg.indexOf(stripName.toLowerCase()) != -1) {
					createPingText(stripName);
					String time = Mathf.floor(afk/60) + " " + ModWork.bungle("afk.min");
					if(afk < 60) {
						time = afk + " " + ModWork.bungle("afk.sec");
					}
					String text = "[lightgray]" + getCustomAfk()
							.replaceAll("@name", Strings.stripColors(stripName))
							.replaceAll("@time", time + "");
					if(text.indexOf("@pingText") == -1) {
						text += ModWork.bungle("afk.automessage-end").replaceFirst("@pingText", pingText);
					} else {
						text = text.replaceAll("@pingText", pingText);
					}
					Call.sendChatMessage(text); 
					//"[lightgray]Похоже Agzam в АФК уже " + time + ". Напиши [orange]" + pingText + " ваше_сообщение[], чтобы отправить ему сообщение");
				}
			}
		});
		
		// Check if player in net game to save traffic and don't get err
		Events.on(ClientServerConnectEvent.class, e -> { 
			if(!UpdateInfo.isCurrentSessionChecked) {
				UpdateInfo.check();
			}
		});

		// mobile OK
		
		if(debug) {
			MobileUI.build();
		}
		if(Vars.mobile) {
			MobileUI.build();
		} else {
			Core.app.addListener(new ApplicationListener() {

				@Override
				public void pause() {
					isPaused = true;
					pauseStartTime = Time.nanos();
				}
				
				@Override
				public void resume() {
					isPaused = false;
				}
			});	
		}
			

		if(true) return;
			
		} catch (Throwable e) {
			Log.err(e);
			if(true) return;
		}
	}
	
	private String getCustomAfk() {
		String def = ModWork.bungle("afk.automessage");
		String str = Core.settings.getString("agzam4mod.afk-start", def);
		if(str.isEmpty()) return def;
		return str;
	}

	private void addKeyBind(Table table, final KeyBinds keybind) {
        Table hotkeyTable = new Table();
        hotkeyTable.add().height(10);
        hotkeyTable.row();
        hotkeyTable.add(ModWork.bungle("settings.keybinds." + keybind.keybind), Color.white).left().padRight(40).padLeft(8);
        hotkeyTable.label(() -> keybind.key.toString()).color(Pal.accent).left().minWidth(90).padRight(20);
        hotkeyTable.button("@settings.rebind", Styles.defaultt, () -> {
        	if(ModWork.hasKeyBoard()) {
            	openDialog(keybind);
        	}
        }).width(130f);
        hotkeyTable.button("@settings.resetKey", Styles.defaultt, () -> {
        	keybind.key = keybind.def;
        	keybind.put();
        }).width(130f).pad(2f).padLeft(4f);
        hotkeyTable.row();
        table.add(hotkeyTable);
        table.row();		
	}

	private void addCategory(Table table, String category) {
        table.add(ModWork.bungle("category." + category)).color(Pal.accent).colspan(4).pad(10).padBottom(4).row();
		table.image().color(Pal.accent).fillX().height(3).pad(6).colspan(4).padTop(0).padBottom(10).row();		
	}

	private void addCheck(Table table, String settings) {
		table.check(ModWork.bungle("settings." + settings), ModWork.setting(settings), b -> {
			ModWork.setting(settings, b);
		}).colspan(4).pad(10).padBottom(4).tooltip(ModWork.bungle("settings-tooltip." + settings)).row();;
	}
	
	private void createPingText(String stripName) {
		pingText = ("@" + stripName + " " + Mathf.random(100, 999)).toLowerCase();
	}

	private long getAfkTimeInSec() {
		long afk = System.nanoTime()-pauseStartTime;
		
		return afk / Time.nanosPerMilli / 1000;
	}

	private void unlockDatabaseContent() {
		Vars.content.units().each(u -> u.hidden = false);
		Vars.content.items().each(i -> i.hidden = false);
		Vars.content.liquids().each(l -> l.hidden = false);		
	}
	
	private void unlockBlocksContent() {
		Vars.content.blocks().each(b -> {
			b.buildVisibility = BuildVisibility.shown;
		});
	}

//	private float megaAccel, megaDragg, megaSpeed;

//	private void comfortMega(boolean b) {
//		comfortMega = b;
//		Core.settings.put("agzam4mod-units.settings.comfortMega", b);
//		Core.settings.saveValues();
//		if(comfortMega) {
//			mega.accel = emanate.accel;
//			mega.drag = emanate.drag;
////			mega.speed = 3;
//		} else {
//			mega.accel = megaAccel;
//			mega.drag = megaDragg;
////			mega.speed = megaSpeed;
//		}
//	}
	
	public static void hideUnits(boolean b) {
		hideUnits = b;
		if(b) {
			for (int i = 0; i < unitTextures.length; i++) {
				unitTextures[i].hideTextures();
				unitTextures[i].hideEngines();
			}
			Core.atlas.addRegion("minelaser", UnitTextures.none);
			Core.atlas.addRegion("minelaser-end", UnitTextures.none);
		} else {
			for (int i = 0; i < unitTextures.length; i++) {
				unitTextures[i].returnTextures();
				unitTextures[i].returnEngines();
			}
			Core.atlas.addRegion("minelaser", minelaser);
			Core.atlas.addRegion("minelaser-end", minelaserEnd);
		}
	}

	Section section = Core.keybinds.getSections()[0];
    
	private void openDialog(final KeyBinds keybind) {
		Dialog keybindDialog = new Dialog(Core.bundle.get("keybind.press"));

		keybindDialog.titleTable.getCells().first().pad(4);
			
        if(section.device.type() == DeviceType.keyboard){

        	keybindDialog.addListener(new InputListener(){
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
                    if(Core.app.isAndroid()) return false;
                    rebind(keybindDialog, keybind, button);
                    return false;
                }

                @Override
                public boolean keyDown(InputEvent event, KeyCode button){
                	keybindDialog.hide();
                    if(button == KeyCode.escape) return false;
                    rebind(keybindDialog, keybind, button);
                    return false;
                }

                @Override
                public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY){
                    keybindDialog.hide();
                    rebind(keybindDialog, keybind, KeyCode.scroll);
                    return false;
                }
            });
        }

        keybindDialog.show();
        Time.runTask(1f, () -> keybindDialog.getScene().setScrollFocus(keybindDialog));
    }
	
	void rebind(Dialog rebindDialog, KeyBinds keyBinds, KeyCode newKey){
        rebindDialog.hide();
        keyBinds.key = newKey;
        keyBinds.put();
    }
	
	static TextureRegion sprite(String name) {
		return new TextureRegion(Core.atlas.find("agzam4mod-" + name));
	}
	//  Core.settings.put("agzam4mod-units.settings.hideUnitsHotkey", new java.lang.Integer(75))
	// Core.settings.getInt("agzam4mod-units.settings.hideUnitsHotkey", KeyCode.h.ordinal())
}
