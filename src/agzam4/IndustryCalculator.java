package agzam4;

import static agzam4.ModWork.bs;
import static agzam4.ModWork.gs;
import static agzam4.ModWork.rs;
import static mindustry.Vars.control;

import agzam4.ModWork.KeyBinds;
import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Font;
import arc.graphics.g2d.GlyphLayout;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.scene.Element;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Align;
import arc.util.Nullable;
import arc.util.pooling.Pools;
import mindustry.Vars;
import mindustry.core.World;
import mindustry.entities.units.BuildPlan;
import mindustry.game.EventType.TileChangeEvent;
import mindustry.game.EventType.WorldLoadEndEvent;
import mindustry.game.Schematic;
import mindustry.gen.Building;
import mindustry.gen.Iconc;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.mod.Mod;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.Liquid;
import mindustry.ui.Fonts;
import mindustry.ui.fragments.HudFragment;
import mindustry.world.Block;
import mindustry.world.Build;
import mindustry.world.Tile;
import mindustry.world.blocks.production.Drill;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.blocks.production.Pump;
import mindustry.world.blocks.production.SolidPump;
import mindustry.world.blocks.units.UnitFactory;
import mindustry.world.blocks.units.UnitFactory.UnitFactoryBuild;
import mindustry.world.consumers.Consume;
import mindustry.world.consumers.ConsumeItems;
import mindustry.world.consumers.ConsumeLiquid;

public class IndustryCalculator {

	private static final Seq<Drill> drills = ModWork.getBlocks(Drill.class);
	private static final Seq<Pump> pumps = ModWork.getBlocks(Pump.class);
	
	private static final Seq<Block>[] crafters = createCrafters();
	private static final Seq<Block>[] liquidCrafters = createLiquidCrafters();
	
	
	public static boolean[] hasLiquid = new boolean[Vars.content.liquids().size]; // that can be got using pumps
	
	static BalanceFragment balanceFragment;
	
