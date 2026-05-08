package app;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.font.TextLayout;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import static app.UiKit.*;

final class PillButton extends JButton {
    final Color fill;
    final Color textColor;

    PillButton(String text, Color fill, Color textColor) {
        super(text);
        this.fill = fill;
        this.textColor = textColor;
        setBorder(new EmptyBorder(10, 18, 10, 18));
        setContentAreaFilled(false);
        setFocusPainted(false);
        setOpaque(false);
        setForeground(textColor);
        setFont(new Font(UI_FONT, Font.BOLD, 14));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        ButtonModel model = getModel();
        Color base = model.isPressed() ? fill.darker() : model.isRollover() ? lighten(fill, 12) : fill;
        int arc = 16;
        g2.setColor(base);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        g2.setColor(new Color(0, 0, 0, 26));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);

        String text = getText();
        Font textFont = fontThatFits(g2, text, getFont(), getWidth() - 24, getHeight() - 16);
        String displayText = textThatFits(g2, text, textFont, getWidth() - 24);
        FontRenderContext context = g2.getFontRenderContext();
        TextLayout layout = new TextLayout(displayText, textFont, context);
        LineMetrics lineMetrics = textFont.getLineMetrics(displayText, context);
        float textX = (float) ((getWidth() - layout.getBounds().getWidth()) / 2.0);
        float textY = (float) ((getHeight() - lineMetrics.getHeight()) / 2.0 + lineMetrics.getAscent());
        g2.setFont(textFont);
        g2.setColor(isEnabled() ? textColor : Theme.MUTED);
        layout.draw(g2, Math.max(12f, textX), textY);
        g2.dispose();
    }
}
