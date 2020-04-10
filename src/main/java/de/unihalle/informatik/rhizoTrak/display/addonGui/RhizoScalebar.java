/* 
 * This file is part of the rhizoTrak project.
 * 
 * Note that rhizoTrak extends TrakEM2, hence, its code base substantially 
 * relies on the source code of the TrakEM2 project and the corresponding Fiji 
 * plugin, initiated by A. Cardona in 2005. Large portions of rhizoTrak's code 
 * are directly derived/copied from the source code of TrakEM2.
 * 
 * For more information on TrakEM2 please visit its websites:
 * 
 *  https://imagej.net/TrakEM2
 * 
 *  https://github.com/trakem2/TrakEM2/wiki
 * 
 * Fore more information on rhizoTrak, visit
 *
 *  https://prbio-hub.github.io/rhizoTrak
 *
 * Both projects, TrakEM2 and rhizoTrak, are released under GPL. 
 * Please find below first the copyright notice of rhizoTrak, and further on
 * (in case that this file was part of the original TrakEM2 source code base
 * and contained a TrakEM2 file header) the original file header with the 
 * TrakEM2 license note.
 */

/*
 * Copyright (C) 2018 - @YEAR@ by the rhizoTrak development team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Fore more information on rhizoTrak, visit
 *
 *    https://prbio-hub.github.io/rhizoTrak
 *
 */

/* 
 * This file is part of the rhizoTrak project.
 * 
 * Note that rhizoTrak extends TrakEM2, hence, its code base substantially 
 * relies on the source code of the TrakEM2 project and the corresponding Fiji 
 * plugin, initiated by A. Cardona in 2005. Large portions of rhizoTrak's code 
 * are directly derived/copied from the source code of TrakEM2.
 * 
 * For more information on TrakEM2 please visit its websites:
 * 
 *  https://imagej.net/TrakEM2
 * 
 *  https://github.com/trakem2/TrakEM2/wiki
 * 
 * Fore more information on rhizoTrak, visit
 *
 *  https://prbio-hub.github.io/rhizoTrak
 *
 * Both projects, TrakEM2 and rhizoTrak, are released under GPL. 
 * Please find below first the copyright notice of rhizoTrak, and further on
 * (in case that this file was part of the original TrakEM2 source code base
 * and contained a TrakEM2 file header) the original file header with the 
 * TrakEM2 license note.
 */

/*
 * Copyright (C) 2018 - @YEAR@ by the rhizoTrak development team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Fore more information on rhizoTrak, visit
 *
 *    https://prbio-hub.github.io/rhizoTrak
 *
 */

package de.unihalle.informatik.rhizoTrak.display.addonGui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import de.unihalle.informatik.rhizoTrak.addon.RhizoProjectConfig;
import de.unihalle.informatik.rhizoTrak.display.Display;
import de.unihalle.informatik.rhizoTrak.display.DisplayCanvas;
import de.unihalle.informatik.rhizoTrak.display.Layer;
import de.unihalle.informatik.rhizoTrak.utils.Utils;
import ij.measure.Calibration;

/**
 * Scalebar visualizing the image calibration as image overlay.
 * 
 * @author moeller
 */
public class RhizoScalebar implements ActionListener {
	
	/**
	 * Position where the scalebar is to be shown.
	 */
	public static enum DisplayPosition {
		/**
		 * Top left corner of the display.
		 */
		TOP_LEFT,
		/**
		 * Top in the middle of the display.
		 */
		TOP_CENTER,
		/**
		 * Top right corner of the display.
		 */
		TOP_RIGHT,
		/**
		 * Bottom left corner of the display.
		 */
		BOTTOM_LEFT,
		/**
		 * Bottom in the middle of the display.
		 */
		BOTTOM_CENTER,
		/**
		 * Bottom right corner of the display.
		 */
		BOTTOM_RIGHT		
	}
	
	/**
	 * RhizoTrak project configuration.
	 */
	protected RhizoProjectConfig projectConfig = null;
	
