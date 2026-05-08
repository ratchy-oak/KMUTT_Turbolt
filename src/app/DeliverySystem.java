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

final class DeliverySystem {
    final List<MenuItem> menu = new ArrayList<>();
    final List<CustomerLocation> locations = new ArrayList<>();
    final List<DeliveryOrder> orders = new ArrayList<>();
    final VillageGraph graph = new VillageGraph();
    LocalDateTime openedAt = LocalDateTime.now();
    int nextId = 1;
    int currentRoundIndex = 0;
    String riderCurrentNode = "Restaurant";

    DeliverySystem() {
        seedMenu();
        seedMap();
    }

    VillageGraph graph() {
        return graph;
    }

    List<CustomerLocation> locations() {
        return locations;
    }

    String riderCurrentNode() {
        return riderCurrentNode;
    }

    // คืนเมนูเฉพาะหมวดที่ลูกค้าเลือก เช่น อาหาร/ของหวาน/เครื่องดื่ม
    List<MenuItem> menuByCategory(String category) {
        List<MenuItem> result = new ArrayList<>();
        for (MenuItem item : menu) {
            if (item.category().equals(category)) {
                result.add(item);
            }
        }
        return result;
    }

    // สร้างออเดอร์พร้อมผูกเข้ารอบ 10 นาทีตามเวลาเปิดร้าน
    DeliveryOrder placeOrder(MenuItem item, CustomerLocation location) {
        return placeOrderAt(item, location, LocalDateTime.now());
    }

    DeliveryOrder placeOrderAt(MenuItem item, CustomerLocation location, LocalDateTime createdAt) {
        RouteResult route = routeTo(location.nodeId());
        int naturalRound = roundIndexFor(createdAt);
        int minimumRound = hasActiveBatch() ? currentRoundIndex + 1 : currentRoundIndex;
        int orderRound = Math.max(naturalRound, minimumRound);
        DeliveryOrder order = new DeliveryOrder(nextId++, item, location, route, createdAt, orderRound, OrderStatus.WAITING);
        orders.add(order);
        return order;
    }

    // เรียงออเดอร์สำหรับหน้าร้านค้า โดยใช้ลำดับแผนส่งเดียวกับไรเดอร์เมื่อมีการเตรียมรอบแล้ว
    List<DeliveryOrder> sortedOrders() {
        List<DeliveryOrder> sorted = new ArrayList<>(orders);
        sorted.sort(Comparator
                .comparingInt(DeliveryOrder::roundIndex)
                .thenComparing(order -> order.deliverySequence() < 0 ? Integer.MAX_VALUE : order.deliverySequence())
                .thenComparing(Comparator.comparingInt((DeliveryOrder order) -> order.priorityScore()).reversed())
                .thenComparing(order -> order.route().distance())
                .thenComparing(DeliveryOrder::createdAt));
        return sorted;
    }

    // เรียงคิวไรเดอร์จากตำแหน่งล่าสุดของไรเดอร์ ไม่ใช่ระยะจากร้านอาหารเสมอไป
    List<DeliveryOrder> riderQueueOrders() {
        List<DeliveryOrder> riderOrders = new ArrayList<>();
        for (DeliveryOrder order : orders) {
            if (order.status() == OrderStatus.PREPARING) {
                riderOrders.add(order);
            }
        }
        riderOrders.sort(riderOrderComparator());
        return riderOrders;
    }

    // ตัดรอบ 10 นาที: ร้านเตรียมได้เฉพาะรอบที่ยังไม่ถูกส่ง และต้องรอให้ไรเดอร์ส่งรอบก่อนครบก่อน
    BatchResult prepareTenMinuteBatch() {
        if (hasActiveBatch()) {
            return new BatchResult(List.of(), "ต้องส่งออเดอร์รอบปัจจุบันให้ครบก่อน จึงจะเริ่มเตรียมรอบถัดไปได้");
        }

        int nextRound = nextWaitingRoundAtOrAfter(currentRoundIndex);
        if (nextRound < 0) {
            return new BatchResult(List.of(), "ยังไม่มีออเดอร์ที่รอจัดเตรียม");
        }
        currentRoundIndex = nextRound;

        List<DeliveryOrder> batch = new ArrayList<>();
            for (DeliveryOrder order : sortedOrders()) {
                if (order.status() == OrderStatus.WAITING && order.roundIndex() == currentRoundIndex) {
                    order.setStatus(OrderStatus.PREPARING);
                    batch.add(order);
                }
            }
            List<DeliveryOrder> plannedBatch = combinedPriorityDeliveryPlan(batch);
            for (int i = 0; i < plannedBatch.size(); i++) {
                plannedBatch.get(i).setDeliverySequence(i);
            }
            String message = "เตรียมออเดอร์รอบที่ " + (currentRoundIndex + 1)
                    + " (" + roundStartMinute(currentRoundIndex) + "-" + roundEndMinute(currentRoundIndex)
                    + " นาทีหลังเปิดร้าน) จำนวน " + plannedBatch.size() + " รายการ";
            return new BatchResult(plannedBatch, message);
        }

