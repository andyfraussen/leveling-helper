package com.levelinghelper.model;

public enum AccountMode
{
	F2P("Free-to-play"),
	P2P("Members");

	private final String label;

	AccountMode(String label)
	{
		this.label = label;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
