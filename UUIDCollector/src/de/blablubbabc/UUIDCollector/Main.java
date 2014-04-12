package de.blablubbabc.UUIDCollector;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {

	private boolean dirty = false;

	@Override
	public void onEnable() {
		this.getConfig().options().header("This file stores uuid <-> playername mappings for joining players. Those data could probably later be needed/useful to convert old playerdata. Check the console log for additional information!");

		// register player join listener:
		Bukkit.getPluginManager().registerEvents(this, this);

		// add all online players:
		for (Player player : Bukkit.getOnlinePlayers()) {
			this.collect(player);
		}
		
		// trigger a save to create the initial file:
		this.save();
		
		// start save task:
		Bukkit.getScheduler().runTaskTimer(this, new Runnable() {

			@Override
			public void run() {
				Main.this.save();
			}
		}, 20 * 60 * 5L, 20 * 60 * 5L);

		this.getLogger().info("----------------------------------------");
		this.getLogger().info("Starting to collect player uuids for connecting players ...");
		this.getLogger().info("The uuids which are collected now will be used later to convert old player data.");
		this.getLogger().info("Make sure that you have selected the correct online mode for your server, because uuids for offline and online mode servers don't match.");
		this.getLogger().info("If your server uses BungeeCoord then you might want to make sure that players have the correct uuids " 
								+ "(I think spigot has some setting to pass uuids from the proxy to the server).");
		this.getLogger().info("We will store all uuids for a playername (which can be multiple after name changing has been enabled and players switch names)," 
								+ "but will later probably only use the first uuid, of the first player which joined with that name.");
		this.getLogger().info("So you might also want to let your players know that if they plan to change their name later, " 
								+ "they should join at least once before name changing gets enabled in order to 'reserve' their old name for later player-data-conversion " 
								+ "(so that not somebody else gets their old data assigned later).");
		this.getLogger().info("Also don't wonder: player uuids won't get written to file immediately but only every 5 minutes and when the plugin/the server shuts down.");
		this.getLogger().info("----------------------------------------");
	}

	@Override
	public void onDisable() {
		Bukkit.getScheduler().cancelTasks(this);
		this.save();
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		this.collect(event.getPlayer());
	}

	private void collect(Player player) {
		String playerName = player.getName();
		String uuid = player.getUniqueId().toString();
		List<String> uuidsForName = this.getConfig().getStringList(playerName);
		if (uuidsForName.contains(uuid)) return; // we already got that player
		uuidsForName.add(uuid);
		this.getConfig().set(playerName, uuidsForName);
		this.dirty = true;
	}

	private void save() {
		if (this.dirty) {
			this.saveConfig();
			this.dirty = false;
		}
	}
}