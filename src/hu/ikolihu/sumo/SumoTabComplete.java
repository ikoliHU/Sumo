package hu.ikolihu.sumo;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class SumoTabComplete implements TabCompleter {

	public SumoTabComplete(Main main) {
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender.hasPermission("sumo.admintab")) {
			if (args.length == 1) {
				List<String> commands = new ArrayList<String>();
				commands.add("ad");
				commands.add("help");
				commands.add("join");
				commands.add("leave");
				commands.add("set");
				commands.add("start");
				return commands;
			}

			if (args.length > 0 && args[0].equalsIgnoreCase("set")) {
				if (args.length >= 2) {
					List<String> setcommands = new ArrayList<String>();
					setcommands.add("1");
					setcommands.add("2");
					setcommands.add("lobby");
					setcommands.add("spawn");
					setcommands.add("y");
					return setcommands;
				}
			}
		} else {
			if (args.length == 1) {
				List<String> commands = new ArrayList<String>();
				commands.add("join");
				commands.add("leave");
				return commands;
			}
		}
		return null;
	}

}
