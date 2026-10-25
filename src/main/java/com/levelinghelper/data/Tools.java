package com.levelinghelper.data;

import com.levelinghelper.model.ItemRequirement;
import com.levelinghelper.model.ItemRequirement.Alternative;
import com.levelinghelper.model.SkillReq;
import java.util.ArrayList;
import java.util.List;
import net.runelite.api.ItemID;
import net.runelite.api.Quest;
import net.runelite.api.Skill;

final class Tools
{
	private Tools() { }

	static ItemRequirement pickaxe()
	{
		List<Alternative> choices = new ArrayList<>();
		add(choices, Skill.MINING, 71, true, Quest.SONG_OF_THE_ELVES, ItemID.CRYSTAL_PICKAXE);
		add(choices, Skill.MINING, 61, true, null, ItemID.DRAGON_PICKAXE, ItemID.DRAGON_PICKAXE_12797, ItemID.DRAGON_PICKAXE_OR,
			ItemID.DRAGON_PICKAXE_OR_25376, ItemID.DRAGON_PICKAXE_OR_30351, ItemID._3RD_AGE_PICKAXE, ItemID.INFERNAL_PICKAXE,
			ItemID.INFERNAL_PICKAXE_UNCHARGED, ItemID.INFERNAL_PICKAXE_OR, ItemID.INFERNAL_PICKAXE_OR_30345, ItemID.INFERNAL_PICKAXE_UNCHARGED_25369,
			ItemID.INFERNAL_PICKAXE_UNCHARGED_30346);
		add(choices, Skill.MINING, 41, false, null, ItemID.RUNE_PICKAXE, ItemID.GILDED_PICKAXE);
		add(choices, Skill.MINING, 31, false, null, ItemID.ADAMANT_PICKAXE);
		add(choices, Skill.MINING, 21, false, null, ItemID.MITHRIL_PICKAXE);
		add(choices, Skill.MINING, 11, false, null, ItemID.BLACK_PICKAXE);
		add(choices, Skill.MINING, 6, false, null, ItemID.STEEL_PICKAXE);
		add(choices, Skill.MINING, 1, false, null, ItemID.IRON_PICKAXE, ItemID.BRONZE_PICKAXE);
		return ItemRequirement.tool("Usable pickaxe", "Buy a bronze pickaxe from Bob in Lumbridge, or a stronger pickaxe from Nurmof in the Dwarven Mine.", choices.toArray(new Alternative[0]));
	}

	static ItemRequirement axe()
	{
		List<Alternative> choices = new ArrayList<>();
		add(choices, Skill.WOODCUTTING, 71, true, Quest.SONG_OF_THE_ELVES, ItemID.CRYSTAL_AXE, ItemID.CRYSTAL_FELLING_AXE);
		add(choices, Skill.WOODCUTTING, 61, true, null, ItemID.DRAGON_AXE, ItemID.DRAGON_AXE_OR, ItemID.DRAGON_AXE_OR_30352,
			ItemID.DRAGON_FELLING_AXE, ItemID._3RD_AGE_AXE, ItemID._3RD_AGE_FELLING_AXE, ItemID.INFERNAL_AXE, ItemID.INFERNAL_AXE_UNCHARGED,
			ItemID.INFERNAL_AXE_OR, ItemID.INFERNAL_AXE_OR_30347, ItemID.INFERNAL_AXE_UNCHARGED_25371, ItemID.INFERNAL_AXE_UNCHARGED_30348);
		add(choices, Skill.WOODCUTTING, 41, false, null, ItemID.RUNE_AXE, ItemID.GILDED_AXE);
		add(choices, Skill.WOODCUTTING, 31, false, null, ItemID.ADAMANT_AXE);
		add(choices, Skill.WOODCUTTING, 21, false, null, ItemID.MITHRIL_AXE);
		add(choices, Skill.WOODCUTTING, 11, false, null, ItemID.BLACK_AXE);
		add(choices, Skill.WOODCUTTING, 6, false, null, ItemID.STEEL_AXE);
		add(choices, Skill.WOODCUTTING, 1, false, null, ItemID.IRON_AXE, ItemID.BRONZE_AXE);
		add(choices, Skill.WOODCUTTING, 1, true, null, ItemID.BRONZE_FELLING_AXE);
		add(choices, Skill.WOODCUTTING, 1, true, null, ItemID.IRON_FELLING_AXE);
		add(choices, Skill.WOODCUTTING, 6, true, null, ItemID.STEEL_FELLING_AXE);
		add(choices, Skill.WOODCUTTING, 11, true, null, ItemID.BLACK_FELLING_AXE);
		add(choices, Skill.WOODCUTTING, 21, true, null, ItemID.MITHRIL_FELLING_AXE);
		add(choices, Skill.WOODCUTTING, 31, true, null, ItemID.ADAMANT_FELLING_AXE);
		add(choices, Skill.WOODCUTTING, 41, true, null, ItemID.RUNE_FELLING_AXE);
		return ItemRequirement.tool("Usable axe", "Buy a bronze or iron axe from Bob's Brilliant Axes in Lumbridge, or smith an axe you can use.", choices.toArray(new Alternative[0]));
	}

	private static void add(List<Alternative> choices, Skill skill, int level, boolean members, Quest quest, int... ids)
	{
		for (int id : ids)
		{
			choices.add(new Alternative(id, members, quest, new SkillReq(skill, level)));
		}
	}
}
