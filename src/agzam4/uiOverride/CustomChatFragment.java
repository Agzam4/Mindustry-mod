package agzam4.uiOverride;

import static arc.Core.input;
import static arc.Core.scene;
import static mindustry.Vars.maxTextLength;
import static mindustry.Vars.mobile;
import static mindustry.Vars.net;
import static mindustry.Vars.player;
import static mindustry.Vars.ui;

import agzam4.ModWork;
import arc.Core;
import arc.Events;
import arc.Input.TextInput;
import arc.func.Boolp;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Font;
import arc.graphics.g2d.GlyphLayout;
import arc.math.Mathf;
import arc.scene.Group;
import arc.scene.ui.Label;
import arc.scene.ui.TextField;
import arc.scene.ui.Label.LabelStyle;
import arc.scene.ui.TextField.TextFieldStyle;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Align;
import arc.util.Reflect;
import arc.util.Time;
import mindustry.Vars;
import mindustry.game.EventType.ClientChatEvent;
import mindustry.gen.Call;
import mindustry.gen.Iconc;
import mindustry.input.Binding;
import mindustry.ui.Fonts;
import mindustry.ui.fragments.ChatFragment;

public class CustomChatFragment extends Table {

	private static final int messagesShown = 10;

	private Seq<String> messages = new Seq<>();
	private TextField chatfield;
	private Label fieldlabel;
	private GlyphLayout layout;
	private Seq<String> history;
	
	public static Font font = Fonts.outline;
	private float fadetime;
	private boolean shown = false;
	private ChatMode mode = ChatMode.normal;
	private float offsetx = Scl.scl(4), offsety = Scl.scl(4), fontoffsetx = Scl.scl(2), chatspace = Scl.scl(50);
	private Color shadowColor = new Color(0, 0, 0, 0.5f);
	private float textspacing = Scl.scl(10);
	private int historyPos = 0;
	private int scrollPos = 0;

	public CustomChatFragment() {
		super();
		
		font = ModWork.setting("outline-chat") ? Fonts.outline : Fonts.def;
		
		messages = Reflect.get(UiOverride.oldChatFragment, "messages");
		chatfield = Reflect.get(UiOverride.oldChatFragment, "chatfield");
		fieldlabel = Reflect.get(UiOverride.oldChatFragment, "fieldlabel");
		layout = Reflect.get(UiOverride.oldChatFragment, "layout");
		history = Reflect.get(UiOverride.oldChatFragment, "history");

//		ChatFragment
		
        setFillParent(true);
//        font = Fonts.outline;
        
        history.insert(0, "");
        setup();
		
		visible(() -> {
			if(!net.active() && messages.size > 0){
				clearMessages();

				if(shown){
					hide();
				}
			}
			return net.active() && ui.hudfrag.shown;
		});
		
		// короче главное меню >> модификации >> Бразуер модификаций >> в поиск [gold]Agzam[] >> Agzam's Mod >> Скачать

		update(() -> {
			if(net.active() && input.keyTap(Binding.chat) && (scene.getKeyboardFocus() == chatfield || scene.getKeyboardFocus() == null || ui.minimapfrag.shown()) && !ui.consolefrag.shown()){
				toggle();
			}
			if(shown){
				if(input.keyTap(Binding.chat_history_prev) && historyPos < history.size - 1){
					if(historyPos == 0){
						String message = chatfield.getText();
//						if(mode.prefix.isEmpty()){
//							if(!message.trim().isEmpty()){
//								history.insert(0, message);
//							}
//						}else{
//							if(message.startsWith(mode.normalizedPrefix())){
//								message = message.substring(mode.normalizedPrefix().length());
//								if(!message.trim().isEmpty()){
//									history.insert(0, message);
//								}
//							}else if(message.startsWith(mode.prefix)){
//								message = message.substring(mode.prefix.length());
//								if(!message.trim().isEmpty()){
//									history.insert(0, message);
//								}
//							}
//						}
						if(!message.isEmpty()) {
							history.insert(0, message);
						}
					}
					historyPos++;
					updateChat();
				}
				if(input.keyTap(Binding.chat_history_next) && historyPos > 0){
					historyPos--;
					updateChat();
				}
				if(input.keyTap(Binding.chat_mode)){
					nextMode();
				}
				scrollPos = (int)Mathf.clamp(scrollPos + input.axis(Binding.chat_scroll), 0, Math.max(0, messages.size - messagesShown));
			}
		});
	}

