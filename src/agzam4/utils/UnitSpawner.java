package agzam4.utils;

import agzam4.ModWork;
import arc.Core;
import arc.graphics.Color;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.event.Touchable;
import arc.scene.style.Drawable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.TextButton;
import arc.scene.ui.TextButton.TextButtonStyle;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.util.Nullable;
import mindustry.Vars;
import mindustry.ctype.UnlockableContent;
import mindustry.game.EventType.TapEvent;
import mindustry.game.Team;
import mindustry.gen.Iconc;
import mindustry.gen.Tex;
import mindustry.type.UnitType;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;
import mindustry.world.Block;
import mindustry.world.blocks.ItemSelection;

public class UnitSpawner {

	// TODO: delayed spawn
	
	public static Table mainTable, container;
	private static Drawable background = ((TextureRegionDrawable)Tex.whiteui).tint(.2f, .2f, .2f, 1f);
	private static Drawable gray = ((TextureRegionDrawable)Tex.whiteui).tint(Color.valueOf("777777"));
	public static TextButtonStyle buttonsStyle = Styles.grayt,
			toggleButtonsStyle = new TextButtonStyle(){{
			            font = Fonts.outline;
			            fontColor = Color.white;
			            checked = gray; //
			            down = gray; //
			            up = Styles.black;
			            over = Styles.flatOver;
			            disabled = Styles.black;
			            disabledFontColor = Color.gray;
			        }};;
	
	private static @Nullable UnlockableContent selected = null;
	private static @Nullable Team team = null;

	private static Cell<TextButton>[] teamsCells;
	
	@SuppressWarnings("unchecked")
	public static void build() {
		mainTable = new Table();
		mainTable.row();
		
		container = mainTable.table(Styles.grayPanel).touchable(Touchable.enabled)
				.width(300) // 300
				.height(450)
				.get();
		
		applyStyle(container.button(Iconc.cancel + "", buttonsStyle, UnitSpawner::hide));//.row();

		container.row();
//		container.row();
//		container.row();

		Table teamTable = container.table(background).touchable(Touchable.enabled)
				.width(300) // 300
				.height(50)
				.get();
		container.row();
		
		teamsCells = new Cell[Team.baseTeams.length];
		for (int i = 0; i < Team.baseTeams.length; i++) {
			final int ii = i;
			Team t = Team.baseTeams[i];
			teamsCells[i] = applyStyle(teamTable.button(t.emoji.isEmpty() ? t.localized().substring(0, 1) : t.emoji, toggleButtonsStyle, () -> {
				team = t;
				for (int j = 0; j < teamsCells.length; j++) {
					teamsCells[j].checked(j == ii);
				}
			}).color(t.color));
		}

		container.row();
		
		container.setPosition(Core.scene.getWidth(), Core.scene.getHeight());
		
		Core.scene.add(mainTable);
//
		new Dragg(mainTable, mainTable).setPosition(
				ModWork.settingFloat("mobile-payloadspawn-x", 0), 
				ModWork.settingFloat("mobile-payloadspawn-y", 0)
				);
		
		mainTable.visible(() -> visible && ModWork.acceptKeyNoFocus());
		
		ItemSelection.buildTable(container, 
			Vars.content.units().select(b -> true).<UnlockableContent>as().add(Vars.content.blocks().select(b -> true).as()),
			() -> selected, 
			b -> {
				selected = b;
			}, 7,7
		);
		
		
	}
	
	private static Cell<TextButton> applyStyle(Cell<TextButton> cell) {
		return cell.width(50).height(50).margin(0).pad(0);
	}
	
	static boolean visible = true;

	public static void show() {
		PlayerUtils.hide();
		visible = true;
	}

	public static void hide() {
		visible = false;
	}

	public static void ontap(TapEvent e) {
		if(!visible) return;
		if(selected == null) return;
		if(e.player != Vars.player) return;
		Team t = team == null ? e.player.team() : team;
		if(selected instanceof UnitType) {
			UnitType ut = (UnitType) selected;
			ut.spawn(e.tile, t);
		}
		if(selected instanceof Block) {
			e.tile.setBlock((Block) selected, t);
		}
	}

	static class Dragg {
		
		Element dragger, parent;
		
		protected Vec2 draggStart = null;
		
		public Dragg(Element dragger, Element parent) {
			this.dragger = dragger;
			this.parent = parent;
			
			dragger.addListener(new InputListener() {

				@Override
				public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
					draggStart = new Vec2(x, y);
					return true;
				}

				@Override
				public void touchDragged(InputEvent e, float x, float y, int pointer) {
					if(e.targetActor != mainTable && e.targetActor != container) return;
					update(x, y);
				}

				@Override
				public void touchUp(InputEvent e, float x, float y, int pointer, KeyCode button) {
					if(e.targetActor != mainTable && e.targetActor != container) return;
					update(x, y);
					draggStart = null;

					ModWork.setting("mobile-payloadspawn-x", parent.x);
					ModWork.setting("mobile-payloadspawn-y", parent.y);
				}
			});
		}

		public void update(float x, float y) {
			setPosition(
					Mathf.clamp(parent.x-draggStart.x+x, 
							parent.getPrefWidth()/2f, 
							Core.scene.getWidth() - parent.getPrefWidth()/2f),
					
					Mathf.clamp(parent.y-draggStart.y+y,
							parent.getPrefHeight()/2f, 
							Core.scene.getHeight() - parent.getPrefHeight()/2f));
		}

		public void setPosition(float x, float y) {
			parent.setPosition(
					Mathf.clamp(x, parent.getPrefWidth()/2f, 
							Core.scene.getWidth() - parent.getPrefWidth()/2f),
					Mathf.clamp(y, parent.getPrefHeight()/2f, 
							Core.scene.getHeight() - parent.getPrefHeight()/2f));
		}
	}
}
