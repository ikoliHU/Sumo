package hu.ikolihu.sumo.files;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import hu.ikolihu.sumo.Main;

public class locationManager {

	private Main plugin;
	private FileConfiguration locationsConfig = null;
	private File configFile = null;

	public locationManager(Main plugin) {
		this.plugin = plugin;
		saveDefaultConfig();
	}

	public void reloadConfig() {
		if (this.configFile == null)
			this.configFile = new File(this.plugin.getDataFolder(), "locations.yml");
		this.locationsConfig = YamlConfiguration.loadConfiguration(this.configFile);

		InputStream defaultStream = this.plugin.getResource("locations.yml");
		if (defaultStream != null) {
			YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream));
			this.locationsConfig.setDefaults(defaultConfig);
		}
	}

	public FileConfiguration getConfig() {
		if (this.locationsConfig == null)
			reloadConfig();
		return this.locationsConfig;
	}

	public void saveConfig() {
		if (this.locationsConfig == null || this.configFile == null)
			return;
		try {
			this.getConfig().save(this.configFile);
		} catch (IOException e) {
			plugin.getLogger().log(Level.SEVERE, "Nem mentheto config ide: " + this.configFile, e);
		}
	}

	public void saveDefaultConfig() {
		if (this.configFile == null)
			this.configFile = new File(this.plugin.getDataFolder(), "locations.yml");

		if (!this.configFile.exists()) {
			this.plugin.saveResource("locations.yml", false);
		}
	}
}