	public static void init() {
		balanceFragment = new BalanceFragment();
		balanceFragment.build();
		
		Events.on(WorldLoadEndEvent.class, e -> {
			for (int i = 0; i < hasLiquid.length; i++) {
				hasLiquid[i] = false;
			}
			
			Vars.world.tiles.forEach(t -> {
				if(t.block().isAir() && t.floor().liquidDrop != null) {
					hasLiquid[t.floor().liquidDrop.id] = true;
				}
			});
		});
		if(!Vars.mobile) {
			Events.on(TileChangeEvent.class, e -> {
				if(e.tile.block().isAir() && e.tile.floor().liquidDrop != null) {
					hasLiquid[e.tile.floor().liquidDrop.id] = true;
				}
			});
		}
		
	}
	
//	private static final Block[] drills = {
//			Blocks.mechanicalDrill,
//			Blocks.pneumaticDrill,
//			Blocks.laserDrill,
//			Blocks.blastDrill
//	};
//
//	private static final boolean[] withWater = {
//			false, false, true, true
//	};
	
	
	public static void draw() {
		drawSelect();
		
		final float mouseX = Core.input.mouseWorldX();
		final float mouseY = Core.input.mouseWorldY();
//		MyDraw.textColor(debug, mouseX, mouseY, 0, 0, 1f, 1, Align.center);
		
		Tile tile = Vars.world.tileWorld(mouseX, mouseY);
		if(tile == null) return;
		if(tile.build == null) return;
		Building building = tile.build;
//		if(building.team != Vars.player.team()) return;

		float health = building.health();
		float maxHealth = building.maxHealth();
		
		int index = ModWork.getGradientIndex(health, maxHealth);
		
		if(ModWork.setting("show-units-health")) {
			MyDraw.textColor(ModWork.round(health), 
					building.getX(), building.getY()+building.block.size*Vars.tilesize/2-MyDraw.textHeight/2f,
					rs[index], gs[index], bs[index], 1, Align.center);
		}
		
//		BuildPlan plan = Vars.player.unit().buildPlan();
		
		if(building.team == Vars.player.team() && ModWork.setting("show-blocks-tooltip")) {
			Block block = building.block();
			float craftSpeed = ModWork.getCraftSpeed(building);
			
			StringBuilder info = new StringBuilder(block.emoji() + " " + block.localizedName.toUpperCase());

//			MyDraw.textColor("craftSpeed: " + craftSpeed, mouseX, mouseY+30, 0, 0, 1f, 1, Align.center);
			if(craftSpeed <= 0) return;

			if(block.consumers != null) {
				for (int i = 0; i < block.consumers.length; i++) {
					ModWork.consumeItems(block.consumers[i], building, craftSpeed, (item, ips) -> {
						addItemInfo(info, block, item, ips, false);
					});
					ModWork.consumeLiquids(block.consumers[i], building, craftSpeed, (liquid, ips) -> {
						addLiquidInfo(info, block, liquid, ips, false);
					});
				}
			}
//			if(block.consumers != null) {
//				if(building instanceof UnitFactoryBuild && block instanceof UnitFactory) {
//					int plan = ((UnitFactoryBuild)building).currentPlan;
//					if(plan != -1) {
//						ItemStack[] requirements = ((UnitFactory)block).plans.get(plan).requirements;
//						for (int i = 0; i < requirements.length; i++) {
//							ItemStack stack = requirements[i];
//							float ips = craftSpeed*stack.amount;
//							addItemInfo(info, block, stack.item, ips, false);
//						}
//					}
//				}
//				for (int c = 0; c < block.consumers.length; c++) {
//					Consume consume = block.consumers[c];
//					if(consume instanceof ConsumeItems) {
//						ConsumeItems items = (ConsumeItems) consume;
//						ItemStack[] stacks = items.items;
//						for (int i = 0; i < stacks.length; i++) {
//							ItemStack stack = stacks[i];
//							float ips = craftSpeed*stack.amount;
//							addItemInfo(info, block, stack.item, ips, false);
//						}
//						continue;
//					}
//					if(consume instanceof ConsumeLiquid) {
//						ConsumeLiquid liquid = (ConsumeLiquid) consume;
//						addLiquidInfo(info, block, liquid.liquid, 60*liquid.amount, false);
//						continue;
//					}
//				}
//			}
			MyDraw.drawTooltip(info.toString(), mouseX, mouseY);
		}
	}
	
