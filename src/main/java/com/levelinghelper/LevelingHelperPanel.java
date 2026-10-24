package com.levelinghelper;

import com.levelinghelper.guide.GuideSnapshot;
import com.levelinghelper.guide.TrainingMath;
import com.levelinghelper.model.AccountMode;
import com.levelinghelper.model.GuideStep;
import com.levelinghelper.model.TrainingMethod;
import com.levelinghelper.model.TrainingStyle;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;
import net.runelite.api.Skill;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

class LevelingHelperPanel extends PluginPanel
{
	private static final Color TEXT = new Color(230, 230, 230);
	private static final Color DIM = new Color(170, 176, 170);
	private static final Color WARN = new Color(230, 150, 90);

	private final LevelingHelperPlugin plugin;
	private final JComboBox<AccountMode> mode = new JComboBox<AccountMode>(AccountMode.values());
	private final JComboBox<TrainingStyle> style = new JComboBox<TrainingStyle>(TrainingStyle.values());
	private final JSpinner goal = new JSpinner(new SpinnerNumberModel(99, 2, 99, 1));
	private final JPanel skills = new JPanel(new GridLayout(0, 2, 4, 4));
	private final JPanel detail = new JPanel();
	private final Map<Skill, JToggleButton> skillButtons = new HashMap<Skill, JToggleButton>();
	private final ButtonGroup skillGroup = new ButtonGroup();
	private boolean suppress;
	private String signature = "";
	private WrapLabel statsLabel;

	LevelingHelperPanel(LevelingHelperPlugin plugin)
	{
		super();
		this.plugin = plugin;
		if (getScrollPane() != null)
		{
			getScrollPane().getVerticalScrollBar().setUnitIncrement(18);
		}

		JPanel top = column();
		top.add(text("Leveling Helper", TEXT));
		top.add(text("Pick a skill. A yellow arrow marks the next bank, tree, or NPC.", DIM));
		top.add(Box.createVerticalStrut(6));
		top.add(labeled("Account", mode));
		top.add(labeled("Style", style));
		top.add(labeled("Goal", goal));
		JButton match = button("Match account");
		match.addActionListener(event -> plugin.detectMembership());
		top.add(match);
		top.add(Box.createVerticalStrut(6));
		styleSkills();
		top.add(skills);

		detail.setLayout(new BoxLayout(detail, BoxLayout.Y_AXIS));
		detail.setBackground(ColorScheme.DARK_GRAY_COLOR);
		detail.setAlignmentX(Component.LEFT_ALIGNMENT);
		add(top);
		add(detail);

		mode.addActionListener(event ->
		{
			if (!suppress)
			{
				plugin.setAccountMode((AccountMode) mode.getSelectedItem());
			}
		});
		style.addActionListener(event ->
		{
			if (!suppress)
			{
				plugin.setTrainingStyle((TrainingStyle) style.getSelectedItem());
			}
		});
		goal.addChangeListener(event ->
		{
			if (!suppress)
			{
				plugin.setGoal((Integer) goal.getValue());
			}
		});
	}

	void load(AccountMode accountMode, TrainingStyle trainingStyle, Skill skill, int goalLevel)
	{
		suppress = true;
		mode.setSelectedItem(accountMode);
		style.setSelectedItem(trainingStyle);
		goal.setValue(goalLevel);
		JToggleButton button = skillButtons.get(skill);
		if (button != null)
		{
			button.setSelected(true);
		}
		suppress = false;
	}

	void selectMode(AccountMode accountMode)
	{
		suppress = true;
		mode.setSelectedItem(accountMode);
		suppress = false;
	}

	void apply(GuideSnapshot snapshot)
	{
		String next = snapshot.skill().name()
			+ snapshot.mode()
			+ snapshot.style()
			+ snapshot.goal()
			+ snapshot.running()
			+ snapshot.loggedIn()
			+ snapshot.atGoal()
			+ snapshot.status()
			+ (snapshot.method() == null ? "" : snapshot.method().id())
			+ snapshot.stepIndex()
			+ snapshot.alternatives().size();
		if (!next.equals(signature) || statsLabel == null)
		{
			signature = next;
			rebuild(snapshot);
		}
		else
		{
			statsLabel.setText(html(stats(snapshot)));
			statsLabel.invalidate();
		}
	}

