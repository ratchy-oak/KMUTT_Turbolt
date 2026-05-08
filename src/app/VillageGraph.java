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

final class VillageGraph {
    final Map<String, Point2D.Double> positions = new LinkedHashMap<>();
    final Map<String, List<RouteEdge>> edges = new LinkedHashMap<>();

    // เพิ่มจุดบนแผนที่ โดย x/y เป็นสัดส่วนของพื้นที่วาดแผนที่
    void addNode(String id, double x, double y) {
        positions.put(id, new Point2D.Double(x, y));
        edges.put(id, new ArrayList<>());
    }

    // เชื่อมถนนสองทางระหว่าง node พร้อมน้ำหนักการเดินทาง
    void connect(String a, String b, int weight) {
        edges.get(a).add(new RouteEdge(a, b, weight));
        edges.get(b).add(new RouteEdge(b, a, weight));
    }

    Map<String, Point2D.Double> positions() {
        return positions;
    }

    // คืน edge แบบไม่ซ้ำ เพื่อใช้วาดถนนบนแผนที่เพียงครั้งเดียว
    List<RouteEdge> visibleEdges() {
        List<RouteEdge> visible = new ArrayList<>();
        for (Map.Entry<String, List<RouteEdge>> entry : edges.entrySet()) {
            for (RouteEdge edge : entry.getValue()) {
                if (edge.from().compareTo(edge.to()) < 0) {
                    visible.add(edge);
                }
            }
        }
        return visible;
    }

    // Dijkstra: หาเส้นทางที่มีน้ำหนักรวมน้อยที่สุดจากร้านอาหารไปยังบ้านลูกค้า
    RouteResult shortestPath(String start, String destination) {
        Map<String, Integer> distance = new HashMap<>();
        Map<String, String> previous = new HashMap<>();
        for (String node : positions.keySet()) {
            distance.put(node, Integer.MAX_VALUE / 4);
        }
        distance.put(start, 0);

        PriorityQueue<NodeDistance> queue = new PriorityQueue<>(Comparator.comparingInt(NodeDistance::distance));
        queue.offer(new NodeDistance(start, 0));

        while (!queue.isEmpty()) {
            NodeDistance current = queue.poll();
            if (current.distance() != distance.get(current.node())) {
                continue;
            }
            if (current.node().equals(destination)) {
                break;
            }
            for (RouteEdge edge : edges.get(current.node())) {
                int nextDistance = current.distance() + edge.weight();
                if (nextDistance < distance.get(edge.to())) {
                    distance.put(edge.to(), nextDistance);
                    previous.put(edge.to(), current.node());
                    queue.offer(new NodeDistance(edge.to(), nextDistance));
                }
            }
        }

        List<String> path = new ArrayList<>();
        String cursor = destination;
        while (cursor != null) {
            path.add(0, cursor);
            cursor = previous.get(cursor);
        }
        return new RouteResult(path, distance.get(destination));
    }
}

