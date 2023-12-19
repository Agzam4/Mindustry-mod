package agzam4.uiOverride;

import arc.scene.Group;
import mindustry.ui.fragments.ConsoleFragment;

public class CustomConsoleFragmentHandle extends ConsoleFragment {

	public CustomConsoleFragment consolefrag;
	
	public CustomConsoleFragmentHandle() {
		super();
		consolefrag = new CustomConsoleFragment();
	}
	
	@Override
	public void build(Group parent) {
		consolefrag.build(parent);
	}
	
	@Override
    public void clearMessages(){
		consolefrag.clearMessages();
    }

	@Override
    public void toggle() {
		consolefrag.toggle();
    }

	@Override
	public void hide() {
		consolefrag.hide();
	}

	@Override
	public void updateChat() {
		consolefrag.updateChat();
	}

	@Override
	public void clearChatInput() {
		consolefrag.clearChatInput();
	}
	
	@Override
	public boolean shown(){
		return consolefrag.shown();
	}

	@Override
	public void addMessage(String message){
		consolefrag.addMessage(message);
	}
	
}
