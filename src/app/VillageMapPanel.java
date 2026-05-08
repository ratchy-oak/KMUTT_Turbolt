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

final class VillageMapPanel extends JPanel {
    final VillageGraph graph;
    RouteResult route;
    String currentNode;
    String previewWarningText;
    boolean previewDimmed;

    VillageMapPanel(VillageGraph graph) {
        this.graph = graph;
        setBackground(Theme.BACKGROUND);
        setPreferredSize(new Dimension(650, 500));
        setMinimumSize(new Dimension(520, 380));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(205, 213, 197)),
                new EmptyBorder(8, 8, 8, 8)
        ));
    }

    void setRoute(RouteResult route) {
        this.route = route;
        repaint();
    }

    void setCurrentNode(String currentNode) {
        this.currentNode = currentNode;
        repaint();
    }

    void setPreviewWarning(String previewWarningText, boolean previewDimmed) {
        this.previewWarningText = previewWarningText;
        this.previewDimmed = previewDimmed;
        repaint();
    }

    @Override
    // ลำดับการวาดสำคัญ: ถนนและ route ก่อน แล้วค่อย node/ตัวเลข เพื่อไม่ให้ข้อมูลถูกทับ
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        drawMapBackground(g2);
        drawGrid(g2);
        drawEdges(g2);
        drawRoute(g2);
        drawNodes(g2);
        drawCurrentPin(g2);
        drawWeights(g2);
        drawPreviewWarning(g2);
        g2.dispose();
    }

    void drawMapBackground(Graphics2D g2) {
        g2.setPaint(new GradientPaint(0, 0, new Color(251, 250, 241), getWidth(), getHeight(), new Color(238, 246, 235)));
        g2.fillRect(0, 0, getWidth(), getHeight());
    }

    void drawGrid(Graphics2D g2) {
        g2.setStroke(new BasicStroke(1));
        g2.setColor(new Color(206, 216, 198, 120));
        for (int x = 0; x < getWidth(); x += 48) {
            g2.drawLine(x, 0, x, getHeight());
        }
        for (int y = 0; y < getHeight(); y += 48) {
            g2.drawLine(0, y, getWidth(), y);
        }
    }

    // วาดถนนพื้นฐานทั้งหมดของหมู่บ้าน
    void drawEdges(Graphics2D g2) {
        for (RouteEdge edge : graph.visibleEdges()) {
            Point2D a = scaled(edge.from());
            Point2D b = scaled(edge.to());
            g2.setStroke(new BasicStroke(9, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(new Color(34, 52, 44, 45));
            g2.drawLine((int) a.getX() + 1, (int) a.getY() + 2, (int) b.getX() + 1, (int) b.getY() + 2);
            g2.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(Theme.ROAD);
            g2.drawLine((int) a.getX(), (int) a.getY(), (int) b.getX(), (int) b.getY());
        }
    }

    // วาดเส้นทางที่ Dijkstra เลือกด้วยสีเด่นกว่าถนนปกติ
    void drawRoute(Graphics2D g2) {
        if (route == null || route.path().size() < 2) {
            return;
        }
        for (int i = 0; i < route.path().size() - 1; i++) {
            Point2D a = scaled(route.path().get(i));
            Point2D b = scaled(route.path().get(i + 1));
            g2.setStroke(new BasicStroke(13, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(new Color(218, 105, 82, 70));
            g2.drawLine((int) a.getX(), (int) a.getY(), (int) b.getX(), (int) b.getY());
            g2.setStroke(new BasicStroke(7, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(Theme.CORAL);
            g2.drawLine((int) a.getX(), (int) a.getY(), (int) b.getX(), (int) b.getY());
        }
    }

    // วาดจุดสำคัญบนแผนที่ เช่น ร้านอาหาร บ้าน และจุดแยกต่าง ๆ
    void drawNodes(Graphics2D g2) {
        for (Map.Entry<String, Point2D.Double> entry : graph.positions().entrySet()) {
            String node = entry.getKey();
            Point2D point = scaled(node);
            boolean active = route != null && route.path().contains(node);
            boolean restaurant = node.equals("Restaurant");
            int radius = restaurant ? 39 : 32;
            Shape circle = new Ellipse2D.Double(point.getX() - radius, point.getY() - radius, radius * 2, radius * 2);

            g2.setColor(new Color(38, 48, 43, 34));
            g2.fillOval((int) point.getX() - radius + 3, (int) point.getY() - radius + 5, radius * 2, radius * 2);
            g2.setColor(restaurant ? Theme.CORAL_LIGHT : active ? Theme.GOLD_LIGHT : Theme.NODE_FILL);
            g2.fill(circle);
            g2.setStroke(new BasicStroke(active || restaurant ? 6 : 4));
            g2.setColor(restaurant ? Theme.CORAL : Theme.PURPLE);
            g2.draw(circle);

            String label = restaurant ? "ร้าน" : shortLabel(node);
            g2.setColor(Theme.INK);
            g2.setFont(getFont().deriveFont(Font.BOLD, restaurant ? 13f : 12f));
            FontMetrics metrics = g2.getFontMetrics();
            g2.drawString(label, (int) (point.getX() - metrics.stringWidth(label) / 2.0), (int) (point.getY() + 5));
        }
    }

    // วาดเลขน้ำหนักเป็นชั้นสุดท้าย เพื่อไม่ให้เส้นทางสีส้มทับตัวเลข
    void drawWeights(Graphics2D g2) {
        for (RouteEdge edge : graph.visibleEdges()) {
            Point2D a = scaled(edge.from());
            Point2D b = scaled(edge.to());
            drawWeight(g2, edge, a, b);
        }
    }

    void drawWeight(Graphics2D g2, RouteEdge edge, Point2D a, Point2D b) {
        int x = (int) ((a.getX() + b.getX()) / 2);
        int y = (int) ((a.getY() + b.getY()) / 2);
        String label = String.valueOf(edge.weight());
        g2.setFont(getFont().deriveFont(Font.BOLD, 15f));
        FontMetrics metrics = g2.getFontMetrics();
        int width = Math.max(36, metrics.stringWidth(label) + 20);
        int height = 30;
        g2.setColor(new Color(42, 53, 47, 38));
        g2.fillRoundRect(x - width / 2 + 2, y - 18, width, height, 16, 16);
        g2.setColor(new Color(255, 253, 246, 250));
        g2.fillRoundRect(x - width / 2, y - 20, width, height, 16, 16);
        g2.setColor(new Color(209, 216, 200));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(x - width / 2, y - 20, width, height, 16, 16);
        g2.setColor(Theme.INK);
        g2.drawString(label, x - metrics.stringWidth(label) / 2, y - 1);
    }

    // วาดหมุดบอกตำแหน่งปัจจุบันของไรเดอร์/จุดเริ่มต้น โดยเลื่อนออกจากกลาง node เพื่อไม่บังชื่อจุด
    void drawCurrentPin(Graphics2D g2) {
        if (currentNode == null || !graph.positions().containsKey(currentNode)) {
            return;
        }

        Point2D point = scaled(currentNode);
        boolean restaurant = currentNode.equals("Restaurant");
        int nodeRadius = restaurant ? 39 : 32;
        int pinX = (int) Math.round(point.getX() + nodeRadius * 0.58);
        int pinY = (int) Math.round(point.getY() - nodeRadius * 0.72);
        int size = 27;

        Path2D pinShadow = pinShape(pinX + 2, pinY + 3, size);
        g2.setColor(new Color(42, 53, 47, 42));
        g2.fill(pinShadow);

        Path2D pin = pinShape(pinX, pinY, size);
        g2.setColor(Theme.LEAF);
        g2.fill(pin);
        g2.setStroke(new BasicStroke(2));
        g2.setColor(Color.WHITE);
        g2.draw(pin);

        int dot = 9;
        g2.setColor(Color.WHITE);
        g2.fillOval(pinX - dot / 2, pinY - size / 2 + 5, dot, dot);
    }

    Path2D pinShape(int centerX, int centerY, int size) {
        double half = size / 2.0;
        double top = centerY - half;
        double bottom = centerY + half + 6;
        Path2D pin = new Path2D.Double();
        pin.moveTo(centerX, bottom);
        pin.curveTo(centerX - half, centerY + 3, centerX - half, top, centerX, top);
        pin.curveTo(centerX + half, top, centerX + half, centerY + 3, centerX, bottom);
        pin.closePath();
        return pin;
    }

    void drawPreviewWarning(Graphics2D g2) {
        if (previewWarningText == null || previewWarningText.isBlank()) {
            return;
        }

        if (previewDimmed) {
            g2.setColor(new Color(255, 253, 246, 96));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }

        g2.setFont(new Font(UI_FONT, Font.BOLD, 13));
        FontMetrics metrics = g2.getFontMetrics();
        int textWidth = metrics.stringWidth(previewWarningText);
        int cardWidth = Math.min(getWidth() - 32, Math.max(280, textWidth + 44));
        int cardHeight = 44;
        int x = getWidth() - cardWidth - 18;
        int y = 18;

        g2.setColor(new Color(42, 53, 47, 36));
        g2.fillRoundRect(x + 2, y + 3, cardWidth, cardHeight, 16, 16);
        g2.setColor(new Color(255, 253, 246, 238));
        g2.fillRoundRect(x, y, cardWidth, cardHeight, 16, 16);
        g2.setColor(new Color(218, 105, 82, 190));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(x, y, cardWidth, cardHeight, 16, 16);

        int iconSize = 20;
        int iconX = x + 16;
        int iconY = y + 12;
        g2.setColor(Theme.CORAL);
        g2.fillOval(iconX, iconY, iconSize, iconSize);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font(UI_FONT, Font.BOLD, 14));
        g2.drawString("!", iconX + 8, iconY + 15);

        g2.setColor(Theme.INK);
        g2.setFont(new Font(UI_FONT, Font.BOLD, 13));
        int textX = iconX + iconSize + 10;
        int textY = y + (cardHeight - metrics.getHeight()) / 2 + metrics.getAscent();
        g2.drawString(textThatFits(g2, previewWarningText, g2.getFont(), cardWidth - 56), textX, textY);
    }

    // แปลงตำแหน่งแบบสัดส่วน 0-1 ให้เป็นพิกัดจริงตามขนาด panel
    Point2D scaled(String node) {
        Point2D.Double point = graph.positions().get(node);
        double marginX = 76;
        double marginY = 54;
        return new Point2D.Double(
                marginX + point.x * (getWidth() - marginX * 2),
                marginY + point.y * (getHeight() - marginY * 2)
        );
    }

    String shortLabel(String node) {
        return switch (node) {
            case "Cloud House" -> "เมฆ";
            case "Bamboo Gate" -> "ไผ่";
            case "Shell Street" -> "เปลือก";
            case "Lotus Pond" -> "บัว";
            case "Fern Lane" -> "เฟิร์น";
            case "Canal Pier" -> "คลอง";
            case "Mango Yard" -> "มะม่วง";
            case "Market Turn" -> "ตลาด";
            case "School Corner" -> "เรียน";
            case "River Bend" -> "น้ำ";
            case "Hill View" -> "เนิน";
            case "Cactus Lane" -> "กระบอง";
            case "Village Gate" -> "ประตู";
            default -> node;
        };
    }
}
