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
		when(client.getIntStack()).thenReturn(new int[]{1});
		doReturn(SupplyTrackerTest.container()).when(client).getItemContainer(net.runelite.api.gameval.InventoryID.INV);
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
		ItemManager items = mock(ItemManager.class);
		ItemComposition bones = mock(ItemComposition.class);
		when(bones.isGeTradeable()).thenReturn(true);
		when(items.getItemComposition(ItemID.BIG_BONES)).thenReturn(bones);
		set("itemManager", items);
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
		plugin.onGameTick(new GameTick());
		thread.drain();
		assertTrue(plugin.controller().running());
	}

	@Test
	public void returningFromAnUnavailableRouteAutomaticallyResumesGuidance()
	{
		start();
		plugin.setSkill(Skill.HERBLORE);
		thread.drain();
		assertFalse(plugin.controller().running());
		assertEquals(0, session.gained());
		verify(map).remove(any());
		plugin.setSkill(Skill.WOODCUTTING);
		thread.drain();
		assertTrue(plugin.controller().running());
	}

	@Test
	public void freeModeReplacesASelectedMembersSkillWithAVisibleSkill()
	{
		plugin.setAccountMode(AccountMode.P2P);
		plugin.setSkill(Skill.AGILITY);
		thread.drain();
		assertEquals(Skill.AGILITY, plugin.controller().method().skill());
		plugin.setAccountMode(AccountMode.F2P);
		thread.drain();
		assertTrue(plugin.controller().running());
		assertEquals(Skill.WOODCUTTING, plugin.controller().method().skill());
		plugin.setSkill(Skill.AGILITY);
		thread.drain();
		assertEquals(Skill.WOODCUTTING, plugin.controller().method().skill());
	}

	@Test
	public void changingSkillAndStyleAutomaticallyGuidesTheNewSelection()
	{
		start();
		plugin.setSkill(Skill.FISHING);
		plugin.setTrainingStyle(TrainingStyle.FAST);
		thread.drain();
		assertTrue(plugin.controller().running());
		assertEquals(Skill.FISHING, plugin.controller().method().skill());
	}

	@Test
	public void reachedGoalStopsUntilTheGoalIsRaised()
	{
		start();
		when(client.getRealSkillLevel(Skill.WOODCUTTING)).thenReturn(99);
		plugin.onStatChanged(new StatChanged(Skill.WOODCUTTING, 13034431, 99, 99));
		assertFalse(plugin.controller().running());
		plugin.onGameTick(new GameTick());
		assertFalse(plugin.controller().running());
		when(client.getRealSkillLevel(Skill.WOODCUTTING)).thenReturn(50);
		plugin.setGoal(50);
		thread.drain();
		assertFalse(plugin.controller().running());
		plugin.setGoal(60);
		thread.drain();
		assertTrue(plugin.controller().running());
	}

	@Test
	public void shutdownInvalidatesAlreadyQueuedCommandsAndDefersClientCleanup() throws Exception
	{
		start();
		plugin.setSkill(Skill.FISHING);
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
		assertTrue(plugin.controller().running());
	}

	@Test
	public void logoutClearsTheSessionAndLoginAutomaticallyResumes()
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
		assertTrue(plugin.controller().running());
		assertEquals(0, session.gained());
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
	public void enablingThePluginAutomaticallyGuidesTheSavedSkill() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> plugin.startUp());
		thread.drain();
		assertTrue(plugin.controller().running());
		assertEquals(Skill.WOODCUTTING, plugin.controller().method().skill());
		SwingUtilities.invokeAndWait(() -> plugin.shutDown());
		thread.drain();
		SwingUtilities.invokeAndWait(() -> { });
	}

	@Test
	public void membershipIsDetectedAndManualOverrideSurvivesTicksAndHops()
	{
		start();
		assertTrue(plugin.controller().method().modes().contains(AccountMode.P2P));
		plugin.setAccountMode(AccountMode.F2P);
		thread.drain();
		plugin.onGameTick(new GameTick());
		assertTrue(plugin.controller().method().modes().contains(AccountMode.F2P));
		GameStateChanged hopping = new GameStateChanged();
		hopping.setGameState(GameState.HOPPING);
		when(client.getGameState()).thenReturn(GameState.HOPPING);
		plugin.onGameStateChanged(hopping);
		when(client.getGameState()).thenReturn(GameState.LOGGED_IN);
		plugin.onGameTick(new GameTick());
		plugin.setSkill(Skill.AGILITY);
		thread.drain();
		assertEquals(Skill.WOODCUTTING, plugin.controller().method().skill());
		GameStateChanged logout = new GameStateChanged();
		logout.setGameState(GameState.LOGIN_SCREEN);
		when(client.getGameState()).thenReturn(GameState.LOGIN_SCREEN);
		plugin.onGameStateChanged(logout);
		when(client.getGameState()).thenReturn(GameState.LOGGED_IN);
		plugin.onGameTick(new GameTick());
		plugin.setSkill(Skill.AGILITY);
		thread.drain();
		assertEquals(Skill.AGILITY, plugin.controller().method().skill());
	}

	@Test
	public void freeAccountDetectionReplacesASavedMembersSkill() throws Exception
	{
		when(client.getWorldType()).thenReturn(EnumSet.noneOf(WorldType.class));
		when(config.accountMode()).thenReturn(AccountMode.P2P);
		when(config.skillName()).thenReturn("Agility");
		SwingUtilities.invokeAndWait(() -> plugin.startUp());
		thread.drain();
		assertTrue(plugin.controller().running());
		assertEquals(Skill.WOODCUTTING, plugin.controller().method().skill());
		SwingUtilities.invokeAndWait(() -> plugin.shutDown());
		thread.drain();
	}

	@Test
	public void memberAccountIsDetectedEvenOnAFreeWorld()
	{
		when(client.getWorldType()).thenReturn(EnumSet.noneOf(WorldType.class));
		when(client.getVarpValue(VarPlayer.MEMBERSHIP_DAYS)).thenReturn(14);
		plugin.onGameTick(new GameTick());
		plugin.setSkill(Skill.AGILITY);
		thread.drain();
		assertEquals(Skill.AGILITY, plugin.controller().method().skill());
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
		when(client.getVarbitValue(net.runelite.api.gameval.VarbitID.SPELLBOOK)).thenReturn(1);
		doReturn(SupplyTrackerTest.container(new Item(ItemID.DEATH_RUNE, 100), new Item(ItemID.CHAOS_RUNE, 100), new Item(ItemID.WATER_RUNE, 100)))
			.when(client).getItemContainer(net.runelite.api.gameval.InventoryID.INV);
		TrainingMethod burst = TrainingCatalog.all().stream().filter(method -> method.name().equals("Ice Burst")).findFirst().get();
		when(client.getRealSkillLevel(any(Skill.class))).thenReturn(80);
		when(client.getIntStack()).thenReturn(new int[]{2});
		plugin.setAccountMode(AccountMode.P2P);
		plugin.setSkill(Skill.MAGIC);
		plugin.pinMethod(burst.id());
		thread.drain();
		assertEquals(burst.id(), plugin.controller().method().id());
		plugin.controller().advance();
		plugin.controller().advance();
		NPC dustDevil = mock(NPC.class);
		when(dustDevil.getId()).thenReturn(NpcID.DUST_DEVIL_7249);
		net.runelite.api.coords.WorldPoint location = client.getLocalPlayer().getWorldLocation();
		when(dustDevil.getWorldLocation()).thenReturn(location);
		when(client.getNpcs()).thenReturn(java.util.Collections.singletonList(dustDevil));
		plugin.onGameTick(new GameTick());
		assertTrue(plugin.highlightedNpcs().isEmpty());
		assertEquals(StepKind.BANK, plugin.controller().current().kind());
		ItemContainer equipment = mock(ItemContainer.class);
		when(equipment.count(ItemID.FACEMASK)).thenReturn(1);
		when(client.getItemContainer(net.runelite.api.gameval.InventoryID.WORN)).thenReturn(equipment);
		plugin.controller().advance();
		plugin.controller().advance();
		plugin.onGameTick(new GameTick());
		assertEquals(1, plugin.highlightedNpcs().size());
		when(client.getItemContainer(net.runelite.api.gameval.InventoryID.WORN)).thenReturn(null);
		plugin.onGameTick(new GameTick());
		assertTrue(plugin.highlightedNpcs().isEmpty());
	}
	private void miningWith(ItemContainer inventory, ItemContainer equipment)
	{
		when(config.autoAdvance()).thenReturn(true);
		when(client.getItemContainer(net.runelite.api.gameval.InventoryID.INV)).thenReturn(inventory);
		when(client.getItemContainer(net.runelite.api.gameval.InventoryID.WORN)).thenReturn(equipment);
		plugin.setAccountMode(AccountMode.F2P);
		plugin.setSkill(Skill.MINING);
		thread.drain();
		plugin.onGameTick(new GameTick());
	}

	@Test
	public void carriedPickaxeSkipsTheUnnecessaryBankVisitAndBankButtonStillWorks()
	{
		miningWith(SupplyTrackerTest.container(), SupplyTrackerTest.container(new Item(ItemID.RUNE_PICKAXE, 1)));
		assertEquals(com.levelinghelper.model.StepKind.TRAVEL, plugin.controller().current().kind());
		assertFalse(plugin.currentStep().instruction().contains("Withdraw"));
		plugin.bankStep();
		thread.drain();
		plugin.onGameTick(new GameTick());
		assertEquals(com.levelinghelper.model.StepKind.BANK, plugin.controller().current().kind());
	}

	@Test
	public void fullMiningInventoryStillNeedsDepositingEvenWithATool()
	{
		Item[] ore = new Item[28];
		java.util.Arrays.fill(ore, new Item(ItemID.IRON_ORE, 1));
		miningWith(SupplyTrackerTest.container(ore), SupplyTrackerTest.container(new Item(ItemID.RUNE_PICKAXE, 1)));
		assertEquals(com.levelinghelper.model.StepKind.BANK, plugin.controller().current().kind());
	}

	@Test
	public void missingBonesCheckTheBankBeforeSuggestingAcquisition()
	{
		when(client.getAccountType()).thenReturn(net.runelite.api.vars.AccountType.NORMAL);
		doReturn(SupplyTrackerTest.container()).when(client).getItemContainer(net.runelite.api.gameval.InventoryID.INV);
		plugin.setAccountMode(AccountMode.F2P);
		plugin.setSkill(Skill.PRAYER);
		thread.drain();
		plugin.onGameTick(new GameTick());
		assertTrue(plugin.currentStep().instruction().startsWith("Open a bank"));
		net.runelite.api.widgets.Widget bankWidget = mock(net.runelite.api.widgets.Widget.class);
		when(client.getWidget(net.runelite.api.gameval.InterfaceID.Bankmain.ITEMS)).thenReturn(bankWidget);
		doReturn(SupplyTrackerTest.container(new Item(ItemID.BIG_BONES, 100))).when(client).getItemContainer(net.runelite.api.gameval.InventoryID.BANK);
		plugin.onGameTick(new GameTick());
		assertTrue(plugin.currentStep().instruction().startsWith("Withdraw"));
		doReturn(SupplyTrackerTest.container(new Item(ItemID.BIG_BONES, 0))).when(client).getItemContainer(net.runelite.api.gameval.InventoryID.BANK);
		plugin.onGameTick(new GameTick());
		assertTrue(plugin.currentStep().instruction().startsWith("Obtain"));
		assertEquals(com.levelinghelper.data.Spots.GE, plugin.currentStep().target());
		when(client.getAccountType()).thenReturn(net.runelite.api.vars.AccountType.IRONMAN);
		plugin.onGameTick(new GameTick());
		assertNull(plugin.currentStep().target());
		assertTrue(plugin.currentStep().instruction().contains("Ironman accounts cannot buy"));
	}

	@Test
	public void depletedFeathersRestockWithoutCallingRemainingTipsFinished()
	{
		when(config.autoAdvance()).thenReturn(true);
		when(client.getRealSkillLevel(Skill.FLETCHING)).thenReturn(60);
		when(client.getSkillExperience(Skill.FLETCHING)).thenReturn(273742);
		doReturn(SupplyTrackerTest.container(new Item(ItemID.MITHRIL_DART_TIP, 100), new Item(ItemID.FEATHER, 50))).when(client).getItemContainer(net.runelite.api.gameval.InventoryID.INV);
		plugin.setAccountMode(AccountMode.P2P);
		plugin.setSkill(Skill.FLETCHING);
		plugin.setTrainingStyle(TrainingStyle.FAST);
		thread.drain();
		plugin.onGameTick(new GameTick());
		assertEquals(com.levelinghelper.model.StepKind.TRAIN, plugin.controller().current().kind());
		doReturn(SupplyTrackerTest.container(new Item(ItemID.MITHRIL_DART_TIP, 50))).when(client).getItemContainer(net.runelite.api.gameval.InventoryID.INV);
		plugin.onGameTick(new GameTick());
		assertEquals(com.levelinghelper.model.StepKind.BANK, plugin.controller().current().kind());
		assertTrue(plugin.currentStep().instruction().contains("Feathers"));
		assertFalse(plugin.currentStep().instruction().contains("Mithril dart tip"));
		doReturn(SupplyTrackerTest.container(new Item(ItemID.MITHRIL_DART_TIP, 50), new Item(ItemID.FEATHER, 100))).when(client).getItemContainer(net.runelite.api.gameval.InventoryID.INV);
		plugin.onGameTick(new GameTick());
		assertEquals(com.levelinghelper.model.StepKind.TRAIN, plugin.controller().current().kind());
	}

	@Test
	public void missingInventoryWaitsInsteadOfSendingThePlayerToBank()
	{
		doReturn(null).when(client).getItemContainer(net.runelite.api.gameval.InventoryID.INV);
		plugin.onGameTick(new GameTick());
		assertTrue(plugin.currentStep().instruction().startsWith("Waiting for inventory"));
		assertNull(plugin.guidanceTarget());
	}

	@Test
	public void bankUpdatesAfterClosingAndLogoutDoNotReuseOldStock()
	{
		plugin.setAccountMode(AccountMode.F2P);
		plugin.setSkill(Skill.PRAYER);
		thread.drain();
		net.runelite.api.widgets.Widget bankWidget = mock(net.runelite.api.widgets.Widget.class);
		when(client.getWidget(net.runelite.api.gameval.InterfaceID.Bankmain.ITEMS)).thenReturn(bankWidget);
		doReturn(SupplyTrackerTest.container(new Item(ItemID.BIG_BONES, 100))).when(client).getItemContainer(net.runelite.api.gameval.InventoryID.BANK);
		plugin.onGameTick(new GameTick());
		assertTrue(plugin.currentStep().instruction().startsWith("Withdraw"));
		when(bankWidget.isHidden()).thenReturn(true);
		plugin.onItemContainerChanged(new ItemContainerChanged(net.runelite.api.gameval.InventoryID.BANK, SupplyTrackerTest.container()));
		plugin.onGameTick(new GameTick());
		assertTrue(plugin.currentStep().instruction().startsWith("Obtain"));
		GameStateChanged logout = new GameStateChanged();
		logout.setGameState(GameState.LOGIN_SCREEN);
		when(client.getGameState()).thenReturn(GameState.LOGIN_SCREEN);
		plugin.onGameStateChanged(logout);
		when(client.getGameState()).thenReturn(GameState.LOGGED_IN);
		when(client.getWorldType()).thenReturn(EnumSet.noneOf(WorldType.class));
		plugin.onGameTick(new GameTick());
		assertTrue(plugin.currentStep().instruction().startsWith("Open a bank"));
	}

	@Test
	public void ultimateIronmanGetsSourcesWithoutAnImpossibleBankVisit()
	{
		when(client.getAccountType()).thenReturn(net.runelite.api.vars.AccountType.ULTIMATE_IRONMAN);
		plugin.setAccountMode(AccountMode.F2P);
		plugin.setSkill(Skill.PRAYER);
		thread.drain();
		plugin.onGameTick(new GameTick());
		assertTrue(plugin.currentStep().instruction().startsWith("Obtain"));
		assertNull(plugin.guidanceTarget());
	}

	@Test
	public void sidebarSnapshotNamesTheHeldToolAndEstimatesRecipeMaterials() throws Exception
	{
		LevelingHelperPanel panel = mock(LevelingHelperPanel.class);
		set("panel", panel);
		ItemManager manager = mock(ItemManager.class);
		ItemComposition pickaxe = mock(ItemComposition.class);
		when(pickaxe.getName()).thenReturn("Rune pickaxe");
		when(manager.getItemComposition(ItemID.RUNE_PICKAXE)).thenReturn(pickaxe);
		set("itemManager", manager);
		miningWith(SupplyTrackerTest.container(), SupplyTrackerTest.container(new Item(ItemID.RUNE_PICKAXE, 1)));
		SwingUtilities.invokeAndWait(() -> { });
		org.mockito.ArgumentCaptor<com.levelinghelper.guide.GuideSnapshot> snapshots = org.mockito.ArgumentCaptor.forClass(com.levelinghelper.guide.GuideSnapshot.class);
		verify(panel, atLeastOnce()).apply(snapshots.capture());
		com.levelinghelper.guide.GuideSnapshot snapshot = snapshots.getValue();
		assertTrue(snapshot.itemsReady());
		assertTrue(snapshot.preparation().stream().anyMatch(line -> line.startsWith("Ready: Rune pickaxe")));
		clearInvocations(panel);
		when(client.getRealSkillLevel(Skill.FLETCHING)).thenReturn(60);
		when(client.getSkillExperience(Skill.FLETCHING)).thenReturn(273742);
		doReturn(SupplyTrackerTest.container(new Item(ItemID.MITHRIL_DART_TIP, 100), new Item(ItemID.FEATHER, 50)))
			.when(client).getItemContainer(net.runelite.api.gameval.InventoryID.INV);
		plugin.setAccountMode(AccountMode.P2P);
		plugin.setSkill(Skill.FLETCHING);
		plugin.setTrainingStyle(TrainingStyle.FAST);
		thread.drain();
		SwingUtilities.invokeAndWait(() -> { });
		verify(panel, atLeastOnce()).apply(snapshots.capture());
		snapshot = snapshots.getValue();
		assertTrue(snapshot.preparation().stream().anyMatch(line -> line.startsWith("To level 61:") && line.contains("2,549 Mithril dart tip")));
		assertTrue(snapshot.preparation().stream().anyMatch(line -> line.startsWith("To level 61:") && line.contains("2,549 Feathers")));
		assertFalse(snapshot.preparation().stream().anyMatch(line -> line.contains("Knife")));
	}

	@Test
	public void manuallySelectedProfitIronGoesToBankWhenFullThenReturnsToTheRocks()
	{
		when(config.autoAdvance()).thenReturn(true);
		when(client.getRealSkillLevel(Skill.MINING)).thenReturn(35);
		doReturn(SupplyTrackerTest.container(new Item(ItemID.ADAMANT_PICKAXE, 1)))
			.when(client).getItemContainer(net.runelite.api.gameval.InventoryID.INV);
		plugin.setAccountMode(AccountMode.F2P);
		plugin.setSkill(Skill.MINING);
		plugin.setTrainingStyle(TrainingStyle.PROFIT);
		thread.drain();
		assertEquals("Coal", plugin.controller().method().name());
		TrainingMethod iron = com.levelinghelper.guide.MethodSelector.alternatives(TrainingCatalog.all(), Skill.MINING,
			AccountMode.F2P, TrainingStyle.PROFIT, 35, null).stream().filter(method -> method.name().equals("Iron")).findFirst().get();
		plugin.pinMethod(iron.id());
		thread.drain();
		assertEquals("Iron", plugin.controller().method().name());
		assertEquals(StepKind.TRAVEL, plugin.controller().current().kind());
		plugin.nextStep();
		thread.drain();
		assertEquals(StepKind.TRAIN, plugin.controller().current().kind());
		Item[] full = new Item[28];
		full[0] = new Item(ItemID.ADAMANT_PICKAXE, 1);
		java.util.Arrays.fill(full, 1, full.length, new Item(ItemID.IRON_ORE, 1));
		doReturn(SupplyTrackerTest.container(full)).when(client).getItemContainer(net.runelite.api.gameval.InventoryID.INV);
		plugin.onGameTick(new GameTick());
		assertEquals(StepKind.BANK, plugin.controller().current().kind());
		assertEquals(com.levelinghelper.data.Spots.AL_KHARID_BANK, plugin.guidanceTarget());
		assertSame(iron, plugin.controller().method());
		plugin.onGameTick(new GameTick());
		assertEquals(StepKind.BANK, plugin.controller().current().kind());
		// A bank opening and closing without depositing must not send a full inventory back to the rocks.
		net.runelite.api.widgets.Widget bank = mock(net.runelite.api.widgets.Widget.class);
		when(client.getWidget(net.runelite.api.gameval.InterfaceID.Bankmain.ITEMS)).thenReturn(bank);
		plugin.onGameTick(new GameTick());
		when(bank.isHidden()).thenReturn(true);
		plugin.onGameTick(new GameTick());
		assertEquals(StepKind.BANK, plugin.controller().current().kind());
		doReturn(SupplyTrackerTest.container(new Item(ItemID.ADAMANT_PICKAXE, 1)))
			.when(client).getItemContainer(net.runelite.api.gameval.InventoryID.INV);
		plugin.onGameTick(new GameTick());
		assertEquals(StepKind.TRAVEL, plugin.controller().current().kind());
		assertEquals(com.levelinghelper.data.Spots.AL_KHARID_MINE, plugin.guidanceTarget());
		assertEquals("Iron", plugin.controller().method().name());
	}

	@Test
	public void optionalFoodDoesNotBlockAgilityAndBankWorksWithoutARouteBankStep()
	{
		when(config.autoAdvance()).thenReturn(true);
		plugin.setAccountMode(AccountMode.P2P);
		plugin.setSkill(Skill.AGILITY);
		thread.drain();
		assertEquals(StepKind.TRAVEL, plugin.currentStep().kind());
		plugin.bankStep();
		thread.drain();
		assertEquals(StepKind.BANK, plugin.currentStep().kind());
		net.runelite.api.widgets.Widget bank = mock(net.runelite.api.widgets.Widget.class);
		when(client.getWidget(net.runelite.api.gameval.InterfaceID.Bankmain.ITEMS)).thenReturn(bank);
		plugin.onGameTick(new GameTick());
		when(bank.isHidden()).thenReturn(true);
		plugin.onGameTick(new GameTick());
		assertEquals(StepKind.TRAVEL, plugin.currentStep().kind());
	}

	@Test
	public void deployedHunterTrapsDoNotTriggerAFalseRestockingTrip()
	{
		when(config.autoAdvance()).thenReturn(true);
		doReturn(SupplyTrackerTest.container(new Item(ItemID.BIRD_SNARE, 2))).when(client).getItemContainer(net.runelite.api.gameval.InventoryID.INV);
		plugin.setAccountMode(AccountMode.P2P);
		plugin.setSkill(Skill.HUNTER);
		plugin.pinMethod(TrainingCatalog.all().stream().filter(method -> method.name().equals("Crimson swifts")).findFirst().get().id());
		// Crimson swifts are only offered at early levels.
		when(client.getRealSkillLevel(Skill.HUNTER)).thenReturn(5);
		thread.drain();
		plugin.nextStep(); thread.drain();
		assertEquals(StepKind.TRAIN, plugin.controller().current().kind());
		doReturn(SupplyTrackerTest.container()).when(client).getItemContainer(net.runelite.api.gameval.InventoryID.INV);
		plugin.onGameTick(new GameTick());
		assertEquals(StepKind.TRAIN, plugin.currentStep().kind());
		assertFalse(plugin.currentStep().instruction().contains("Obtain"));
	}

	@Test
	public void selectingHunterMidTripCanAcknowledgeAlreadyDeployedTraps()
	{
		when(config.autoAdvance()).thenReturn(true);
		when(client.getRealSkillLevel(Skill.HUNTER)).thenReturn(5);
		plugin.setAccountMode(AccountMode.P2P); plugin.setSkill(Skill.HUNTER); thread.drain();
		assertTrue(plugin.currentStep().instruction().contains("already paid, deployed or prepared"));
		plugin.nextStep(); thread.drain();
		assertEquals(StepKind.TRAVEL, plugin.currentStep().kind());
	}

	@Test
	public void depletedHousePlanksOfferTheServantBeforeLeavingTheHouse()
	{
		when(config.autoAdvance()).thenReturn(true);
		when(client.getRealSkillLevel(Skill.CONSTRUCTION)).thenReturn(40);
		doReturn(SupplyTrackerTest.container(new Item(ItemID.SAW, 1), new Item(ItemID.HAMMER, 1), new Item(ItemID.OAK_PLANK, 8)))
			.when(client).getItemContainer(net.runelite.api.gameval.InventoryID.INV);
		plugin.setAccountMode(AccountMode.P2P); plugin.setSkill(Skill.CONSTRUCTION); plugin.setTrainingStyle(TrainingStyle.FAST); thread.drain();
		plugin.nextStep(); thread.drain();
		assertEquals(StepKind.NOTE, plugin.controller().current().kind());
		doReturn(SupplyTrackerTest.container(new Item(ItemID.SAW, 1), new Item(ItemID.HAMMER, 1), new Item(ItemID.OAK_PLANK, 7)))
			.when(client).getItemContainer(net.runelite.api.gameval.InventoryID.INV);
		plugin.onGameTick(new GameTick());
		assertTrue(plugin.currentStep().instruction().contains("servant"));
		assertEquals(StepKind.NOTE, plugin.controller().current().kind());
		plugin.bankStep(); thread.drain();
		assertEquals(StepKind.BANK, plugin.currentStep().kind());
	}

	@Test
	public void motherlodeFullInventoryGoesToTheHopperBeforeAnyBankLoop()
	{
		when(config.autoAdvance()).thenReturn(true);
		doReturn(SupplyTrackerTest.container(new Item(ItemID.RUNE_PICKAXE, 1))).when(client).getItemContainer(net.runelite.api.gameval.InventoryID.WORN);
		plugin.setAccountMode(AccountMode.P2P); plugin.setSkill(Skill.MINING); thread.drain();
		assertEquals("Motherlode Mine", plugin.controller().method().name());
		while (plugin.controller().current().kind() != StepKind.TRAIN) plugin.controller().advance();
		Item[] payDirt = new Item[28]; java.util.Arrays.fill(payDirt, new Item(ItemID.PAYDIRT, 1));
		doReturn(SupplyTrackerTest.container(payDirt)).when(client).getItemContainer(net.runelite.api.gameval.InventoryID.INV);
		plugin.onGameTick(new GameTick());
		assertEquals(AdvanceWhen.NEAR_AND_EMPTIED, plugin.currentStep().advance());
		assertTrue(plugin.currentStep().instruction().contains("hopper"));
	}

	@Test
	public void wrongSpellbookStaysAnExplicitSetupCheckEvenWhenRunesAreOwned()
	{
		when(client.getRealSkillLevel(Skill.SLAYER)).thenReturn(65);
		when(client.getRealSkillLevel(Skill.MAGIC)).thenReturn(80);
		when(client.getIntStack()).thenReturn(new int[]{2});
		doReturn(SupplyTrackerTest.container(new Item(ItemID.DEATH_RUNE, 100), new Item(ItemID.CHAOS_RUNE, 100), new Item(ItemID.WATER_RUNE, 100)))
			.when(client).getItemContainer(net.runelite.api.gameval.InventoryID.INV);
		doReturn(SupplyTrackerTest.container(new Item(ItemID.FACEMASK, 1))).when(client).getItemContainer(net.runelite.api.gameval.InventoryID.WORN);
		plugin.setAccountMode(AccountMode.P2P); plugin.setSkill(Skill.MAGIC);
		plugin.pinMethod(TrainingCatalog.all().stream().filter(method -> method.name().equals("Ice Burst")).findFirst().get().id()); thread.drain();
		assertTrue(plugin.currentStep().instruction().contains("Switch to Ancient Magicks"));
		int before = plugin.controller().stepIndex();
		plugin.nextStep(); thread.drain();
		assertEquals(before, plugin.controller().stepIndex());
		when(client.getVarbitValue(net.runelite.api.gameval.VarbitID.SPELLBOOK)).thenReturn(1);
		plugin.onGameTick(new GameTick());
		assertFalse(plugin.currentStep().instruction().startsWith("Switch to Ancient Magicks"));
	}

	@Test
	public void membersRoutesWaitForAMembersWorldAndAutomaticallyResumeAfterHopping()
	{
		when(client.getWorldType()).thenReturn(EnumSet.noneOf(WorldType.class));
		when(client.getVarpValue(VarPlayer.MEMBERSHIP_DAYS)).thenReturn(14);
		plugin.onGameTick(new GameTick());
		plugin.setSkill(Skill.AGILITY); thread.drain();
		assertTrue(plugin.currentStep().instruction().contains("members world"));
		assertNull(plugin.guidanceTarget());
		when(client.getWorldType()).thenReturn(EnumSet.of(WorldType.MEMBERS));
		plugin.onGameTick(new GameTick());
		assertEquals(StepKind.TRAVEL, plugin.currentStep().kind());
	}

	@Test
	public void untradeableQuestEquipmentIsRecoveredAtItsSourceRatherThanBoughtAtTheExchange()
	{
		when(client.getIntStack()).thenReturn(new int[]{2});
		doReturn(SupplyTrackerTest.container(new Item(ItemID.GOLD_ORE, 100))).when(client).getItemContainer(net.runelite.api.gameval.InventoryID.INV);
		plugin.setAccountMode(AccountMode.P2P); plugin.setSkill(Skill.SMITHING);
		plugin.pinMethod(TrainingCatalog.all().stream().filter(method -> method.name().equals("Blast Furnace gold")).findFirst().get().id()); thread.drain();
		net.runelite.api.widgets.Widget bank = mock(net.runelite.api.widgets.Widget.class);
		when(client.getWidget(net.runelite.api.gameval.InterfaceID.Bankmain.ITEMS)).thenReturn(bank);
		doReturn(SupplyTrackerTest.container()).when(client).getItemContainer(net.runelite.api.gameval.InventoryID.BANK);
		plugin.onGameTick(new GameTick());
		assertTrue(plugin.currentStep().instruction().startsWith("Obtain"));
		assertTrue(plugin.currentStep().instruction().contains("recovered at their source"));
		assertNull(plugin.guidanceTarget());
	}

}