	/**
	 * Flag if scalebar is visible or not.
	 */
	private boolean isVisible = false;

	/**
	 * Position where the scalebar is to be drawn.
	 */
	protected DisplayPosition position = DisplayPosition.BOTTOM_LEFT;
	
	/**
	 * Width of the scalebar line.
	 */
	protected int linewidth=3;

	/**
	 * Width of the scalebar in pixels.
	 */
	protected int pixelwidth = 200;

	/**
	 * Color of scalebar.
	 */
	protected Color color = new Color(255,255,0,255); // yellow with full alpha
	
	/**
	 * Font size to be used.
	 */
	protected int fontSize = 24;

	/**
	 * Configuration window for scalebar.
	 */
	private ScalebarConfigFrame configWindow = new ScalebarConfigFrame(this);
	
	/**
	 * Hide or unhide the scalebar.
	 * @param flag	If true, the scalebar becomes visible.
	 */
	public void setVisible(boolean flag) {
		this.isVisible = flag;
		if (this.projectConfig != null)
			this.projectConfig.setShowScalebar(flag);
	}

	/**
	 * Set project configuration.
	 * @param pc	Configuration of associated project.
	 */
	public void setProjectConfig(RhizoProjectConfig pc) {
		this.projectConfig = pc;
	}
	
	/**
	 * Repaint the scalebar.
	 * @param g				Graphics object.
	 * @param canvas	Target canvas.
	 */
	public void repaint(final Graphics2D g, DisplayCanvas canvas) {

		if (!this.isVisible)
			return;

		Calibration calib = null;
		//			calib = Display.this.getLayerSet().getCalibration();
		//			if (calib == null) {
		//				// get calibration information from active image if there is none in the layerset
		//				Layer activeLayer = Display.getFrontLayer();
		//				ImagePlus activeImg = activeLayer.getPatches(true).get(0).getImagePlus();
		//				calib = activeImg.getCalibration();
		//			}
		//			

		// get calibration information
		Layer activeLayer = Display.getFrontLayer();
		calib = activeLayer.getPatches(true).get(0).getImagePlus().getCalibration();

		// no calibration information available
		if (calib == null) {
			Utils.showMessage("Scalebar - no calibration data found!", 
					"Could not find calibration data, please check your image metadata.");
			return;				
		}

		String unitString = calib.getUnit();
		double physicalPixelWidth = calib.pixelWidth;

		// always place the scalebar in the lower left corner
		double[] pos = this.getLineCoordinates(canvas);
		double physicalScalebarWidth = pixelwidth*physicalPixelWidth; 
		String resolutionString = String.format("%.3f", physicalScalebarWidth);

		// if there is a comma in the string, convert to digital dot
		resolutionString = resolutionString.replace(",", ".");

		Line2D.Double line = new Line2D.Double(pos[0], pos[1], pos[2], pos[3]);
		g.setStroke(new BasicStroke((float)(this.linewidth/canvas.getMagnification())));
		g.setColor(this.color);
		g.draw(line);
		// draw vertical lines at the ends
		double barLengthHalf = 6;
		Line2D.Double leftLine = new Line2D.Double(pos[0], 
				pos[1] - barLengthHalf/canvas.getMagnification(), 
				pos[0], 
				pos[1] + barLengthHalf/canvas.getMagnification());
		g.draw(leftLine);
		Line2D.Double rightLine = new Line2D.Double(pos[2], 
				pos[3] - barLengthHalf/canvas.getMagnification(), 
				pos[2], 
				pos[3] + barLengthHalf/canvas.getMagnification());
		g.draw(rightLine);
		Font font = new Font("TimesRoman", Font.PLAIN, (int)(this.fontSize/canvas.getMagnification())); 
		this.drawCenteredString(g, canvas, line, resolutionString + " " + unitString, font);
	}

