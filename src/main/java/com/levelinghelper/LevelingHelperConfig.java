package com.levelinghelper;

import com.levelinghelper.model.AccountMode;
import com.levelinghelper.model.TrainingStyle;
import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup(LevelingHelperConfig.GROUP)
public interface LevelingHelperConfig extends Config
{
	String GROUP = "levelinghelper";

	@ConfigItem(
		keyName = "showOverlay",
		name = "Show guide box",
		description = "Show the current step, distance, and XP rates on the game view.",
		position = 1
	)
	default boolean showOverlay()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showTiles",
		name = "Mark the destination",
		description = "Highlight the tile you are walking to.",
		position = 2
	)
	default boolean showTiles()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showObjects",
		name = "Highlight targets",
		description = "Highlight the NPCs and objects for the current step.",
		position = 3
	)
	default boolean showObjects()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showArrow",
		name = "Direction arrow",
		description = "Point toward the destination when it is off screen.",
		position = 4
	)
	default boolean showArrow()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showHintArrow",
		name = "Native hint arrow",
		description = "Also use the game's shared yellow arrow when no other plugin or server arrow is active.",
		position = 9
	)
	default boolean showHintArrow()
	{
		return false;
	}

	@ConfigItem(
		keyName = "allowWilderness",
		name = "Include Wilderness routes",
		description = "Allow routes where other players can kill you and take your items. Disabled by default.",
		position = 10
	)
	default boolean allowWilderness()
	{
		return false;
	}

	@ConfigItem(
		keyName = "showWorldMap",
		name = "World map marker",
		description = "Mark the destination on the world map.",
		position = 5
	)
	default boolean showWorldMap()
	{
		return true;
	}

	@ConfigItem(
		keyName = "autoAdvance",
		name = "Auto-advance steps",
		description = "Move to the next step when you arrive, finish at the bank, or fill your inventory.",
		position = 6
	)
	default boolean autoAdvance()
	{
		return true;
	}

	@Range(min = 3, max = 40)
	@ConfigItem(
		keyName = "arrivalDistance",
		name = "Arrival distance",
		description = "Maximum distance in tiles that counts as arriving. Routes may require a tighter radius.",
		position = 7
	)
	default int arrivalDistance()
	{
		return 10;
	}

	@ConfigItem(
		keyName = "highlightColor",
		name = "Highlight color",
		description = "Color used for the destination and the targets.",
		position = 8
	)
	default Color highlightColor()
	{
		return new Color(80, 210, 140);
	}

	@ConfigItem(
		keyName = "accountMode",
		name = "Account mode",
		description = "Saved free-to-play or members choice.",
		hidden = true
	)
	default AccountMode accountMode()
	{
		return AccountMode.F2P;
	}

	@ConfigItem(
		keyName = "trainingStyle",
		name = "Training style",
		description = "Saved training style.",
		hidden = true
	)
	default TrainingStyle trainingStyle()
	{
		return TrainingStyle.BALANCED;
	}

	@ConfigItem(
		keyName = "goalLevel",
		name = "Goal level",
		description = "Saved goal level.",
		hidden = true
	)
	default int goalLevel()
	{
		return 99;
	}

	@ConfigItem(
		keyName = "skillName",
		name = "Skill",
		description = "Saved skill.",
		hidden = true
	)
	default String skillName()
	{
		return "Woodcutting";
	}
}
