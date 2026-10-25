package com.levelinghelper;

import com.levelinghelper.data.TrainingCatalog;
import com.levelinghelper.guide.GuideSnapshot;
import com.levelinghelper.model.AccountMode;
import com.levelinghelper.model.TrainingMethod;
import com.levelinghelper.model.TrainingStyle;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import net.runelite.api.Skill;
import net.runelite.client.ui.laf.RuneLiteLAF;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PanelUiTest
{
	@Test
	public void freeModeHidesMemberSkillsAndMemberModeRestoresThem() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			LevelingHelperPlugin plugin = mock(LevelingHelperPlugin.class);
			LevelingHelperPanel panel = new LevelingHelperPanel(plugin);
			for (AccountMode mode : new AccountMode[]{AccountMode.F2P, AccountMode.P2P, AccountMode.F2P})
			{
				panel.load(mode, TrainingStyle.BALANCED, Skill.MINING, 99);
				List<String> skills = new ArrayList<>();
				for (Component component : descendants(panel))
				{
					if (component instanceof JToggleButton)
					{
						skills.add(((JToggleButton) component).getText());
					}
				}
				for (Skill skill : Skill.values())
				{
					assertEquals(skill.getName(), skill != Skill.OVERALL && (mode == AccountMode.P2P || !skill.isMembers()),
						skills.contains(skill.getName()));
				}
			}
			verifyNoInteractions(plugin);
		});
	}

	@Test
	public void stepControlsSitAfterTheChecklistAndOnlyAdvanceTheGuide() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			LevelingHelperPlugin plugin = mock(LevelingHelperPlugin.class);
			LevelingHelperPanel panel = new LevelingHelperPanel(plugin);
			GuideSnapshot snapshot = snapshot(AccountMode.F2P);
			panel.apply(snapshot);
			List<Component> components = descendants(panel);
			List<String> buttons = new ArrayList<>();
			int lastStep = -1;
			int previous = -1;
			for (int i = 0; i < components.size(); i++)
			{
				Component component = components.get(i);
				if (component instanceof JLabel && ((JLabel) component).getText().contains(
					snapshot.method().steps().get(snapshot.method().steps().size() - 1).instruction()))
				{
					lastStep = i;
				}
				if (component instanceof AbstractButton && !(component instanceof JToggleButton))
				{
					AbstractButton button = (AbstractButton) component;
					buttons.add(button.getText());
					if (button.getText().equals("Previous")) previous = i;
					if (List.of("Previous", "Next", "Bank").contains(button.getText())) button.doClick();
				}
			}
			assertTrue(lastStep >= 0 && previous > lastStep);
			assertTrue(buttons.containsAll(List.of("Previous", "Next", "Bank")));
			assertFalse(buttons.contains("Start guiding"));
			assertFalse(buttons.contains("Stop guiding"));
			assertFalse(buttons.contains("Match account"));
			verify(plugin).previousStep();
			verify(plugin).nextStep();
			verify(plugin).bankStep();
		});
	}

	@Test
	public void renderBothAccountModesForVisualReview() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			try
			{
				RuneLiteLAF.setup();
				BufferedImage image = new BufferedImage(520, 1500, BufferedImage.TYPE_INT_RGB);
				Graphics2D graphics = image.createGraphics();
				graphics.setColor(new Color(35, 35, 35));
				graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
				int x = 10;
				for (AccountMode mode : AccountMode.values())
				{
					LevelingHelperPanel panel = new LevelingHelperPanel(mock(LevelingHelperPlugin.class));
					panel.apply(snapshot(mode));
					panel.setSize(240, 1450);
					layout(panel);
					Graphics2D column = (Graphics2D) graphics.create(x, 25, 240, 1450);
					panel.printAll(column);
					column.dispose();
					graphics.setColor(Color.WHITE);
					graphics.drawString(mode.toString(), x, 17);
					x += 260;
				}
				graphics.dispose();
				File output = new File("build/reports/sidebar-ui.png");
				output.getParentFile().mkdirs();
				ImageIO.write(image, "png", output);
			}
			catch (Exception exception)
			{
				throw new RuntimeException(exception);
			}
		});
	}

	@Test
	public void renderSupplyReadinessForVisualReview() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			try
			{
				RuneLiteLAF.setup();
				BufferedImage image = new BufferedImage(520, 1900, BufferedImage.TYPE_INT_RGB);
				Graphics2D graphics = image.createGraphics();
				graphics.setColor(new Color(35, 35, 35));
				graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
				TrainingMethod iron = snapshot(AccountMode.F2P).method();
				TrainingMethod darts = TrainingCatalog.all().stream().filter(method -> method.name().equals("Mithril darts")).findFirst().get();
				GuideSnapshot[] states = {
					new GuideSnapshot(true, false, Skill.MINING, AccountMode.F2P, TrainingStyle.BALANCED, 99, 50, 101333,
						true, iron, 1, 0, 0, "85 tiles northeast", null, Collections.emptyList(), "",
						List.of("Ready: Rune pickaxe (carried/equipped)."), Collections.emptyList(), null, true),
					new GuideSnapshot(true, true, Skill.FLETCHING, AccountMode.P2P, TrainingStyle.FAST, 99, 60, 273742,
						true, darts, 0, 0, 0, "12 tiles north", null, Collections.emptyList(), "Supplies needed before training.",
						List.of("Ready: Mithril dart tip (100 carried).", "Missing: Feathers. Open a bank to check stock (need 1, have 0).",
							"To level 61: about 2,549 Mithril dart tip (100 carried; bank unknown).",
							"To level 61: about 2,549 Feathers (0 carried; bank unknown)."), Collections.emptyList(),
						com.levelinghelper.guide.GuideSteps.bank("Open a bank to check for Feathers. Close the bank once ready.", com.levelinghelper.data.Spots.GE), false)
				};
				int x = 10;
				for (GuideSnapshot state : states)
				{
					LevelingHelperPanel panel = new LevelingHelperPanel(mock(LevelingHelperPlugin.class));
					panel.apply(state);
					panel.setSize(240, 1850);
					layout(panel);
					Graphics2D column = (Graphics2D) graphics.create(x, 25, 240, 1850);
					panel.printAll(column);
					column.dispose();
					graphics.setColor(Color.WHITE);
					graphics.drawString(state.itemsReady() ? "Tool already owned" : "Secondary ingredient missing", x, 17);
					x += 260;
				}
				graphics.dispose();
				File output = new File("build/reports/sidebar-supplies.png");
				output.getParentFile().mkdirs();
				ImageIO.write(image, "png", output);
			}
			catch (Exception exception)
			{
				throw new RuntimeException(exception);
			}
		});
	}

	private static GuideSnapshot snapshot(AccountMode mode)
	{
		TrainingMethod method = TrainingCatalog.all().stream()
			.filter(candidate -> candidate.skill() == Skill.MINING && candidate.name().equals("Iron") && candidate.modes().contains(mode))
			.findFirst().get();
		return new GuideSnapshot(true, mode == AccountMode.P2P, Skill.MINING, mode, TrainingStyle.BALANCED,
			99, 34, 22000, true, method, 1, 0, 0, "85 tiles northeast", null, Collections.emptyList(), "",
			Collections.emptyList(), Collections.emptyList());
	}

	private static List<Component> descendants(Container container)
	{
		List<Component> result = new ArrayList<>();
		for (Component component : container.getComponents())
		{
			result.add(component);
			if (component instanceof Container) result.addAll(descendants((Container) component));
		}
		return result;
	}

	private static void layout(Container container)
	{
		container.doLayout();
		for (Component component : container.getComponents())
		{
			if (component instanceof Container) layout((Container) component);
		}
	}
}