	/**
	 * Set position where to draw the scalebar.
	 * @param dp	Scalebar position.
	 */
	public void setPosition(DisplayPosition dp) {
		this.position = dp;
		if (this.projectConfig != null)
			this.projectConfig.setScalebarPosition(dp);
	}

	/**
	 * Set color of the scalebar.
	 * @param c	Color of scalebar.
	 */
	public void setColor(Color c) {
		this.color = c;
		if (this.projectConfig != null)
			this.projectConfig.setScalebarColor(c);
	}

	/**
	 * Set length of scalebar in pixels.
	 * @param pixellength	Length in pixels.
	 */
	public void setPixelWidth(int pw) {
		this.pixelwidth = pw;
		if (this.projectConfig != null)
			this.projectConfig.setScalebarPixelwidth(pw);
	}

	/**
	 * Set linewidth of scalebar.
	 * @param linewidth	Line width of scalebar.
	 */
	public void setLinewidth(int lw) {
		this.linewidth = lw;
		if (this.projectConfig != null)
			this.projectConfig.setScalebarLinewidth(lw);
	}

	/**
	 * Set size of font of scalebar label.
	 * @param size	Font size.
	 */
	public void setLabelFontsize(int size) {
		this.fontSize = size;
		if (this.projectConfig != null)
			this.projectConfig.setScalebarFontsize(size);
	}

	/**
	 * Calculate start and end position of scalebar.
	 * @param canvas	Canvas where to draw the scalebar.
	 * @return	Array with coordinates {x1,y1,x2,y2}.
	 */
	private double[] getLineCoordinates(DisplayCanvas canvas) {
		double x1 = 0, y1 = 0, x2 = 0, y2 = 0;
		Rectangle viewPane = canvas.getSrcRect();
		
		// absolute center position in x of the viewing pane
		double centerX = (viewPane.getMaxX() - viewPane.getMinX())/2.0 + viewPane.getMinX();

		switch(this.position)
		{
		case TOP_LEFT:
			y1 = viewPane.getMinY() + 50/canvas.getMagnification();
			x1 = viewPane.getMinX() + 50/canvas.getMagnification();
			x2 = x1 + this.pixelwidth;
			break;
		case TOP_CENTER:
			y1 = viewPane.getMinY() + 50/canvas.getMagnification();
			x1 = centerX - 0.5*this.pixelwidth;
			x2 = x1 + this.pixelwidth;
			break;
		case TOP_RIGHT:
			y1 = viewPane.getMinY() + 50/canvas.getMagnification();
			x1 = viewPane.getMaxX() - 50/canvas.getMagnification();
			x2 = x1 - this.pixelwidth;
			break;
		case BOTTOM_LEFT:
			y1 = viewPane.getMaxY() - 50/canvas.getMagnification();
			x1 = viewPane.getMinX() + 50/canvas.getMagnification();
			x2 = x1 + this.pixelwidth;
			break;
		case BOTTOM_CENTER:
			y1 = viewPane.getMaxY() - 50/canvas.getMagnification();
			x1 = centerX - 0.5*this.pixelwidth;
			x2 = x1 + this.pixelwidth;
			break;
		case BOTTOM_RIGHT:		
			y1 = viewPane.getMaxY() - 50/canvas.getMagnification();
			x1 = viewPane.getMaxX() - 50/canvas.getMagnification();
			x2 = x1 - this.pixelwidth;
			break;
		}
		// line is always horizontal
		y2 = y1;
		return new double[] {x1, y1, x2, y2};
	}
	
	/**
	 * Draw a string centered on the given line.
	 * @param g 			Graphics instance.
	 * @param canvas	Target canvas.
	 * @param text 		String to draw.
	 * @param line		Line on which to center the text.
	 */
	private void drawCenteredString(Graphics g, DisplayCanvas canvas, 
			Line2D.Double line, String text, Font font) {
		FontMetrics metrics = g.getFontMetrics(font);
		double length = line.getX2() - line.getX1();
		int x = (int)(line.getX1() + (length - metrics.stringWidth(text)) / 2);
		int y = (int)(line.getY1() - 15/canvas.getMagnification());
		g.setFont(font);
		g.drawString(text, x, y);
	}

