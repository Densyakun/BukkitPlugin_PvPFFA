package io.github.densyakun.bukkit.pvpffa;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import com.iCo6.iConomy;
import com.iCo6.system.Account;
import com.iCo6.util.Messaging;
import com.iCo6.util.Template;

import io.github.densyakun.bukkit.minigamemanager.Game;
import io.github.densyakun.bukkit.minigamemanager.MiniGameCommandListener;
import io.github.densyakun.bukkit.minigamemanager.MiniGameManager;
public class Main extends JavaPlugin implements Listener, MiniGameCommandListener {
	public static final String param_is_not_enough = "パラメータが足りません";
	public static final String param_wrong_cmd = "パラメータが間違っています";
	public static final String cmd_player_only = "このコマンドはプレイヤーのみ実行できます";
	
	public static Main main;
	String prefix;
	World lobby;
	double prize = 10.0;
	double lost = 5.0;
	boolean minus = false;
	String defaultkit = "default";
	private File mapsfile;
	private File datafile;
	private File kitsfile;
	private List<MapFFA> maps = new ArrayList<MapFFA>();
	private List<PlayerData> pdata = new ArrayList<PlayerData>();
	private List<Kit> kits = new ArrayList<Kit>();
	@Override
	public void onEnable() {
		Main.main = this;
		prefix = ChatColor.GREEN + "[" + getName() + "]";
		mapsfile = new File(getDataFolder(), "maps.dat");
		datafile = new File(getDataFolder(), "data.dat");
		kitsfile = new File(getDataFolder(), "kits.dat");
		load();
		getServer().getPluginManager().registerEvents(this, this);
		MiniGameManager.minigamemanager.addMiniGameCommandListener(this);
		getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[" + getName() + "]有効");
	}
	@SuppressWarnings("unchecked")
	public void load() {
		saveDefaultConfig();
		lobby = getServer().getWorld(getConfig().getString("lobby-world", "world"));
		if (lobby != null) {
			getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[" + getName() + "] Lobby: " + lobby.toString());
		} else {
			getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[" + getName() + "] " + ChatColor.RED + "ロビーのワールドが見つかりません");
		}
		prize = getConfig().getDouble("prize", prize);
		lost = getConfig().getDouble("lost", lost);
		minus = getConfig().getBoolean("minus", minus);
		defaultkit = getConfig().getString("defaultkit", defaultkit);
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(mapsfile));
			maps = (ArrayList<MapFFA>) ois.readObject();
			ois.close();
		} catch (IOException | ClassNotFoundException e) {
			maps = new ArrayList<MapFFA>();
		}
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(datafile));
			pdata = (ArrayList<PlayerData>) ois.readObject();
			ois.close();
		} catch (IOException | ClassNotFoundException e) {
			pdata = new ArrayList<PlayerData>();
		}
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(kitsfile));
			kits = (ArrayList<Kit>) ois.readObject();
			ois.close();
		} catch (IOException | ClassNotFoundException e) {
			kits = new ArrayList<Kit>();
		}
	}
	@Override
	public boolean MiniGameCommand(CommandSender sender, String[] args) {
		if (args[0].equalsIgnoreCase("ffa")) {
			if (args.length == 1) {
				sender.sendMessage(prefix + ChatColor.GOLD + param_is_not_enough);
				sender.sendMessage(ChatColor.GREEN + "/game ffa (map|kit|join|stats)");
			} else if (args[1].equalsIgnoreCase("map")) {
				if (sender.isOp() || sender.hasPermission("pvpffa.admin")) {
					if (args.length == 2) {
						sender.sendMessage(prefix + ChatColor.GOLD + param_is_not_enough);
						sender.sendMessage(ChatColor.GREEN + "/game ffa map (create|delete|spawn)");
					} else if (args[2].equalsIgnoreCase("create")) {
						if (args.length == 3) {
							sender.sendMessage(prefix + ChatColor.GOLD + param_is_not_enough);
							sender.sendMessage(ChatColor.GREEN + "/game ffa map create (name)");
						} else {
							for (int a = 0; a < maps.size(); a++) {
								if (maps.get(a).mapname.equals(args[3])) {
									sender.sendMessage(ChatColor.GREEN + "[" + getName() + "] " + ChatColor.RED + "そのマップ名は使用されています");
									return true;
								}
							}
							if (args[3].indexOf(',') == -1 && args[3].indexOf('[') == -1 && args[3].indexOf(']') == -1) {
								maps.add(new MapFFA(args[3], null));
								sender.sendMessage(ChatColor.GREEN + "[" + getName() + "] " + ChatColor.BLUE + "新しいマップを作成しました マップ名: " + args[3]);
								mapsave();
							} else {
								sender.sendMessage(ChatColor.GREEN + "[" + getName() + "] " + ChatColor.RED + "マップ名に\",\", \"[\", \"]\"は使用できません");
							}
						}
					} else if (args[2].equalsIgnoreCase("delete")) {
						if (args.length == 3) {
							sender.sendMessage(prefix + ChatColor.GOLD + param_is_not_enough);
							sender.sendMessage(ChatColor.GREEN + "/game ffa map delete (name)");
						} else {
							for (int a = 0; a < maps.size(); a++) {
								if (maps.get(a).mapname.equals(args[3])) {
									sender.sendMessage(ChatColor.GREEN + "[" + getName() + "] " + ChatColor.BLUE + "マップを削除しました マップ名: " + args[3]);
									maps.remove(a);
									mapsave();
									return true;
								}
							}
							sender.sendMessage(ChatColor.GREEN + "[" + getName() + "] " + ChatColor.RED + "マップが見つかりません");
						}
					} else if (args[2].equalsIgnoreCase("spawn")) {
						if (args.length <= 4) {
							sender.sendMessage(prefix + ChatColor.GOLD + param_is_not_enough);
							sender.sendMessage(ChatColor.GREEN + "/game 1vs1 map spawn (set|get) (map)");
						} else if (args[3].equalsIgnoreCase("set")) {
							for (int b = 0; b < maps.size(); b++) {
								MapFFA map = maps.get(b);
								if (map.mapname.equals(args[4])) {
									map.spawn = adjustLocation(((Entity) sender).getLocation());
									mapsave();
									sender.sendMessage(prefix + ChatColor.AQUA + "マップ\"" + map.mapname + "\"のスタート地点を設定しました");
									return true;
								}
							}
							sender.sendMessage(prefix + ChatColor.RED + "マップが見つかりませんでした");
						} else if (args[3].equalsIgnoreCase("get")) {
							for (int b = 0; b < maps.size(); b++) {
								MapFFA map = maps.get(b);
								if (map.mapname.equals(args[4])) {
									if (map.spawn == null) {
										sender.sendMessage(prefix + ChatColor.AQUA + "マップ\"" + map.mapname + "\"のスタート地点は設定されていません");
									} else {
										((Entity) sender).teleport(map.spawn);
										sender.sendMessage(prefix + ChatColor.AQUA + "マップ\"" + map.mapname + "\"のスタート地点に移動しました");
									}
									return true;
								}
							}
							sender.sendMessage(prefix + ChatColor.RED + "マップが見つかりませんでした");
						} else {
							sender.sendMessage(prefix + ChatColor.GOLD + param_wrong_cmd);
							sender.sendMessage(ChatColor.GREEN + "/game 1vs1 map spawn (set|get) (map)");
						}
					} else {
						sender.sendMessage(prefix + ChatColor.GOLD + param_wrong_cmd);
						sender.sendMessage(ChatColor.GREEN + "/game ffa map (create|delete|spawn)");
					}
				} else {
					sender.sendMessage(ChatColor.GREEN + "[" + getName() + "] " + ChatColor.RED + "権限がありません");
				}
			} else if (args[1].equalsIgnoreCase("kit")) {
				if (sender instanceof Player && (sender.isOp() || sender.hasPermission("pvpffa.admin"))) {
					if (args.length <= 2) {
						sender.sendMessage(prefix + ChatColor.GOLD + param_is_not_enough);
						sender.sendMessage(ChatColor.GREEN + "/game ffa kit (set|get)");
					} else if (args[2].equalsIgnoreCase("set")) {
						if (args.length <= 3) {
							sender.sendMessage(prefix + ChatColor.GOLD + param_is_not_enough);
							sender.sendMessage(ChatColor.GREEN + "/game ffa kit set (kit)");
						} else {
							PlayerInventory inv = ((HumanEntity) sender).getInventory();
							Kit kit = new Kit(args[3]);
							kit.setKit(inv);
							putKit(kit);
							kitssave();
							sender.sendMessage(prefix + ChatColor.AQUA + "キット\"" + args[3] + "\"を設定しました");
						}
					} else if (args[2].equalsIgnoreCase("get")) {
						if (args.length <= 3) {
							sender.sendMessage(prefix + ChatColor.GOLD + param_is_not_enough);
							sender.sendMessage(ChatColor.GREEN + "/game ffa kit set (kit)");
						} else {
							Kit kit = getKit(args[3]);
							if (kit == null) {
								sender.sendMessage(prefix + ChatColor.RED + "キットが見つかりません");
							} else {
								kit.applyKit(((HumanEntity) sender).getInventory());
								sender.sendMessage(prefix + ChatColor.AQUA + "キット\"" + args[3] + "\"を呼び出しました");
								return true;
							}
						}
					} else {
						sender.sendMessage(prefix + ChatColor.GOLD + param_wrong_cmd);
						sender.sendMessage(ChatColor.GREEN + "/game ffa kit (set|get)");
					}
				} else {
					sender.sendMessage(ChatColor.GREEN + "[" + getName() + "] " + ChatColor.RED + "権限がありません");
				}
			} else if (args[1].equalsIgnoreCase("join")) {
				if (sender instanceof Player) {
					if (MiniGameManager.minigamemanager
							.getPlayingGame(((OfflinePlayer) sender).getUniqueId()) == null) {
						List<Game> games = MiniGameManager.minigamemanager.getPlayingGames();
						for (int a = 0; a < games.size(); a++) {
							Game game = games.get(a);
							if (!(game instanceof GameFFA && game.isJoinable())) {
								games.remove(a);
							}
						}
						if (args.length != 2) {
							for (int a = 0; a < maps.size(); a++) {
								MapFFA map = maps.get(a);
								if (map.mapname.equals(args[2])) {
									for (int c = 0; c < games.size(); c++) {
										Game game = games.get(c);
										if (game instanceof GameFFA
												&& map.mapname.equals(((GameFFA) game).map.mapname)) {
											if (!MiniGameManager.minigamemanager.joinGame((Player) sender, game)) {
												sender.sendMessage(prefix
														+ ChatColor.RED + "このマップが使用しているゲームに入ることが出来ません");
											}
											return true;
										}
									}
									break;
								}
							}
							sender.sendMessage(prefix + ChatColor.RED + "マップが見つかりません");
						}
						if (0 < games.size()) {
							int a;
							while (0 < games.size() && !MiniGameManager.minigamemanager.joinGame((Player) sender,
									games.get(a = new Random().nextInt(games.size())))) {
								games.remove(a);
							}
						}
						if (0 == games.size()) {
							List<MapFFA> b = maps;
							for (int c = 0; c < b.size();) {
								boolean d = true;
								for (int e = 0; e < games.size(); e++) {
									Game game = games.get(e);
									if (game instanceof GameFFA && maps.get(c).equals(((GameFFA) game).map)) {
										b.remove(c);
										d = false;
										break;
									}
								}
								if (d) {
									c++;
								}
							}
							if (0 < b.size()) {
								MiniGameManager.minigamemanager.joinGame((Player) sender,
										new GameFFA(b.get(new Random().nextInt(b.size()))));
							} else {
								sender.sendMessage(prefix + ChatColor.RED + "使用可能なマップがありません");
							}
						}
					} else {
						sender.sendMessage(prefix + ChatColor.RED + "ゲーム中です");
					}
				}
			}/* else if (args[1].equalsIgnoreCase("join")) {
				if (sender instanceof Player) {
					if (MiniGameManager.minigamemanager.getPlayingGame(((Player) sender).getUniqueId()) == null) {
						List<Game> games = MiniGameManager.minigamemanager.getPlayingGames();
						if (2 < args.length) {
							for (int a = 0; a < maps.size(); a++) {
								if (maps.get(a).mapname.equals(args[2])) {
									List<Integer> b = new ArrayList<Integer>();
									for (int c = 0; c < games.size(); c++) {
										if (games.get(c) instanceof GameFFA && maps.get(a).equals(((GameFFA) games.get(c)).map)) {
											b.add(new Integer(c));
										}
									}
									if (0 < b.size()) {
										List<Integer> c = new ArrayList<Integer>();
										for (int d = 0; d < b.size(); d++) {
											if (games.get(d).getPlayers().size() < games.get(d).getMaxplayers()) {
												c.add(d);
											}
										}
										if (0 < c.size()) {
											games.get(c.get(new Random().nextInt(c.size()))).addPlayer(sender);
										} else {
											sender.sendMessage(ChatColor.GREEN + "[" + getName() + "] " + ChatColor.RED + "このマップが使用しているゲームに入ることが出来ません");
										}
									} else {
										new GameFFA(this, org.densyakun.bukkit.minigamemanager.Main.getMinigamemanager(), maps.get(a), sender);
									}
									return true;
								}
							}
							sender.sendMessage(ChatColor.GREEN + "[" + getName() + "] " + ChatColor.RED + "マップが見つかりません");
						} else {
							List<Integer> a = new ArrayList<Integer>();
							for (int b = 0; b < games.size(); b++) {
								if (games.get(b) instanceof GameFFA) {
									a.add(new Integer(b));
								}
							}
							if (0 < a.size()) {
								((MultiGame) games.get(a.get(new Random().nextInt(a.size())))).addPlayer(sender);
							} else {
								List<Integer> b = new ArrayList<Integer>();
								for (int c = 0; c < maps.size(); c++) {
									if (maps.get(c).spawn != null) {
										boolean d = true;
										for (int e = 0; e < games.size(); e++) {
											if (games.get(e) instanceof GameFFA && maps.get(c).equals(((GameFFA) games.get(e)).map)) {
												d = false;
												break;
											}
										}
										if (d) {
											b.add(c);
										}
									}
								}
								if (0 < b.size()) {
									new GameFFA(this, org.densyakun.bukkit.minigamemanager.Main.getMinigamemanager(), maps.get(b.get(new Random().nextInt(b.size()))), sender);
								} else {
									sender.sendMessage(ChatColor.GREEN + "[" + getName() + "] " + ChatColor.RED + "使用可能なマップがありません");
								}
							}
						}
					} else {
						sender.sendMessage(ChatColor.GREEN + "[" + getName() + "] " + ChatColor.RED + "ゲーム中です");
					}
				}
			}*/ else if (args[1].equalsIgnoreCase("join")) {
				if (sender instanceof Player) {
					if (MiniGameManager.minigamemanager
							.getPlayingGame(((OfflinePlayer) sender).getUniqueId()) == null) {
						List<Game> games = MiniGameManager.minigamemanager.getPlayingGames();
						for (int a = 0; a < games.size(); a++) {
							Game game = games.get(a);
							if (!(game instanceof GameFFA && game.isJoinable())) {
								games.remove(a);
							}
						}
						MapFFA map = null;
						if (args.length != 2) {
							for (int a = 0; a < maps.size(); a++) {
								map = maps.get(a);
								if (map.mapname.equals(args[2])) {
									for (int c = 0; c < games.size(); c++) {
										Game game = games.get(c);
										if (game instanceof GameFFA
												&& map.mapname.equals(((GameFFA) game).map.mapname)) {
											if (!MiniGameManager.minigamemanager.joinGame((Player) sender, game)) {
												sender.sendMessage(prefix
														+ ChatColor.RED + "このマップが使用しているゲームに入ることが出来ません");
											}
											return true;
										}
									}
									return true;
								}
							}
							sender.sendMessage(prefix + ChatColor.RED + "マップが見つかりません");
						}
						if (0 < games.size()) {
							int a;
							while (0 < games.size() && !MiniGameManager.minigamemanager.joinGame((Player) sender,
									games.get(a = new Random().nextInt(games.size())))) {
								games.remove(a);
							}
						}
						if (0 == games.size()) {
							List<MapFFA> b = maps;
							for (int c = 0; c < b.size();) {
								for (int e = 0; e < games.size(); e++) {
									Game game = games.get(e);
									if (game instanceof GameFFA && maps.get(c).equals(((GameFFA) game).map)) {
										b.remove(c);
										break;
									}
								}
							}
							if (0 < b.size()) {
								MiniGameManager.minigamemanager.joinGame((Player) sender,
										new GameFFA(b.get(new Random().nextInt(b.size()))));
							} else {
								sender.sendMessage(prefix + ChatColor.RED + "使用可能なマップがありません");
							}
						}
					} else {
						sender.sendMessage(prefix + ChatColor.RED + "ゲーム中です");
					}
				}
			} else if (args[1].equalsIgnoreCase("stats")) {
				if (args.length <= 2) {
					if (sender instanceof Player) {
						PlayerData data = getPlayerData(((OfflinePlayer) sender).getUniqueId());
						if (data != null) {
							sender.sendMessage(prefix + ChatColor.GOLD + ((Player) sender).getDisplayName()
											+ "の情報: \nKill: " + data.getKill() + "\nDeath: " + data.getDeath());
						}
					}
				} else {
					@SuppressWarnings("deprecation")
					OfflinePlayer player = getServer().getOfflinePlayer(args[2]);
					if (player != null) {
						PlayerData data = getPlayerData(player.getUniqueId());
						if (data != null) {
							sender.sendMessage(prefix + ChatColor.GOLD
									+ (player.getPlayer() != null ? player.getPlayer().getDisplayName()
											: player.getName())
									+ "の情報: \nKill: " + data.getKill() + "\nDeath: " + data.getDeath());
						}
					}
				}
			} else {
				sender.sendMessage(prefix + ChatColor.GOLD + param_wrong_cmd);
				sender.sendMessage(ChatColor.GREEN + "/game ffa (map|kit|join|stats)");
			}
			return true;
		}
		return false;
	}
	public void mapsave() {
		getDataFolder().mkdirs();
		try {
			mapsfile.createNewFile();
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(mapsfile));
			oos.writeObject(maps);
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void kitssave() {
		getDataFolder().mkdirs();
		try {
			kitsfile.createNewFile();
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(kitsfile));
			oos.writeObject(kits);
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void pdatasave() {
		getDataFolder().mkdirs();
		try {
			datafile.createNewFile();
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(datafile));
			oos.writeObject(pdata);
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Kit getKit(String name) {
		for (int a = 0; a < kits.size(); a++) {
			Kit kit = kits.get(a);
			if (kit.name.equalsIgnoreCase(name)) {
				return kit;
			}
		}
		return null;
	}

	public void putKit(Kit kit) {
		for (int a = 0; a < kits.size(); a++) {
			if (kits.get(a).name.equalsIgnoreCase(kit.name)) {
				kits.set(a, kit);
				return;
			}
		}
		kits.add(kit);
	}
	
	public PlayerData getPlayerData(UUID uuid) {
		for (int a = 0; a < pdata.size(); a++) {
			if (pdata.get(a).getUuid().equals(uuid)) {
				return pdata.get(a);
			}
		}
		PlayerData data = new PlayerData(uuid);
		pdata.add(data);
		return data;
	}
	
	public void UpdatePlayerData(PlayerData data) {
		boolean a = true;
		for (int b = 0; b < pdata.size(); b++) {
			if (pdata.get(b).getUuid().equals(data.getUuid())) {
				a = false;
				pdata.set(b, data);
				break;
			}
		}
		if (a) {
			pdata.add(data);
		}
		pdatasave();
	}
	
	public List<MapFFA> getMaps() {
		return maps;
	}
	
	public List<MapFFA> getEnabledMaps() {
		List<Game> games = MiniGameManager.minigamemanager.getPlayingGames();
		List<MapFFA> a = maps;
		for (int b = 0; b < games.size(); b++) {
			Game game = games.get(b);
			if (game instanceof GameFFA) {
				for (int c = 0; c < a.size();) {
					if (((GameFFA) game).map.mapname.equals(a.get(c))) {
						a.remove(c);
						break;
					} else {
						c++;
					}
				}
			}
		}
		return a;
	}
	
	public Location adjustLocation(Location location) {
		location.setX((double) (Math.round(location.getX() * 2)) / 2);
		location.setY((double) (Math.round(location.getY() * 2)) / 2);
		location.setZ((double) (Math.round(location.getZ() * 2)) / 2);
		location.setYaw((float) (Math.round(location.getYaw() / 15)) * 15);
		location.setPitch((float) (Math.round(location.getPitch() / 15)) * 15);
		return location;
	}
	@EventHandler
	public void PlayerDeath(PlayerDeathEvent e) {
		Game game = MiniGameManager.minigamemanager.getPlayingGame(e.getEntity().getUniqueId());
		if (game != null && game instanceof GameFFA) {
			e.setKeepInventory(true);
			PlayerData data = main.getPlayerData(e.getEntity().getUniqueId());
			data.death += 1;
			main.UpdatePlayerData(data);
			Player killer = e.getEntity().getKiller();
			if (killer != null) {
				killer.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE));
				if (main.getServer().getPluginManager().getPlugin("iConomy") != null && 0.1 <= main.prize) {
					new Account(e.getEntity().getKiller().getName()).getHoldings().add(main.prize);
					iConomy.Template.set(Template.Node.PLAYER_CREDIT);
					iConomy.Template.add("name", e.getEntity().getKiller().getName());
					iConomy.Template.add("amount", iConomy.format(main.prize));
					Messaging.send(e.getEntity().getKiller(), iConomy.Template.color(Template.Node.TAG_MONEY) + iConomy.Template.parse());
					Account account = new Account(e.getEntity().getName());
					if (0.1 <= ((!main.minus && account.getHoldings().getBalance() < main.lost) ? account.getHoldings().getBalance() : main.lost)) {
						iConomy.Template.set(Template.Node.PLAYER_DEBIT);
						if (!main.minus && account.getHoldings().getBalance() < main.lost) {
							iConomy.Template.add("amount", iConomy.format(account.getHoldings().getBalance()));
							account.getHoldings().subtract(account.getHoldings().getBalance());
						} else {
							account.getHoldings().subtract(main.lost);
							iConomy.Template.add("amount", iConomy.format(main.lost));
						}
						iConomy.Template.add("name", e.getEntity().getName());
						Messaging.send(e.getEntity(), iConomy.Template.color(Template.Node.TAG_MONEY) + iConomy.Template.parse());
					}
				}
				PlayerData killerdata = main.getPlayerData(e.getEntity().getKiller().getUniqueId());
				killerdata.kill += 1;
				main.UpdatePlayerData(killerdata);
				e.getEntity().getKiller().getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE));
			}
			game.removePlayer(e.getEntity().getUniqueId());
		}
	}
	@EventHandler
	public void PlayerTeleport(PlayerTeleportEvent e) {
		Game game = MiniGameManager.minigamemanager.getPlayingGame(e.getPlayer().getUniqueId());
		if (game != null && game instanceof GameFFA && e.getCause() == TeleportCause.COMMAND) {
			e.setCancelled(true);
		}
	}
	@EventHandler
	public void PlayerPickupItem(PlayerPickupItemEvent e) {
		Game game = MiniGameManager.minigamemanager.getPlayingGame(e.getPlayer().getUniqueId());
		if (game != null && game instanceof GameFFA) {
			e.setCancelled(true);
		}
	}
	@EventHandler
	public void PlayerDropItem(PlayerDropItemEvent e) {
		Game game = MiniGameManager.minigamemanager.getPlayingGame(e.getPlayer().getUniqueId());
		if (game != null && game instanceof GameFFA) {
			e.setCancelled(true);
		}
	}
	@EventHandler
	public void BlockBreak(BlockBreakEvent e) {
		Game game = MiniGameManager.minigamemanager.getPlayingGame(e.getPlayer().getUniqueId());
		if (game != null && game instanceof GameFFA) {
			e.setCancelled(true);
		}
	}
	@EventHandler
	public void BlockPlace(BlockPlaceEvent e) {
		Game game = MiniGameManager.minigamemanager.getPlayingGame(e.getPlayer().getUniqueId());
		if (game != null && game instanceof GameFFA) {
			e.setCancelled(true);
		}
	}
}
