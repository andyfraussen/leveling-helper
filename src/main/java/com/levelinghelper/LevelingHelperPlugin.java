package com.levelinghelper;

import com.google.inject.Provides;
import com.levelinghelper.data.TrainingCatalog;
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
	@Inject
	private SessionLifecycle session;

	@Inject
	private TargetHighlighter targets;

	@Inject
	private HintArrowController hintArrows;

	private AccountMode mode = AccountMode.F2P;
	private TrainingStyle style = TrainingStyle.BALANCED;
	private Skill skill = Skill.WOODCUTTING;
	private int goal = 99;
	private String pinnedId;
	private String announcedMethodId;
	private boolean highlightsDirty = true;
	private boolean goalAnnounced;
	private boolean guidePaused = true;
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
			mode = config.accountMode();
			style = config.trainingStyle();
			goal = clampGoal(config.goalLevel());
			skill = skillByName(config.skillName());
			guidePaused = true;
			goalAnnounced = false;
			pinnedId = null;
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
		highlightsDirty = true;
		log.debug("Leveling Helper started");
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		clientToolbar.removeNavigation(navigationButton);
		enabled = false;
		long stoppedLifecycle = ++lifecycle;
		clientThread.invoke(() ->
		{
			if (lifecycle == stoppedLifecycle)
			{
				guidePaused = true;
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
		if (!guidePaused)
		{
			ensureGuiding();
		}
		targets.refresh(trainingEquipmentReady() ? controller.current() : null, highlightsDirty);
		highlightsDirty = false;
		if (controller.running() && trainingEquipmentReady())
		{
			boolean changed = controller.tick(guideTick(), config.autoAdvance(), config.arrivalDistance());
			if (changed)
			{
				primeStep();
				targets.refresh(trainingEquipmentReady() ? controller.current() : null, true);
			}
		}
		updateHintArrow();
		updateMapPoint();
		publish();
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		if (!enabled)
		{
			return;
		}
		session.onXp(event.getSkill(), event.getXp());
		if (!guidePaused && client.getGameState() == GameState.LOGGED_IN)
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
			session.stop();
			targets.clear();
			hintArrows.clear();
			removeMapPoint();
			highlightsDirty = true;
		}
		if (event.getGameState() == GameState.LOGIN_SCREEN)
		{
			stopCurrentGuide();
			guidePaused = true;
			goalAnnounced = false;
		}
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			onClient(this::publish);
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
			if (!guidePaused)
			{
				ensureGuiding();
			}
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
			mode = value;
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
			if (value == null || value == skill)
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
		if (!guidePaused)
		{
			ensureGuiding();
		}
		publish();
	}

	public void startGuide()
	{
		onClient(() ->
		{
			guidePaused = false;
			announcedMethodId = null;
			ensureGuiding();
			publish();
		});
	}

	public void stopGuide()
	{
		onClient(() ->
		{
			guidePaused = true;
			stopCurrentGuide();
			publish();
		});
	}

	private void stopCurrentGuide()
	{
		controller.stop();
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
			controller.advance();
			primeStep();
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
			controller.jumpToBank();
			primeStep();
			highlightsDirty = true;
			publish();
		});
	}

	public void detectMembership()
	{
		onClient(() ->
		{
			if (client.getGameState() != GameState.LOGGED_IN)
			{
				chat("Log in, then use Match account.");
				return;
			}
			boolean member = client.getVarpValue(VarPlayer.MEMBERSHIP_DAYS) > 0;
			AccountMode detected = member ? AccountMode.P2P : AccountMode.F2P;
			mode = detected;
			pinnedId = null;
			configManager.setConfiguration(LevelingHelperConfig.GROUP, "accountMode", detected);
			selectionChanged();
			chat(member ? "Members account. Showing members routes." : "Free-to-play account. Showing free-to-play routes.");
			publish();
		});
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
		GuideStep step = controller.running() ? controller.current() : null;
		if (step != null && step.target() != null && loggedIn)
		{
			distance = TrainingMath.direction(playerLocation(), step.target());
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
			MethodSelector.alternatives(TrainingCatalog.all(), skill, mode, level, lookup),
			status, preparation(method, loggedIn), lockedRoutes(lookup, level));
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
		details.addAll(method.preparation());
		if (loggedIn)
		{
			details.addAll(missingItems(method));
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
		if (mode == AccountMode.P2P && !client.getWorldType().contains(WorldType.MEMBERS))
		{
			return "You are on a free-to-play world. Hop before you follow a members route.";
		}
		List<String> missing = missingItems(method);
		if (!missing.isEmpty())
		{
			return "Preparation needed: " + String.join("; ", missing) + ". Bank auto-advance waits for these items.";
		}
		return "This guides you. It never clicks, banks, or walks for you.";
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
				return pinned;
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
			return false;
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
		if (client.getGameState() != GameState.LOGGED_IN)
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
			guidePaused = true;
			stopCurrentGuide();
			return;
		}
		TrainingMethod method = chooseMethod();
		if (method == null)
		{
			guidePaused = true;
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
		controller.start(method);
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
			watchedItemGone(controller.current()), targets.visible(), missingItems(controller.method()).isEmpty(),
			client.getItemContainer(InventoryID.INV) != null);
	}

	private void primeStep()
	{
		if (controller.running())
		{
			controller.observe(guideTick());
		}
	}

	private List<String> missingItems(TrainingMethod method)
	{
		List<String> missing = new ArrayList<>();
		if (method == null)
		{
			return missing;
		}
		for (ItemRequirement requirement : method.items())
		{
			ItemContainer inventory = requirement.countsInventory() ? client.getItemContainer(InventoryID.INV) : null;
			ItemContainer equipment = requirement.countsEquipment() ? client.getItemContainer(InventoryID.WORN) : null;
			if (!requirement.met(id -> (inventory == null ? 0 : inventory.count(id)) + (equipment == null ? 0 : equipment.count(id))))
			{
				missing.add((requirement.equipped() ? "Equip: " : "Bring: ") + requirement.label());
			}
		}
		return missing;
	}

	private boolean trainingEquipmentReady()
	{
		if (!controller.running() || controller.current().kind() != StepKind.TRAIN)
		{
			return true;
		}
		for (ItemRequirement requirement : controller.method().items())
		{
			if (!requirement.equipped())
			{
				continue;
			}
			ItemContainer equipment = client.getItemContainer(InventoryID.WORN);
			if (!requirement.met(id -> equipment == null ? 0 : equipment.count(id)))
			{
				return false;
			}
		}
		return true;
	}

	private void updateHintArrow()
	{
		GuideStep step = controller.current();
		NPC npc = targets.closestNpc();
		TileObject object = targets.closestObject();
		WorldPoint point = object != null ? object.getWorldLocation() : step == null ? null : step.target();
		hintArrows.update(enabled && config.showArrow() && config.showHintArrow() && controller.running() && trainingEquipmentReady(), npc, point);
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
		GuideStep step = controller.current();
		if (step == null || step.target() == null)
		{
			removeMapPoint();
			return;
		}
		if (step.target().equals(mapPointTarget) && controller.method().id().equals(mapPointMethod))
		{
			return;
		}
		removeMapPoint();
		mapPointTarget = step.target();
		mapPointMethod = controller.method().id();
		mapPoint = new WorldMapPoint(step.target(), ICON);
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
