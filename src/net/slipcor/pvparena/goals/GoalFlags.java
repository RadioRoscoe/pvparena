package net.slipcor.pvparena.goals;

import java.util.HashMap;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.classes.PACheckResult;
import net.slipcor.pvparena.classes.PALocation;
import net.slipcor.pvparena.commands.PAA_Region;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.StringParser;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.loadables.ArenaGoal;
import net.slipcor.pvparena.managers.SpawnManager;
import net.slipcor.pvparena.managers.StatisticsManager.type;
import net.slipcor.pvparena.managers.TeamManager;
import net.slipcor.pvparena.runnables.EndRunnable;

public class GoalFlags extends ArenaGoal {

	public GoalFlags(Arena arena) {
		super(arena, "Flags");
		db = new Debug(100);
	}
	
	private HashMap<String, Integer> paTeamLives = new HashMap<String, Integer>();
	private HashMap<String, String> paTeamFlags = new HashMap<String, String>();
	private HashMap<String, ItemStack> paHeadGears = new HashMap<String, ItemStack>();
	
	private Material flagMaterial = Material.WOOL; //TODO set
	
	@Override
	public String version() {
		return "v0.9.0.0";
	}
	
	@Override
	public GoalFlags clone() {
		return new GoalFlags(arena);
	}

	@Override
	public boolean allowsJoinInBattle() {
		return arena.getArenaConfig().getBoolean("join.inbattle");
	}
	
	@Override
	public void setDefaults(YamlConfiguration config) {
		if (arena.isFreeForAll()) {
			return;
		}
		
		if (arena.getArenaConfig().get("teams.free") != null) {
			arena.getArenaConfig().set("teams",null);
		}
		if (arena.getArenaConfig().get("teams") == null) {
			db.i("no teams defined, adding custom red and blue!");
			arena.getArenaConfig().getYamlConfiguration().addDefault("teams.red",
					ChatColor.RED.name());
			arena.getArenaConfig().getYamlConfiguration().addDefault("teams.blue",
					ChatColor.BLUE.name());
		}
		if (arena.getArenaConfig().getBoolean("game.woolFlagHead")
				&& (arena.getArenaConfig().get("flagColors") == null)) {
			db.i("no flagheads defined, adding white and black!");
			config.addDefault("flagColors.red", "WHITE");
			config.addDefault("flagColors.blue", "BLACK");
		}
	}

	@Override
	public String checkForMissingSpawns(Set<String> list) {
		for (ArenaTeam team : arena.getTeams()) {
			String sTeam = team.getName();
			if (!list.contains(team + "flag")) {
				boolean found = false;
				for (String s : list) {
					if (s.startsWith(sTeam) && s.endsWith("flag")) {
						found = true;
						break;
					}
				}
				if (!found)
					return team.getName() + "flag not set";
			}
		}
		return null;
	}
	
	@Override
	public PACheckResult checkEnd(PACheckResult res) {
		int priority = 3;
		
		if (res.getPriority() > 2) {
			return res;
		}
		
		int count = TeamManager.countActiveTeams(arena);

		if (count == 1) {
			res.setPriority(priority); // yep. only one team left. go!
		} else if (count == 0) {
			res.setError("No teams playing!");
		}

		return res;
	}

	/**
	 * hook into an interacting player
	 * @param res 
	 * 
	 * @param player
	 *            the interacting player
	 * @param clickedBlock
	 *            the block being clicked
	 * @return 
	 */
	public PACheckResult checkInteract(Arena arena, PACheckResult res, Player player, Block block) {
		int priority = 1;
		if (block == null || res.getPriority() > priority) {
			return res;
		}
		db.i("checking interact");

		if (!block.getType().equals(flagMaterial)) {
			db.i("block, but not flag");
			return res;
		}
		db.i("flag click!");

		Vector vLoc;
		String sTeam;
		Vector vFlag = null;
		ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());

		
		