	//    private static final int messagesShown = 10;
	//    private Seq<String> messages = new Seq<>();
	//    private float fadetime;
	//    private boolean shown = false;
	//    private TextField chatfield;
	//    private Label fieldlabel = new Label(">");
	//    private ChatMode mode = ChatMode.normal;
	//    private Font font;
	//    private GlyphLayout layout = new GlyphLayout();
	//    private float offsetx = Scl.scl(4), offsety = Scl.scl(4), fontoffsetx = Scl.scl(2), chatspace = Scl.scl(50);
	//    private Color shadowColor = new Color(0, 0, 0, 0.5f);
	//    private float textspacing = Scl.scl(10);
	//    private Seq<String> history = new Seq<>();
	//    private int historyPos = 0;
	//    private int scrollPos = 0;

	//    public ChatFragment(){
	//        super();
	//
	//        setFillParent(true);
	//        font = Fonts.def;
	//
	//
	//
	//        history.insert(0, "");
	//        setup();
	//    }

	public void build(Group parent){
		scene.add(this);
	}

	public void clearMessages(){
		messages.clear();
		history.clear();
		history.insert(0, "");
	}

	private void setup(){
		fieldlabel.setStyle(new LabelStyle(fieldlabel.getStyle()));
		fieldlabel.getStyle().font = font;
		fieldlabel.setStyle(fieldlabel.getStyle());

		chatfield = new TextField("", new TextFieldStyle(scene.getStyle(TextFieldStyle.class)));
//		chatfield.setStyle(new TextFieldStyle(scene.getStyle(TextFieldStyle.class)));
		chatfield.setMaxLength(Vars.maxTextLength);
		chatfield.getStyle().background = null;
		chatfield.getStyle().fontColor = Color.white;
		chatfield.setStyle(chatfield.getStyle());

		chatfield.typed(this::handleType);

		bottom().left().marginBottom(offsety).marginLeft(offsetx * 2).add(fieldlabel).padBottom(6f);

		add(chatfield).padBottom(offsety).padLeft(offsetx).growX().padRight(offsetx).height(28);

		if(Vars.mobile){
			marginBottom(105f);
			marginRight(240f);
		}
	}

	//no mobile support.
	private void handleType(char c){
		int cursor = chatfield.getCursorPosition();
		if(c == ':'){
			int index = chatfield.getText().lastIndexOf(':', cursor - 2);
			if(index >= 0 && index < cursor){
				String text = chatfield.getText().substring(index + 1, cursor - 1);
				String uni = Fonts.getUnicodeStr(text);
				if(uni != null && uni.length() > 0){
					chatfield.setText(chatfield.getText().substring(0, index) + uni + chatfield.getText().substring(cursor));
					chatfield.setCursorPosition(index + uni.length());
				}
			}
		}
	}

	protected void rect(float x, float y, float w, float h){
		//prevents texture bindings; the string lookup is irrelevant as it is only called <10 times per frame, and maps are very fast anyway
		Draw.rect("whiteui", x + w/2f, y + h/2f, w, h);
	}

	@Override
	public void draw(){
		float opacity = Core.settings.getInt("chatopacity") / 100f;
		float textWidth = Math.min(Core.graphics.getWidth()/1.5f, Scl.scl(700f));

		Draw.color(shadowColor);

		if(shown){
			rect(offsetx, chatfield.y + scene.marginBottom, chatfield.getWidth() + 15f, chatfield.getHeight() - 1);
		}

		super.draw();

		float spacing = chatspace;

		chatfield.visible = shown;
		fieldlabel.visible = shown;

		Draw.color(shadowColor);
		Draw.alpha(shadowColor.a * opacity);

		float theight = offsety + spacing + getMarginBottom() + scene.marginBottom;
		for(int i = scrollPos; i < messages.size && i < messagesShown + scrollPos && (i < fadetime || shown); i++){

			layout.setText(font, messages.get(i), Color.white, textWidth, Align.bottomLeft, true);
			theight += layout.height + textspacing;
			if(i - scrollPos == 0) theight -= textspacing + 1;

			font.getCache().clear();
			font.getCache().setColor(Color.white);
			font.getCache().addText(messages.get(i), fontoffsetx + offsetx, offsety + theight, textWidth, Align.bottomLeft, true);

			if(!shown && fadetime - i < 1f && fadetime - i >= 0f){
				font.getCache().setAlphas((fadetime - i) * opacity);
				Draw.color(0, 0, 0, shadowColor.a * (fadetime - i) * opacity);
			}else{
				font.getCache().setAlphas(opacity);
			}

			rect(offsetx, theight - layout.height - 2, textWidth + Scl.scl(4f), layout.height + textspacing);
			Draw.color(shadowColor);
			Draw.alpha(opacity * shadowColor.a);

			font.getCache().draw();
		}

		Draw.color();

		if(fadetime > 0 && !shown){
			fadetime -= Time.delta / 180f;
		}
	}

