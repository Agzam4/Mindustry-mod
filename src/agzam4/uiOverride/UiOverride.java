package agzam4.uiOverride;

import agzam4.ModWork;
import arc.Core;
import mindustry.Vars;
import mindustry.ui.fragments.ChatFragment;

public class UiOverride {

	public static CustomChatFragmentHandle customChatFragment;
	public static ChatFragment oldChatFragment;
	
	public static void init() {
		oldChatFragment = Vars.ui.chatfrag;
		
		set();
	}
	
	public static void set() {
		if(ModWork.setting("ui.custom-chat-fragment")) {
			if(customChatFragment == null) customChatFragment = new CustomChatFragmentHandle();
			Core.scene.root.removeChild(Vars.ui.chatfrag);
			Vars.ui.chatfrag = customChatFragment;
			Vars.ui.chatfrag.build(Core.scene.root);
		} else {
			if(customChatFragment != null) {
				Core.scene.root.removeChild(customChatFragment.chatfrag);
			}
			Core.scene.root.removeChild(Vars.ui.chatfrag);
			Vars.ui.chatfrag = oldChatFragment;
			Vars.ui.chatfrag.build(Core.scene.root);
		}
		
	}

}
