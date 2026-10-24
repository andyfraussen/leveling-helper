package com.levelinghelper.guide;

import java.util.Locale;
import net.runelite.api.Experience;
import net.runelite.api.coords.WorldPoint;

public final class TrainingMath
{
	private TrainingMath()
	{
	}

	public static int xpBetween(int currentXp, int targetLevel)
	{
		int clamped = Math.max(1, Math.min(99, targetLevel));
		return Math.max(0, Experience.getXpForLevel(clamped) - Math.max(0, currentXp));
	}

	public static int actions(int xpRemaining, double xpEach)
	{
		if (xpRemaining <= 0)
		{
			return 0;
		}
		if (xpEach <= 0)
		{
			return -1;
		}
		return (int) Math.ceil(xpRemaining / xpEach);
	}

	public static long millis(int xpRemaining, double xpPerHour)
	{
		if (xpRemaining <= 0)
		{
			return 0L;
		}
		if (xpPerHour <= 0)
		{
			return -1L;
		}
		return (long) (xpRemaining / xpPerHour * 3_600_000.0);
	}

	public static String duration(long millis)
	{
		if (millis < 0)
		{
			return "n/a";
		}
		if (millis == 0)
		{
			return "done";
		}
		long minutes = millis / 60000L;
		if (minutes < 1)
		{
			return "under a minute";
		}
		if (minutes < 60)
		{
			return minutes + " min";
		}
		long hours = minutes / 60;
		long rem = minutes % 60;
		if (hours < 48)
		{
			return hours + "h " + rem + "m";
		}
		long days = hours / 24;
		return days + "d " + (hours % 24) + "h";
	}

	public static String xp(int value)
	{
		int abs = Math.abs(value);
		if (abs >= 1_000_000)
		{
			return String.format(Locale.US, "%.2fm", value / 1_000_000.0);
		}
		if (abs >= 10_000)
		{
			return String.format(Locale.US, "%.1fk", value / 1000.0);
		}
		return String.format(Locale.US, "%,d", value);
	}

	public static String rate(double perHour)
	{
		if (perHour <= 0)
		{
			return "n/a";
		}
		return xp((int) Math.round(perHour)) + "/hr";
	}

	public static int combatLevel(int attack, int strength, int defence, int hitpoints, int prayer, int ranged, int magic)
	{
		double base = 0.25 * (defence + hitpoints + (prayer / 2));
		double melee = 0.325 * (attack + strength);
		double range = 0.325 * ((ranged * 3) / 2);
		double mage = 0.325 * ((magic * 3) / 2);
		return (int) Math.floor(base + Math.max(melee, Math.max(range, mage)));
	}

	public static String direction(WorldPoint from, WorldPoint to)
	{
		if (from == null || to == null)
		{
			return "";
		}
		if (from.getPlane() != to.getPlane())
		{
			return "on another floor";
		}
		int dx = to.getX() - from.getX();
		int dy = to.getY() - from.getY();
		int dist = Math.max(Math.abs(dx), Math.abs(dy));
		if (dist == 0)
		{
			return "at the spot";
		}
		String northSouth = dy > 2 ? "north" : dy < -2 ? "south" : "";
		String eastWest = dx > 2 ? "east" : dx < -2 ? "west" : "";
		String compass = (northSouth + eastWest).isEmpty() ? "nearby" : northSouth + eastWest;
		return dist + " tiles " + compass;
	}
}