	private static void drawSelect() {
		if(!ModWork.setting("selection-calculations")) return;
        final float ts = Vars.tilesize/2f;
		if(selectStart.x != -1 && selectEnd.x != -1) {
			int minX = Math.min(selectStart.x, selectEnd.x);
			int maxX = Math.max(selectStart.x, selectEnd.x)+1;
			int minY = Math.min(selectStart.y, selectEnd.y);
			int maxY = Math.max(selectStart.y, selectEnd.y);
//			debug = "X: " + minX + " -> " + maxX + " | Y: " + minY + " -> " + maxY;

//			MyDraw.textColor("Start", minX*Vars.tilesize, mouseY, 0, 0, 1f, 1, Align.center);
			Draw.z(Layer.plans);
	        Lines.stroke(2f);
	        Draw.color(selectBack);
	        Lines.rect(minX*Vars.tilesize - ts, minY*Vars.tilesize - 1 - ts, (maxX-minX)*Vars.tilesize, (maxY-minY+1)*Vars.tilesize);
	        Draw.color(select);
	        Lines.rect(minX*Vars.tilesize - ts, minY*Vars.tilesize - ts, (maxX-minX)*Vars.tilesize, (maxY-minY+1)*Vars.tilesize);

	        Lines.stroke(1);
	        for (int y = minY; y <= maxY; y++) {
				for (int x = minX; x < maxX; x++) {
					Tile tile = Vars.world.tile(x, y);
					if(tile.build == null) continue;
					if(tile.build.team != Vars.player.team()) continue;
					boolean needDraw = true;
					final int size = tile.block().size;
					int zeroX = tile.build.tileX() - Mathf.floor((size-1)/2f);
					int zeroY = tile.build.tileY() - Mathf.floor((size-1)/2f);

					int tx = Math.min(Math.max(minX, zeroX), maxX);
					int ty = Math.min(Math.max(minY, zeroY), maxY);

					needDraw = x == tx && y == ty;
					
					if(!tile.block().isMultiblock()) needDraw = true;
					if(needDraw) {
						float dSize = tile.block().size*Vars.tilesize;
						float dx = zeroX*Vars.tilesize + dSize/2f - ts;//(tile.build.tileX() - size/2f)*Vars.tilesize;
						float dy = zeroY*Vars.tilesize + dSize/2f - ts;//(tile.build.tileY() - size/2f)*Vars.tilesize;
						Draw.z(Layer.blockAdditive);
				        Draw.color(select);
				        Lines.rect(dx-dSize/2f, dy-dSize/2f, dSize, dSize);
						Draw.z(Layer.blockAdditive);
				        Draw.color(selectHower);
				        Fill.rect(dx, dy, dSize, dSize);
					}
				}
			}
		}
	        
		for (int i = 0; i < selected.size; i++) {
			Tile tile = selected.get(i);
			Block block = tile.block();
			int zeroX = tile.centerX() - Mathf.floor((block.size-1)/2f);
			int zeroY = tile.centerY() - Mathf.floor((block.size-1)/2f);

			float dSize = tile.block().size*Vars.tilesize;
			float dx = zeroX*Vars.tilesize + dSize/2f - ts;
			float dy = zeroY*Vars.tilesize + dSize/2f - ts;
			Draw.z(Layer.blockAdditive);
			Draw.color(select);
			Lines.rect(dx-dSize/2f, dy-dSize/2f, dSize, dSize);
			Draw.z(Layer.blockAdditive);
			Draw.color(selectHower);
			Fill.rect(dx, dy, dSize, dSize);
		}
	}
	
	
	// a73e3e
	// D8D8D8 A3A3A3
	// 00DB00 3EA53E 00DB00
	private static final Color select = Color.valueOf("ffffff"), 
			selectBack = Color.valueOf("a3a3a3"), selectHower = Color.valueOf("ffffff").a(.5f);
	
	private static final Seq<Tile> selected = new Seq<>();

	private static Point2 selectStart = new Point2(-1, -1);
	private static Point2 selectEnd = new Point2(-1, -1);
	
	static String debug = "none";
	
	public static void update() {
		if(!ModWork.setting("selection-calculations")) return;
		int tileX = World.toTile(Core.input.mouseWorldX());
		int tileY = World.toTile(Core.input.mouseWorldY());
		if(tileX < 0) return;
		if(tileY < 0) return;
		if(tileX >= Vars.world.width()) return;
		if(tileY >= Vars.world.height()) return;
		
//		DesktopInput;
		if(ModWork.acceptKey()) {
			if(Core.input.keyDown(KeyBinds.clearSelection.key)) { // TODO
				if(selected.size > 0) {
					selected.clear();
					return;
				}
			}
		}

		if(Core.input.keyDown(KeyBinds.selection.key)) {
			if(selectStart.x == -1 || selectStart.y == -1) {
				selectStart.x = tileX;
				selectStart.y = tileY;
			}
			selectEnd.x = tileX;
			selectEnd.y = tileY;
		} else {
			if(selectStart.x != -1) {
				int minX = Math.min(selectStart.x, selectEnd.x);
				int maxX = Math.max(selectStart.x, selectEnd.x)+1;
				int minY = Math.min(selectStart.y, selectEnd.y);
				int maxY = Math.max(selectStart.y, selectEnd.y);
				
				Building startBuilding = Vars.world.build(selectStart.x, selectStart.y);
				boolean add = true;
				if(startBuilding != null) {
					if(selected.contains(startBuilding.tileOn())) {
						add = false;
					}
				}
		        for (int y = minY; y <= maxY; y++) {
					for (int x = minX; x < maxX; x++) {
						Building build = Vars.world.build(x, y);
						if(build == null) continue;
						if(add) {
							if(!selected.contains(build.tileOn())) {
								selected.add(build.tileOn());
							}
						} else {
							selected.remove(build.tileOn());
						}
					}
		        }
				
			}
			selectStart.x = -1;
			selectStart.y = -1;
		}
		
		StringBuilder info = new StringBuilder();
		for (int i = 0; i < selected.size; i++) {
//			info.append("[white]" + selected.get(i).block().emoji() + " [gold]" + selected.get(i).block().localizedName.toUpperCase());
			info.append("[white] Line [gold]#" + i + "/" + selected.size);
			info.append('\n');
		}
		
		calcBalance();
//		balanceFragment.setText(info.toString());
	}