	private void rebuild(GuideSnapshot snapshot)
	{
		detail.removeAll();
		detail.add(text(snapshot.status(), snapshot.status().startsWith("You are on a free") || snapshot.status().contains("members skill") ? WARN : DIM));
		detail.add(controls(snapshot));
		detail.add(Box.createVerticalStrut(6));
		if (snapshot.method() == null)
		{
			statsLabel = null;
			refresh();
			return;
		}
		TrainingMethod method = snapshot.method();
		detail.add(text(method.name(), new Color(120, 220, 150)));
		if (!method.steps().isEmpty())
		{
			int stepIndex = snapshot.running()
				? Math.min(snapshot.stepIndex(), method.steps().size() - 1)
				: 0;
			GuideStep currentStep = method.steps().get(stepIndex);
			String heading = currentStep.instruction();
			if (!snapshot.distanceText().isEmpty())
			{
				heading = heading + "\n" + snapshot.distanceText();
			}
			detail.add(text(heading, new Color(255, 214, 120)));
		}
		detail.add(text(method.summary(), TEXT));
		statsLabel = text(stats(snapshot), DIM);
		detail.add(statsLabel);
		detail.add(text(method.rateNote(), DIM));
		if (snapshot.upcoming() != null)
		{
			detail.add(text("Next unlock at " + (method.maxLevel() + 1) + ": " + snapshot.upcoming().name(), DIM));
		}
		if (snapshot.alternatives().size() > 1)
		{
			JComboBox<TrainingMethod> others = new JComboBox<TrainingMethod>();
			for (TrainingMethod alternative : snapshot.alternatives())
			{
				others.addItem(alternative);
			}
			others.setSelectedItem(method);
			others.setAlignmentX(Component.LEFT_ALIGNMENT);
			others.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
			others.addActionListener(event ->
			{
				TrainingMethod selected = (TrainingMethod) others.getSelectedItem();
				if (selected != null)
				{
					plugin.pinMethod(selected.id());
				}
			});
			detail.add(Box.createVerticalStrut(4));
			detail.add(text("Other methods at this level", DIM));
			detail.add(others);
		}
		detail.add(Box.createVerticalStrut(8));
		detail.add(text("Steps", TEXT));
		int index = snapshot.running() ? snapshot.stepIndex() : -1;
		int number = 1;
		for (GuideStep step : method.steps())
		{
			boolean current = number - 1 == index;
			String prefix = current ? ">> " : number + ". ";
			Color color = current ? new Color(255, 196, 90) : TEXT;
			if (step.instruction().contains("Wilderness") || step.instruction().contains("dangerous"))
			{
				color = current ? new Color(255, 140, 110) : WARN;
			}
			detail.add(text(prefix + step.instruction(), color));
			number++;
		}
		if (method.loopFrom() > 0)
		{
			detail.add(text("After the last step, repeat from step " + (method.loopFrom() + 1) + ".", DIM));
		}
		else if (method.steps().size() > 1)
		{
			detail.add(text("These steps repeat.", DIM));
		}
		if (!method.optimizations().isEmpty())
		{
			detail.add(Box.createVerticalStrut(8));
			detail.add(text("Optimizations", TEXT));
			for (String optimization : method.optimizations())
			{
				detail.add(text("- " + optimization, DIM));
			}
		}
		if (method.wikiTitle() != null)
		{
			JButton wiki = button("Open wiki");
			wiki.addActionListener(event -> plugin.openWiki(method.wikiTitle()));
			detail.add(Box.createVerticalStrut(4));
			detail.add(wiki);
		}
		refresh();
	}

	private String stats(GuideSnapshot snapshot)
	{
		TrainingMethod method = snapshot.method();
		int xpToLevel = TrainingMath.xpBetween(snapshot.xp(), snapshot.level() + 1);
		int xpToGoal = TrainingMath.xpBetween(snapshot.xp(), snapshot.goal());
		StringBuilder text = new StringBuilder();
		if (snapshot.loggedIn())
		{
			text.append("Level ").append(snapshot.level())
				.append(" · ").append(TrainingMath.xp(snapshot.xp())).append(" XP\n")
				.append(TrainingMath.xp(xpToLevel)).append(" XP to level ").append(Math.min(99, snapshot.level() + 1)).append("\n")
				.append(TrainingMath.xp(xpToGoal)).append(" XP to goal ").append(snapshot.goal());
		}
		else
		{
			text.append("Goal ").append(snapshot.goal());
		}
		text.append("\nTypical ").append(TrainingMath.rate(method.xpPerHour()));
		if (method.xpEach() > 0 && xpToGoal > 0)
		{
			int actions = TrainingMath.actions(xpToGoal, method.xpEach());
			text.append("\nAbout ").append(String.format("%,d", actions)).append(" actions to the goal");
		}
		if (xpToGoal > 0)
		{
			text.append("\nAbout ").append(TrainingMath.duration(TrainingMath.millis(xpToGoal, method.xpPerHour()))).append(" at the typical rate");
		}
		if (snapshot.running() && !snapshot.distanceText().isEmpty())
		{
			text.append("\n").append(snapshot.distanceText());
		}
		if (snapshot.running())
		{
			text.append("\nSession ").append(TrainingMath.xp(snapshot.sessionXp())).append(" XP · ").append(TrainingMath.rate(snapshot.sessionPerHour()));
			if (snapshot.sessionPerHour() > 0 && xpToGoal > 0)
			{
				text.append("\nAbout ").append(TrainingMath.duration(TrainingMath.millis(xpToGoal, snapshot.sessionPerHour()))).append(" at your session rate");
			}
		}
		return text.toString();
	}

