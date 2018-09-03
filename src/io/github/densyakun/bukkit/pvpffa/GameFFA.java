package io.github.densyakun.bukkit.pvpffa;
import java.util.List;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import io.github.densyakun.bukkit.minigamemanager.Game;
public class GameFFA extends Game {
	
	public static final String name = "PvPFFA";
	public MapFFA map;
	//public Map<UUID, Kit> setkits = new HashMap<UUID, Kit>();
	
	public GameFFA(MapFFA map) {
		super(name);
		this.map = map;
		setEntrytime(0);
		setStarttime(0);
		setEndtime(0);
		setStoptime(0);
		setMinplayers(Integer.MAX_VALUE);
		setMaxplayers(0);
	}
	
	@Override
	public void stop() {
		List<Player> players = getPlayers();
		for (int a = 0; a < players.size(); a++) {
			players.get(a).getInventory().clear();
			players.get(a).getInventory().setArmorContents(null);
			players.get(a).setGameMode(GameMode.ADVENTURE);
		}
		if (Main.main.lobby != null) {
			for (int a = 0; a < players.size(); a++) {
				players.get(a).teleport(Main.main.lobby.getSpawnLocation());
			}
		}
		super.stop();
	}
	
	@Override
	public void removePlayer(UUID uuid) {
		Player player = Main.main.getServer().getPlayer(uuid);
		if (player != null) {
			player.getInventory().clear();
			player.getInventory().setArmorContents(null);
			player.setGameMode(GameMode.ADVENTURE);
			if (Main.main.lobby != null) {
				player.teleport(Main.main.lobby.getSpawnLocation());
			}
		}
		super.removePlayer(uuid);
	}
	
	@Override
	protected boolean addPlayer(Player player) {
		if (super.addPlayer(player)) {
			player.getInventory().clear();
			Kit kit = null;
			/*if (setkits == null) {
				setkits = new HashMap<UUID, Kit>();
			}*/
			//if ((kit = setkits.get(player.getUniqueId())) == null) {
				if ((kit = Main.main.getKit(Main.main.defaultkit)) == null) {
					kit = Kit.getDefaultKit();
				}
			//}
			kit.applyKit(player.getInventory());
			player.leaveVehicle();
			player.resetMaxHealth();
			player.setFireTicks(0);
			player.setFoodLevel(20);
			player.setGameMode(GameMode.SURVIVAL);
			player.setHealth(player.getMaxHealth());
			if (map.spawn != null) {
				player.teleport(map.spawn);
			}
			return true;
		}
		return false;
	}
}