	private static float itemsBalance[] = new float[Vars.content.items().size];
	private static float liquidBalance[] = new float[Vars.content.liquids().size];

	private static float itemsBalanceTotal[] = new float[Vars.content.items().size];
	private static float liquidBalanceTotal[] = new float[Vars.content.liquids().size];
	private static boolean itemsWarn[] = new boolean[Vars.content.items().size];
	
	private static float itemsBalanceFixed[] = new float[Vars.content.items().size];
	private static float liquidBalanceFixed[] = new float[Vars.content.liquids().size];

	
	static int updates = 0;
	private static void calcBalance() {
		StringBuilder info = new StringBuilder();
		
		
		for (int i = 0; i < itemsBalance.length; i++) {
			itemsBalance[i] = 0;
			itemsWarn[i] = false;
		}
		for (int i = 0; i < liquidBalance.length; i++) {
			liquidBalance[i] = 0;
		}
		
		if(selected.size == 0) {
			for (int i = 0; i < itemsBalance.length; i++) {
				itemsBalanceFixed[i] = itemsBalanceTotal[i] = 0;
			}
			for (int i = 0; i < liquidBalance.length; i++) {
				liquidBalanceFixed[i] = liquidBalanceTotal[i] = 0;
			}
			updates = 0;
		}

		boolean buildPlans = false;
		if(ModWork.setting("buildplans-calculations")) {
			if(Vars.player.unit() != null) {
				if(Vars.player.unit().plans != null) {
					if(Vars.player.unit().plans().size > 0) {
						for (int i = 0; i < Vars.player.unit().plans().size; i++) {
							BuildPlan buildPlan = Vars.player.unit().plans().get(i);
							if(buildPlan.breaking) continue;
							float craftSpeed = ModWork.getCraftSpeed(buildPlan.block,
									buildPlan.x, buildPlan.y, buildPlan.config);
							ModWork.consumeBlock(buildPlan.block, buildPlan.x, buildPlan.y, 
									buildPlan.config, craftSpeed, 
									(item, ips) -> itemsBalance[item.id] -= ips,
									(liquid, lps) -> liquidBalance[liquid.id] -= lps);
							ModWork.produceBlock(buildPlan.block, buildPlan.x, buildPlan.y, 
									buildPlan.config, craftSpeed, 
									(item, ips) -> itemsBalance[item.id] += ips,
									(liquid, lps) -> liquidBalance[liquid.id] += lps);
						}
						
						buildPlans = true;
					}
				}
			}
			if(Vars.control.input.selectPlans.size > 0) {
				for (int i = 0; i < Vars.control.input.selectPlans.size; i++) {
					BuildPlan buildPlan = Vars.control.input.selectPlans.get(i);
					if(buildPlan.breaking) continue;
					float craftSpeed = ModWork.getCraftSpeed(buildPlan.block,
							buildPlan.x, buildPlan.y, buildPlan.config);
					ModWork.consumeBlock(buildPlan.block, buildPlan.x, buildPlan.y, 
							buildPlan.config, craftSpeed, 
							(item, ips) -> itemsBalance[item.id] -= ips,
							(liquid, lps) -> liquidBalance[liquid.id] -= lps);
					ModWork.produceBlock(buildPlan.block, buildPlan.x, buildPlan.y, 
							buildPlan.config, craftSpeed, 
							(item, ips) -> itemsBalance[item.id] += ips,
							(liquid, lps) -> liquidBalance[liquid.id] += lps);
				}
				buildPlans = true;
			}
			if(Vars.control.input.linePlans.size > 0) {
				for (int i = 0; i < Vars.control.input.linePlans.size; i++) {
					BuildPlan buildPlan = Vars.control.input.linePlans.get(i);
					if(buildPlan.breaking) continue;
					float craftSpeed = ModWork.getCraftSpeed(buildPlan.block,
							buildPlan.x, buildPlan.y, buildPlan.config);
					ModWork.consumeBlock(buildPlan.block, buildPlan.x, buildPlan.y, 
							buildPlan.config, craftSpeed, 
							(item, ips) -> itemsBalance[item.id] -= ips,
							(liquid, lps) -> liquidBalance[liquid.id] -= lps);
					ModWork.produceBlock(buildPlan.block, buildPlan.x, buildPlan.y, 
							buildPlan.config, craftSpeed, 
							(item, ips) -> itemsBalance[item.id] += ips,
							(liquid, lps) -> liquidBalance[liquid.id] += lps);
				}
				buildPlans = true;
			}
		}
		
		if(selected.size > 0) {
			info.append("\n[accent]Selected");
			if(buildPlans) {
				info.append(" & build plans");
			}
		} else if(buildPlans) {
			info.append("\n[accent]Build plans");
		}
		
		for (int s = 0; s < selected.size; s++) {
			Tile tile = selected.get(s);
			Building building = tile.build;
			Block block = tile.block();
			if(building == null) continue;

			float craftSpeed = ModWork.getCraftSpeed(building);

//			if(building instanceof UnitFactoryBuild && block instanceof UnitFactory) {
//				int plan = ((UnitFactoryBuild)building).currentPlan;
//				if(plan != -1) {
//					ItemStack[] requirements = ((UnitFactory)block).plans.get(plan).requirements;
//					for (int i = 0; i < requirements.length; i++) {
//						ItemStack stack = requirements[i];
//						float ips = craftSpeed*stack.amount*building.timeScale();
//						itemsBalance[stack.item.id] -= ips;
//					}
//				}
//			}

			ModWork.produceItems(building, craftSpeed, (item, ips) -> {
				itemsBalance[item.id] += ips;
				if(building.items != null) {
					if(building.items.get(item) >= building.getMaximumAccepted(item)) {
						itemsWarn[item.id] = true;
					}
				}
			});
			
			ModWork.produceLiquids(building, craftSpeed, (liquid, lps) -> {
				liquidBalance[liquid.id] += lps;
			});
			
			for (int c = 0; c < block.consumers.length; c++) {
				Consume consume = block.consumers[c];
				ModWork.consumeItems(consume, building, craftSpeed, (item, ips) -> {
					itemsBalance[item.id] -= ips;
				});
				ModWork.consumeLiquids(consume, building, craftSpeed, (liquid, lps) -> {
					liquidBalance[liquid.id] -= lps;
				});
			}
		}

		for (int i = 0; i < itemsBalance.length; i++) {
			itemsBalanceTotal[i] += itemsBalance[i];
		}
		for (int i = 0; i < liquidBalance.length; i++) {
			liquidBalanceTotal[i] += liquidBalance[i];
		}

		if(updates%60 == 0 && updates > 0) {
			for (int i = 0; i < itemsBalance.length; i++) {
				itemsBalanceFixed[i] = itemsBalanceTotal[i]/60f;
				itemsBalanceTotal[i] = 0;//itemsBalance[i];
			}
			for (int i = 0; i < liquidBalance.length; i++) {
				liquidBalanceFixed[i] = liquidBalanceTotal[i]/60f;
				liquidBalanceTotal[i] = 0;//liquidBalance[i];
			}
		} else {
		}

		for (int i = 0; i < itemsBalance.length; i++) {
			Item item = Vars.content.item(i);
			float ips = itemsBalanceFixed[i];
			if(updates < 60) ips = itemsBalance[i];
//			boolean warn = needWarn(itemsBalance[i], ips);
			if(ips == 0) {
				if(itemsWarn[i]) info.append("[accent]0/sec" + " [yellow]" + Iconc.warning);
				continue;
			}
			if(ips < 0) {
				addItemInfo(info, null, item, ips, itemsWarn[i]);
			} else {
				info.append("\n[white]" + item.emoji() + " [green]+" + ModWork.round(ips) + "/sec");
				if(itemsWarn[i]) info.append(" [yellow]" + Iconc.warning);
			}
//			info.append(item.emoji() + "[lightgray] " + ModWork.round(ips) + "/sec");
		}
		
		for (int i = 0; i < liquidBalance.length; i++) {
			Liquid liquid = Vars.content.liquid(i);
			float lps = liquidBalanceFixed[i];
			if(updates < 60) lps = liquidBalance[i];
//			boolean warn = needWarn(liquidBalance[i], lps);
			if(lps == 0) continue;
			if(lps < 0) {
				addLiquidInfo(info, null, liquid, lps, false);
			} else {
				info.append("\n[white]" + liquid.emoji() + " [green]+" + ModWork.round(lps) + "/sec");
//				if(warn) info.append(" [yellow]" + Iconc.warning);
			}
		}
		updates++;
		
		balanceFragment.setText(info.toString());
	}
	
