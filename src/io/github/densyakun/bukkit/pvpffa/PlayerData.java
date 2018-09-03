package io.github.densyakun.bukkit.pvpffa;
import java.io.Serializable;
import java.util.UUID;
public class PlayerData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	UUID uuid;
	int kill = 0;
	int death = 0;
	public PlayerData(UUID uuid) {
		this.uuid = uuid;
	}
	public UUID getUuid() {
		return uuid;
	}
	public int getKill() {
		return kill;
	}
	public int getDeath() {
		return death;
	}
	public void clear() {
		kill = 0;
		death = 0;
	}
}
