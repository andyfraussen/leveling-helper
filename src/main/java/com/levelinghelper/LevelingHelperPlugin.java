package com.levelinghelper;

import com.google.inject.Provides;
import com.levelinghelper.data.TrainingCatalog;
import com.levelinghelper.guide.GuideController;
import com.levelinghelper.guide.GuideSnapshot;
import com.levelinghelper.guide.GuideTick;
import com.levelinghelper.guide.LevelLookup;
import com.levelinghelper.guide.MethodSelector;
import com.levelinghelper.guide.TrainingMath;
import com.levelinghelper.guide.XpSession;
import com.levelinghelper.model.AccountMode;
import com.levelinghelper.model.GuideStep;
import com.levelinghelper.model.HighlightMode;
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
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.NPC;
import net.runelite.api.ObjectComposition;
import net.runelite.api.Player;
import net.runelite.api.Scene;
import net.runelite.api.Skill;
import net.runelite.api.Tile;
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

	private LevelingHelperPanel panel;
	private NavigationButton navigationButton;
	private final GuideController controller = new GuideController();
	private final XpSession session = new XpSession();
	private final List<NPC> highlightedNpcs = new ArrayList<NPC>();
	private final List<TileObject> highlightedObjects = new ArrayList<TileObject>();

	private AccountMode mode = AccountMode.F2P;
	private TrainingStyle style = TrainingStyle.BALANCED;
	private Skill skill = Skill.WOODCUTTING;
	private int goal = 99;
	private String pinnedId;
	private String announcedMethodId;
	private boolean highlightsDirty = true;
	private boolean goalAnnounced;
	private boolean guidePaused;
	private boolean hintOwned;
	private WorldMapPoint mapPoint;

	@Provides
	LevelingHelperConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(LevelingHelperConfig.class);
	}

	@Override
	protected void startUp()
	{
		mode = config.accountMode();
		style = config.trainingStyle();
		goal = clampGoal(config.goalLevel());
		skill = skillByName(config.skillName());
		panel = new LevelingHelperPanel(this);
		panel.load(mode, style, skill, goal);
		navigationButton = NavigationButton.builder()
			.tooltip("Leveling Helper")
			.icon(icon())
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
		clearHighlights();
		removeMapPoint();
		clearOwnedHint();
		controller.stop();
		session.stop();
		panel = null;
		navigationButton = null;
		log.debug("Leveling Helper stopped");
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		if (!guidePaused && client.getGameState() == GameState.LOGGED_IN)
		{
			ensureGuiding();
		}
		if (controller.running())
		{
			GuideStep step = controller.current();
			boolean changed = controller.tick(new GuideTick(
				playerLocation(),
				bankOpen(),
				inventoryFull(),
				watchedItemGone(step),
				highlightVisible(step)), config.autoAdvance(), config.arrivalDistance());
			if (changed)
			{
				highlightsDirty = true;
			}
			maybeAdvanceLevel();
		}
		if (highlightsDirty)
		{
			rebuildHighlights();
			highlightsDirty = false;
		}
		updateHintArrow();
		updateMapPoint();
		publish();
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		session.onXp(event.getSkill(), event.getXp());
		if (controller.running() && event.getSkill() == skill)
		{
			maybeAdvanceLevel();
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOADING || event.getGameState() == GameState.LOGIN_SCREEN)
		{
			clearHighlights();
			highlightsDirty = true;
		}
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			goalAnnounced = false;
			clientThread.invokeLater(this::publish);
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!LevelingHelperConfig.GROUP.equals(event.getGroup()))
		{
			return;
		}
		highlightsDirty = true;
		updateMapPoint();
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		considerNpc(event.getNpc());
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		highlightedNpcs.remove(event.getNpc());
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		considerObject(event.getGameObject());
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event)
	{
		highlightedObjects.remove(event.getGameObject());
	}

	@Subscribe
	public void onWallObjectSpawned(WallObjectSpawned event)
	{
		considerObject(event.getWallObject());
	}

	@Subscribe
	public void onWallObjectDespawned(WallObjectDespawned event)
	{
		highlightedObjects.remove(event.getWallObject());
	}

	@Subscribe
	public void onGroundObjectSpawned(GroundObjectSpawned event)
	{
		considerObject(event.getGroundObject());
	}

	@Subscribe
	public void onGroundObjectDespawned(GroundObjectDespawned event)
	{
		highlightedObjects.remove(event.getGroundObject());
	}

	@Subscribe
	public void onDecorativeObjectSpawned(DecorativeObjectSpawned event)
	{
		considerObject(event.getDecorativeObject());
	}

	@Subscribe
	public void onDecorativeObjectDespawned(DecorativeObjectDespawned event)
	{
		highlightedObjects.remove(event.getDecorativeObject());
	}

	public void setAccountMode(AccountMode value)
	{
		mode = value;
		pinnedId = null;
		configManager.setConfiguration(LevelingHelperConfig.GROUP, "accountMode", value);
		clientThread.invoke(this::publish);
	}

	public void setTrainingStyle(TrainingStyle value)
	{
		style = value;
		pinnedId = null;
		guidePaused = false;
		configManager.setConfiguration(LevelingHelperConfig.GROUP, "trainingStyle", value);
		refreshRunningMethod(false);
		clientThread.invoke(this::publish);
	}

	public void setGoal(int value)
	{
		goal = clampGoal(value);
		goalAnnounced = false;
		configManager.setConfiguration(LevelingHelperConfig.GROUP, "goalLevel", goal);
		clientThread.invoke(this::publish);
	}

	public void setSkill(Skill value)
	{
		if (value == null || value == skill)
		{
			return;
		}
		skill = value;
		pinnedId = null;
		guidePaused = false;
		configManager.setConfiguration(LevelingHelperConfig.GROUP, "skillName", value.getName());
		clientThread.invoke(() ->
		{
			ensureGuiding();
			publish();
		});
	}

	public void pinMethod(String id)
	{
		pinnedId = id;
		refreshRunningMethod(true);
		clientThread.invoke(this::publish);
	}

	public void startGuide()
	{
		guidePaused = false;
		clientThread.invoke(() ->
		{
			announcedMethodId = null;
			ensureGuiding();
			publish();
		});
	}

	public void stopGuide()
	{
		guidePaused = true;
		clientThread.invoke(() ->
		{
			controller.stop();
			session.stop();
			announcedMethodId = null;
			clearHighlights();
			removeMapPoint();
			clearOwnedHint();
			publish();
		});
	}

	public void nextStep()
	{
		clientThread.invoke(() ->
		{
			controller.advance();
			highlightsDirty = true;
			publish();
		});
	}

	public void previousStep()
	{
		clientThread.invoke(() ->
		{
			controller.back();
			highlightsDirty = true;
			publish();
		});
	}

	public void bankStep()
	{
		clientThread.invoke(() ->
		{
			controller.jumpToBank();
			highlightsDirty = true;
			publish();
		});
	}

	public void detectMembership()
	{
		clientThread.invoke(() ->
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
			if (panel != null)
			{
				panel.selectMode(detected);
			}
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
		return highlightedNpcs;
	}

	List<TileObject> highlightedObjects()
	{
		return highlightedObjects;
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
			status);
		final LevelingHelperPanel target = panel;
		SwingUtilities.invokeLater(() ->
		{
			if (target != null)
			{
				target.apply(snapshot);
			}
		});
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
			return "No route for this skill at level " + level + ".";
		}
		if (mode == AccountMode.P2P && !client.getWorldType().contains(WorldType.MEMBERS))
		{
			return "You are on a free-to-play world. Hop before you follow a members route.";
		}
		return "This guides you. It never clicks, banks, or walks for you.";
	}

	private void maybeAdvanceLevel()
	{
		if (!controller.running() || client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}
		int level = client.getRealSkillLevel(skill);
		if (level >= goal)
		{
			if (!goalAnnounced)
			{
				goalAnnounced = true;
				chat("Goal reached: level " + goal + " " + skill.getName() + ".");
			}
			return;
		}
		TrainingMethod current = controller.method();
		if (current != null && current.containsLevel(level) && current.requirementsMet(liveLookup()))
		{
			return;
		}
		TrainingMethod next = chooseMethod();
		if (next == null)
		{
			return;
		}
		if (current != null && current.id().equals(next.id()))
		{
			return;
		}
		controller.start(next);
		highlightsDirty = true;
		if (!next.id().equals(announcedMethodId))
		{
			announcedMethodId = next.id();
			chat("Next method: " + next.name() + ".");
		}
	}

	private void refreshRunningMethod(boolean force)
	{
		clientThread.invoke(() ->
		{
			if (!controller.running())
			{
				return;
			}
			TrainingMethod next = chooseMethod();
			if (next == null)
			{
				return;
			}
			if (!force && controller.method() != null && controller.method().id().equals(next.id()))
			{
				return;
			}
			controller.start(next);
			announcedMethodId = next.id();
			highlightsDirty = true;
			publish();
		});
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

	private void rebuildHighlights()
	{
		highlightedNpcs.clear();
		highlightedObjects.clear();
		GuideStep step = controller.current();
		if (step == null || step.names().isEmpty() || client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}
		for (NPC npc : client.getNpcs())
		{
			considerNpc(npc);
		}
		Scene scene = client.getScene();
		if (scene == null)
		{
			return;
		}
		Tile[][][] tiles = scene.getTiles();
		int plane = client.getPlane();
		if (tiles == null || plane < 0 || plane >= tiles.length)
		{
			return;
		}
		for (Tile[] row : tiles[plane])
		{
			if (row == null)
			{
				continue;
			}
			for (Tile tile : row)
			{
				if (tile == null)
				{
					continue;
				}
				GameObject[] gameObjects = tile.getGameObjects();
				if (gameObjects != null)
				{
					for (GameObject gameObject : gameObjects)
					{
						considerObject(gameObject);
					}
				}
				considerObject(tile.getWallObject());
				considerObject(tile.getGroundObject());
				considerObject(tile.getDecorativeObject());
			}
		}
	}

	private void considerNpc(NPC npc)
	{
		GuideStep step = controller.current();
		if (npc == null || step == null || npc.getName() == null)
		{
			return;
		}
		if (npc.getWorldLocation() != null && npc.getWorldLocation().getPlane() != client.getPlane())
		{
			return;
		}
		if (!matches(npc.getName(), step) || !inRange(npc.getWorldLocation(), step))
		{
			return;
		}
		if (!highlightedNpcs.contains(npc))
		{
			highlightedNpcs.add(npc);
		}
	}

	private void considerObject(TileObject object)
	{
		GuideStep step = controller.current();
		if (object == null || step == null || object.getId() == -1)
		{
			return;
		}
		ObjectComposition composition = client.getObjectDefinition(object.getId());
		if (composition == null)
		{
			return;
		}
		if (composition.getImpostorIds() != null)
		{
			ObjectComposition impostor = composition.getImpostor();
			if (impostor != null)
			{
				composition = impostor;
			}
		}
		if (!matches(composition.getName(), step) || !inRange(object.getWorldLocation(), step))
		{
			return;
		}
		if (!highlightedObjects.contains(object))
		{
			highlightedObjects.add(object);
		}
	}

	private boolean matches(String name, GuideStep step)
	{
		String cleaned = clean(name);
		return !cleaned.isEmpty() && !"null".equals(cleaned) && step.names().contains(cleaned);
	}

	private boolean inRange(WorldPoint location, GuideStep step)
	{
		if (step.highlightMode() == HighlightMode.ANYWHERE)
		{
			return true;
		}
		if (location == null)
		{
			return false;
		}
		WorldPoint origin = step.highlightMode() == HighlightMode.NEAR_PLAYER ? playerLocation() : step.target();
		if (origin == null || origin.getPlane() != location.getPlane())
		{
			return false;
		}
		int dx = Math.abs(origin.getX() - location.getX());
		int dy = Math.abs(origin.getY() - location.getY());
		return Math.max(dx, dy) <= step.highlightRadius();
	}

	private boolean highlightVisible(GuideStep step)
	{
		if (step == null || step.names().isEmpty())
		{
			return false;
		}
		for (NPC npc : client.getNpcs())
		{
			if (npc.getName() != null && matches(npc.getName(), step))
			{
				return true;
			}
		}
		return false;
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
		TrainingMethod method = chooseMethod();
		if (method == null)
		{
			return;
		}
		if (controller.running() && controller.method() != null && controller.method().id().equals(method.id()))
		{
			return;
		}
		controller.start(method);
		if (!session.active() || session.skill() != skill)
		{
			session.start(skill, client.getSkillExperience(skill));
		}
		highlightsDirty = true;
		if (!method.id().equals(announcedMethodId))
		{
			announcedMethodId = method.id();
			GuideStep step = controller.current();
			String where = step == null || step.target() == null
				? method.name()
				: method.name() + ". Follow the yellow arrow.";
			chat(where);
		}
	}

	private void updateHintArrow()
	{
		if (!controller.running() || client.getGameState() != GameState.LOGGED_IN)
		{
			clearOwnedHint();
			return;
		}
		GuideStep step = controller.current();
		if (step == null)
		{
			clearOwnedHint();
			return;
		}
		NPC npc = closestNpc();
		if (npc != null)
		{
			client.setHintArrow(npc);
			hintOwned = true;
			return;
		}
		TileObject object = closestObject();
		if (object != null && object.getWorldLocation() != null)
		{
			client.setHintArrow(object.getWorldLocation());
			hintOwned = true;
			return;
		}
		if (step.target() != null)
		{
			client.setHintArrow(step.target());
			hintOwned = true;
			return;
		}
		clearOwnedHint();
	}

	private NPC closestNpc()
	{
		WorldPoint player = playerLocation();
		NPC closest = null;
		int best = Integer.MAX_VALUE;
		for (NPC npc : highlightedNpcs)
		{
			if (npc.getWorldLocation() == null || player == null)
			{
				continue;
			}
			int distance = player.distanceTo(npc.getWorldLocation());
			if (distance < best)
			{
				best = distance;
				closest = npc;
			}
		}
		return closest;
	}

	private TileObject closestObject()
	{
		WorldPoint player = playerLocation();
		TileObject closest = null;
		int best = Integer.MAX_VALUE;
		for (TileObject object : highlightedObjects)
		{
			if (object.getWorldLocation() == null || player == null)
			{
				continue;
			}
			int distance = player.distanceTo(object.getWorldLocation());
			if (distance < best)
			{
				best = distance;
				closest = object;
			}
		}
		return closest;
	}

	private void clearOwnedHint()
	{
		if (!hintOwned || client.getGameState() != GameState.LOGGED_IN)
		{
			hintOwned = false;
			return;
		}
		client.clearHintArrow();
		hintOwned = false;
	}

	private void updateMapPoint()
	{
		removeMapPoint();
		if (!config.showWorldMap() || !controller.running())
		{
			return;
		}
		GuideStep step = controller.current();
		if (step == null || step.target() == null)
		{
			return;
		}
		mapPoint = new WorldMapPoint(step.target(), icon());
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
	}

	private void clearHighlights()
	{
		highlightedNpcs.clear();
		highlightedObjects.clear();
	}

	private void chat(String message)
	{
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Leveling Helper: " + message, null);
	}

	private static String clean(String name)
	{
		if (name == null)
		{
			return "";
		}
		return name.replaceAll("<[^>]*>", "").trim().toLowerCase();
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
				return candidate;
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