    // รับงานตามแผนเส้นทางที่คำนวณไว้ทั้งรอบแล้ว ไม่ลัดคิวใหม่ระหว่างทาง
    DeliveryOrder acceptNextForRider() {
        if (hasOutForDelivery()) {
            return null;
        }
        for (DeliveryOrder order : riderQueueOrders()) {
            if (order.roundIndex() == currentRoundIndex) {
                order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
                return order;
            }
        }
        return null;
    }

    boolean markDelivered(DeliveryOrder order) {
        order.setStatus(OrderStatus.DELIVERED);
        riderCurrentNode = order.location().nodeId();
        if (!hasOpenWorkInRound(order.roundIndex())) {
            riderCurrentNode = "Restaurant";
            currentRoundIndex = Math.max(currentRoundIndex, order.roundIndex() + 1);
            return true;
        }
        return false;
    }

    RouteResult routeTo(String destination) {
        return graph.shortestPath("Restaurant", destination);
    }

    // เส้นทางสำหรับไรเดอร์ เริ่มจากตำแหน่งล่าสุดที่ส่งสำเร็จ ไม่ใช่เริ่มจากร้านเสมอไป
    RouteResult riderRouteTo(DeliveryOrder order) {
        return graph.shortestPath(riderCurrentNode, order.location().nodeId());
    }

    RouteResult plannedLegRouteTo(DeliveryOrder order) {
        return graph.shortestPath(plannedLegStartNode(order), order.location().nodeId());
    }

    RouteResult displayRouteForOrder(DeliveryOrder order) {
        return order.deliverySequence() >= 0 ? plannedLegRouteTo(order) : order.route();
    }

    String plannedLegStartNode(DeliveryOrder order) {
        String startNode = "Restaurant";
        for (DeliveryOrder candidate : orders) {
            if (candidate.roundIndex() == order.roundIndex()
                    && candidate.deliverySequence() == order.deliverySequence() - 1) {
                startNode = candidate.location().nodeId();
                break;
            }
        }
        return startNode;
    }

    DeliveryOrder previousOpenOrderBefore(DeliveryOrder order) {
        DeliveryOrder previous = null;
        for (DeliveryOrder candidate : orders) {
            if (candidate.roundIndex() == order.roundIndex()
                    && candidate.deliverySequence() < order.deliverySequence()
                    && candidate.status() != OrderStatus.DELIVERED
                    && (previous == null || candidate.deliverySequence() > previous.deliverySequence())) {
                previous = candidate;
            }
        }
        return previous;
    }

    Comparator<DeliveryOrder> riderOrderComparator() {
        return Comparator.comparingInt(DeliveryOrder::deliverySequence)
                .thenComparing(DeliveryOrder::createdAt);
    }

    List<DeliveryOrder> combinedPriorityDeliveryPlan(List<DeliveryOrder> batch) {
        List<DeliveryOrder> remaining = new ArrayList<>(batch);
        List<DeliveryOrder> planned = new ArrayList<>();
        String currentNode = "Restaurant";

        while (!remaining.isEmpty()) {
            String startNode = currentNode;
            remaining.sort(Comparator
                    .comparingInt((DeliveryOrder order) -> order.priorityScore(graph.shortestPath(startNode, order.location().nodeId()))).reversed()
                    .thenComparing(order -> graph.shortestPath(startNode, order.location().nodeId()).distance())
                    .thenComparing(DeliveryOrder::createdAt));
            DeliveryOrder next = remaining.remove(0);
            planned.add(next);
            currentNode = next.location().nodeId();
        }

        return planned;
    }

