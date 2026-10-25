package com.levelinghelper;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class PanelWrappingTest
{
	@Test
	public void introductionFitsNarrowPanelsAndLargerFonts() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			LevelingHelperPanel panel = new LevelingHelperPanel(mock(LevelingHelperPlugin.class));
			JLabel label = introduction(panel);
			assertNotNull(label);
			assertNotNull("Initial text must render as HTML before any font change", label.getClientProperty(BasicHTML.propertyKey));
			for (int fontSize : new int[]{13, 18, 24})
			{
				label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontSize));
				int narrowHeight = 0;
				for (int width : new int[]{160, 205, 280})
				{
					label.getParent().setSize(width, 1000);
					Dimension preferred = label.getPreferredSize();
					View view = (View) label.getClientProperty(BasicHTML.propertyKey);
					assertTrue("HTML must wrap inside the sidebar", view.getMinimumSpan(View.X_AXIS) <= width);
					assertEquals(width, preferred.width);
					if (width == 160) narrowHeight = preferred.height;
					assertTrue("Narrow labels need at least as much height", preferred.height <= narrowHeight);
				}
			}
		});
		renderIntroductionAtSeveralSidebarWidths();
	}

	private void renderIntroductionAtSeveralSidebarWidths() throws Exception
	{
		SwingUtilities.invokeAndWait(() ->
		{
			try
			{
				JLabel label = introduction(new LevelingHelperPanel(mock(LevelingHelperPlugin.class)));
				BufferedImage image = new BufferedImage(780, 310, BufferedImage.TYPE_INT_RGB);
				Graphics2D graphics = image.createGraphics();
				graphics.setColor(new Color(40, 40, 40));
				graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
				int x = 10;
				for (int width : new int[]{160, 205, 280})
				{
					label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
					label.getParent().setSize(width, 1000);
					label.setSize(label.getPreferredSize());
					Graphics2D column = (Graphics2D) graphics.create(x, 40, width, 250);
					label.paint(column);
					column.dispose();
					graphics.setColor(Color.WHITE);
					graphics.drawString(width + " px", x, 20);
					x += width + 30;
				}
				graphics.dispose();
				File output = new File("build/reports/sidebar-wrapping.png");
				output.getParentFile().mkdirs();
				ImageIO.write(image, "png", output);
			}
			catch (Exception exception)
			{
				throw new RuntimeException(exception);
			}
		});
	}

	private JLabel introduction(Container container)
	{
		for (Component component : container.getComponents())
		{
			if (component instanceof JLabel && ((JLabel) component).getText().contains("Pick a skill."))
			{
				return (JLabel) component;
			}
			if (component instanceof Container)
			{
				JLabel result = introduction((Container) component);
				if (result != null) return result;
			}
		}
		return null;
	}
}