	private static boolean needWarn(float a, float b) {
		return Mathf.round(a/10) != Mathf.round(b);
	}
//	static int tileX(float cursorX){
//        Vec2 vec = Core.input.mouseWorld(cursorX, 0);
//        if(selectedBlock()){
//            vec.sub(block.offset, block.offset);
//        }
//        return World.toTile(vec.x);
//    }
//
//	static int tileY(float cursorY){
//        Vec2 vec = Core.input.mouseWorld(0, cursorY);
//        if(selectedBlock()){
//            vec.sub(block.offset, block.offset);
//        }
//        return World.toTile(vec.y);
//    }
    
	private static void addItemInfo(StringBuilder info, @Nullable Block block, Item item, float ips, boolean warn) {
		if(ips < 0) {
			info.append("\n[white]" + item.emoji() + " [scarlet]" + ModWork.round(ips) + "/sec");
			ips = -ips;
		} else {
			info.append("\n[white]" + item.emoji() + " [lightgray]" + ModWork.round(ips) + "/sec");
		}
		if(warn) info.append(" [yellow]" + Iconc.warning);
		addDrills(info, block, item, ips);
		addCrafters(info, block, item, ips);
	}
	
	private static void addLiquidInfo(StringBuilder info, @Nullable Block block, Liquid liquid, float lps, boolean warn) {
		if(lps < 0) {
			info.append("\n[white]" + liquid.emoji() + " [scarlet]" + ModWork.round(lps) + "/sec");
			lps = -lps;
		} else {
			info.append("\n[white]" + liquid.emoji() + " [lightgray]" + ModWork.round(lps) + "/sec");
		}
		if(warn) info.append(warn);
		addPumps(info, block, liquid, lps);
		addLiquidCrafters(info, block, liquid, lps);
	}