    // นับออเดอร์ที่ยังไม่จบงาน เพื่อแสดงในสถิติ "คิวคงเหลือ"
    int waitingCount() {
        int count = 0;
        for (DeliveryOrder order : orders) {
            if (order.status() != OrderStatus.DELIVERED) {
                count++;
            }
        }
        return count;
    }

    String nextOpenOrderLabel() {
        for (DeliveryOrder order : sortedOrders()) {
            if (order.status() != OrderStatus.DELIVERED) {
                return order.displayId();
            }
        }
        return "-";
    }

    String elapsedSinceOpenLabel() {
        long seconds = Math.max(0, Duration.between(openedAt, LocalDateTime.now()).getSeconds());
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        if (minutes < 60) {
            return String.format("%02d:%02d", minutes, remainingSeconds);
        }
        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;
        return String.format("%d:%02d:%02d", hours, remainingMinutes, remainingSeconds);
    }

    String currentRoundLabel() {
        return "รอบ " + (currentRoundIndex + 1);
    }

    int roundIndexFor(LocalDateTime createdAt) {
        long minutesAfterOpen = Math.max(0, Duration.between(openedAt, createdAt).toMinutes());
        return (int) (minutesAfterOpen / 10);
    }

    int nextWaitingRoundAtOrAfter(int roundIndex) {
        int next = Integer.MAX_VALUE;
        for (DeliveryOrder order : orders) {
            if (order.status() == OrderStatus.WAITING && order.roundIndex() >= roundIndex) {
                next = Math.min(next, order.roundIndex());
            }
        }
        return next == Integer.MAX_VALUE ? -1 : next;
    }

    boolean hasActiveBatch() {
        for (DeliveryOrder order : orders) {
            if (order.status() == OrderStatus.PREPARING || order.status() == OrderStatus.OUT_FOR_DELIVERY) {
                return true;
            }
        }
        return false;
    }

    boolean hasOutForDelivery() {
        for (DeliveryOrder order : orders) {
            if (order.status() == OrderStatus.OUT_FOR_DELIVERY) {
                return true;
            }
        }
        return false;
    }

    boolean hasOpenWorkInRound(int roundIndex) {
        for (DeliveryOrder order : orders) {
            if (order.roundIndex() == roundIndex && order.status() != OrderStatus.DELIVERED) {
                return true;
            }
        }
        return false;
    }

    int roundStartMinute(int roundIndex) {
        return roundIndex * 10;
    }

    int roundEndMinute(int roundIndex) {
        return roundStartMinute(roundIndex) + 10;
    }

    void seedDemoOrders() {
        if (!orders.isEmpty()) {
            return;
        }
        openedAt = LocalDateTime.now().minusMinutes(20);
        placeOrderAt(menu.get(0), locations.get(1), openedAt.plusMinutes(2));
        placeOrderAt(menu.get(12), locations.get(5), openedAt.plusMinutes(6));
        placeOrderAt(menu.get(9), locations.get(3), openedAt.plusMinutes(12));
        placeOrderAt(menu.get(14), locations.get(7), openedAt.plusMinutes(16));
    }

    // ข้อมูลเมนูตั้งต้นจากรายการในรูปที่ผู้ใช้ให้มา
    void seedMenu() {
        addMenu("ข้าวกะเพราหมูสับ", "อาหาร", 59, 3);
        addMenu("ข้าวผัด", "อาหาร", 55, 3);
        addMenu("ข้าวหมูกระเทียม", "อาหาร", 59, 3);
        addMenu("ข้าวไข่เจียวหมูสับ", "อาหาร", 45, 3);
        addMenu("ราดหน้า", "อาหาร", 55, 3);
        addMenu("ผัดซีอิ๊ว", "อาหาร", 55, 3);
        addMenu("ต้มเลือดหมู", "อาหาร", 65, 3);
        addMenu("ข้าวมันไก่", "อาหาร", 55, 3);
        addMenu("มักกะโรนีไก่", "อาหาร", 69, 3);
        addMenu("สปาเกตตี้", "อาหาร", 75, 3);
        addMenu("ข้าวคะน้าหมูกรอบ", "อาหาร", 65, 3);
        addMenu("เฉาก๊วยนมสด", "ของหวาน", 35, 2);
        addMenu("ไอติมไข่แข็ง", "ของหวาน", 39, 2);
        addMenu("ลอดช่องชาววัง", "ของหวาน", 30, 2);
        addMenu("นมสดเย็น", "เครื่องดื่ม", 30, 1);
        addMenu("โกโก้เย็น", "เครื่องดื่ม", 35, 1);
        addMenu("ชาดำเย็น", "เครื่องดื่ม", 25, 1);
    }

