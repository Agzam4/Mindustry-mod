package agzam4;

import arc.Core;
import arc.func.Cons;
import arc.func.Cons2;
import arc.graphics.Color;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.Strings;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.Liquid;
import mindustry.type.LiquidStack;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.blocks.production.Pump;
import mindustry.world.blocks.units.Reconstructor;
import mindustry.world.blocks.units.UnitFactory;
import mindustry.world.blocks.units.UnitFactory.UnitFactoryBuild;
import mindustry.world.consumers.Consume;
import mindustry.world.consumers.ConsumeItemCharged;
import mindustry.world.consumers.ConsumeItemDynamic;
import mindustry.world.consumers.ConsumeItems;
import mindustry.world.consumers.ConsumeLiquid;
import mindustry.world.consumers.ConsumeLiquids;
import mindustry.world.Block;
import mindustry.world.blocks.production.AttributeCrafter.AttributeCrafterBuild;
import mindustry.world.blocks.production.Drill.DrillBuild;
import mindustry.world.blocks.production.Pump.PumpBuild;

public class ModWork {

	public static final int gradient = 30;
	public static final float rs[] = new float[gradient];
	public static final float gs[] = new float[gradient];
	public static final float bs[] = new float[gradient];
	
	static {
		for (int i = 0; i < gradient; i++) {
			// 6 - 136
			Color c = Color.HSVtoRGB(6 + i*130/gradient, 90, 100);
			rs[i] = c.r;
			gs[i] = c.g;
			bs[i] = c.b;
		}
	}
	
	public enum KeyBinds {
		slowMovement("slow-movement", KeyCode.altLeft),
		hideUnits("hide-units", KeyCode.h),
		selection("selection", KeyCode.g),
		clearSelection("clear-selection", KeyCode.q);

		public KeyCode def;
		public KeyCode key;
		public String keybind;
		
		KeyBinds(String keybind, KeyCode def) {
			this.def = def;
			this.keybind = keybind;
			int bind = Core.settings.getInt("agzam4mod.settings.keybinds." + keybind, def.ordinal());
			if(bind < 0 || bind >= KeyCode.all.length) {
				key = def;
			} else {
				key = KeyCode.all[bind];
			}
		}

		void put() {
			Core.settings.put("agzam4mod.settings.keybinds." + keybind, key.ordinal());
		}
		
	}
	
	public static int getGradientIndex(float health, float maxHealth) {
		int index = (int) (health*gradient/maxHealth);
		if(index < 0) return 0;
		if(index >= gradient) return gradient-1;
		return index;
	}
	