		if (paTeamFlags.containsValue(player.getName())) {
			db.i("player " + player.getName() + " has got a flag");
			vLoc = block.getLocation().toVector();
			sTeam = ap.getArenaTeam().getName();
			db.i("block: " + vLoc.toString());
			if (SpawnManager.getSpawns(arena, sTeam + "flag").size() > 0) {
				vFlag = SpawnManager.getNearest(
						SpawnManager.getSpawns(arena, sTeam + "flag"),
						new PALocation(player.getLocation())).toLocation().toVector();
			} else {
				db.i(sTeam + "flag = null");
			}

			db.i("player is in the team " + sTeam);
			if ((vFlag != null && vLoc.distance(vFlag) < 2)) {

				db.i("player is at his flag");

				if (paTeamFlags.containsKey(sTeam)) {
					db.i("the flag of the own team is taken!");

					if (arena.getArenaConfig().getBoolean("game.mustbesafe")) {
						db.i("cancelling");

						arena.msg(player, Language.parse(MSG.GOAL_FLAGS_NOTSAFE));
						return res;
					}
				}

				String flagTeam = getHeldFlagTeam(arena, player.getName());

				db.i("the flag belongs to team " + flagTeam);

				try {

					arena.broadcast(Language.parse(MSG.GOAL_FLAGS_BROUGHTHOME, arena
							.getTeam(sTeam).colorizePlayer(player)
							+ ChatColor.YELLOW, arena.getTeam(flagTeam)
							.getColoredName() + ChatColor.YELLOW, String
							.valueOf(paTeamLives.get(flagTeam) - 1)));
					paTeamFlags.remove(flagTeam);
				} catch (Exception e) {
					Bukkit.getLogger().severe(
							"[PVP Arena] team unknown/no lives: " + flagTeam);
					e.printStackTrace();
				}

				takeFlag(arena.getTeam(flagTeam).getColor().name(), false,
						SpawnManager.getCoords(arena, flagTeam + "flag"));
				if (arena.getArenaConfig().getBoolean("game.woolFlagHead")) {
					player.getInventory().setHelmet(
							paHeadGears.get(player.getName()).clone());
					paHeadGears.remove(player.getName());
				}

				reduceLivesCheckEndAndCommit(arena, flagTeam); // TODO move to "commit" ?
			}
		} else {
			ArenaTeam pTeam = ap.getArenaTeam();
			if (pTeam == null) {
				return res;
			}
			for (ArenaTeam team : arena.getTeams()) {
				String aTeam = team.getName();

				if (aTeam.equals(pTeam.getName()))
					continue;
				if (team.getTeamMembers().size() < 1)
					continue; // dont check for inactive teams
				if (paTeamFlags != null && paTeamFlags.containsKey(aTeam)) {
					continue; // already taken
				}
				db.i("checking for flag of team " + aTeam);
				vLoc = block.getLocation().toVector();
				db.i("block: " + vLoc.toString());
				if (SpawnManager.getSpawns(arena, aTeam + "flag").size() > 0) {
					vFlag = SpawnManager.getNearest(
							SpawnManager.getSpawns(arena, aTeam + "flag"),
							new PALocation(player.getLocation())).toLocation().toVector();
				}
				if ((vFlag != null) && (vLoc.distance(vFlag) < 2)) {
					db.i("flag found!");
					db.i("vFlag: " + vFlag.toString());
					arena.broadcast(Language.parse(MSG.GOAL_FLAGS_GRABBED,
							pTeam.colorizePlayer(player) + ChatColor.YELLOW,
							team.getColoredName() + ChatColor.YELLOW));

					if (arena.getArenaConfig().getBoolean("game.woolFlagHead")) {
						try {
							paHeadGears.put(player.getName(), player
									.getInventory().getHelmet().clone());
						} catch (Exception e) {

						}
						ItemStack is = block.getState().getData().toItemStack()
								.clone();
						is.setDurability(getFlagOverrideTeamShort(arena, aTeam));
						player.getInventory().setHelmet(is);

					}

					takeFlag(team.getColor().name(), true, new PALocation(block.getLocation()));
					paTeamFlags.put(aTeam, player.getName()); // TODO move to "commit" ?
					return res; 
				}
			}
		}
		
