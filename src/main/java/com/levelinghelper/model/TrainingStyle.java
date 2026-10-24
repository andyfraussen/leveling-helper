package com.levelinghelper.model;

public enum TrainingStyle
{
	BALANCED("Balanced"),
	FAST("Fastest XP"),
	AFK("More AFK"),
	PROFIT("Better profit");

	private final String label;

	TrainingStyle(String label)
	{
		this.label = label;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
