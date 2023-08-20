package agzam4.utils;

import agzam4.ModWork;
import arc.Core;
import arc.graphics.Color;
import arc.scene.ui.layout.Table;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

public class PlayerUtils {
	
	private static BaseDialog utilsDialog;
	
	public static void build() {
		ProcessorGenerator.build();
		
		utilsDialog = new BaseDialog(ModWork.bungle("dialog.utils"));
		utilsDialog.title.setColor(Color.white);
		utilsDialog.titleTable.remove();
		utilsDialog.closeOnBack();
//		utilsDialog.defaults().left().pad(3f);
//		PlayerListFragment

		utilsDialog.cont.pane(p -> {
//            t.defaults().left().pad(3f);

			p.defaults().left();
			
			Table t = new Table();
			p.add(t).row();

            t.button(Blocks.microProcessor.emoji() + " " + ModWork.bungle("dialog.utils.processor-generator"), Styles.defaultt, () -> {
            			ProcessorGenerator.show();
            }).growX().pad(10).padBottom(4).wrapLabel(false).row();
            
//            if(Vars.mobile)
            t.button("@back", Styles.defaultt, () -> {
            	hide();
            }).growX().pad(10).padBottom(4).wrapLabel(false).row();
		});
	}
	
	public static void show() {
		if(utilsDialog.isShown()) return;
		utilsDialog.show();
	}

	public static void hide() {
		utilsDialog.hide();
	}
}