		return res;
	}
	
	/**
	 * notify the goal of a player death, return higher priority if goal should handle the death as WIN/LOSE
	 * @param arena the arena
	 * @param player the dying player
	 * @return a PACheckResult instance to hand forth for parsing
	 */
	public PACheckResult checkPlayerDeath(Arena arena, PACheckResult res, Player player) {
		
		if (paTeamFlags == null) {
			return res;
		}

		ArenaTeam flagTeam = arena.getTeam(
				getHeldFlagTeam(arena, player.getName()));
		if (flagTeam != null) {
			ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
			arena.broadcast(Language.parse(MSG.GOAL_FLAGS_DROPPED,
					ap.getArenaTeam().colorizePlayer(player), flagTeam.getName() + ChatColor.YELLOW));
			paTeamFlags.remove(flagTeam.getName());
			if (paHeadGears != null
					&& paHeadGears.get(player.getName()) != null) {
				player.getInventory().setHelmet(
						paHeadGears.get(player.getName()).clone());
				paHeadGears.remove(player.getName());
			}

			takeFlag(flagTeam.getColor().name(), false,
					SpawnManager.getCoords(arena, flagTeam.getName() + "flag"));
			
		}
		
		
		// don't take priority in handling the death, just, react to it! 
		
		return res;
	}
	
	public PACheckResult checkSetFlag(Arena arena, PACheckResult res, Player player, Block block) {
		
		int priority = 1;
		
		if (res.getPriority() > priority || PAA_Region.activeSelections.containsKey(player.getName())) {
			return res;
		}
		res.setPriority(priority); // success :)
		
		return res;
	}

	private void commit(Arena arena, String sTeam, boolean win) {
		db.i("[CTF] committing end: " + sTeam);
		db.i("win: " + String.valueOf(win));

		String winteam = sTeam;

		for (ArenaTeam team : arena.getTeams()) {
			if (team.getName().equals(sTeam) == win) {
				continue;
			}
			for (ArenaPlayer ap : team.getTeamMembers()) {

				ap.addStatistic(arena.getName(), type.LOSSES, 1);
				arena.tpPlayerToCoordName(ap.get(), "spectator");
				ap.setTelePass(false);
			}
		}
		for (ArenaTeam team : arena.getTeams()) {
			for (ArenaPlayer ap : team.getTeamMembers()) {
				if (!ap.getStatus().equals(Status.FIGHT)) {
					continue;
				}
				winteam = team.getName();
				break;
			}
		}

		if (arena.getTeam(winteam) != null) {
			PVPArena.instance.getAmm().announceWinner(arena,
					Language.parse(MSG.TEAM_HAS_WON, "Team " + winteam));
			arena.broadcast(Language.parse(MSG.TEAM_HAS_WON,
					arena.getTeam(winteam).getColor() + "Team "
							+ winteam));
		}

		paTeamLives.clear();
		EndRunnable er = new EndRunnable(arena, arena.getArenaConfig().getInt("goal.endtimer"));
		Bukkit.getScheduler().scheduleSyncRepeatingTask(PVPArena.instance,
				er, 20L, 20L);
	}

	@Override
	public void commitEnd() {
		db.i("[TEAMS]");

		ArenaTeam aTeam = null;
		
		for (ArenaTeam team : arena.getTeams()) {
			for (ArenaPlayer ap : team.getTeamMembers()) {
				if (ap.getStatus().equals(Status.FIGHT)) {
					aTeam = team;
					break;
				}
			}
		}

		if (aTeam != null) {
			PVPArena.instance.getAmm().announceWinner(arena,
					Language.parse(MSG.TEAM_HAS_WON, "Team " + aTeam.getName()));

			arena.broadcast(Language.parse(MSG.TEAM_HAS_WON, aTeam.getColor()
					+ "Team " + aTeam.getName()));
		}

		if (PVPArena.instance.getAmm().commitEnd(arena, aTeam)) {
			return;
		}
		new EndRunnable(arena, arena.getArenaConfig().getInt("goal.endtimer"));
	}
	
	public boolean commitSetFlag(Arena arena, Player player, Block block) {
		
		if (block == null || !block.getType().equals(flagMaterial)) {
			return false;
		}
		
		if (!PVPArena.hasAdminPerms(player)
				&& !(PVPArena.hasCreatePerms(player, arena))) {
			return false;
		}

		db.i("trying to set a flag");

		String sName = PAA_Region.activeSelections.get(player.getName()).getName().replace(arena.getName() + ":", "");

		// command : /pa redflag1
		// location: red1flag:

		SpawnManager.setCoords(arena, block.getLocation(), sName + "flag");

		arena.msg(player, Language.parse(MSG.GOAL_FLAGS_SET, sName));

		PAA_Region.activeSelections.remove(player.getName());
		
		return false;
	}

	private short getFlagOverrideTeamShort(Arena arena, String team) {
		if (arena.getArenaConfig().get("flagColors." + team) == null) {

			return StringParser.getColorDataFromENUM(arena.getTeam(team)
					.getColor().name());
		}
		return StringParser.getColorDataFromENUM(arena.getArenaConfig()
				.getString("flagColors." + team));
	}

	/**
	 * get the team name of the flag a player holds
	 * 
	 * @param player
	 *            the player to check
	 * @return a team name
	 */
	private String getHeldFlagTeam(Arena arena, String player) {
		if (paTeamFlags.size() < 1) {
			return null;
		}
		
		db.i("getting held FLAG of player " + player);
		for (String sTeam : paTeamFlags.keySet()) {
			db.i("team " + sTeam + " is in " + paTeamFlags.get(sTeam)
					+ "s hands");
			if (player.equals(paTeamFlags.get(sTeam))) {
				return sTeam;
			}
		}
		return null;
	}
	
	@Override
	public String guessSpawn(String place) {
		// no exact match: assume we have multiple spawnpoints
		HashMap<Integer, String> locs = new HashMap<Integer, String>();
		int i = 0;

		db.i("searching for team spawns: " + place);
		
		HashMap<String, Object> coords = (HashMap<String, Object>) arena.getArenaConfig()
				.getYamlConfiguration().getConfigurationSection("spawns")
				.getValues(false);
		for (String name : coords.keySet()) {
			if (name.startsWith(place)) {
				locs.put(i++, name);
				db.i("found match: " + name);
			}
			if (name.endsWith("flag")) {
				for (ArenaTeam team : arena.getTeams()) {
					String sTeam = team.getName();
					if (name.startsWith(sTeam) && place.startsWith(sTeam)) {
						locs.put(i++, name);
						db.i("found match: " + name);
					}
				}
			}
		}

		if (locs.size() < 1) {
			return null;
		}
		Random r = new Random();

		place = locs.get(r.nextInt(locs.size()));

		return place;
	}

	@Override
	public boolean hasSpawn(String string) {
		for (String teamName : arena.getTeamNames()) {
			if (string.toLowerCase().equals(teamName.toLowerCase()+"flag")) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void teleportAllToSpawn() {
		db.i("initiating arena");
		paTeamLives.clear();
		for (ArenaTeam team : arena.getTeams()) {
			if (team.getTeamMembers().size() > 0) {
				db.i("adding team " + team.getName());
				// team is active
				paTeamLives.put(team.getName(),
						arena.getArenaConfig().getInt("game.lives", 3));
			}
			takeFlag(team.getColor().name(), false,
					SpawnManager.getCoords(arena, team.getName() + "flag"));
		}
	}
	
	public boolean reduceLivesCheckEndAndCommit(Arena arena, String team) {

		db.i("reducing lives of team " + team);
		if (paTeamLives.get(team) != null) {
			int i = paTeamLives.get(team) - 1;
			if (i > 0) {
				paTeamLives.put(team, i);
			} else {
				paTeamLives.remove(team);
				commit(arena, team, false);
				return true;
			}
		}
		return false;
	}

	@Override
	public void reset(boolean force) {
		if (paTeamFlags != null) {
			paTeamFlags.clear();
		}
		if (paHeadGears != null) {
			paHeadGears.clear();
		}
	}

	/**
	 * take/reset an arena flag
	 * 
	 * @param flagColor
	 *            the teamcolor to reset
	 * @param take
	 *            true if take, else reset
	 * @param pumpkin
	 *            true if pumpkin, false otherwise
	 * @param lBlock
	 *            the location to take/reset
	 */
	public static void takeFlag(String flagColor, boolean take, PALocation lBlock) {
		if (take) {
			lBlock.toLocation().getBlock().setData(
					StringParser.getColorDataFromENUM("WHITE"));
		} else {
			lBlock.toLocation().getBlock().setData(
					StringParser.getColorDataFromENUM(flagColor));
		}
	}

	@Override
	public HashMap<String, Double> timedEnd(
			HashMap<String, Double> scores) {
		
		for (String s : paTeamLives.keySet()) {
			double score = scores.containsKey(s) ? scores.get(s) : 0;
			score += paTeamLives.get(s); // every team life is worth 1 point
			
			scores.put(s, score);
		}
		
		return scores;
	}
	
	@Override
	public void unload(Player player) {
		if (paTeamFlags == null) {
			return;
		}
		
		ArenaTeam flagTeam = arena.getTeam(
				getHeldFlagTeam(arena, player.getName()));
		if (flagTeam != null) {
			ArenaPlayer ap = ArenaPlayer.parsePlayer(player.getName());
			arena.broadcast(Language.parse(MSG.GOAL_FLAGS_DROPPED,
					ap.getArenaTeam().colorizePlayer(player), flagTeam.getName() + ChatColor.YELLOW));
			paTeamFlags.remove(flagTeam.getName());
			if (paHeadGears != null
					&& paHeadGears.get(player.getName()) != null) {
				player.getInventory().setHelmet(
						paHeadGears.get(player.getName()).clone());
				paHeadGears.remove(player.getName());
			}

			takeFlag(flagTeam.getColor().name(), false,
					SpawnManager.getCoords(arena, flagTeam.getName() + "flag"));

			paTeamFlags.clear();
		}
	}
}