	public static String round(final float d) {
		if(Mathf.round(d) == d) return "" + (int)d;
		return "" + Mathf.round(d*100)/100f;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> Seq<T> getBlocks(Class<T> type) {
		Seq<T> seq = new Seq<T>();
		Vars.content.blocks().each(b -> {
			if(type.isInstance(b)) seq.add((T) b);
		});
		return seq;
	}

	public static float getCraftSpeed(Building building) {
		Block block = building.block();
		if(block.consumers.length == 0) return 0;
		boolean hasConsumer = false;
		for (int i = 0; i < block.consumers.length; i++) {
			if(block.consumers[i] instanceof ConsumeItems
					|| block.consumers[i] instanceof ConsumeLiquid
					|| block.consumers[i] instanceof ConsumeItemDynamic) {
				hasConsumer = true;
				break;
			}
		}
		if(!hasConsumer) return 0;
		float craftSpeed = 1f;
		if(block instanceof GenericCrafter) {
			craftSpeed = 60f / ((GenericCrafter) block).craftTime;
		}
		if(building instanceof AttributeCrafterBuild) {
			craftSpeed *= ((AttributeCrafterBuild) building).efficiencyMultiplier();
		}
		if(building instanceof UnitFactoryBuild && block instanceof UnitFactory) {
			int plan = ((UnitFactoryBuild)building).currentPlan;
			if(plan == -1) return 0;
			craftSpeed = 60f/((UnitFactory)block).plans.get(plan).time;
		}
		if(block instanceof Reconstructor) {
			craftSpeed = 60f/((Reconstructor)block).constructTime;
		}
		return craftSpeed;
	}

	public static boolean isBuildingZero(Building build, int x, int y) {
		final int size = build.block.size;
    	final int from = (size/2)+1-size;
    	final int tx = build.tileX();
    	final int ty = build.tileY();
    	if(tx + from == x && ty + from == y) return true;
		return false;
	}

	public static String bungle(String string) {
		return Core.bundle.get("agzam4mod." + string, "ERR TO LOAD TEXT");
	}

	public static boolean setting(String string) {
		return Core.settings.getBool("agzam4mod.settings." + string, true);
	}
	
	public static void setting(String string, boolean value) {
		Core.settings.put("agzam4mod.settings." + string, value);
	}

	public static String strip(String name) {
		return Strings.stripGlyphs(Strings.stripColors(name));
	}

	public static final String enToRu[][] = {
			{"OO", "У"},
			{"A", "А"},
			{"B", "Б"},
			{"C", "С"},
			{"D", "Д"},
			{"E", "Е"},
			{"F", "Ф"},
			{"G", "Г"},
			{"H", "Х"},
			{"I", "И"},
			{"J", "ДЖ"},
			{"K", "К"},
			{"L", "Л"},
			{"M", "М"},
			{"N", "Н"},
			{"O", "О"},
			{"P", "П"},
			{"Q", "К"},
			{"R", "Р"},
			{"S", "С"},
			{"T", "Т"},
			{"U", "У"},
			{"V", "В"},
			{"W", "В"},
			{"X", "Х"},
			{"Y", "У"},
			{"Z", "З"},
	};
	
	public static String toRus(String stripName) {
		stripName = stripName.toUpperCase();
		for (int i = 0; i < enToRu.length; i++) {
			stripName = stripName.replaceAll(enToRu[i][0], enToRu[i][1]);
		}
		return stripName.toLowerCase();
	}


	public static void consumeItems(Consume consume, Building building, float craftSpeed, Cons2<Item, Float> cons) {
		if(consume instanceof ConsumeItems) {
			ConsumeItems items = (ConsumeItems) consume;
			ItemStack[] stacks = items.items;
			for (int item = 0; item < stacks.length; item++) {
				ItemStack stack = stacks[item];
				float ips = craftSpeed*stack.amount*building.timeScale();
				cons.get(stack.item, ips);
			}
			return;
		}
		if(consume instanceof ConsumeItemDynamic) {
			ConsumeItemDynamic dynamic = (ConsumeItemDynamic) consume;
			ItemStack[] stacks = dynamic.items.get(building);
			if(stacks == null) return;
			for (int item = 0; item < stacks.length; item++) {
				ItemStack stack = stacks[item];
				float ips = craftSpeed*stack.amount*building.timeScale();
				cons.get(stack.item, ips);
			}
			return;
		}
	}

	public static void produceItems(Building building, float craftSpeed, Cons2<Item, Float> cons) {
		if(building instanceof DrillBuild) {
			DrillBuild drill = (DrillBuild) building;
			cons.get(drill.dominantItem, drill.lastDrillSpeed*60*drill.timeScale());
		}
		if(building.block() instanceof GenericCrafter) {
			GenericCrafter crafter = (GenericCrafter) building.block();
			if(crafter.outputItems != null) {
				for (int i = 0; i < crafter.outputItems.length; i++) {
					ItemStack output = crafter.outputItems[i];
					cons.get(output.item, craftSpeed*output.amount*building.timeScale());
				}
			}
		}
	}
	
	public static void consumeLiquids(Consume consume, Building building, float craftSpeed, Cons2<Liquid, Float> cons) {
		if(consume instanceof ConsumeLiquid) {
			ConsumeLiquid liquid = (ConsumeLiquid) consume;
			float lps = craftSpeed*liquid.amount*building.timeScale();
			cons.get(liquid.liquid, lps);
			return;
		}
		if(consume instanceof ConsumeLiquids) {
			ConsumeLiquids liquids = (ConsumeLiquids) consume;
			LiquidStack[] stacks = liquids.liquids;
			if(stacks == null) return;
			for (int liquid = 0; liquid < stacks.length; liquid++) {
				LiquidStack stack = stacks[liquid];
				float lps = 60f*stack.amount*building.timeScale();
				cons.get(stack.liquid, lps);
			}
			return;
		}
	}

	public static void produceLiquids(Building building, float craftSpeed, Cons2<Liquid, Float> con) {
		if(building instanceof PumpBuild && building.block() instanceof Pump) {
			PumpBuild pump = (PumpBuild) building;
			if(pump.liquidDrop != null) {
				con.get(pump.liquidDrop, pump.amount * ((Pump)building.block()).pumpAmount * 60f * building.timeScale());
			}
		}
		if(building.block() instanceof GenericCrafter) {
			GenericCrafter crafter = (GenericCrafter) building.block();
			if(crafter.outputLiquids != null) {
				for (int i = 0; i < crafter.outputLiquids.length; i++) {
					LiquidStack output = crafter.outputLiquids[i];
					con.get(output.liquid, 60*output.amount*building.timeScale());
				}
			}
		}
	}

}