	private void sendMessage(){
		String message = chatfield.getText();
		clearChatInput();

//		if(message.startsWith(mode.prefix)){
//			message = message.substring(mode.prefix.length());
//		}
		message = message.trim();

		//avoid sending empty messages
		if(message.isEmpty()) return;

		history.insert(1, message);

		message = mode.normalizedPrefix() + message;

		Events.fire(new ClientChatEvent(message));
		Call.sendChatMessage(message);
	}

	public void toggle(){

		if(!shown){
			scene.setKeyboardFocus(chatfield);
			shown = true;
			if(mobile){
				TextInput input = new TextInput();
				input.maxLength = maxTextLength;
				input.accepted = text -> {
					chatfield.setText(text);
					sendMessage();
					hide();
					Core.input.setOnscreenKeyboardVisible(false);
				};
				input.canceled = this::hide;
				Core.input.getTextInput(input);
			}else{
				chatfield.fireClick();
			}
		}else{
			//sending chat has a delay; workaround for issue #1943
			Time.runTask(2f, () ->{
				scene.setKeyboardFocus(null);
				shown = false;
				scrollPos = 0;
				sendMessage();
			});
		}
	}

	public void hide(){
		scene.setKeyboardFocus(null);
		shown = false;
		clearChatInput();
	}

	public void updateChat(){
		// mode.normalizedPrefix() + 
		chatfield.setText(history.get(historyPos));
		updateCursor();
	}
	
	public void nextMode() {
//		ChatMode prev = mode;

		do{
			mode = mode.next();
		}while(!mode.isValid());


		if(mode == ChatMode.normal) {
			fieldlabel.setText(mode.displayText);
			fieldlabel.setColor(Color.white);
		} else if(mode == ChatMode.team) {
			fieldlabel.setText("<" + Iconc.players + ">");
			fieldlabel.setColor(player.team().color);
		} else if(mode == ChatMode.admin) {
			fieldlabel.setText("<" + Iconc.admin + ">");
			fieldlabel.setColor(Color.red);
		}
		
//		if(chatfield.getText().startsWith(prev.normalizedPrefix())){
//			chatfield.setText(mode.normalizedPrefix() + chatfield.getText().substring(prev.normalizedPrefix().length()));
//		}else{
//			chatfield.setText(mode.normalizedPrefix());
//		}

		updateCursor();
	}

	public void clearChatInput(){
		historyPos = 0;
		history.set(0, "");
		chatfield.setText("");//mode.normalizedPrefix());
		updateCursor();
	}

	public void updateCursor(){
		chatfield.setCursorPosition(chatfield.getText().length());
	}

	public boolean shown(){
		return shown;
	}

	public void addMessage(String message){
		if(message == null) return;
		messages.insert(0, message);

		fadetime += 1f;
		fadetime = Math.min(fadetime, messagesShown) + 1f;

		if(scrollPos > 0) scrollPos++;
	}

	private enum ChatMode{
		normal("", ">"), // "<" + Iconc.chat + ">"
		team("/t", "<" + Iconc.players + ">"),
		admin("/a", "<" + Iconc.admin + ">")
		;

		public String prefix, displayText;
		public Boolp valid;
		public static final ChatMode[] all = values();

		ChatMode(String prefix, String displayText) {
			this.prefix = prefix;
			this.displayText = displayText;
			this.valid = () -> true;
		}

		ChatMode(String prefix, Boolp valid){
			this.prefix = prefix;
			this.valid = valid;
		}

		public ChatMode next(){
			return all[(ordinal() + 1) % all.length];
		}

		public String normalizedPrefix(){
			return prefix.isEmpty() ? "" : prefix + " ";
		}

		public boolean isValid(){
			return valid.get();
		}
	}
}
