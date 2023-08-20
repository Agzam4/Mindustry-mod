package agzam4;

import arc.Core;
import arc.util.Http;
import arc.util.Log;
import arc.util.serialization.Jval;
import arc.util.serialization.Jval.JsonArray;
import mindustry.Vars;

public class UpdateInfo {
	
	public static boolean isCurrentSessionChecked = false;
	
	public static void check() {
		Log.info("Checking mod updates");
		Http.get(Vars.ghApi + "/repos/Agzam4/Mindustry-mod/releases", res -> {
			String version = Vars.mods.getMod("agzam4mod").meta.version;
			if(version == null) version = "1.0";
			
			Jval json = Jval.read(res.getResultAsString());
			JsonArray releases = json.asArray();
			if(releases.size > 0) {
				Jval latest = releases.get(0);
				String ver = latest.getString("tag_name");
				ver = ver.replaceFirst("v", "");
				Log.info("Version: @/@", version, ver);
				ModWork.setting("needupdate", !version.equalsIgnoreCase(ver));
			}
			UpdateInfo.isCurrentSessionChecked = true;
		}, t -> Core.app.post(() -> {}));
	}
	
	public static boolean needUpdate() {
		return ModWork.settingDef("needupdate", false);
	}
	
}