	private static Seq<Block>[] createCrafters() {
		@SuppressWarnings("unchecked")
		Seq<Block>[] crafters = new Seq[Vars.content.items().size];
		for (int i = 0; i < crafters.length; i++) {
			Seq<Block> crafter = new Seq<>();
			crafters[i] = crafter;
		}

		Vars.content.blocks().each(b -> {
			if(b instanceof GenericCrafter) {
				GenericCrafter crafter = (GenericCrafter) b;
				if(crafter.outputItem != null) {
					crafters[crafter.outputItem.item.id].add(b);
				}
				if(crafter.outputItems != null) {
					for (int i = 0; i < crafter.outputItems.length; i++) {
						if(!crafters[crafter.outputItems[i].item.id].contains(b))
						crafters[crafter.outputItems[i].item.id].add(b);
					}
				}
			}
			// TODO: Separator
		});
		
		return crafters;
	}
	
	private static Seq<Block>[] createLiquidCrafters() {
		@SuppressWarnings("unchecked")
		Seq<Block>[] crafters = new Seq[Vars.content.liquids().size];
		for (int i = 0; i < crafters.length; i++) {
			Seq<Block> crafter = new Seq<>();
			crafters[i] = crafter;
		}

		Vars.content.blocks().each(b -> {
			if(b instanceof GenericCrafter) {
				GenericCrafter crafter = (GenericCrafter) b;
				if(crafter.outputLiquid != null) {
					crafters[crafter.outputLiquid.liquid.id].add(b);
				}
				if(crafter.outputLiquids != null) {
					for (int i = 0; i < crafter.outputLiquids.length; i++) {
						if(!crafters[crafter.outputLiquids[i].liquid.id].contains(b))
						crafters[crafter.outputLiquids[i].liquid.id].add(b);
					}
				}
			}
		});
		
		return crafters;
	}