    // แปลงลำดับหมวดสินค้าเป็นคะแนนพื้นฐานของ priority
    void addMenu(String name, String category, int price, int categoryRank) {
        int categoryBonus = switch (categoryRank) {
            case 1 -> 30;
            case 2 -> 20;
            default -> 10;
        };
        menu.add(new MenuItem(name, category, price, categoryBonus));
    }

    // สร้างกราฟหมู่บ้านแบบมีน้ำหนัก โดยน้ำหนักแทนเวลา/ระยะทางในการเดินทาง
    void seedMap() {
        graph.addNode("Restaurant", 0.82, 0.18);
        graph.addNode("Cloud House", 0.55, 0.20);
        graph.addNode("Bamboo Gate", 0.30, 0.20);
        graph.addNode("Shell Street", 0.18, 0.42);
        graph.addNode("Lotus Pond", 0.46, 0.42);
        graph.addNode("Fern Lane", 0.68, 0.43);
        graph.addNode("Canal Pier", 0.84, 0.45);
        graph.addNode("Mango Yard", 0.32, 0.64);
        graph.addNode("Market Turn", 0.56, 0.65);
        graph.addNode("School Corner", 0.80, 0.66);
        graph.addNode("River Bend", 0.20, 0.82);
        graph.addNode("Hill View", 0.48, 0.84);
        graph.addNode("Cactus Lane", 0.70, 0.84);
        graph.addNode("Village Gate", 0.90, 0.84);

        graph.connect("Restaurant", "Cloud House", 1);
        graph.connect("Restaurant", "Canal Pier", 5);
        graph.connect("Cloud House", "Bamboo Gate", 5);
        graph.connect("Cloud House", "Fern Lane", 7);
        graph.connect("Bamboo Gate", "Lotus Pond", 5);
        graph.connect("Bamboo Gate", "Shell Street", 1);
        graph.connect("Shell Street", "River Bend", 12);
        graph.connect("Lotus Pond", "Mango Yard", 2);
        graph.connect("Lotus Pond", "Market Turn", 3);
        graph.connect("Mango Yard", "Hill View", 1);
        graph.connect("Hill View", "Market Turn", 4);
        graph.connect("Market Turn", "Fern Lane", 9);
        graph.connect("Fern Lane", "Canal Pier", 4);
        graph.connect("Fern Lane", "School Corner", 3);
        graph.connect("School Corner", "Village Gate", 2);
        graph.connect("Cactus Lane", "Village Gate", 1);
        graph.connect("Cactus Lane", "School Corner", 3);

        locations.add(new CustomerLocation("บ้าน A - บ้านเมฆ", "Cloud House"));
        locations.add(new CustomerLocation("บ้าน B - ประตูไผ่", "Bamboo Gate"));
        locations.add(new CustomerLocation("บ้าน C - ถนนเปลือกหอย", "Shell Street"));
        locations.add(new CustomerLocation("บ้าน D - สระบัว", "Lotus Pond"));
        locations.add(new CustomerLocation("บ้าน E - ซอยเฟิร์น", "Fern Lane"));
        locations.add(new CustomerLocation("บ้าน F - ท่าเรือคลอง", "Canal Pier"));
        locations.add(new CustomerLocation("บ้าน G - ลานมะม่วง", "Mango Yard"));
        locations.add(new CustomerLocation("บ้าน H - ทางแยกตลาด", "Market Turn"));
        locations.add(new CustomerLocation("บ้าน I - มุมโรงเรียน", "School Corner"));
        locations.add(new CustomerLocation("บ้าน J - โค้งแม่น้ำ", "River Bend"));
        locations.add(new CustomerLocation("บ้าน K - จุดชมวิวเนิน", "Hill View"));
        locations.add(new CustomerLocation("บ้าน L - ซอยกระบองเพชร", "Cactus Lane"));
        locations.add(new CustomerLocation("บ้าน M - ประตูหมู่บ้าน", "Village Gate"));
    }
}
