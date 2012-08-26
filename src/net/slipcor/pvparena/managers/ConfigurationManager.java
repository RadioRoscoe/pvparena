package net.slipcor.pvparena.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.core.Config;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.neworder.ArenaGoal;
import net.slipcor.pvparena.neworder.ArenaRegionShape;

/**
 * <pre>Configuration Manager class</pre>
 * 
 * Provides static methods to manage Configurations
 * 
 * @author slipcor
 * 
 * @version v0.9.0
 */

public class ConfigurationManager {
	private static Debug db = new Debug(25);

	/**
	 * create a config manager instance
	 * 
	 * @param arena
	 *            the arena to load
	 * @param cfg
	 *            the configuration
	 */
	public static void configParse(Arena arena, Config cfg) {
		cfg.load();
		YamlConfiguration config = cfg.getYamlConfiguration();

		if (cfg.get("general.type") != null) {
			// opening existing arena
			arena.setFree(cfg.getString("general.type").equals("free"));
			
			List<String> list = cfg.getStringList("arenagoals", new ArrayList<String>());
			for (String type : list) {
				ArenaGoal aType = PVPArena.instance.getAgm().getType(type);

				arena.addGoal((ArenaGoal) aType.clone());
			}
			
		} else {
			System.out.print("oouuups");
		}

		if (config.get("classitems") == null) {
			if (PVPArena.instance.getConfig().get("classitems") != null) {
				for (String key : PVPArena.instance.getConfig().getKeys(false)) {
					config.addDefault("classitems."+key, PVPArena.instance.getConfig().get("classitems."+key));
				}
			} else {
				config.addDefault("classitems.Ranger", "261,262:64,298,299,300,301");
				config.addDefault("classitems.Swordsman", "276,306,307,308,309");
				config.addDefault("classitems.Tank", "272,310,311,312,313");
				config.addDefault("classitems.Pyro", "259,46:3,298,299,300,301");
			}
		}
		config.addDefault("tp.win", "old");
		config.addDefault("tp.lose", "old");
		config.addDefault("tp.exit", "exit");
		config.addDefault("tp.death", "spectator");

		config.addDefault("setup.wand", Integer.valueOf(280));

		config.addDefault("game.allowDrops", Boolean.valueOf(true));
		config.addDefault("game.dropSpawn", Boolean.valueOf(false));
		config.addDefault("game.lives", Integer.valueOf(3));
		config.addDefault("game.preventDeath", Boolean.valueOf(true));
		config.addDefault("game.teamKill", Boolean.valueOf(arena.isFreeForAll()));
		config.addDefault("game.refillInventory", Boolean.valueOf(false));
		config.addDefault("game.weaponDamage", Boolean.valueOf(true));
		config.addDefault("game.mustbesafe", Boolean.valueOf(false));
		config.addDefault("game.woolFlagHead", Boolean.valueOf(false));

		config.addDefault("messages.language", "en");
		config.addDefault("messages.chat", Boolean.valueOf(true));
		config.addDefault("messages.defaultChat", Boolean.valueOf(false));
		config.addDefault("messages.onlyChat", Boolean.valueOf(false));

		config.addDefault("general.classperms", Boolean.valueOf(false));
		config.addDefault("general.enabled", Boolean.valueOf(true));
		config.addDefault("general.restoreChests", Boolean.valueOf(false));
		config.addDefault("general.signs", Boolean.valueOf(true));
		config.addDefault("general.type", arena.isFreeForAll()?"free":"team");
		config.addDefault("general.item-rewards", "none");
		config.addDefault("general.random-reward", Boolean.valueOf(false));
		config.addDefault("general.prefix", "PVP Arena");
		config.addDefault("general.cmdfailjoin", Boolean.valueOf(true));

		config.addDefault("region.spawncampdamage", Integer.valueOf(1));
		config.addDefault("region.timer", Integer.valueOf(20));
		
		config.addDefault("join.explicitPermission", Boolean.valueOf(false));
		config.addDefault("join.manual", Boolean.valueOf(!arena.isFreeForAll()));
		config.addDefault("join.random", Boolean.valueOf(true));
		config.addDefault("join.onCountdown", Boolean.valueOf(false));
		config.addDefault("join.forceeven", Boolean.valueOf(false));
		config.addDefault("join.inbattle", Boolean.valueOf(false));
		config.addDefault("join.range", Integer.valueOf(0));
		config.addDefault("join.warmup", Integer.valueOf(0));
		
		config.addDefault("arenatype.randomSpawn", arena.isFreeForAll());
		config.addDefault("goal.timed", Integer.valueOf(0));
		config.addDefault("goal.endtimer", Integer.valueOf(20));

		config.addDefault("periphery.checkRegions", Boolean.valueOf(false));

		config.addDefault("protection.spawn", Integer.valueOf(3));
		config.addDefault("protection.restore", Boolean.valueOf(true));
		config.addDefault("protection.enabled", Boolean.valueOf(true));
		
		config.addDefault("protection.blockplace", Boolean.valueOf(true));
		config.addDefault("protection.blockdamage", Boolean.valueOf(true));
		config.addDefault("protection.blocktntdamage", Boolean.valueOf(true));
		config.addDefault("protection.decay", Boolean.valueOf(true));
		config.addDefault("protection.drop", Boolean.valueOf(true));
		config.addDefault("protection.fade", Boolean.valueOf(true));
		config.addDefault("protection.form", Boolean.valueOf(true));
		config.addDefault("protection.fluids", Boolean.valueOf(true));
		config.addDefault("protection.firespread", Boolean.valueOf(true));
		config.addDefault("protection.grow", Boolean.valueOf(true));
		config.addDefault("protection.lavafirespread", Boolean.valueOf(true));
		config.addDefault("protection.lighter", Boolean.valueOf(true));
		config.addDefault("protection.painting", Boolean.valueOf(true));
		config.addDefault("protection.piston", Boolean.valueOf(true));
		config.addDefault("protection.punish", Boolean.valueOf(false));
		config.addDefault("protection.tnt", Boolean.valueOf(true));
		
		config.addDefault("protection.checkExit", Boolean.valueOf(false));
		config.addDefault("protection.checkSpectator", Boolean.valueOf(false));
		config.addDefault("protection.checkLounges", Boolean.valueOf(false));
		config.addDefault("protection.inventory", Boolean.valueOf(false));

		config.addDefault("delays.giveitems", Integer.valueOf(0));
		config.addDefault("delays.inventorysave", Integer.valueOf(0));
		config.addDefault("delays.inventoryprepare", Integer.valueOf(0));
		config.addDefault("delays.playerdestroy", Integer.valueOf(0));

		config.addDefault("start.countdown", Integer.valueOf(5));
		config.addDefault("start.health", Integer.valueOf(20));
		config.addDefault("start.foodLevel", Integer.valueOf(20));
		config.addDefault("start.saturation", Integer.valueOf(20));
		config.addDefault("start.exhaustion", Float.valueOf(0));

		config.addDefault("ready.startRatio", Float.valueOf((float) 0.5));
		config.addDefault("ready.block", "IRON_BLOCK");
		config.addDefault("ready.checkEach", Boolean.valueOf(true));
		config.addDefault("ready.checkEachTeam", Boolean.valueOf(true));
		config.addDefault("ready.min", Integer.valueOf(2));
		config.addDefault("ready.max", Integer.valueOf(0));
		config.addDefault("ready.minTeam", Integer.valueOf(1));
		config.addDefault("ready.maxTeam", Integer.valueOf(0));
		config.addDefault("ready.autoclass", "none");
		config.addDefault("ready.startRatio", Float.valueOf((float) 0.5));

		PVPArena.instance.getAgm().setDefaults(arena, config);

		config.options().copyDefaults(true);

		cfg.set("cfgver", "0.9.0.0");
		cfg.save();
		cfg.load();

		Map<String, Object> classes = config.getConfigurationSection(
				"classitems").getValues(false);
		arena.getClasses().clear();
		db.i("reading class items");
		for (String className : classes.keySet()) {
			String s = "";
			
			try {
				s = (String) classes.get(className);
			} catch (Exception e) {
				Bukkit.getLogger().severe("[PVP Arena] Error while parsing class, skipping: " + className);
				continue;
			}
			String[] ss = s.split(",");
			ItemStack[] items = new ItemStack[ss.length];

			for (int i = 0; i < ss.length; i++) {
				items[i] = StringParser.getItemStackFromString(ss[i]);
				if (items[i] == null) {
					db.w("unrecognized item: " + items[i]);
				}
			}
			arena.addClass(className, items);
			db.i("adding class items to class " + className);
		}
		arena.addClass("custom", StringParser.getItemStacksFromString("0"));
		if (cfg.getString("general.owner") != null) {
			arena.setOwner(cfg.getString("general.owner"));
		}
		if (config.getConfigurationSection("regions") != null) {
			Map<String, Object> regs = config
					.getConfigurationSection("regions").getValues(false);
			for (String rName : regs.keySet()) {
				ArenaRegionShape region = PVPArena.instance.getArsm().readRegionFromConfig(rName, config, arena);
				
				if (region == null) {
					System.out.print("[SEVERE] Error while loading arena, region null: " + rName);
				} else if (region.getWorld() == null) {
					System.out.print("[SEVERE] Error while loading arena, world null: " + rName);
				} else {
					arena.getRegions().add(region);
				}
			}
		}

		Map<String, Object> tempMap = (Map<String, Object>) cfg
				.getYamlConfiguration().getConfigurationSection("teams")
				.getValues(true);

		for (String sTeam : tempMap.keySet()) {
			ArenaTeam team = new ArenaTeam(sTeam, (String) tempMap.get(sTeam));
			arena.getTeams().add(team);
			db.i("added team " + team.getName() + " => "
					+ team.getColorCodeString());
		}
		cfg.save();

		PVPArena.instance.getAgm().configParse(arena, config);
		PVPArena.instance.getAmm().configParse(arena, config);
		
		cfg.reloadMaps();

		arena.setPrefix(cfg.getString("general.prefix", "PVP Arena"));
	}

	/**
	 * check if an arena is configured completely
	 * 
	 * @param arena
	 *            the arena to check
	 * @return an error string if there is something missing, null otherwise
	 */
	public static String isSetup(Arena arena) {
		arena.getArenaConfig().load();

		if (arena.getArenaConfig().get("spawns") == null) {
			return "no spawns set";
		}

		if (arena.isLocked()) {
			return "edit mode!";
		}

		Set<String> list = arena.getArenaConfig().getYamlConfiguration()
				.getConfigurationSection("spawns").getValues(false).keySet();

		// we need the 2 that every arena has

		if (!list.contains("spectator"))
			return "spectator not set";
		if (!list.contains("exit"))
			return "exit not set";
		String error = PVPArena.instance.getAmm().checkForMissingSpawns(arena, list);
		if (error != null) {
			return error;
		}
		return PVPArena.instance.getAgm().checkForMissingSpawns(arena, list);
	}
}