	/**
	 * Add pumps info
	 * @param builder - StringBuilder to append text
	 * @param item - type of liquid
	 * @param lps - required liquid per second
	 */
	private static void addPumps(StringBuilder builder, @Nullable Block block, Liquid liquid, float lps) {
		// TODO: hid if world has not liquid
		for (int d = 0; d < pumps.size; d++) {
			Pump pump = pumps.get(d);
			if(!pump.environmentBuildable()) continue;
			if(!pump.isPlaceable()) continue;
			float pumpLps = pump.pumpAmount*60;
			if(pump instanceof SolidPump) {
				if(((SolidPump) pump).result != liquid) continue;
			} else {
				if(!hasLiquid[liquid.id]) continue;
				pumpLps *= pump.size*pump.size;
			}
			float count = lps/pumpLps;
			builder.append("\n[lightgray]> [white]" + pump.emoji() + " [white]x" + ModWork.round(count));
			if(block != null)
			builder.append("[lightgray] or [white]" + block.emoji() + " x" + ModWork.round(1/count));	
		}
	}
	
	/**
	 * Add drills info
	 * @param builder - StringBuilder to append text
	 * @param item - type of item
	 * @param ips - required items per second
	 */
	private static void addDrills(StringBuilder builder, @Nullable Block block, Item item, float ips) {
		if(!Vars.indexer.hasOre(item)) return;
		for (int d = 0; d < drills.size; d++) {
			Drill drill = drills.get(d);
			if(!drill.environmentBuildable()) continue;
			if(!drill.isPlaceable()) continue;
			if(item.hardness > drill.tier) continue;
			boolean liquid = ModWork.needDrillWaterBoost(drill, item);
			float count = ips/ModWork.drillSpeed(drill, item, liquid);
			builder.append("\n[lightgray]> [white]" + drill.emoji() + (liquid ? " [sky]" : " [white]") + "x" + ModWork.round(count));
			if(block != null) builder.append("[lightgray] or [white]" + block.emoji() + " x" + ModWork.round(1/count));	
		}
	}
	
	/**
	 * Add GenericCrafter info
	 * @param builder - StringBuilder to append text
	 * @param item - type of item
	 * @param ips - required items per second
	 */
	private static void addCrafters(StringBuilder builder, @Nullable Block block, Item item, float ips) {
		for (int c = 0; c < crafters[item.id].size; c++) {
			Block crafter = crafters[item.id].get(c);
			if(!crafter.environmentBuildable()) continue;
			if(!crafter.isPlaceable()) continue;
			float cps = 0f; // crafts per second
			if(crafter instanceof GenericCrafter) {
				GenericCrafter gCrafter = (GenericCrafter) crafter;
				if(gCrafter.outputItem != null) {
					if(gCrafter.outputItem.item == item) cps = gCrafter.outputItem.amount;
				}
				if(gCrafter.outputItems != null) {
					for (int oi = 0; oi < rs.length; oi++) {
						if(gCrafter.outputItems[oi].item == item) {
							cps = gCrafter.outputItems[oi].amount;
							break;
						}
					}
				}
				cps *= 60f / gCrafter.craftTime;
			}
			
			float count = ips/cps;
			builder.append("\n[lightgray]> [white]" + crafter.emoji() + " [white]" + "x" + ModWork.round(count));
			if(block != null) builder.append("[lightgray] or [white]" + block.emoji() + " x" + ModWork.round(1/count));	
		}
	}

