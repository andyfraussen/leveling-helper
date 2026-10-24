package com.levelinghelper;

import com.levelinghelper.guide.XpSession;
import java.util.concurrent.atomic.AtomicLong;
import net.runelite.api.*;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SessionLifecycleTest
{
	@Test
	public void rateUsesElapsedMonotonicTimeAndStopClearsTheCounters()
	{
		AtomicLong clock = new AtomicLong();
		XpSession xp = new XpSession(clock::get);
		xp.start(Skill.MINING, 1000);
		xp.onXp(Skill.MINING, 1100);
		assertEquals(0, xp.xpPerHour(), 0);
		clock.set(60000);
		assertEquals(6000, xp.xpPerHour(), 0.01);
		xp.onXp(Skill.FISHING, 500000);
		assertEquals(100, xp.gained());
		xp.stop();
		assertEquals(0, xp.gained());
		assertEquals(0, xp.xpPerHour(), 0);
	}

	@Test
	public void xpResetRebasesInsteadOfIgnoringFutureGains()
	{
		XpSession xp = new XpSession();
		xp.start(Skill.MINING, 1000);
		xp.onXp(Skill.MINING, 100);
		xp.onXp(Skill.MINING, 125);
		assertEquals(25, xp.gained());
	}

	@Test
	public void accountChangesAndReconnectsStartFreshBaselines()
	{
		Client client = mock(Client.class);
		Player player = mock(Player.class);
		when(client.getGameState()).thenReturn(GameState.LOGGED_IN);
		when(client.getLocalPlayer()).thenReturn(player);
		when(player.getName()).thenReturn("First account");
		when(client.getSkillExperience(Skill.MINING)).thenReturn(1000);
		SessionLifecycle session = new SessionLifecycle(client);
		session.sync(Skill.MINING);
		session.onXp(Skill.MINING, 1025);
		assertEquals(25, session.gained());
		when(player.getName()).thenReturn("Second account");
		when(client.getSkillExperience(Skill.MINING)).thenReturn(100000);
		session.onXp(Skill.MINING, 100000);
		assertEquals(0, session.gained());
		session.onXp(Skill.MINING, 100025);
		assertEquals(25, session.gained());
		session.stop();
		when(client.getGameState()).thenReturn(GameState.LOGIN_SCREEN);
		session.onXp(Skill.MINING, 200000);
		assertEquals(0, session.gained());
		when(client.getGameState()).thenReturn(GameState.LOGGED_IN);
		when(client.getSkillExperience(Skill.MINING)).thenReturn(100);
		session.sync(Skill.MINING);
		session.onXp(Skill.MINING, 125);
		assertEquals(25, session.gained());
	}
}
