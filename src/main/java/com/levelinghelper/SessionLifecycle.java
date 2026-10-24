package com.levelinghelper;

import com.levelinghelper.guide.XpSession;
import java.util.Objects;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;

/** Rates cover a single logged-in guiding session; reconnects start a fresh baseline. */
final class SessionLifecycle
{
	private final Client client;
	private final XpSession xp;
	private String playerName;

	@Inject
	SessionLifecycle(Client client)
	{
		this(client, new XpSession());
	}

	SessionLifecycle(Client client, XpSession xp)
	{
		this.client = client;
		this.xp = xp;
	}

	void sync(Skill skill)
	{
		if (client.getGameState() != GameState.LOGGED_IN || client.getLocalPlayer() == null)
		{
			stop();
			return;
		}
		String currentName = client.getLocalPlayer().getName();
		if (currentName == null)
		{
			stop();
			return;
		}
		if (!xp.active() || xp.skill() != skill || !Objects.equals(playerName, currentName))
		{
			playerName = currentName;
			xp.start(skill, client.getSkillExperience(skill));
		}
	}

	void onXp(Skill skill, int experience)
	{
		if (client.getGameState() == GameState.LOGGED_IN && xp.active())
		{
			sync(xp.skill());
			xp.onXp(skill, experience);
		}
	}

	void stop()
	{
		xp.stop();
		playerName = null;
	}

	int gained()
	{
		return xp.gained();
	}

	double xpPerHour()
	{
		return xp.xpPerHour();
	}
}
