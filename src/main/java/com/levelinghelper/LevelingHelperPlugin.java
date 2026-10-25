package com.levelinghelper;

import com.google.inject.Provides;
import com.levelinghelper.data.TrainingCatalog;
import com.levelinghelper.data.Spots;
import com.levelinghelper.guide.GuideSteps;
import com.levelinghelper.model.AdvanceWhen;
import com.levelinghelper.model.HighlightMode;
import com.levelinghelper.guide.GuideController;
import com.levelinghelper.guide.GuideSnapshot;
import com.levelinghelper.guide.GuideTick;
import com.levelinghelper.guide.LevelLookup;
import com.levelinghelper.guide.MethodSelector;
import com.levelinghelper.guide.TrainingMath;
import com.levelinghelper.model.AccountMode;
import com.levelinghelper.model.GuideStep;
import com.levelinghelper.model.ItemRequirement;
import com.levelinghelper.model.StepKind;
import com.levelinghelper.model.TrainingMethod;
import com.levelinghelper.model.TrainingStyle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;
import net.runelite.api.TileObject;
import net.runelite.api.VarPlayer;
import net.runelite.api.WorldType;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.DecorativeObjectDespawned;
import net.runelite.api.events.DecorativeObjectSpawned;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.GroundObjectDespawned;
import net.runelite.api.events.GroundObjectSpawned;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.WallObjectDespawned;
import net.runelite.api.events.WallObjectSpawned;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.worldmap.WorldMapPoint;
import net.runelite.client.ui.overlay.worldmap.WorldMapPointManager;
import net.runelite.client.util.LinkBrowser;

@Slf4j
@PluginDescriptor(
	name = "Leveling Helper",
	description = "Pick a skill and follow a training route, with banking, highlights, and XP rates",
	tags = {"skilling", "training", "guide", "xp", "leveling"}
)
public class LevelingHelperPlugin extends Plugin
{
	private static final int INVENTORY_SIZE = 28;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private LevelingHelperConfig config;

	@Inject
	private ConfigManager configManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ItemManager itemManager;

	@Inject
	private WorldMapPointManager worldMapPointManager;

	@Inject
	private LevelingHelperOverlay overlay;

	private volatile LevelingHelperPanel panel;
	private volatile boolean enabled;
	private volatile long lifecycle;
	private NavigationButton navigationButton;
	private final GuideController controller = new GuideController();
	private final SupplyTracker supplies = new SupplyTracker();
	private GuideStep supplyStep;
	private boolean forceBank;
	private boolean requestedBankOpened;
	private boolean departurePrepared;
	private boolean inventorySynchronized;
	private List<SupplyTracker.Stock> stockCache;
	private TrainingMethod stockCacheMethod;
	@Inject
	private SessionLifecycle session;

	@Inject
	private TargetHighlighter targets;

	@Inject
	private HintArrowController hintArrows;

	@Inject
	private LevelingHelperMinimapOverlay minimapOverlay;

	private AccountMode mode = AccountMode.F2P;
	private TrainingStyle style = TrainingStyle.BALANCED;
	private Skill skill = Skill.WOODCUTTING;
	private int goal = 99;
	private String pinnedId;
	private String announcedMethodId;
	private boolean highlightsDirty = true;
	private boolean goalAnnounced;
	private boolean manualModeOverride;
	private WorldMapPoint mapPoint;
	private WorldPoint mapPointTarget;
	private String mapPointMethod;
	private static final BufferedImage ICON = icon();