	private void styleSkills()
	{
		skills.setBackground(ColorScheme.DARK_GRAY_COLOR);
		skills.setAlignmentX(Component.LEFT_ALIGNMENT);
		for (Skill skill : Skill.values())
		{
			if ("Overall".equals(skill.getName()))
			{
				continue;
			}
			JToggleButton button = new JToggleButton(skill.getName());
			button.setMargin(new Insets(1, 2, 1, 2));
			button.setFocusable(false);
			button.setBackground(ColorScheme.DARKER_GRAY_COLOR);
			button.setForeground(TEXT);
			skillGroup.add(button);
			skillButtons.put(skill, button);
			button.addActionListener(event ->
			{
				if (!suppress && button.isSelected())
				{
					plugin.setSkill(skill);
				}
			});
			skills.add(button);
		}
	}

	private JPanel labeled(String name, Component field)
	{
		JPanel row = new JPanel(new BorderLayout(6, 0));
		row.setBackground(ColorScheme.DARK_GRAY_COLOR);
		row.setAlignmentX(Component.LEFT_ALIGNMENT);
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
		JLabel label = new JLabel(name);
		label.setForeground(DIM);
		row.add(label, BorderLayout.WEST);
		row.add(field, BorderLayout.CENTER);
		return row;
	}

	private JPanel column()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		return panel;
	}

	private JPanel controls(GuideSnapshot snapshot)
	{
		JPanel box = column();
		JButton start = button(snapshot.running() ? "Stop guiding" : "Start guiding");
		start.addActionListener(event ->
		{
			if (snapshot.running())
			{
				plugin.stopGuide();
			}
			else
			{
				plugin.startGuide();
			}
		});
		box.add(start);
		if (snapshot.running())
		{
			JPanel row = new JPanel(new GridLayout(1, 3, 4, 0));
			row.setBackground(ColorScheme.DARK_GRAY_COLOR);
			row.setAlignmentX(Component.LEFT_ALIGNMENT);
			row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
			JButton back = button("Back");
			JButton next = button("Next");
			JButton bank = button("Bank");
			back.addActionListener(event -> plugin.previousStep());
			next.addActionListener(event -> plugin.nextStep());
			bank.addActionListener(event -> plugin.bankStep());
			row.add(back);
			row.add(next);
			row.add(bank);
			box.add(Box.createVerticalStrut(4));
			box.add(row);
		}
		return box;
	}

	private WrapLabel text(String value, Color color)
	{
		WrapLabel label = new WrapLabel(html(value), color);
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		return label;
	}

	private JButton button(String value)
	{
		JButton button = new JButton(value);
		button.setFocusable(false);
		button.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		button.setForeground(TEXT);
		button.setAlignmentX(Component.LEFT_ALIGNMENT);
		button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
		return button;
	}

	private static String html(String value)
	{
		return "<html><body style='width:190px'>" + escape(value) + "</body></html>";
	}

	private static final class WrapLabel extends JLabel
	{
		private WrapLabel(String html, Color color)
		{
			super(html);
			setForeground(color);
			putClientProperty(BasicHTML.propertyKey, null);
			setText(html);
		}

		@Override
		public Dimension getPreferredSize()
		{
			int width = 205;
			if (getParent() != null && getParent().getWidth() > 40)
			{
				width = getParent().getWidth();
			}
			View view = (View) getClientProperty(BasicHTML.propertyKey);
			if (view == null)
			{
				return super.getPreferredSize();
			}
			view.setSize(width, 0);
			int height = (int) Math.ceil(view.getPreferredSpan(View.Y_AXIS));
			return new Dimension(width, Math.max(16, height));
		}

		@Override
		public Dimension getMaximumSize()
		{
			Dimension preferred = getPreferredSize();
			return new Dimension(Integer.MAX_VALUE, preferred.height);
		}
	}

	private static String escape(String value)
	{
		return value
			.replace("&", "&amp;")
			.replace("<", "&lt;")
			.replace(">", "&gt;")
			.replace("\n", "<br>");
	}

	private void refresh()
	{
		revalidate();
		repaint();
	}
}
