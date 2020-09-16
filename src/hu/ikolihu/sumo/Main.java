package hu.ikolihu.sumo;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import hu.ikolihu.sumo.files.locationManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class Main extends JavaPlugin implements Listener {
	public ArrayList<String> names = new ArrayList<>();
	public Rounder counter;
	public boolean join = true;
	public boolean start = true;

	public locationManager locations;

	@Override
	public void onEnable() {
		counter = new Rounder(this);
		this.getServer().getPluginManager().registerEvents(counter, this);
		getCommand("sumo").setTabCompleter(new SumoTabComplete(this));
		this.reloadConfig();
		this.saveDefaultConfig();
		this.locations = new locationManager(this);
		System.out.println("SUMO started");
	}

	@Override
	public void onDisable() {

	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player = (Player) sender;

		if (label.equalsIgnoreCase("sumo")) {
			if (args.length == 0) {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&',
						"&4&m▬▬▬▬▬&8&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬&4&m▬▬▬▬▬\n"
								+ "&f            Készítő: &cikoliHU\n" + "&f             Verzió: &c"
								+ this.getDescription().getVersion() + "\n"
								+ "&4&m▬▬▬▬▬&8&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬&4&m▬▬▬▬▬"));
			}
			if (args.length > 0) {
				if (args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("j")) {
					Location lobby = locations.getConfig().getLocation("lobby");
					if (join == true) {
						if (lobby != null) {
							if (!names.contains(player.getName())) {
								player.sendMessage(
										ChatColor.translateAlternateColorCodes('&',
												this.getConfig().getString("prefix") + this.getConfig()
														.getString("messages.player-join")
														.replace("{minplayers}",
																String.valueOf(this.getConfig().getInt("minplayers")))
														.replace("{count}", String.valueOf(names.size() + 1))));
								for (int i = 0; i < names.size(); i++) {
									getServer().getPlayer(names.get(i)).sendMessage(ChatColor
											.translateAlternateColorCodes('&', this.getConfig().getString("prefix")
													+ this.getConfig().getString("messages.join")
															.replace("{player}", player.getName())
															.replace("{count}", String.valueOf(names.size() + 1))
															.replace("{minplayers}",
																	this.getConfig().getString("minplayers"))));
								}
								names.add(player.getName());
								player.teleport(lobby);

							} else {
								player.sendMessage(
										ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("prefix")
												+ this.getConfig().getString("messages.already-joined")));
							}

						} else {
							sender.sendMessage(
									ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("prefix"))
											+ ChatColor.translateAlternateColorCodes('&',
													this.getConfig().getString("messages.none-lobby")));
						}
					} else {
						sender.sendMessage(
								ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("prefix")
										+ this.getConfig().getString("messages.already-started")));
					}

				} else if (args[0].equalsIgnoreCase("help")) {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&',
							"&4&m▬▬▬▬▬&8&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬&4&m▬▬▬▬▬\n"
									+ "&f /sumo &7ad - &cHirdetés\n &f/sumo &7join - &c Csatlakozás az eventhez\n &f/sumo &7leave - &CEvent elhagyása\n &f/sumo &7set - &cSumo set parancsok \n &f/sumo &7start - &c Event indítása"
									+ "\n&4&m▬▬▬▬▬&8&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬&4&m▬▬▬▬▬"));
				} else if (args[0].equalsIgnoreCase("leave") || args[0].equalsIgnoreCase("l")) {
					if (names.contains(player.getName())) {
						names.remove(player.getName());
						player.sendMessage(
								ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("prefix")
										+ this.getConfig().getString("messages.player-leave")));
						if (counter.isIngame(sender)) {
							counter.checkWinner();
						}
						for (int i = 0; i < names.size(); i++) {
							getServer().getPlayer(names.get(i))
									.sendMessage(ChatColor.translateAlternateColorCodes('&',
											this.getConfig().getString("prefix") + this.getConfig()
													.getString("messages.leave").replace("{player}", player.getName())
													.replace("{minplayers}", this.getConfig().getString("minplayers"))
													.replace("{count}", String.valueOf(names.size()))));
						}
						player.teleport(locations.getConfig().getLocation("spawn"));
					}
				} else if (args[0].equalsIgnoreCase("set")) {
					if (sender.hasPermission("sumo.admin")) {
						if (args.length > 1) {
							if (args[1].equalsIgnoreCase("lobby")) {

								locations.getConfig().set("lobby", player.getLocation());
								locations.saveConfig();
								locations.reloadConfig();
								sender.sendMessage(
										ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("prefix")
												+ this.getConfig().getString("messages.lobby-set")));
							} else if (args[1].equalsIgnoreCase("spawn")) {

								locations.getConfig().set("spawn", player.getLocation());
								locations.saveConfig();
								locations.reloadConfig();
								sender.sendMessage(
										ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("prefix")
												+ this.getConfig().getString("messages.spawn-set")));

							} else if (args[1].equalsIgnoreCase("y")) {
								if (sender.hasPermission("sumo.admin")) {
									locations.getConfig().set("min-y-height", player.getLocation().getBlockY());
									locations.saveConfig();
									locations.reloadConfig();
									sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
											this.getConfig().getString("prefix")
													+ this.getConfig().getString("messages.y-set")));
								}
							} else if (args[1].equalsIgnoreCase("1")) {
								locations.getConfig().set("location1", player.getLocation());
								locations.saveConfig();
								locations.reloadConfig();
								sender.sendMessage(
										ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("prefix")
												+ this.getConfig().getString("messages.location-set1")));
							} else if (args[1].equalsIgnoreCase("2")) {
								if (sender.hasPermission("sumo.admin")) {
									locations.getConfig().set("location2", player.getLocation());
									locations.saveConfig();
									locations.reloadConfig();
									sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
											this.getConfig().getString("prefix")
													+ this.getConfig().getString("messages.location-set2")));
								}
							}
						} else {
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
									"&4&m▬▬▬▬▬&8&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬&4&m▬▬▬▬▬\n&r &c/sumo set spawn - &7Spawn beállítása\n &c/sumo set lobby - &7Sumo lobby beállítása\n "
											+ "&c/sumo set 1 - &7Első harcos helye\n &c/sumo set 2 -&7Második harcos helye\n &c/sumo set y - &7Y érték beállítása\n"
											+ "&4&m▬▬▬▬▬&8&m▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬&4&m▬▬▬▬▬"));
						}
					} else {
						sender.sendMessage(
								ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("prefix"))
										+ ChatColor.translateAlternateColorCodes('&',
												this.getConfig().getString("messages.noperm")));
					}
				} else if (args[0].equalsIgnoreCase("reload")) {
					if (!sender.hasPermission("sumo.admin")) {
						sender.sendMessage(
								ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("prefix"))
										+ ChatColor.translateAlternateColorCodes('&',
												this.getConfig().getString("messages.noperm")));
					} else {
						this.reloadConfig();
						this.saveDefaultConfig();
						sender.sendMessage(
								ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("prefix"))
										+ ChatColor.translateAlternateColorCodes('&',
												this.getConfig().getString("messages.reload")));
					}
				}  else if (args[0].equalsIgnoreCase("start") || args[0].equalsIgnoreCase("s")) {
					if (sender.hasPermission("sumo.start")) {
						if (names.size() >= this.getConfig().getInt("minplayers")) {
							if (!(locations.getConfig().getLocation("location1") == null)) {
								if (!(locations.getConfig().getLocation("location2") == null)) {
									if (!(locations.getConfig().getLocation("lobby") == null)) {
										if (!(locations.getConfig().getLocation("spawn") == null)) {
											if (!(locations.getConfig().getString("min-y-height") == null)) {
												if (start == true) {
													start = false;
													counter.start();
												} else {
													sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
															this.getConfig().getString("prefix") + this.getConfig()
																	.getString("messages.arena-already-started")));

												}
											} else {
												sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
														this.getConfig().getString("prefix")
																+ this.getConfig().getString("messages.y-null")));
											}
										} else {
											sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
													this.getConfig().getString("prefix")
															+ this.getConfig().getString("messages.none-spawn")));
										}
									} else {
										sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
												this.getConfig().getString("prefix")
														+ this.getConfig().getString("messages.none-lobby")));
									}
								} else {
									sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
											this.getConfig().getString("prefix")
													+ this.getConfig().getString("messages.none-loc2")));
								}
							} else {
								sender.sendMessage(
										ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("prefix")
												+ this.getConfig().getString("messages.none-loc1")));
							}

						} else {
							sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
									this.getConfig().getString("prefix")
											+ this.getConfig().getString("messages.minplayers")
													.replace("{minplayers}", this.getConfig().getString("minplayers"))
													.replace("{count}", String.valueOf(names.size()))));

						}
					} else {
						sender.sendMessage(
								ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("prefix"))
										+ ChatColor.translateAlternateColorCodes('&',
												this.getConfig().getString("messages.noperm")));
					}
				} else if (args[0].equalsIgnoreCase("ad")) {
					if (sender.hasPermission("sumo.start")) {
						TextComponent message = new TextComponent(ChatColor.translateAlternateColorCodes('&',
								ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("prefix"))
										+ this.getConfig().getString("messages.ad")
												.replace("{minplayers}", this.getConfig().getString("minplayers"))
												.replace("{count}", String.valueOf(names.size()))

						));
						message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sumo join"));
						message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
								new ComponentBuilder(ChatColor.translateAlternateColorCodes('&',
										this.getConfig().getString("messages.hover"))).create()));
						getServer().spigot().broadcast(message);
					} else {
						sender.sendMessage(
								ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("prefix"))
										+ ChatColor.translateAlternateColorCodes('&',
												this.getConfig().getString("messages.noperm")));
					}
				}
			}
		}

		return true;
	}
}