	@Provides
	LevelingHelperConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(LevelingHelperConfig.class);
	}

	@Override
	protected void startUp()
	{
		enabled = true;
		lifecycle++;
		panel = new LevelingHelperPanel(this);
		panel.load(config.accountMode(), config.trainingStyle(), skillByName(config.skillName()), clampGoal(config.goalLevel()));
		onClient(() ->
		{
			stopCurrentGuide();
			supplies.clear();
			inventorySynchronized = false;
			stockCache = null;
			mode = config.accountMode();
			style = config.trainingStyle();
			goal = clampGoal(config.goalLevel());
			skill = skillByName(config.skillName());
			manualModeOverride = false;
			goalAnnounced = false;
			pinnedId = null;
			syncMembership();
			ensureGuiding();
			publish();
		});
		navigationButton = NavigationButton.builder()
			.tooltip("Leveling Helper")
			.icon(ICON)
			.priority(6)
			.panel(panel)
			.build();
		clientToolbar.addNavigation(navigationButton);
		overlayManager.add(overlay);
		overlayManager.add(minimapOverlay);
		highlightsDirty = true;
		log.debug("Leveling Helper started");
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		overlayManager.remove(minimapOverlay);
		clientToolbar.removeNavigation(navigationButton);
		enabled = false;
		long stoppedLifecycle = ++lifecycle;
		clientThread.invoke(() ->
		{
			if (lifecycle == stoppedLifecycle)
			{
				stopCurrentGuide();
			}
		});
		panel = null;
		navigationButton = null;
		log.debug("Leveling Helper stopped");
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		if (!enabled || client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}
		// GameTick runs after the server packets have been processed. An empty inventory
		// need not have a container node yet, particularly when logging in with a worn tool.
		inventorySynchronized = client.getLocalPlayer() != null;
		stockCache = null;
		syncMembership();
		observeBank();
		if (forceBank && bankOpen())
		{
			requestedBankOpened = true;
		}
		if (forceBank && requestedBankOpened && !bankOpen())
		{
			forceBank = false;
			requestedBankOpened = false;
		}
		ensureGuiding();
		updateSupplies();
		if (controller.running())
		{
			controller.observe(guideTick());
		}
		targets.refresh(trainingEquipmentReady() ? currentStep() : null, highlightsDirty);
		highlightsDirty = false;
		if (controller.running() && supplyStep == null && trainingEquipmentReady())
		{
			boolean changed = controller.tick(guideTick(), config.autoAdvance(), config.arrivalDistance());
			if (changed)
			{
				primeStep();
				updateSupplies();
				targets.refresh(trainingEquipmentReady() ? currentStep() : null, true);
			}
		}
		updateHintArrow();
		updateMapPoint();
		publish();
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		stockCache = null;
		if (!enabled)
		{
			return;
		}
		session.onXp(event.getSkill(), event.getXp());
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			ensureGuiding();
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (!enabled)
		{
			return;
		}
		if (event.getGameState() != GameState.LOGGED_IN)
		{
			inventorySynchronized = false;
			stockCache = null;
			session.stop();
			supplies.clear();
			supplyStep = null;
			targets.clear();
			hintArrows.clear();
			removeMapPoint();
			highlightsDirty = true;
		}
		if (event.getGameState() == GameState.LOGIN_SCREEN)
		{
			stopCurrentGuide();
			manualModeOverride = false;
			goalAnnounced = false;
		}
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			onClient(() ->
			{
				syncMembership();
				ensureGuiding();
				publish();
			});
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!LevelingHelperConfig.GROUP.equals(event.getGroup()))
		{
			return;
		}
		onClient(() ->
		{
			highlightsDirty = true;
			ensureGuiding();
			updateHintArrow();
			updateMapPoint();
			publish();
		});
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		targets.considerNpc(event.getNpc());
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		targets.remove(event.getNpc());
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		targets.considerObject(event.getGameObject());
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event)
	{
		targets.remove(event.getGameObject());
	}

	@Subscribe
	public void onWallObjectSpawned(WallObjectSpawned event)
	{
		targets.considerObject(event.getWallObject());
	}

	@Subscribe
	public void onWallObjectDespawned(WallObjectDespawned event)
	{
		targets.remove(event.getWallObject());
	}

	@Subscribe
	public void onGroundObjectSpawned(GroundObjectSpawned event)
	{
		targets.considerObject(event.getGroundObject());
	}

	@Subscribe
	public void onGroundObjectDespawned(GroundObjectDespawned event)
	{
		targets.remove(event.getGroundObject());
	}

	@Subscribe
	public void onDecorativeObjectSpawned(DecorativeObjectSpawned event)
	{
		targets.considerObject(event.getDecorativeObject());
	}

	@Subscribe
	public void onDecorativeObjectDespawned(DecorativeObjectDespawned event)
	{
		targets.remove(event.getDecorativeObject());
	}

	/** UI commands enqueue the complete mutation, including saved configuration, on the client thread. */
	private void onClient(Runnable action)
	{
		long expected = lifecycle;
		clientThread.invoke(() ->
		{
			if (enabled && lifecycle == expected)
			{
				action.run();
			}
		});
	}

	public void setAccountMode(AccountMode value)
	{
		onClient(() ->
		{
			manualModeOverride = client.getGameState() == GameState.LOGGED_IN && client.getLocalPlayer() != null;
			mode = value;
			ensureVisibleSkill();
			pinnedId = null;
			configManager.setConfiguration(LevelingHelperConfig.GROUP, "accountMode", value);
			selectionChanged();
		});
	}

	public void setTrainingStyle(TrainingStyle value)
	{
		onClient(() ->
		{
			style = value;
			pinnedId = null;
			configManager.setConfiguration(LevelingHelperConfig.GROUP, "trainingStyle", value);
			selectionChanged();
		});
	}

	public void setGoal(int value)
	{
		onClient(() ->
		{
			goal = clampGoal(value);
			goalAnnounced = false;
			configManager.setConfiguration(LevelingHelperConfig.GROUP, "goalLevel", goal);
			selectionChanged();
		});
	}

	public void setSkill(Skill value)
	{
		onClient(() ->
		{
			if (value == null || value == Skill.OVERALL || value == skill || mode == AccountMode.F2P && value.isMembers())
			{
				return;
			}
			skill = value;
			pinnedId = null;
			configManager.setConfiguration(LevelingHelperConfig.GROUP, "skillName", value.getName());
			selectionChanged();
		});
	}

	public void pinMethod(String id)
	{
		onClient(() ->
		{
			pinnedId = id;
			selectionChanged();
		});
	}

	private void selectionChanged()
	{
		stockCache = null;
		goalAnnounced = false;
		ensureGuiding();
		updateSupplies();
		publish();
	}

	private void stopCurrentGuide()
	{
		controller.stop();
		supplyStep = null;
		forceBank = false;
		requestedBankOpened = false;
		session.stop();
		announcedMethodId = null;
		targets.clear();
		removeMapPoint();
		hintArrows.clear();
		highlightsDirty = true;
	}

	public void nextStep()
	{
		onClient(() ->
		{
			if (supplyStep != null)
			{
				// First-trip fees/traps can already be paid or deployed when this guide is selected.
				List<SupplyTracker.Stock> missing = requiredStock(controller.method()).stream()
					.filter(item -> !item.ready()).collect(java.util.stream.Collectors.toList());
				if (!missing.isEmpty() && missing.stream().allMatch(item -> item.requirement.departureOnly()))
				{
					departurePrepared = true;
				}
				updateSupplies();
			}
			else
			{
				controller.advance();
				primeStep();
				updateSupplies();
			}
			highlightsDirty = true;
			publish();
		});
	}

	public void previousStep()
	{
		onClient(() ->
		{
			controller.back();
			primeStep();
			highlightsDirty = true;
			publish();
		});
	}

	public void bankStep()
	{
		onClient(() ->
		{
			forceBank = true;
			requestedBankOpened = bankOpen();
			controller.jumpToBank();
			primeStep();
			updateSupplies();
			highlightsDirty = true;
			publish();
		});
	}

	/** Detect each login; a sidebar override lasts until the next logout. */
	private void syncMembership()
	{
		if (manualModeOverride || client.getGameState() != GameState.LOGGED_IN || client.getLocalPlayer() == null)
		{
			return;
		}
		AccountMode detected = client.getVarpValue(VarPlayer.MEMBERSHIP_DAYS) > 0
			|| client.getWorldType().contains(WorldType.MEMBERS) ? AccountMode.P2P : AccountMode.F2P;
		if (mode != detected)
		{
			mode = detected;
			pinnedId = null;
			configManager.setConfiguration(LevelingHelperConfig.GROUP, "accountMode", mode);
		}
		ensureVisibleSkill();
	}

	private void ensureVisibleSkill()
	{
		if (mode == AccountMode.F2P && skill.isMembers())
		{
			skill = Skill.WOODCUTTING;
			pinnedId = null;
			goalAnnounced = false;
			configManager.setConfiguration(LevelingHelperConfig.GROUP, "skillName", skill.getName());
		}
	}

	public void openWiki(String title)
	{
		if (title == null || title.isEmpty())
		{
			return;
		}
		LinkBrowser.browse("https://oldschool.runescape.wiki/w/" + title.replace(' ', '_'));
	}

	List<NPC> highlightedNpcs()
	{
		return targets.npcs();
	}

	List<TileObject> highlightedObjects()
	{
		return targets.objects();
	}

	GuideController controller()
	{
		return controller;
	}

	private void publish()
	{
		if (panel == null)
		{
			return;
		}
		boolean loggedIn = client.getGameState() == GameState.LOGGED_IN && client.getLocalPlayer() != null;
		int level = 1;
		int xp = 0;
		LevelLookup lookup = loggedIn ? liveLookup() : previewLookup();
		if (loggedIn)
		{
			level = client.getRealSkillLevel(skill);
			xp = client.getSkillExperience(skill);
		}
		TrainingMethod method = chooseMethod(lookup, level);
		TrainingMethod upcoming = null;
		if (method != null && method.maxLevel() < 99)
		{
			TrainingMethod next = MethodSelector.select(TrainingCatalog.all(), skill, mode, style, method.maxLevel() + 1, lookup);
			if (next != null && !next.id().equals(method.id()))
			{
				upcoming = next;
			}
		}
		String distance = "";
		WorldPoint destination = controller.running() ? guidanceTarget() : null;
		if (destination != null && loggedIn)
		{
			distance = TrainingMath.direction(playerLocation(), destination);
		}
		String status = statusFor(loggedIn, method, level);
		final GuideSnapshot snapshot = new GuideSnapshot(
			loggedIn,
			loggedIn && client.getWorldType().contains(WorldType.MEMBERS),
			skill,
			mode,
			style,
			goal,
			level,
			xp,
			controller.running(),
			controller.running() ? controller.method() : method,
			controller.running() ? controller.stepIndex() : 0,
			session.gained(),
			session.xpPerHour(),
			distance,
			upcoming,
			MethodSelector.alternatives(TrainingCatalog.all(), skill, mode, style, level, lookup),
			status, preparation(method, loggedIn), lockedRoutes(lookup, level), supplyStep, itemsReady(method));
		final LevelingHelperPanel target = panel;
		SwingUtilities.invokeLater(() ->
		{
			if (enabled && panel == target)
			{
				target.apply(snapshot);
			}
		});
	}

	private List<String> preparation(TrainingMethod method, boolean loggedIn)
	{
		List<String> details = new ArrayList<>();
		if (method == null)
		{
			return details;
		}
		if (method.risk() != com.levelinghelper.model.RouteRisk.SAFE)
		{
			details.add(method.risk().description());
		}
		for (Quest quest : method.quests())
		{
			details.add("Quest complete: " + quest.getName());
		}
		for (String check : method.manualChecks()) details.add("Player check: " + check);
		if (!method.noItemsReason().isEmpty()) details.add(method.noItemsReason());
		if (loggedIn) for (String issue : method.setupIssues(liveLookup())) details.add("Setup needed: " + issue);
		if (loggedIn)
		{
			details.addAll(supplyLines(method));
		}
		else for (ItemRequirement item : method.items()) details.add((item.equipped() ? "Equip: " : "Bring: ") + item.label());
		return details;
	}

	private List<String> lockedRoutes(LevelLookup lookup, int level)
	{
		List<String> locked = new ArrayList<>();
		for (TrainingMethod method : TrainingCatalog.all())
		{
			if (method.skill() == skill && method.modes().contains(mode) && method.containsLevel(level))
			{
				List<String> missing = method.unmetRequirements(lookup);
				if (!missing.isEmpty())
				{
					locked.add(method.name() + ": " + String.join(", ", missing));
				}
			}
		}
		return locked;
	}

	private String statusFor(boolean loggedIn, TrainingMethod method, int level)
	{
		if (!loggedIn)
		{
			return "Log in to use your levels. The route below is a level 1 preview.";
		}
		if (skill.isMembers() && mode == AccountMode.F2P)
		{
			return skill.getName() + " is a members skill. Switch the mode to Members, or pick a free-to-play skill.";
		}
		if (level >= goal)
		{
			return "You are already level " + level + ". Raise the goal if you want to keep going.";
		}
		if (method == null)
		{
			return "No eligible route at level " + level + ". Check the locked routes below for requirements.";
		}
		if (!method.modes().contains(AccountMode.F2P) && !client.getWorldType().contains(WorldType.MEMBERS))
		{
			return "You are on a free-to-play world. Hop before you follow a members route.";
		}
		if (!inventoryKnown())
		{
			return "Waiting for inventory data.";
		}
		List<String> missing = missingItems(method);
		if (!missing.isEmpty())
		{
			return supplyStep == null ? "Preparation needed: " + String.join("; ", missing) + "." : "Supplies needed before training.";
		}
		return "";
	}

	private TrainingMethod chooseMethod()
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return chooseMethod(previewLookup(), 1);
		}
		return chooseMethod(liveLookup(), client.getRealSkillLevel(skill));
	}

	private TrainingMethod chooseMethod(LevelLookup lookup, int level)
	{
		if (pinnedId != null)
		{
			TrainingMethod pinned = TrainingCatalog.byId(pinnedId);
			if (pinned != null
				&& pinned.skill() == skill
				&& pinned.modes().contains(mode)
				&& pinned.containsLevel(level)
				&& pinned.requirementsMet(lookup))
			{
				return pinned.forStyle(style);
			}
			pinnedId = null;
		}
		return MethodSelector.select(TrainingCatalog.all(), skill, mode, style, level, lookup);
	}

	private LevelLookup liveLookup()
	{
		return new LevelLookup()
		{
			@Override
			public int level(Skill queried)
			{
				return client.getRealSkillLevel(queried);
			}

			@Override
			public int combatLevel()
			{
				return TrainingMath.combatLevel(
					client.getRealSkillLevel(Skill.ATTACK),
					client.getRealSkillLevel(Skill.STRENGTH),
					client.getRealSkillLevel(Skill.DEFENCE),
					client.getRealSkillLevel(Skill.HITPOINTS),
					client.getRealSkillLevel(Skill.PRAYER),
					client.getRealSkillLevel(Skill.RANGED),
					client.getRealSkillLevel(Skill.MAGIC));
			}
			@Override
			public boolean questComplete(Quest quest)
			{
				return quest.getState(client) == QuestState.FINISHED;
			}

			@Override
			public boolean matches(com.levelinghelper.model.ItemCategory category, int itemId)
			{
				return new ItemRules(client, itemManager).matches(category, itemId);
			}

			@Override
			public int itemCount(int id)
			{
				ItemContainer inv = client.getItemContainer(InventoryID.INV);
				return (int) Math.min(Integer.MAX_VALUE, (long) (inv == null ? 0 : inv.count(id)) + extraItemCount(id));
			}

			@Override
			public int equippedCount(int id)
			{
				ItemContainer worn = client.getItemContainer(InventoryID.WORN);
				return worn == null ? 0 : worn.count(id);
			}

			@Override
			public boolean membersWorld() { return client.getWorldType().contains(WorldType.MEMBERS); }

			@Override
			public boolean equippedMatches(com.levelinghelper.model.ItemCategory category)
			{
				ItemContainer worn = client.getItemContainer(InventoryID.WORN);
				if (worn == null || worn.getItems() == null) return false;
				for (Item item : worn.getItems()) if (item.getId() > 0 && item.getQuantity() > 0 && matches(category, item.getId())) return true;
				return false;
			}

			@Override
			public int extraItemCount(int itemId) { return new ItemRules(client, itemManager).pouchCount(itemId); }

			@Override
			public boolean providerUsable(int id)
			{
				ItemComposition item = itemManager.getItemComposition(id);
				return item != null && (!item.isMembers() || membersWorld());
			}

			@Override
			public int storedItemCount(int id)
			{
				int bit = id == ItemID.SPADE ? net.runelite.api.gameval.VarbitID.FARMING_TOOLS_SPADE
					: id == ItemID.SEED_DIBBER ? net.runelite.api.gameval.VarbitID.FARMING_TOOLS_DIBBER
					: id == ItemID.RAKE ? net.runelite.api.gameval.VarbitID.FARMING_TOOLS_RAKE
					: id == ItemID.ULTRACOMPOST ? net.runelite.api.gameval.VarbitID.FARMING_TOOLS_ULTRACOMPOST : -1;
				return bit < 0 ? 0 : Math.max(0, client.getVarbitValue(bit));
			}

			@Override
			public int varbit(int id) { return client.getVarbitValue(id); }

			@Override
			public boolean allowWilderness()
			{
				return config.allowWilderness();
			}

		};
	}

	private LevelLookup previewLookup()
	{
		return new LevelLookup()
		{
			@Override
			public int level(Skill queried)
			{
				return 1;
			}

			@Override
			public int combatLevel()
			{
				return 3;
			}
		};
	}

	private boolean bankOpen()
	{
		Widget bank = client.getWidget(InterfaceID.Bankmain.ITEMS);
		return bank != null && !bank.isHidden();
	}

	private boolean inventoryKnown()
	{
		return client.getGameState() == GameState.LOGGED_IN && client.getLocalPlayer() != null
			&& (client.getItemContainer(InventoryID.INV) != null || inventorySynchronized);
	}

	private boolean inventoryFull()
	{
		ItemContainer inventory = client.getItemContainer(InventoryID.INV);
		if (inventory == null)
		{
			return false;
		}
		int used = 0;
		for (Item item : inventory.getItems())
		{
			if (item.getId() != -1)
			{
				used++;
			}
		}
		return used >= INVENTORY_SIZE;
	}

	private boolean watchedItemGone(GuideStep step)
	{
		if (step == null || step.watchedItem() == null)
		{
			return false;
		}
		ItemContainer inventory = client.getItemContainer(InventoryID.INV);
		if (inventory == null)
		{
			return inventoryKnown();
		}
		for (Item item : inventory.getItems())
		{
			if (item.getId() <= 0)
			{
				continue;
			}
			ItemComposition composition = itemManager.getItemComposition(item.getId());
			if (composition == null || composition.getName() == null)
			{
				continue;
			}
			if (step.watchedItem().equalsIgnoreCase(composition.getName()))
			{
				return false;
			}
		}
		return true;
	}

	private WorldPoint playerLocation()
	{
		Player player = client.getLocalPlayer();
		return player == null ? null : player.getWorldLocation();
	}

	private void ensureGuiding()
	{
		if (client.getGameState() != GameState.LOGGED_IN || client.getLocalPlayer() == null)
		{
			return;
		}
		if (client.getRealSkillLevel(skill) >= goal)
		{
			if (!goalAnnounced)
			{
				goalAnnounced = true;
				chat("Goal reached: level " + goal + " " + skill.getName() + ".");
			}
			stopCurrentGuide();
			return;
		}
		TrainingMethod method = chooseMethod();
		if (method == null)
		{
			stopCurrentGuide();
			return;
		}
		session.sync(skill);
		if (controller.running() && controller.method().id().equals(method.id()))
		{
			return;
		}
		targets.clear();
		hintArrows.clear();
		removeMapPoint();
		stockCache = null;
		controller.start(method);
		departurePrepared = false;
		supplyStep = null;
		forceBank = false;
		primeStep();
		highlightsDirty = true;
		if (!method.id().equals(announcedMethodId))
		{
			announcedMethodId = method.id();
			chat(method.name() + ". Follow the highlighted step.");
		}
	}

	private GuideTick guideTick()
	{
		return new GuideTick(playerLocation(), bankOpen(), inventoryFull(),
			watchedItemGone(controller.current()), targets.visible(), itemsReady(controller.method())
				&& (controller.current() == null || controller.current().kind() != StepKind.BANK
					|| emptySlots() >= controller.method().preparationFreeSlots()),
			inventoryKnown());
	}

	private void primeStep()
	{
		if (controller.running())
		{
			controller.observe(guideTick());
		}
	}

	private List<SupplyTracker.Stock> stock(TrainingMethod method)
	{
		if (stockCache == null || stockCacheMethod != method)
		{
			stockCache = supplies.check(method, client.getItemContainer(InventoryID.INV), client.getItemContainer(InventoryID.WORN),
				liveLookup(), client.getWorldType().contains(WorldType.MEMBERS));
			stockCacheMethod = method;
		}
		return stockCache;
	}

	private List<SupplyTracker.Stock> requiredStock(TrainingMethod method)
	{
		return stock(method).stream().filter(item -> !item.requirement.optional()
			&& (!item.requirement.departureOnly() || !departurePrepared))
			.collect(java.util.stream.Collectors.toList());
	}

	private boolean itemsReady(TrainingMethod method)
	{
		return inventoryKnown()
			&& requiredStock(method).stream().allMatch(SupplyTracker.Stock::ready)
			&& (method == null || method.setupIssues(liveLookup()).isEmpty());
	}

	private List<String> missingItems(TrainingMethod method)
	{
		List<String> missing = new ArrayList<>();
		for (SupplyTracker.Stock item : requiredStock(method))
		{
			if (!item.ready())
			{
				missing.add((item.requirement.equipped() ? "Equip: " : "Bring: ") + item.requirement.label());
			}
		}
		return missing;
	}

	private List<String> supplyLines(TrainingMethod method)
	{
		List<String> lines = new ArrayList<>();
		if (!inventoryKnown())
		{
			return java.util.Collections.singletonList("Waiting for inventory data before checking supplies.");
		}
		boolean ironman = client.getAccountType() != null && client.getAccountType().isIronman();
		boolean ultimate = client.getAccountType() == net.runelite.api.vars.AccountType.ULTIMATE_IRONMAN;
		for (SupplyTracker.Stock item : stock(method))
		{
			String name = item.requirement.label();
			if (item.requirement.optional())
			{
				lines.add("Optional: " + name + (item.ready() ? " (available)." : ". " + item.requirement.source()));
				continue;
			}
			if (item.requirement.departureOnly() && departurePrepared && !item.ready())
			{
				lines.add("Before another trip: " + name + ". " + item.requirement.source());
				continue;
			}
			if (item.ready())
			{
				ItemComposition composition = item.presentId > 0 ? itemManager.getItemComposition(item.presentId) : null;
				String carried = composition == null || composition.getName() == null ? name : composition.getName();
				String description = item.supplied ? name + (item.presentId > 0 ? " supplied by " + carried + ". Check remaining charges where applicable." : " supplied by the active effect.")
					: item.requirement.countsEquipment()
					? carried + (item.requirement.equipped() ? " (equipped)." : " (carried/equipped).")
					: name + " (" + item.held + " carried).";
				lines.add("Ready: " + description);
			}
			else if (item.needsEquipping())
			{
				lines.add("Equip: " + itemName(item.carriedProvider, name) + " for " + name + " - already in your inventory.");
			}
			else if (item.canRetrieve())
			{
				lines.add("Retrieve: " + name + " from a tool leprechaun (stored supplies detected).");
			}
			else if (item.bankProvider > 0)
			{
				lines.add("Withdraw and equip: " + itemName(item.bankProvider, name) + " to supply " + name + ".");
			}
			else if (ultimate)
			{
				lines.add("Obtain: " + name + ". " + item.requirement.source());
			}
			else if (!item.bankKnown)
			{
				lines.add("Missing: " + name + ". Open a bank to check stock (need " + item.requirement.quantity() + ", have " + item.held + ").");
			}
			else if (item.canWithdraw())
			{
				lines.add("Withdraw: " + name + " (take " + Math.min(item.bank, item.requirement.quantity() - item.held)
					+ "; " + item.bank + " in the last bank check)." + (item.requirement.equipped() ? " Then equip it." : "")
					+ (item.absentFromBank() ? " Obtain " + (item.requirement.quantity() - item.held - item.bank) + " more afterwards." : ""));
			}
			else
			{
				lines.add("Obtain: " + name + " (need " + (item.requirement.quantity() - item.held) + ", bank " + item.bank + "). "
					+ (ironman || !canBuy(item) ? item.requirement.source() : "Buy a suitable item at the Grand Exchange. Other source: " + item.requirement.source()));
			}
		}
		if (method != null && method.xpEach() > 0 && client.getRealSkillLevel(method.skill()) < 99)
		{
			int nextLevel = client.getRealSkillLevel(method.skill()) + 1;
			int actions = TrainingMath.actions(TrainingMath.xpBetween(client.getSkillExperience(method.skill()), nextLevel), method.xpEach());
			for (SupplyTracker.Stock item : stock(method))
			{
				if (item.requirement.unitsPerAction() <= 0 || item.supplied || item.requirement.optional())
				{
					continue;
				}
				long needed = (long) Math.ceil(actions * item.requirement.unitsPerAction());
				lines.add("To level " + nextLevel + ": about " + String.format("%,d", needed) + " " + item.requirement.label()
					+ " (" + item.held + " carried; " + (item.bankKnown ? item.bank + " banked at last check" : "bank unknown") + ").");
			}
		}
		if (method != null && !method.items().isEmpty())
		{
			lines.add("Ready checks need enough to start. Materials must be unnoted.");
		}
		return lines;
	}

	private void observeBank()
	{
		Player player = client.getLocalPlayer();
		supplies.bind(player == null ? null : player.getName());
		if (bankOpen())
		{
			supplies.observeBank(client.getItemContainer(InventoryID.BANK));
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		stockCache = null;
		if (!enabled || client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}
		if (event.getContainerId() == InventoryID.INV)
		{
			inventorySynchronized = client.getLocalPlayer() != null;
		}
		Player player = client.getLocalPlayer();
		supplies.bind(player == null ? null : player.getName());
		if (event.getContainerId() == InventoryID.BANK && (bankOpen() || supplies.bankKnown()))
		{
			supplies.observeBank(event.getItemContainer());
		}
	}

	/** Keep the selected method, restarting its bank/travel sequence when supplies run out. */
	private void updateSupplies()
	{
		GuideStep previous = supplyStep;
		supplyStep = null;
		if (!controller.running())
		{
			return;
		}
		TrainingMethod method = controller.method();
		List<SupplyTracker.Stock> missing = new ArrayList<>();
		for (SupplyTracker.Stock item : requiredStock(method))
		{
			if (!item.ready())
			{
				missing.add(item);
			}
		}
		if (!inventoryKnown())
		{
			supplyStep = GuideSteps.note("Waiting for inventory data before checking supplies.");
		}
		else if (!method.modes().contains(AccountMode.F2P) && !client.getWorldType().contains(WorldType.MEMBERS))
		{
			supplyStep = GuideSteps.note("Hop to a members world to follow this route.");
		}
		else if (forceBank && method.steps().stream().noneMatch(step -> step.kind() == StepKind.BANK))
		{
			supplyStep = GuideSteps.bank("Visit a bank. Close it when ready to resume the selected route.", nearestBank());
		}
		else if (!method.setupIssues(liveLookup()).isEmpty())
		{
			supplyStep = GuideSteps.note(String.join(" ", method.setupIssues(liveLookup())));
		}
		else if (missing.isEmpty())
		{
			departurePrepared = true;
			if (config.autoAdvance() && !forceBank && controller.current().kind() == StepKind.BANK
				&& method.preparationFreeSlots() >= 0 && !bankOpen()
				&& emptySlots() >= method.preparationFreeSlots())
			{
				controller.advance();
				primeStep();
				highlightsDirty = true;
			}
		}
		else if (missing.stream().anyMatch(SupplyTracker.Stock::canRetrieve) && !forceBank)
		{
			String names = missing.stream().filter(SupplyTracker.Stock::canRetrieve).map(item -> item.requirement.label())
				.collect(java.util.stream.Collectors.joining(", "));
			supplyStep = GuideSteps.find("Retrieve " + names + " from a tool leprechaun near your patch, then continue.", "Tool Leprechaun");
		}
		else if (method.localRestock() != null && !forceBank && controller.current().kind() != StepKind.BANK
			&& missing.stream().allMatch(item -> item.requirement.unitsPerAction() > 0))
		{
			supplyStep = GuideSteps.note(method.localRestock());
		}
		else if (!missing.isEmpty())
		{
			if (controller.current().kind() != StepKind.BANK && controller.current().kind() != StepKind.DROP
				&& method.steps().stream().anyMatch(step -> step.kind() == StepKind.BANK))
			{
				controller.jumpToBank();
				primeStep();
			}
			String names = missing.stream().map(item -> itemName(item.carriedProvider > 0 ? item.carriedProvider : item.bankProvider, item.requirement.label()))
				.collect(java.util.stream.Collectors.joining(", "));
			String firstTrip = missing.stream().allMatch(item -> item.requirement.departureOnly())
				? " If already paid, deployed or prepared for this trip, press Next to skip this first-trip check." : "";
			boolean ironman = client.getAccountType() != null && client.getAccountType().isIronman();
			boolean obtain = client.getAccountType() == net.runelite.api.vars.AccountType.ULTIMATE_IRONMAN
				|| missing.stream().anyMatch(SupplyTracker.Stock::absentFromBank);
			if (missing.stream().allMatch(SupplyTracker.Stock::needsEquipping))
			{
				supplyStep = GuideSteps.note("Equip your carried " + names + ", then continue training.");
			}
			else if (obtain && missing.stream().noneMatch(SupplyTracker.Stock::canWithdraw) && !forceBank)
			{
				boolean exchange = !ironman && missing.stream().allMatch(this::canBuy);
				String instruction = "Obtain missing " + names + (ironman ? ". Follow the item sources below; Ironman accounts cannot buy these on the Grand Exchange."
					: exchange ? " at the Grand Exchange, or gather/make them. Follow the item sources below."
					: ". Follow the item sources below; quest and activity supplies may need to be recovered at their source.");
				supplyStep = new GuideStep(instruction + firstTrip, StepKind.NOTE, exchange ? Spots.GE : null, 0, 18,
					AdvanceWhen.MANUAL, HighlightMode.NEAR_TARGET, GuideStep.namesOf("Grand Exchange Clerk"), null);
			}
			else
			{
				GuideStep bank = method.steps().stream().filter(step -> step.kind() == StepKind.BANK).findFirst().orElse(null);
				String instruction = (supplies.bankKnown() ? "Withdraw " : "Open a bank to check for ") + names + ". "
					+ "Close the bank once ready.";
				WorldPoint destination = bank == null || bank.target() == null ? nearestBank() : bank.target();
				supplyStep = GuideSteps.bank(instruction + firstTrip, destination);
			}
		}
		if (controller.current().kind() != StepKind.BANK && supplyStep == null)
		{
			forceBank = false;
		}
		if (previous != null || supplyStep != null)
		{
			// Refresh only when the detour changes, rather than rescanning scene objects every tick.
			if (previous == null || supplyStep == null || !previous.instruction().equals(supplyStep.instruction()) || !java.util.Objects.equals(previous.target(), supplyStep.target()))
			{
				targets.clear();
				highlightsDirty = true;
			}
			else
			{
				supplyStep = previous;
			}
		}
	}

	private WorldPoint nearestBank()
	{
		WorldPoint player = playerLocation();
		if (player == null)
		{
			return Spots.GE;
		}
		List<WorldPoint> banks = new ArrayList<>(java.util.Arrays.asList(Spots.GE, Spots.LUMBRIDGE_BANK, Spots.DRAYNOR_BANK,
			Spots.VARROCK_EAST_BANK, Spots.VARROCK_WEST_BANK, Spots.AL_KHARID_BANK,
			Spots.EDGEVILLE_BANK, Spots.FALADOR_EAST_BANK, Spots.FALADOR_WEST_BANK));
		if (client.getWorldType().contains(WorldType.MEMBERS)) banks.addAll(java.util.Arrays.asList(
			Spots.SEERS_BANK, Spots.CATHERBY_BANK, Spots.ARDOUGNE_NORTH_BANK, Spots.ARDOUGNE_SOUTH_BANK,
			Spots.CASTLE_WARS, Spots.WINTERTODT_BANK));
		return banks.stream()
			.min(java.util.Comparator.comparingInt(point -> point.distanceTo2D(player))).orElse(Spots.GE);
	}

	private int emptySlots()
	{
		ItemContainer inventory = client.getItemContainer(InventoryID.INV);
		if (inventory == null)
		{
			return inventoryKnown() ? INVENTORY_SIZE : 0;
		}
		int occupied = 0;
		for (Item item : inventory.getItems())
		{
			if (item.getId() > 0 && item.getQuantity() > 0)
			{
				occupied++;
			}
		}
		return Math.max(0, INVENTORY_SIZE - occupied);
	}

	GuideStep currentStep()
	{
		return supplyStep == null ? controller.current() : supplyStep;
	}

	private boolean trainingEquipmentReady()
	{
		return !controller.running() || currentStep().kind() != StepKind.TRAIN || itemsReady(controller.method());
	}

	private boolean canBuy(SupplyTracker.Stock item)
	{
		for (int id : item.requirement.itemIds())
		{
			if (id == Integer.MAX_VALUE) continue;
			ItemComposition definition = itemManager.getItemComposition(id);
			if (definition != null && definition.isGeTradeable()) return true;
		}
		return false;
	}

	private String itemName(int id, String fallback)
	{
		ItemComposition item = id > 0 ? itemManager.getItemComposition(id) : null;
		return item == null || item.getName() == null ? fallback : item.getName();
	}

	private void updateHintArrow()
	{
		NPC npc = targets.closestNpc();
		WorldPoint point = controller.running() ? guidanceTarget() : null;
		hintArrows.update(enabled && config.showArrow() && config.showHintArrow() && controller.running() && trainingEquipmentReady(), npc, point);
	}

	WorldPoint guidanceTarget()
	{
		if (!controller.running() || !trainingEquipmentReady())
		{
			return null;
		}
		NPC npc = targets.closestNpc();
		if (npc != null)
		{
			return npc.getWorldLocation();
		}
		TileObject object = targets.closestObject();
		return object != null ? object.getWorldLocation() : currentStep().target() != null ? currentStep().target()
			: currentStep().kind() == StepKind.BANK ? nearestBank() : null;
	}

	boolean active()
	{
		return enabled;
	}

	private void updateMapPoint()
	{
		if (!enabled || !config.showWorldMap() || !controller.running() || client.getGameState() != GameState.LOGGED_IN)
		{
			removeMapPoint();
			return;
		}
		WorldPoint destination = guidanceTarget();
		if (destination == null)
		{
			removeMapPoint();
			return;
		}
		if (destination.equals(mapPointTarget) && controller.method().id().equals(mapPointMethod))
		{
			return;
		}
		removeMapPoint();
		mapPointTarget = destination;
		mapPointMethod = controller.method().id();
		mapPoint = new WorldMapPoint(destination, ICON);
		mapPoint.setTooltip(controller.method().name());
		mapPoint.setJumpOnClick(true);
		mapPoint.setName(controller.method().name());
		worldMapPointManager.add(mapPoint);
	}

	private void removeMapPoint()
	{
		if (mapPoint != null)
		{
			worldMapPointManager.remove(mapPoint);
			mapPoint = null;
		}
		mapPointTarget = null;
		mapPointMethod = null;
	}

	private void chat(String message)
	{
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Leveling Helper: " + message, null);
	}

	private static int clampGoal(int value)
	{
		return Math.max(2, Math.min(99, value));
	}

	private static Skill skillByName(String name)
	{
		for (Skill candidate : Skill.values())
		{
			if (candidate.getName().equalsIgnoreCase(name))
			{
				if (candidate != Skill.OVERALL)
				{
					return candidate;
				}
			}
		}
		return Skill.WOODCUTTING;
	}

	private static BufferedImage icon()
	{
		BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		java.awt.Graphics2D graphics = image.createGraphics();
		graphics.setColor(new java.awt.Color(70, 180, 110));
		graphics.fillRoundRect(0, 0, 16, 16, 4, 4);
		graphics.setColor(java.awt.Color.WHITE);
		graphics.fillRect(3, 9, 2, 4);
		graphics.fillRect(7, 6, 2, 7);
		graphics.fillRect(11, 3, 2, 10);
		graphics.dispose();
		return image;
	}
}
