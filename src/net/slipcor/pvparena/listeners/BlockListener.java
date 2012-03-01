package net.slipcor.pvparena.listeners;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.definitions.Arena;
import net.slipcor.pvparena.definitions.ArenaBoard;
import net.slipcor.pvparena.managers.Arenas;
import net.slipcor.pvparena.managers.Blocks;
import net.slipcor.pvparena.managers.Spawns;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;

/**
 * block listener class
 * 
 * -
 * 
 * PVP Arena Block Listener
 * 
 * @author slipcor
 * 
 * @version v0.6.15
 * 
 */

public class BlockListener implements Listener {
	private Debug db = new Debug(18);

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockBreak(BlockBreakEvent event) {
		Arena arena = Arenas.getArenaByRegionLocation(event.getBlock()
				.getLocation());
		if (arena == null)
			return; // no arena => out

		db.i("block break inside the arena");
		if (arena.edit || (!(arena.cfg.getBoolean("protection.enabled", true)))
				|| (!(arena.cfg.getBoolean("protection.blockdamage", true)))) {
			if (arena.fightInProgress) {
				Blocks.saveBlock(event.getBlock());
			}
			return; // we don't need protection => OUT!
		}

		event.setCancelled(true);
		return;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockIgnite(BlockIgniteEvent event) {
		Arena arena = Arenas.getArenaByRegionLocation(event.getBlock()
				.getLocation());
		if (arena == null)
			return; // no arena => out

		db.i("block ignite inside the arena");

		if (arena.pm.getPlayers().size() < 1)
			return; // no players, no game, no protection!

		BlockIgniteEvent.IgniteCause cause = event.getCause();
		if ((arena.cfg.getBoolean("protection.enabled", true))
				&& (((arena.cfg.getBoolean("protection.lavafirespread", true)) && (cause == BlockIgniteEvent.IgniteCause.LAVA))
						|| ((arena.cfg
								.getBoolean("protection.firespread", true)) && (cause == BlockIgniteEvent.IgniteCause.SPREAD)) || ((arena.cfg
						.getBoolean("protection.lighter", true)) && (cause == BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL)))) {
			// if an event happened that we would like to block
			event.setCancelled(true); // ->cancel!
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockBurn(BlockBurnEvent event) {
		Arena arena = Arenas.getArenaByRegionLocation(event.getBlock()
				.getLocation());
		if (arena == null)
			return; // no arena => out

		db.i("block burn inside the arena");
		if ((!(arena.cfg.getBoolean("protection.enabled", true)))
				|| (!(arena.cfg.getBoolean("protection.fire.firespread", true))))
			// if not an event happend that we would like to block => OUT
			return;

		event.setCancelled(true); // else->cancel!
		return;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockPlace(BlockPlaceEvent event) {
		Arena arena = Arenas.getArenaByRegionLocation(event.getBlock()
				.getLocation());
		if (arena == null)
			return; // no arena => out

		db.i("block place inside the arena");
		if (arena.edit || (!(arena.cfg.getBoolean("protection.enabled", true)))
				|| (!(arena.cfg.getBoolean("protection.blockplace", true)))) {
			if (arena.fightInProgress) {
				Blocks.saveBlock(event.getBlock(), event
						.getBlockReplacedState().getType());
			}
			// if not an event happend that we would like to block => OUT
			return;
		}

		if (!arena.cfg.getBoolean("protection.tnt", true)
				&& event.getBlock().getTypeId() == 46) {
			if (arena.fightInProgress) {
				Blocks.saveBlock(event.getBlock(), event
						.getBlockReplacedState().getType());
			}
			return; // we do not block TNT, so just return if it is TNT
		}
		event.setCancelled(true);
		return;
	}

	@EventHandler()
	public void onSignChange(SignChangeEvent event) {
		String headline = event.getLine(0);
		if ((headline == null) || (headline.equals(""))) {
			return;
		}

		if (!headline.startsWith("[PAA]")) {
			return;
		}

		headline = headline.replace("[PAA]", "");

		Arena a = Arenas.getArenaByName(headline);
		if (a == null) {
			return;
		}

		// trying to create an arena leaderboard

		if (Arenas.boards.containsKey(event.getBlock().getLocation())) {
			Arenas.tellPlayer(event.getPlayer(), Language.parse("boardexists"));
			return;
		}

		if (!PVPArena.hasAdminPerms(event.getPlayer())
				&& !PVPArena.hasCreatePerms(event.getPlayer(), a)) {
			Arenas.tellPlayer(
					event.getPlayer(),
					Language.parse("nopermto",
							Language.parse("createleaderboard")));
			return;
		}

		event.setLine(0, headline);
		Arenas.boards.put(event.getBlock().getLocation(), new ArenaBoard(event
				.getBlock().getLocation(), a));
		Spawns.setCoords(a, event.getBlock().getLocation(), "leaderboard");
	}
}