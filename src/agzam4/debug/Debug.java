package agzam4.debug;

import mindustry.Vars;

public class Debug {

	public static void init() {
		Vars.mods.getScripts().context.evaluateString(Vars.mods.getScripts().scope,
				"var mod = Vars.mods.getMod(\"agzam4mod\");\n"
				+ "var get = (pkg) => mod.loader.loadClass(pkg).newInstance();\n"
				+ "const AgzamDebug = get(\"agzam4.debug.Debug\")\n"
				+ "const AgzamUI = get(\"agzam4.MobileUI\")", "main.js", 0);
	}
	
}