	/**
	 * Add GenericCrafter info
	 * @param builder - StringBuilder to append text
	 * @param liquid - type of liquid
	 * @param lps - required liquid per second
	 */
	private static void addLiquidCrafters(StringBuilder builder, @Nullable Block block, Liquid liquid, float lps) {
		for (int c = 0; c < liquidCrafters[liquid.id].size; c++) {
			Block crafter = liquidCrafters[liquid.id].get(c);
			if(!crafter.environmentBuildable()) continue;
			if(!crafter.isPlaceable()) continue;
			float cps = 0f; // crafts per second
			if(crafter instanceof GenericCrafter) {
				GenericCrafter gCrafter = (GenericCrafter) crafter;
				if(gCrafter.outputItem != null) {
					if(gCrafter.outputLiquid.liquid == liquid) cps = gCrafter.outputItem.amount;
				}
				if(gCrafter.outputLiquids != null) {
					for (int ol = 0; ol < rs.length; ol++) {
						if(gCrafter.outputLiquids[ol].liquid == liquid) {
							cps = gCrafter.outputLiquids[ol].amount;
							break;
						}
					}
				}
				cps *= 60f;// / gCrafter.craftTime;
			}
			
			float count = lps/cps;
			builder.append("\n[lightgray]> [white]" + crafter.emoji() + " [white]" + "x" + ModWork.round(count));
			if(block != null)
			builder.append("[lightgray] or [white]" + block.emoji() + " x" + ModWork.round(1/count));	
		}
	}
	
	
	static class BalanceFragment extends Table {

	    
	    private void build() {
			Core.scene.add(balanceFragment);
		}
	    
	    @Override
	    public Element update(Runnable r) {
	    	return super.update(r);
	    }
	    
	    String text;
	    
	    private void setText(String text) {
	    	this.text = text;
//	    	label.setText(text);
		}
	    
	    private void updatePosition() {
	    	
//			setBounds(Core.scene.getWidth()-Core.scene.getWidth()/2,
//					Core.scene.getHeight()-label.getHeight(),
//					label.getWidth()/2, label.getHeight());
		}
	    
		@Override
		public void draw() {
			if(!ModWork.setting("selection-calculations")) return;
			updatePosition();
			if(text.isEmpty()) return;
			if(Vars.state.isMenu()) return;
			if(Vars.ui.schematics.isShown()) return;
			if(Vars.ui.content.isShown()) return;
			if(Vars.ui.database.isShown()) return;
			if(!Vars.ui.hudfrag.shown) return;
			
			Font font = Fonts.outline;

			GlyphLayout layout = Pools.obtain(GlyphLayout.class, GlyphLayout::new);
			font.setUseIntegerPositions(false);
			font.getData().setScale(1f);//0.25f / Scl.scl(1f));
			font.getData().setLineHeight(MyDraw.textHeight*2f);
			layout.setText(font, text);

			float width = Math.max(layout.width, MyDraw.textHeight*20);
			float x = Core.scene.getWidth() - width;// - MyDraw.textHeight;
			float y = Core.scene.getHeight();// - layout.height/4;
//			y -= layout.height;
			
			Draw.color(0f, 0f, 0f, 0.75f);
			Fill.rect(x + width/2 - MyDraw.textHeight, y + MyDraw.textHeight - layout.height/2,
					width + MyDraw.textHeight*3, layout.height + MyDraw.textHeight*5);
			
			Draw.color();
			Draw.alpha(1f);
			font.setColor(1, 1, 1, 1);
			font.draw(text, x, y, 0, Align.left, false);
			Draw.color();
			
//			cLabel.setBounds(Core.scene.getWidth()-label.getWidth(),
//					Core.scene.getHeight()-label.getHeight(),
//					label.getWidth(), label.getHeight());
//			label.setPosition(Core.scene.getWidth()-label.getWidth(),
//					Core.scene.getHeight()-label.getHeight());
			
//			debug = "Draw...";
//			Draw.color(1, 0, 0, .5f);
//	        rect(this.x, this.y, getWidth(), getHeight());
//
//			Draw.color(0, 1, 0, .5f);
//	        rect(label.x, label.y, label.getWidth(), label.getHeight());
//			Draw.color(0, 1, 0, .5f);
//	        Draw.rect("whiteui", x, y, getWidth(), getHeight());
//			super.draw();
		} 
		
		protected void rect(float x, float y, float w, float h){
	        Draw.rect("whiteui", x + w/2f, y + h/2f, w, h);
	    }
	}
}
