package net.slipcor.pvparena.classes;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class PABlockLocation {
	private final String world;
	private int x;
	private int y;
	private int z;
	
	public PABlockLocation(String world, int x, int y, int z) {
		this.world= world;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public PABlockLocation(Location l) {
		this.world = l.getWorld().getName();
		this.x = l.getBlockX();
		this.y = l.getBlockY();
		this.z = l.getBlockZ();
	}

	public double getDistance(PABlockLocation o) {
		if (o == null)
			throw new IllegalArgumentException(
					"Cannot measure distance to a null location");
		if (!o.world.equals(world)) {
			throw new IllegalArgumentException("Cannot measure distance between " + world + " and " + o.world);
		}
		
		return Math.sqrt(Math.pow(this.x - o.x, 2.0D) + Math.pow(this.y - o.y, 2.0D) + Math.pow(this.z - o.z, 2.0D));
	}

	public String getWorldName() {
		return world;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}

	public void setX(int i) {
		x = i;
	}

	public void setY(int i) {
		y = i;
	}

	public void setZ(int i) {
		z = i;
	}

	public boolean isInAABB(PABlockLocation l1, PABlockLocation l2) {
		if (this.getX() < l1.getX() || this.getX() > l2.getX()) {
			return false;
		}
		if (this.getY() < l1.getY() || this.getY() > l2.getY()) {
			return false;
		}
		if (this.getZ() < l1.getZ() || this.getZ() > l2.getZ()) {
			return false;
		}
		return true;
	}

	public Location toLocation() {
		return new Location(Bukkit.getWorld(world), x, y, z);
	}

	public PABlockLocation getMidpoint(PABlockLocation l) {
		return new PABlockLocation(world, (x + l.x)/2, (y + l.y)/2, (z + l.z)/2);
	}

	public PABlockLocation pointTo(PABlockLocation dest, Double length) {
		Vector source = new Vector(x, y, z);
		Vector destination = new Vector(dest.x, dest.y, dest.z);
		
		Vector goal = source.subtract(destination);
		
		goal = goal.normalize().multiply(length);
		
		return new PABlockLocation(world, x+x+goal.getBlockX(), y+goal.getBlockY(), z+goal.getBlockZ());
	}
}