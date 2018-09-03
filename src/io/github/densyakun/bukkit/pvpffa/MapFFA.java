package io.github.densyakun.bukkit.pvpffa;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;

import org.bukkit.Location;
public class MapFFA implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public String mapname;
	public transient Location spawn;
	public MapFFA(String mapname, Location spawn) {
		this.mapname = mapname;
		this.spawn = spawn;
	}
	
	private void writeObject(ObjectOutputStream stream) throws IOException {
		stream.defaultWriteObject();
		stream.writeObject(spawn == null ? null : spawn.serialize());
	}

	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		Map<String, Object> spawn = (Map<String, Object>) stream.readObject();
		if (spawn != null) {
			this.spawn = Location.deserialize(spawn);
		}
	}
}
