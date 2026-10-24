package com.levelinghelper;

import com.levelinghelper.model.*;
import com.levelinghelper.data.TrainingCatalog;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.EnumSet;
import java.util.Queue;
import javax.swing.SwingUtilities;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.worldmap.WorldMapPointManager;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PluginLifecycleTest
{
	private static final class QueuedClientThread extends ClientThread
	{
		private final Queue<Runnable> tasks = new ArrayDeque<>();
		@Override
		public void invoke(Runnable action) { tasks.add(action); }
		void drain() { while (!tasks.isEmpty()) tasks.remove().run(); }
	}

	private final QueuedClientThread thread = new QueuedClientThread();
	private Client client;
	private LevelingHelperPlugin plugin;
	private LevelingHelperConfig config;
	private WorldMapPointManager map;
	private SessionLifecycle session;

	@Before
	public void setUp() throws Exception
	{
		client = mock(Client.class);
		Player player = mock(Player.class);
		when(player.getName()).thenReturn("Training account");
		when(player.getWorldLocation()).thenReturn(new net.runelite.api.coords.WorldPoint(3093, 3243, 0));
		when(client.getLocalPlayer()).thenReturn(player);
		when(client.getGameState()).thenReturn(GameState.LOGGED_IN);
		when(client.getWorldType()).thenReturn(EnumSet.of(WorldType.MEMBERS));
		when(client.getNpcs()).thenReturn(java.util.Collections.emptyList());
		when(client.getRealSkillLevel(any(Skill.class))).thenReturn(50);
		when(client.getSkillExperience(any(Skill.class))).thenReturn(101333);
		config = mock(LevelingHelperConfig.class);
		when(config.arrivalDistance()).thenReturn(10);
		when(config.showWorldMap()).thenReturn(true);
		when(config.accountMode()).thenReturn(AccountMode.F2P);
		when(config.trainingStyle()).thenReturn(TrainingStyle.BALANCED);
		when(config.skillName()).thenReturn("Woodcutting");
		when(config.goalLevel()).thenReturn(99);
		map = mock(WorldMapPointManager.class);
		plugin = new LevelingHelperPlugin();
		session = new SessionLifecycle(client);
		set("client", client);
		set("clientThread", thread);
		set("config", config);
		set("configManager", mock(ConfigManager.class));
		set("itemManager", mock(ItemManager.class));
		set("worldMapPointManager", map);
		set("overlayManager", mock(OverlayManager.class));
		set("clientToolbar", mock(ClientToolbar.class));
		set("targets", new TargetHighlighter(client));
		set("hintArrows", new HintArrowController(client));
		set("session", session);
		set("enabled", true);
	}

	private void set(String name, Object value) throws Exception
	{
		Field field = LevelingHelperPlugin.class.getDeclaredField(name);
		field.setAccessible(true);
		field.set(plugin, value);
	}

	private void start()
	{
		plugin.startGuide();
		thread.drain();
		assertTrue(plugin.controller().running());
	}

	@Test
	public void unsupportedSkillClearsTheOldGuideAndMapImmediately()
	{
		start();
		plugin.onGameTick(new GameTick());
		verify(map).add(any());
		plugin.setSkill(Skill.SLAYER);
		thread.drain();
		assertFalse(plugin.controller().running());
		assertEquals(0, session.gained());
		verify(map).remove(any());
	}

	@Test
	public void returningFromAnUnsupportedSkillRequiresAnExplicitRestart()
	{
		start();
		plugin.setSkill(Skill.SLAYER);
		thread.drain();
		plugin.setSkill(Skill.WOODCUTTING);
		thread.drain();
		plugin.onGameTick(new GameTick());
		assertFalse(plugin.controller().running());
		start();
	}

	@Test
	public void startingAnUnsupportedSkillDoesNotArmAFutureSelection()
	{
		plugin.setSkill(Skill.SLAYER);
		plugin.startGuide();
		thread.drain();
		assertFalse(plugin.controller().running());
		plugin.setSkill(Skill.WOODCUTTING);
		thread.drain();
		plugin.onGameTick(new GameTick());
		assertFalse(plugin.controller().running());
	}

	@Test
	public void switchingAMembersSkillToFreeModeClearsTheRoute()
	{
		plugin.setAccountMode(AccountMode.P2P);
		plugin.setSkill(Skill.AGILITY);
		thread.drain();
		start();
		plugin.setAccountMode(AccountMode.F2P);
		thread.drain();
		assertFalse(plugin.controller().running());
		plugin.setAccountMode(AccountMode.P2P);
		thread.drain();
		plugin.onGameTick(new GameTick());
		assertFalse(plugin.controller().running());
	}

	@Test
	public void stopRemainsStoppedWhenChangingSettings()
	{
		start();
		plugin.stopGuide();
		plugin.setSkill(Skill.FISHING);
		plugin.setTrainingStyle(TrainingStyle.FAST);
		thread.drain();
		plugin.onGameTick(new GameTick());
		assertFalse(plugin.controller().running());
	}

	@Test
	public void reachedGoalStopsAndDoesNotAutoRestart()
	{
		start();
		when(client.getRealSkillLevel(Skill.WOODCUTTING)).thenReturn(99);
		plugin.onStatChanged(new StatChanged(Skill.WOODCUTTING, 13034431, 99, 99));
		assertFalse(plugin.controller().running());
		plugin.onGameTick(new GameTick());
		assertFalse(plugin.controller().running());
	}

	@Test
	public void shutdownInvalidatesAlreadyQueuedCommandsAndDefersClientCleanup() throws Exception
	{
		start();
		plugin.startGuide();
		clearInvocations(client);
		SwingUtilities.invokeAndWait(() -> plugin.shutDown());
		assertFalse(plugin.active());
		verifyNoInteractions(client);
		thread.drain();
		assertFalse(plugin.controller().running());
	}

	@Test
	public void uiCommandsDoNotReadOrMutateTheClientOnTheSwingThread() throws Exception
	{
		clearInvocations(client);
		SwingUtilities.invokeAndWait(() -> plugin.setAccountMode(AccountMode.P2P));
		verifyNoInteractions(client);
		thread.drain();
		assertFalse(plugin.controller().running());
	}

	@Test
	public void logoutStopsTheGuideAndRequiresAnExplicitRestart()
	{
		start();
		session.onXp(Skill.WOODCUTTING, 101400);
		assertTrue(session.gained() > 0);
		GameStateChanged event = new GameStateChanged();
		event.setGameState(GameState.LOGIN_SCREEN);
		when(client.getGameState()).thenReturn(GameState.LOGIN_SCREEN);
		plugin.onGameStateChanged(event);
		assertFalse(plugin.controller().running());
		assertEquals(0, session.gained());
		when(client.getGameState()).thenReturn(GameState.LOGGED_IN);
		plugin.onGameTick(new GameTick());
		assertFalse(plugin.controller().running());
	}

	@Test
	public void unchangedTicksReuseTheWorldMapMarker()
	{
		start();
		plugin.onGameTick(new GameTick());
		plugin.onGameTick(new GameTick());
		verify(map, times(1)).add(any());
		verify(map, never()).remove(any());
	}

	@Test
	public void enablingThePluginDoesNotStartGuidingBeforeTheUserAsks() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> plugin.startUp());
		thread.drain();
		plugin.onGameTick(new GameTick());
		assertFalse(plugin.controller().running());
		plugin.startGuide();
		thread.drain();
		assertTrue(plugin.controller().running());
		SwingUtilities.invokeAndWait(() -> plugin.shutDown());
		thread.drain();
		SwingUtilities.invokeAndWait(() -> { });
	}

	@Test
	public void disablingDirectionArrowsAlsoDisablesTheNativeArrow()
	{
		when(config.showHintArrow()).thenReturn(true);
		when(config.showArrow()).thenReturn(false);
		start();
		plugin.onGameTick(new GameTick());
		verify(client, never()).setHintArrow(any(net.runelite.api.coords.WorldPoint.class));
		verify(client, never()).setHintArrow(any(NPC.class));
	}

	@Test
	public void dustDevilHighlightsRequireProtectionToRemainEquipped()
	{
		TrainingMethod burst = TrainingCatalog.all().stream().filter(method -> method.name().equals("Ice Burst")).findFirst().get();
		plugin.controller().start(burst);
		plugin.controller().advance();
		plugin.controller().advance();
		NPC dustDevil = mock(NPC.class);
		when(dustDevil.getId()).thenReturn(NpcID.DUST_DEVIL_7249);
		net.runelite.api.coords.WorldPoint location = client.getLocalPlayer().getWorldLocation();
		when(dustDevil.getWorldLocation()).thenReturn(location);
		when(client.getNpcs()).thenReturn(java.util.Collections.singletonList(dustDevil));
		plugin.onGameTick(new GameTick());
		assertTrue(plugin.highlightedNpcs().isEmpty());
		ItemContainer equipment = mock(ItemContainer.class);
		when(equipment.count(ItemID.FACEMASK)).thenReturn(1);
		when(client.getItemContainer(net.runelite.api.gameval.InventoryID.WORN)).thenReturn(equipment);
		plugin.onGameTick(new GameTick());
		assertEquals(1, plugin.highlightedNpcs().size());
		when(client.getItemContainer(net.runelite.api.gameval.InventoryID.WORN)).thenReturn(null);
		plugin.onGameTick(new GameTick());
		assertTrue(plugin.highlightedNpcs().isEmpty());
	}
}
