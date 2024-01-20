package agzam4.utils;

import agzam4.MyIndexer;
import arc.math.geom.Position;
import mindustry.Vars;
import mindustry.content.UnitTypes;
import mindustry.gen.Call;
import mindustry.gen.Unit;
import mindustry.type.Item;
import mindustry.world.Tile;
import mindustry.world.blocks.storage.CoreBlock.CoreBuild;

public class PlayerAI {
	
	public static boolean enabled = false;

	static int mineCoolDown = 0;
	
	
	static int searchCoolDown = 0;
	
	public static void updatePlayer() {
		if(!enabled) return;
		if(Vars.player == null) return;
		Unit unit = Vars.player.unit();
		if(unit == null) return;
		if(unit.isBuilding()) return;
		
		if(!unit.canMine()) return;
		
		if(unit.hasItem()) {
			if(unit.stack.amount < unit.itemCapacity()) {
				if(unit.mining()) return;
				if(MyIndexer.hasOre(unit.stack.item, unit.type.mineFloor, unit.type.mineWalls)) {
					updateSearchAndMine(unit.stack.item);
					return;
				}
				updateDropToCore();
				return;
			}
			if(unit.stack.amount == unit.itemCapacity()) {
				updateDropToCore();
				return;
			}
		} else {
			if(unit.mining() && mineCoolDown > 0) {
				if(unit.mineTile != null) {
					if(unit.mineTile.drop() != null) {
						mineCoolDown--;
						return;
					}
				}
			}
			CoreBuild core = unit.closestCore();
			if(core == null) return;
			
			Item target = null;
			int minCount = core.storageCapacity;
			for (int i = 0; i < Vars.content.items().size; i++) {
				Item item = Vars.content.item(i);
				if(!unit.canMine(item)) continue;
				if(!MyIndexer.hasOre(item, unit.type.mineFloor, unit.type.mineWalls)) continue;
				
				if(core.items.get(item) < minCount) {
					minCount = core.items.get(item);
					target = item;
				}
			}
			
			if(target != null) {
				updateSearchAndMine(target);
			}
		}
	}

	private static void updateDropToCore() {
		Unit unit = Vars.player.unit();
		CoreBuild core = unit.closestCore();
		if(core == null) return;
		if(unit.within(core, 2)) {
			Call.transferInventory(Vars.player, core);
			return;
		}
		circle(core, 1, unit.speed());
	}

	private static void updateSearchAndMine(Item item) {
		if(Vars.player == null) return;
		Unit unit = Vars.player.unit();
		
		Tile ore = MyIndexer.findClosestOre(unit, item, unit.type.mineFloor, unit.type.mineWalls);
		if(ore == null) return;
		
		if(unit.within(ore, 2)) {
			unit.mineTile(ore);
			mineCoolDown = 60*30;
			return;
		}
		
		circle(ore, 1, unit.speed());
	}

    public static void circle(Position target, float circleLength, float speed) {
		if(Vars.player == null) return;
		Unit unit = Vars.player.unit();
        if(target == null) return;

        unit.vel.set(target).sub(unit);

        if(unit.vel.len() < circleLength){
        	unit.vel.rotate((circleLength - unit.vel.len()) / circleLength * 180f);
        }

        unit.vel.setLength(speed);

        unit.moveAt(unit.vel);
    }
}
