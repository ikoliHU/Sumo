package hu.ikolihu.sumo;

import java.util.ArrayList;
import java.util.Collections;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.command.CommandSender;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Rounder implements Listener {
	private ArrayList<String> freez = new ArrayList<String>();
	private ArrayList<String> ingame = new ArrayList<String>();
	private int cd = 0;
	private int time;
	private int next = 0;
	private int leaver = 0;

	static Main plugin;

	public Rounder(Main main) {
		plugin = main;
	}

	public boolean isIngame(CommandSender sender) {
		return (ingame.contains(sender.getName()));
	}

	public String inGameContent() {
		if (ingame.size() != 0) {
			return (ingame.get(0));
		} else {
			return "§4Nem elérhetõ egy játékos sem";
		}
	}

	public void nextRound() {
		Player p1 = plugin.getServer().getPlayer(plugin.names.get(next));
		Player p2 = plugin.getServer().getPlayer(plugin.names.get(next + 1));

		p1.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 9999, true));
		p2.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 9999, true));

		p1.teleport(plugin.locations.getConfig().getLocation("location1"));
		p2.teleport(plugin.locations.getConfig().getLocation("location2"));

		for (int i = 0; i < plugin.names.size(); i++) {
			plugin.getServer().getPlayer(plugin.names.get(i)).playSound(plugin.getServer().getPlayer(plugin.names.get(i)).getLocation(), Sound.BLOCK_BELL_USE, 10, 1);
		}
		freez.add(p1.getName());
		freez.add(p2.getName());
		ingame.add(p1.getName());
		ingame.add(p2.getName());
		time = 4;
		cd = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			@Override
			public void run() {
				p1.sendTitle(ChatColor.GREEN + String.valueOf(time), "", 20, 30, 20);
				p2.sendTitle(ChatColor.GREEN + String.valueOf(time), "", 20, 30, 20);
				if (time == 0) {
					freez.remove(p1.getName());
					freez.remove(p2.getName());
					p1.sendTitle("", "", 20, 30, 20);
					p2.sendTitle("", "", 20, 30, 20);
					Bukkit.getScheduler().cancelTask(cd);
				}
				time--;
			}
		}, 0, 20);
	}

	public void checkWinner() {
		if (plugin.names.size() == 1) {
			plugin.join = true;
			plugin.start = true;
			for (String c : plugin.getConfig().getStringList("winner-commands")) {
				plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
						c.replace("{player}", plugin.names.get(0)));
			}
			Player p = plugin.getServer().getPlayer(plugin.names.get(0));
			p.sendMessage(ChatColor.translateAlternateColorCodes('&',
					plugin.getConfig().getString("prefix") + plugin.getConfig().getString("messages.match-winner")));
			Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix")
					+ plugin.getConfig().getString("messages.winner").replace("{player}", p.getName())));
			p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 10, 1);
			fireWork();
			plugin.names.clear();
			ingame.clear();
			freez.clear();
			playerLeaver();
		} else {
			next++;
			if (next + 1 < plugin.names.size()) {
			} else {
				next = 0;
			}
			nextRound();

		}
	}

	public void playerLeaver() {

		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				leaver = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
					@Override
					public void run() {

						World world = plugin.locations.getConfig().getLocation("lobby").getWorld();
						if (world != null) {
							if (world.getPlayers().size() > 0) {
								world.getPlayers().get(0).teleport(plugin.locations.getConfig().getLocation("spawn"));
								if (world.getPlayers().size() == 0) {
									Bukkit.getScheduler().cancelTask(leaver);
								}
								return;
							}
						}

					}
				}, 0, 10);
			}

		}, 160);
	}

	public void onStop() {

		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				leaver = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
					@Override
					public void run() {

						World world = plugin.locations.getConfig().getLocation("lobby").getWorld();
						if (world != null) {
							if (world.getPlayers().size() > 0) {
								world.getPlayers().get(0).teleport(plugin.locations.getConfig().getLocation("spawn"));
								if (world.getPlayers().size() == 0) {
									Bukkit.getScheduler().cancelTask(leaver);
									Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
											plugin.getConfig().getString("prefix")
													+ plugin.getConfig().getString("messages.stop")));
								}
								return;
							}
						}

					}
				}, 0, 10);
			}

		}, 160);
	}

	public void fireWork() {
		Player p = plugin.getServer().getPlayer(plugin.names.get(0));
		Firework fw = (Firework) p.getWorld().spawn(p.getPlayer().getLocation(), Firework.class);
		FireworkMeta fwm = fw.getFireworkMeta();
		Builder builder = FireworkEffect.builder();

		fwm.addEffect(builder.flicker(true).withFlicker().withColor(Color.BLUE).build());
		fwm.setPower(1);
		fw.setFireworkMeta(fwm);
	}

	@EventHandler
	public void cancelMove(PlayerMoveEvent event) {
		if (freez.contains(event.getPlayer().getName())) {
			event.setCancelled(true);
		}
		if (ingame.contains(event.getPlayer().getName())) {
			if (ingame.size() != 1) {
				if (event.getPlayer().getLocation().getBlockY() <= plugin.locations.getConfig()
						.getInt("min-y-height")) {
					plugin.names.remove(event.getPlayer().getName());
					ingame.remove(event.getPlayer().getName());
					event.getPlayer().removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
					event.getPlayer().teleport(plugin.locations.getConfig().getLocation("spawn"));
					event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ENTITY_BEE_DEATH, 10, 1);
					event.getPlayer().sendMessage(
							ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix")
									+ plugin.getConfig().getString("messages.round-looser")));
					Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
						@Override
						public void run() {
							if (!ingame.isEmpty()) {
								Player p = plugin.getServer().getPlayer(ingame.get(0));
								if (p != null) {
									p.teleport(plugin.locations.getConfig().getLocation("lobby"));
									p.sendMessage(ChatColor.translateAlternateColorCodes('&',
											plugin.getConfig().getString("prefix")
													+ plugin.getConfig().getString("messages.round-winner")));
									p.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
									p.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 10, 1);
									Bukkit.getScheduler().cancelTask(leaver);
									ingame.clear();
									checkWinner();

								}
							}
						}

					}, 40);
				}

			}
		}
	}

	@EventHandler
	public void onTp(PlayerTeleportEvent event) {

		Player player = event.getPlayer();
		if (player != null)
			if (plugin.names.contains(player.getName())) {
				if (!plugin.locations.getConfig().getLocation("lobby").getWorld().getName()
						.equals(event.getTo().getWorld().getName())) {
					leavePlayer(player);
				}
			}
	}

	public void leavePlayer(Player player) {
		if (plugin.names.contains(player.getName())) {
			for (int i = 0; i < plugin.names.size(); i++) {
				plugin.getServer().getPlayer(plugin.names.get(i))
						.sendMessage(ChatColor.translateAlternateColorCodes('&',
								plugin.getConfig().getString("prefix") + plugin.getConfig().getString("messages.leave")
										.replace("{player}", player.getName())
										.replace("{minplayers}", plugin.getConfig().getString("minplayers"))
										.replace("{count}", String.valueOf(plugin.names.size()))));
			}
			plugin.names.remove(player.getName());
		}
		if (ingame.contains(player.getName())) {
			Bukkit.getScheduler().cancelTask(cd);
			ingame.remove(player.getName());
			freez.remove(player.getName());
			player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
			if (!ingame.isEmpty()) {
				Player p = plugin.getServer().getPlayer(ingame.get(0));
				if (p != null) {
					p.teleport(plugin.locations.getConfig().getLocation("lobby"));
					p.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
					ingame.remove(p.getName());
					freez.remove(p.getName());
				}
			}
			nextRound();
		}
	}

	@EventHandler
	public void onLeave(PlayerQuitEvent event) {
		leavePlayer(event.getPlayer());
	}

	public void start() {
		time = plugin.getConfig().getInt("countdown");
		cd = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			@Override
			public void run() {
				if (plugin.getConfig().getStringList("alert-times").contains(String.valueOf(time))) {
					Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',
							plugin.getConfig().getString("prefix") + plugin.getConfig().getString("messages.counter")
									.replace("{time}", String.valueOf(time))));
				} else if (time == 0) {
					Collections.shuffle(plugin.names);
					Bukkit.getScheduler().cancelTask(cd);
					plugin.join = false;
					Bukkit.broadcastMessage(
							ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("prefix")
									+ plugin.getConfig().getString("messages.arena-started")));
					for (int i = 0; i < plugin.names.size(); i++) {
						plugin.getServer().getPlayer(plugin.names.get(i)).playSound(
								plugin.getServer().getPlayer(plugin.names.get(i)).getLocation(),
								Sound.ENTITY_ELDER_GUARDIAN_CURSE, 10, 1);
					}
					nextRound();
				}
				time--;
				if (plugin.names.size() < plugin.getConfig().getInt("minplayers")) {
					plugin.start = true;
					Bukkit.getScheduler().cancelTask(cd);
					for (int i = 0; i < plugin.names.size(); i++) {
						plugin.getServer().getPlayer(plugin.names.get(i))
								.sendMessage(
										ChatColor
												.translateAlternateColorCodes('&',
														plugin.getConfig().getString("prefix")
																+ plugin.getConfig().getString("messages.minplayers")
																		.replace("{minplayers}",
																				plugin.getConfig()
																						.getString("minplayers"))
																		.replace("{count}",
																				String.valueOf(plugin.names.size()))
																+ " "));

					}
				}
			}
		}, 0, 20);
	}
}
