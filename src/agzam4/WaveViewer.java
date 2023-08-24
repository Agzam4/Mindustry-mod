package agzam4;

import arc.Events;
import arc.func.Cons;
import arc.math.geom.Point2;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.game.EventType.TapEvent;
import mindustry.game.EventType.WaveEvent;
import mindustry.game.SpawnGroup;
import mindustry.type.UnitType;
import mindustry.world.Tile;

public class WaveViewer {

	private static int visibleWaves = 5;
	
	public static Seq< SpawnInfo> spawns = new Seq<>();

	public static void init() {
		Events.on(TapEvent.class, e -> {
			
		});
		
		Events.on(WaveEvent.class, e -> {
			spawns.clear();

			for (int i = 0; i < Vars.spawner.getSpawns().size; i++) {
				spawns.add(new SpawnInfo(Vars.spawner.getSpawns().get(i)));
			}

			for (int g = 0; g < Vars.state.rules.spawns.size; g++) {
				StringBuilder waveInfo = new StringBuilder();

				SpawnGroup group = Vars.state.rules.spawns.get(g);
				group.getSpawned(Vars.state.wave);
				if(g != 0) waveInfo.append(' ');
				waveInfo.append(group.type.emoji());
				waveInfo.append(' ');
				waveInfo.append(group.getSpawned(Vars.state.wave));

				spawnByPos(group.spawn, spawn -> spawn.addInfo(group, Vars.state.wave));
			}
		});
	}
	
	private static void spawnByPos(int pos, Cons<SpawnInfo> cons) {
		for (int i = 0; i < spawns.size; i++) {
			if(pos == -1 || pos == spawns.get(i).tile.pos()) {
				cons.get(spawns.get(i));
			}
		}
	}

	public static void draw() {
		if(spawns == null) return;
		
		for (int s = 0; s < spawns.size; s++) {
			SpawnInfo info = spawns.get(s);
			
			MyDraw.text(info.toString(), info.tile.worldx(), info.tile.worldy(), true);
			// TODO: draw
		}
//		for (int g = 0; g < Vars.state.rules.spawns.size; g++) {
//			StringBuilder waveInfo = new StringBuilder();
//			
//			SpawnGroup group = Vars.state.rules.spawns.get(g);
//			group.getSpawned(Vars.state.wave);
//			if(g != 0) waveInfo.append(' ');
//			waveInfo.append(group.type.emoji());
//			waveInfo.append(' ');
//			waveInfo.append(group.getSpawned(Vars.state.wave));
//			
//			Point2 spawnpoint = Point2.unpack(group.spawn);
//			MyDraw.text(waveInfo.toString(), spawnpoint.x*Vars.tilesize, spawnpoint.y*Vars.tilesize, true);
//			
//		}
	}

	private static class SpawnInfo {

		final Tile tile;
		
		int unitsCount[] = new int[Vars.content.units().size];
		
		public SpawnInfo(Tile tile) {
			this.tile = tile;
		}
		
		public void addInfo(SpawnGroup group, int wave) {
			addUnits(group.type, group.getSpawned(wave));
		}

		private void addUnits(UnitType type, int amount) {
			unitsCount[type.id] += amount;
		}
		
		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return super.toString();
		}
	}
}
