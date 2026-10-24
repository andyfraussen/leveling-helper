package com.levelinghelper.model;

public enum RouteRisk
{
	SAFE("Ordinary training"),
	DANGEROUS("Combat or dangerous activity: bring suitable protection"),
	WILDERNESS("Wilderness: other players can kill you and take your items");

	private final String description;

	RouteRisk(String description)
	{
		this.description = description;
	}

	public String description()
	{
		return description;
	}
}
