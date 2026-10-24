package com.levelinghelper.data;

import com.levelinghelper.model.TrainingMethod;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TrainingCatalog
{
	private static final List<TrainingMethod> ALL = build();

	private TrainingCatalog()
	{
	}

	public static List<TrainingMethod> all()
	{
		return ALL;
	}

	public static TrainingMethod byId(String id)
	{
		if (id == null)
		{
			return null;
		}
		for (TrainingMethod method : ALL)
		{
			if (method.id().equals(id))
			{
				return method;
			}
		}
		return null;
	}

	private static List<TrainingMethod> build()
	{
		List<TrainingMethod> all = new ArrayList<TrainingMethod>();
		CombatMethods.register(all);
		GatheringMethods.register(all);
		ProductionMethods.register(all);
		SupportMethods.register(all);
		return Collections.unmodifiableList(all);
	}
}