	@Override
	public void actionPerformed(ActionEvent ae) {

		switch(ae.getActionCommand())
		{
		case "close":
			this.configWindow.setVisible(false);
			break;
		case "configure":
			this.configWindow.setVisible(true);
			break;
		}
		
	}		
	
	/**
	 * Configuration frame for scalebar object.
	 */
	@SuppressWarnings("serial")
	private class ScalebarConfigFrame extends JFrame 
			implements ActionListener, ChangeListener, DocumentListener, ItemListener {
		
		/**
		 * Scalebar configured by this frame.
		 */
		private RhizoScalebar scalebar;
		
		/**
		 * Set of text fields in frame.
		 */
		private HashMap<Document, JTextField> textFields = new HashMap<>();
		
		/**
		 * Text field to configure scalebar width.
		 */
		private JTextField tfLength;
		
		/**
		 * Text field to configure line width.
		 */
		private JTextField tfWidth;

		/**
		 * Text field to configure font size.
		 */
		private JTextField tfSize;

		/**
		 * Default color for selections.
		 */
		private final Color selectColor = Color.LIGHT_GRAY;
		
		/**
		 * Default constructor.
		 * @param rs	Scalebar to configure.
		 */
		public ScalebarConfigFrame(RhizoScalebar rs) {
			
			this.scalebar = rs;
			
			this.setSize(400, 300);
			this.setTitle("Scalebar Configuration");

			JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
			mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
			
			JPanel configPanel = new JPanel();
			configPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			configPanel.setBorder(new EmptyBorder(0, 0, 5, 0));
			configPanel.setAlignmentX(LEFT_ALIGNMENT);
			configPanel.add(new JLabel("Position"));
			JComboBox<DisplayPosition> posSelect = new JComboBox<RhizoScalebar.DisplayPosition>();
			posSelect.addItemListener(this);
			DisplayPosition defPos = this.scalebar.position;
			for (DisplayPosition dp : DisplayPosition.values()) {
				posSelect.addItem(dp);
			}
			posSelect.setSelectedItem(defPos);
			posSelect.updateUI();
			configPanel.add(posSelect);
			mainPanel.add(configPanel);

			configPanel = new JPanel();
			configPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			configPanel.setBorder(new EmptyBorder(0, 0, 5, 0));
			configPanel.setAlignmentX(LEFT_ALIGNMENT);
			configPanel.add(new JLabel("Width (pixels)"));
			this.tfLength = new JTextField(Integer.toString(this.scalebar.pixelwidth), 10);
			this.tfLength.getDocument().addDocumentListener(this);
			this.textFields.put(this.tfLength.getDocument(), this.tfLength);
			configPanel.add(this.tfLength);
			mainPanel.add(configPanel);

			configPanel = new JPanel();
			configPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			configPanel.setBorder(new EmptyBorder(0, 0, 5, 0));
			configPanel.setAlignmentX(LEFT_ALIGNMENT);
			configPanel.add(new JLabel("Line width"));
			this.tfWidth = new JTextField(Integer.toString(this.scalebar.linewidth), 10);
			this.tfWidth.getDocument().addDocumentListener(this);
			this.textFields.put(tfWidth.getDocument(), this.tfWidth);
			configPanel.add(this.tfWidth);
			mainPanel.add(configPanel);

			configPanel = new JPanel();
			configPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			configPanel.setBorder(new EmptyBorder(0, 0, 5, 0));
			configPanel.setAlignmentX(LEFT_ALIGNMENT);
			configPanel.add(new JLabel("Color"));
			configPanel.add(this.getColorChooser());
			mainPanel.add(configPanel);

			configPanel = new JPanel();
			configPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			configPanel.setBorder(new EmptyBorder(0, 0, 5, 0));
			configPanel.setAlignmentX(LEFT_ALIGNMENT);
			configPanel.add(new JLabel("Label font size"));
			this.tfSize = new JTextField(Integer.toString(this.scalebar.fontSize), 10);
			this.tfSize.getDocument().addDocumentListener(this);
			this.textFields.put(this.tfSize.getDocument(), this.tfSize);
			configPanel.add(this.tfSize);
			mainPanel.add(configPanel);

			configPanel = new JPanel();
			configPanel.setLayout(new BorderLayout());
			configPanel.setBorder(new EmptyBorder(0, 0, 5, 0));
			JPanel buttonPanel = new JPanel();
			JButton closeButton = new JButton("Close");
			closeButton.addActionListener(RhizoScalebar.this);
			closeButton.setActionCommand("close");
			buttonPanel.add(closeButton);
			configPanel.add(buttonPanel, BorderLayout.CENTER);
			mainPanel.add(configPanel);
			this.add(mainPanel);
			this.repaint();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			JButton source = (JButton) e.getSource();
			Color selectedColor = JColorChooser.showDialog(source, "Choose color", Color.WHITE);
			if (selectedColor != null)  {
				this.scalebar.setColor(selectedColor);
				source.setBackground(selectedColor);
			}
		}

		@Override
		public void itemStateChanged(ItemEvent ie) {
			DisplayPosition dp = (DisplayPosition)ie.getItem();
			this.scalebar.setPosition(dp);			
		}
		
		// alpha change slider action
		@Override
		public void stateChanged(ChangeEvent e) {
			JSlider currentSlider = (JSlider) e.getSource();
			this.scalebar.color = new Color(this.scalebar.color.getRed(),
				this.scalebar.color.getGreen(), this.scalebar.color.getBlue(), 
					currentSlider.getValue());
		}

		@Override
		public void removeUpdate(DocumentEvent e)	{
			updateTF(e.getDocument());
		}

		@Override 
		public void insertUpdate(DocumentEvent e){
			updateTF(e.getDocument());
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			updateTF(e.getDocument());
		}

		/**
		 * Updates configuration settings linked to text fields.
		 * @param d	Document which has been updated.
		 */
		private void updateTF(Document d) {
			JTextField tf = this.textFields.get(d);
			String value = tf.getText();
			if (value != null && !value.isEmpty()) {
				if (tf.equals(this.tfLength)) {
					this.scalebar.setPixelWidth(Integer.valueOf(value));
				}
				else if (tf.equals(this.tfWidth)) {
					this.scalebar.setLinewidth(Integer.valueOf(value));
				} 
				else if (tf.equals(this.tfSize)) {
					this.scalebar.setLabelFontsize(Integer.valueOf(value));
				}
			}
		}

		/**
		 * Generates a button to open a color selection panel.
		 * @return	Color configuration button.
		 */
		private JPanel getColorChooser() {
			
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
			panel.setBorder(new EmptyBorder(0, 15, 5, 15));

			JSlider slider = new JSlider();
			slider.setMinimum(0);
			slider.setMaximum(255);
			slider.setValue(this.scalebar.color.getAlpha());

			slider.addChangeListener(this);
			panel.add(slider);

			JButton button = new JButton();
			button.setActionCommand("change_color");
			button.addActionListener(this);
			button.setMaximumSize(new Dimension(33, 15));
			button.setMinimumSize(new Dimension(33, 15));
			button.setPreferredSize(new Dimension(33, 12));
			button.setContentAreaFilled(false);
			button.setOpaque(true);
			button.setBackground(this.scalebar.color);
			panel.add(button);
			
			panel.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					panel.setBackground(selectColor);
				}

				public void mouseEntered(MouseEvent e) {
					panel.setBorder(BorderFactory.createLineBorder(selectColor));
				}

				public void mouseExited(MouseEvent e) {
					panel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
				}
			});
			return panel;
		}
	}
}	