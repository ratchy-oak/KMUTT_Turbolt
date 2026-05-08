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

final class TurboltFrame extends JFrame {
    final DeliverySystem system;
    final DefaultListModel<MenuItem> menuModel = new DefaultListModel<>();
    final DefaultListModel<DeliveryOrder> riderQueueModel = new DefaultListModel<>();
    final DefaultTableModel orderTableModel;
    final VillageMapPanel customerMapPanel;
    final VillageMapPanel riderMapPanel;
    final JComboBox<String> categoryBox;
    final JList<MenuItem> menuList;
    final JComboBox<CustomerLocation> locationBox;
    final JLabel queueCountLabel;
    final JLabel topOrderLabel;
    final JLabel openElapsedLabel;
    final JLabel currentRoundLabel;
    final JTextArea activeDeliveryText;
    final JTextArea riderPlanHintText;
    final javax.swing.Timer elapsedTimer;
    DeliveryOrder activeDelivery;

    TurboltFrame(DeliverySystem system) {
        super("Turbolt - เดลิเวอรี่หมู่บ้านหอยทาก");
        this.system = system;
        this.customerMapPanel = new VillageMapPanel(system.graph());
        this.riderMapPanel = new VillageMapPanel(system.graph());
        this.riderMapPanel.setCurrentNode(system.riderCurrentNode());
        this.categoryBox = new JComboBox<>(new String[]{"อาหาร", "ของหวาน", "เครื่องดื่ม"});
        this.menuList = new FitWidthList<>(menuModel);
        this.locationBox = new JComboBox<>(new DefaultComboBoxModel<>(system.locations().toArray(new CustomerLocation[0])));
        styleComboBox(categoryBox);
        styleComboBox(locationBox);
        this.locationBox.setRenderer(new PlaceholderComboBoxRenderer("กรุณาเลือกสถานที่ปลายทาง"));
        this.locationBox.setSelectedIndex(-1);
        this.queueCountLabel = new JLabel();
        this.topOrderLabel = new JLabel();
        this.openElapsedLabel = new JLabel();
        this.currentRoundLabel = new JLabel();
        this.activeDeliveryText = textBlock("ยังไม่มีงานจัดส่ง", 14, Theme.INK);
        this.riderPlanHintText = textBlock("เลือกออเดอร์ในคิวเพื่อดูเส้นทางตามแผน", 12, Theme.MUTED);
        this.orderTableModel = new ReadOnlyOrderTableModel(new String[]{"#", "เมนู", "ประเภท", "ลูกค้า", "ระยะทาง", "เวลาที่สั่ง", "สถานะ"});

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1180, 780));
        setSize(1280, 840);
        setLocationRelativeTo(null);

        setContentPane(buildRoot());
        applyBaseFont(this);
        refreshMenu();
        refreshAll();
        this.elapsedTimer = new javax.swing.Timer(1000, e -> updateOpenElapsedTime());
        this.elapsedTimer.start();
    }

    // รวมหน้าใหญ่ของแอปและสร้างแท็บหลักทั้งสามบทบาท
    JComponent buildRoot() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BACKGROUND);
        root.add(buildHeader(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(Theme.BACKGROUND);
        tabs.addTab("ลูกค้า", buildCustomerPanel());
        tabs.addTab("ร้านค้า", buildMerchantPanel());
        tabs.addTab("ผู้จัดส่ง", buildRiderPanel());
        root.add(tabs, BorderLayout.CENTER);
        return root;
    }

    // สร้างแถบหัวแอปด้านบน พร้อมโลโก้ ข้อความสรุป และสถิติออเดอร์
    JComponent buildHeader() {
        JPanel header = new HeaderGradientPanel(new BorderLayout(18, 0));
        header.setBorder(new EmptyBorder(18, 26, 18, 26));

        JPanel title = transparent(new BorderLayout(14, 0));
        title.add(new SnailLogo(), BorderLayout.WEST);

        JPanel copy = transparent(new BorderLayout(0, 4));
        JLabel name = label("Turbolt", 34, Font.BOLD, Theme.INK);
        JTextArea sub = textBlock("ระบบเดลิเวอรี่ร้านอาหารในหมู่บ้านหอยทาก\nจัดคิวออเดอร์ คำนวณเส้นทาง และมอบหมายงานผู้จัดส่ง", 15, Theme.MUTED);
        sub.setPreferredSize(new Dimension(440, 50));
        copy.add(name, BorderLayout.NORTH);
        copy.add(sub, BorderLayout.CENTER);
        title.add(copy, BorderLayout.CENTER);

        JPanel stats = transparent(new GridLayout(1, 4, 8, 0));
        stats.add(statCard("เปิดร้านมาแล้ว", openElapsedLabel));
        stats.add(statCard("รอบปัจจุบัน", currentRoundLabel));
        stats.add(statCard("คิวคงเหลือ", queueCountLabel));
        stats.add(statCard("ออเดอร์ถัดไป", topOrderLabel));
        stats.setPreferredSize(new Dimension(560, 96));

        header.add(title, BorderLayout.WEST);
        header.add(stats, BorderLayout.EAST);
        return header;
    }

    // หน้า "ลูกค้า": เลือกประเภท/เมนู/ที่อยู่ แล้วแสดงเส้นทางบนแผนที่ทันที
    JComponent buildCustomerPanel() {
        JPanel page = paddedPage(new BorderLayout(18, 18));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.PANEL);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.LINE),
                new EmptyBorder(20, 20, 20, 20)
        ));
        form.setPreferredSize(new Dimension(LEFT_COLUMN_WIDTH, 0));
        form.setMinimumSize(new Dimension(LEFT_COLUMN_WIDTH, 0));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.gridy = 0;
        form.add(sectionTitle("สั่งอาหาร"), c);

        c.gridy++;
        form.add(fieldLabel("ประเภทเมนู"), c);
        c.gridy++;
        form.add(categoryBox, c);

        c.gridy++;
        form.add(fieldLabel("เมนูจากข้อมูลที่ให้มา"), c);
        c.gridy++;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        menuList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        menuList.setCellRenderer(new MenuRenderer());
        menuList.setFixedCellHeight(62);
        JScrollPane menuScroll = new JScrollPane(menuList);
        menuScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        menuScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        menuScroll.setBorder(BorderFactory.createLineBorder(Theme.LINE));
        form.add(menuScroll, c);

        c.gridy++;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weighty = 0;
        form.add(fieldLabel("ที่อยู่จัดส่ง"), c);
        c.gridy++;
        form.add(locationBox, c);

        JButton orderButton = primaryButton("สั่งอาหาร");
        orderButton.addActionListener(this::placeOrder);
        c.gridy++;
        form.add(orderButton, c);

        JPanel preview = new JPanel(new BorderLayout(14, 14));
        preview.setBackground(Theme.BACKGROUND);
        preview.add(customerMapPanel, BorderLayout.CENTER);

        categoryBox.addActionListener(e -> refreshMenu());
        locationBox.addActionListener(e -> previewRouteForSelection());
        menuList.addListSelectionListener(e -> previewRouteForSelection());

        page.add(form, BorderLayout.WEST);
        page.add(preview, BorderLayout.CENTER);
        return page;
    }

    // หน้า "ร้านค้า": แสดงออเดอร์ทั้งหมด เรียงตามลำดับความสำคัญและสถานะ
    JComponent buildMerchantPanel() {
        JPanel page = paddedPage(new BorderLayout(14, 14));
        JPanel actions = transparent(new BorderLayout(10, 10));
        actions.add(sectionTitle("จัดการออเดอร์ร้านค้า"), BorderLayout.WEST);

        JPanel buttons = transparent(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setPreferredSize(new Dimension(590, 48));
        JButton sortButton = secondaryButton("เรียงลำดับ");
        JButton batchButton = primaryButton("เตรียมรอบ 10 นาที");
        JButton resetButton = secondaryButton("ออเดอร์ตัวอย่าง");
        sortButton.addActionListener(e -> refreshAll());
        batchButton.addActionListener(e -> prepareBatch());
        resetButton.addActionListener(e -> seedDemoOrders());
        buttons.add(sortButton);
        buttons.add(batchButton);
        buttons.add(resetButton);
        actions.add(buttons, BorderLayout.EAST);

        JTable table = new JTable(orderTableModel);
        table.setRowHeight(34);
        table.setShowGrid(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD));
        table.getTableHeader().setPreferredSize(new Dimension(0, 42));
        table.getTableHeader().setDefaultRenderer(new TableHeaderRenderer());
        table.setDefaultRenderer(Object.class, new OrderTableRenderer());
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0 && row < system.sortedOrders().size()) {
                showOrder(system.sortedOrders().get(row));
            }
        });

        page.add(actions, BorderLayout.NORTH);
        page.add(new JScrollPane(table), BorderLayout.CENTER);
        return page;
    }

    // หน้า "ผู้จัดส่ง": แยกงานปัจจุบัน คิวรอจัดส่ง และแผนที่เส้นทาง
    JComponent buildRiderPanel() {
        JPanel page = paddedPage(new BorderLayout(18, 18));
        JList<DeliveryOrder> queueList = new FitWidthList<>(riderQueueModel);
            queueList.setCellRenderer(new RiderQueueRenderer(system));
        queueList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        queueList.setFixedCellHeight(68);
        queueList.addListSelectionListener(e -> {
            DeliveryOrder order = queueList.getSelectedValue();
            if (order != null) {
                showRiderOrder(order);
            }
        });

        JPanel active = new JPanel(new BorderLayout(8, 8));
        active.setBackground(Theme.PANEL);
        active.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.LINE),
                new EmptyBorder(16, 16, 16, 16)
        ));
        active.add(sectionTitle("งานจัดส่งปัจจุบัน"), BorderLayout.NORTH);
        active.add(activeDeliveryText, BorderLayout.CENTER);
        active.setPreferredSize(new Dimension(LEFT_COLUMN_WIDTH, 150));
        active.setMinimumSize(new Dimension(LEFT_COLUMN_WIDTH, 130));

        JPanel queuePanel = new JPanel(new BorderLayout(10, 10));
        queuePanel.setBackground(Theme.PANEL);
        queuePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.LINE),
                new EmptyBorder(16, 16, 16, 16)
        ));
        queuePanel.setPreferredSize(new Dimension(LEFT_COLUMN_WIDTH, 0));
        queuePanel.setMinimumSize(new Dimension(LEFT_COLUMN_WIDTH, 260));
        queuePanel.add(sectionTitle("คิวผู้จัดส่ง"), BorderLayout.NORTH);
        JScrollPane queueScroll = new JScrollPane(queueList);
        queueScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        queueScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        queuePanel.add(queueScroll, BorderLayout.CENTER);

        JPanel actions = transparent(new GridLayout(1, 2, 8, 0));
        actions.setPreferredSize(new Dimension(0, 46));
        JButton accept = primaryButton("รับงานถัดไป");
        JButton delivered = secondaryButton("ส่งสำเร็จ");
        accept.addActionListener(e -> acceptNextDelivery());
        delivered.addActionListener(e -> markDelivered());
        actions.add(accept);
        actions.add(delivered);

        JPanel queueFooter = new JPanel(new BorderLayout(0, 8));
        queueFooter.setOpaque(false);
        riderPlanHintText.setBorder(new EmptyBorder(2, 2, 2, 2));
        queueFooter.add(riderPlanHintText, BorderLayout.NORTH);
        queueFooter.add(actions, BorderLayout.SOUTH);
        queuePanel.add(queueFooter, BorderLayout.SOUTH);

        JPanel left = new JPanel(new GridBagLayout());
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(LEFT_COLUMN_WIDTH, 0));
        left.setMinimumSize(new Dimension(LEFT_COLUMN_WIDTH, 0));
        GridBagConstraints leftConstraints = new GridBagConstraints();
        leftConstraints.gridx = 0;
        leftConstraints.fill = GridBagConstraints.BOTH;
        leftConstraints.weightx = 1;
        leftConstraints.insets = new Insets(0, 0, 9, 0);
        leftConstraints.gridy = 0;
        leftConstraints.weighty = 0;
        left.add(active, leftConstraints);
        leftConstraints.gridy = 1;
        leftConstraints.weighty = 1;
        leftConstraints.insets = new Insets(9, 0, 0, 0);
        left.add(queuePanel, leftConstraints);

        JPanel right = new JPanel(new BorderLayout(12, 12));
        right.setBackground(Theme.BACKGROUND);
        right.add(riderMapPanel, BorderLayout.CENTER);

        page.add(left, BorderLayout.WEST);
        page.add(right, BorderLayout.CENTER);
        return page;
    }

    // โหลดรายการเมนูใหม่ตามประเภทที่ผู้ใช้เลือกใน combo box
    void refreshMenu() {
        String category = Objects.toString(categoryBox.getSelectedItem(), "อาหาร");
        menuModel.clear();
        for (MenuItem item : system.menuByCategory(category)) {
            menuModel.addElement(item);
        }
        menuList.clearSelection();
        previewRouteForSelection();
    }

    // รับข้อมูลจากหน้าลูกค้า สร้างออเดอร์ใหม่ และเพิ่มเข้า queue ของระบบ
    void placeOrder(ActionEvent event) {
        MenuItem item = menuList.getSelectedValue();
        CustomerLocation location = (CustomerLocation) locationBox.getSelectedItem();
        if (item == null || location == null) {
            JOptionPane.showMessageDialog(this, "กรุณาเลือกเมนูและที่อยู่จัดส่งก่อน");
            return;
        }

        DeliveryOrder order = system.placeOrder(item, location);
        showOrder(order);
        refreshAll();
        JOptionPane.showMessageDialog(this, "เพิ่มออเดอร์ " + order.displayId() + " เข้าคิวเรียบร้อยแล้ว");
    }

    // ให้ร้านค้าตัดรอบ 10 นาที: เตรียมเฉพาะออเดอร์ในรอบเวลาปัจจุบันก่อน
    void prepareBatch() {
        BatchResult result = system.prepareTenMinuteBatch();
        refreshAll();
        if (result.orders().isEmpty()) {
            JOptionPane.showMessageDialog(this, result.message());
            return;
        }
        showOrder(result.orders().get(0));
        JOptionPane.showMessageDialog(this, result.message());
    }

    // สร้างข้อมูลตัวอย่างสำหรับ demo โดยไม่เพิ่มซ้ำถ้ามีออเดอร์อยู่แล้ว
    void seedDemoOrders() {
        system.seedDemoOrders();
        refreshAll();
        if (!system.sortedOrders().isEmpty()) {
            showOrder(system.sortedOrders().get(0));
        }
    }

    // ผู้จัดส่งรับงานถัดไปจาก priority queue และแสดงเป็นงานปัจจุบัน
    void acceptNextDelivery() {
        if (activeDelivery != null) {
            JOptionPane.showMessageDialog(this, "กรุณาส่งงานปัจจุบันให้สำเร็จก่อนรับงานถัดไป");
            return;
        }
        DeliveryOrder next = system.acceptNextForRider();
        if (next == null) {
            JOptionPane.showMessageDialog(this, "ยังไม่มีออเดอร์ที่ร้านเตรียมเสร็จสำหรับรอบนี้");
            return;
        }
        activeDelivery = next;
        RouteResult riderRoute = system.riderRouteTo(next);
        activeDeliveryText.setText(next.displayId() + "\n"
                + next.item().name() + "\n"
                + next.location().name() + " - ถึงโดยประมาณ " + next.etaMinutes(riderRoute) + " นาที");
        showRiderOrder(next);
        refreshAll();
    }

    // ปิดงานจัดส่งปัจจุบันและเปลี่ยนสถานะเป็นส่งสำเร็จ
    void markDelivered() {
        if (activeDelivery == null) {
            JOptionPane.showMessageDialog(this, "กรุณารับงานก่อน");
            return;
        }
        boolean completedRound = system.markDelivered(activeDelivery);
        activeDelivery = null;
        activeDeliveryText.setText("ยังไม่มีงานจัดส่ง");
        refreshAll();
        setRouteOnMaps(null);
        if (completedRound) {
            JOptionPane.showMessageDialog(this, "ส่งครบทั้งรอบแล้ว ผู้จัดส่งกลับไปเริ่มที่ร้านอาหาร และร้านสามารถเตรียมรอบถัดไปได้");
        }
    }

    // แสดงเส้นทาง preview บนแผนที่ตามที่อยู่ที่เลือก แม้ยังไม่ได้สั่งอาหาร
    void previewRouteForSelection() {
        CustomerLocation location = (CustomerLocation) locationBox.getSelectedItem();
        if (location == null) {
            customerMapPanel.setRoute(null);
            return;
        }
        RouteResult route = system.routeTo(location.nodeId());
        setRouteOnMaps(route);
    }

    void showOrder(DeliveryOrder order) {
        setRouteOnMaps(order.route());
    }

    // แสดงเส้นทางตามแผนส่งของรอบนั้น เช่น ร้าน -> บ้านแรก -> บ้านถัดไป
    void showRiderOrder(DeliveryOrder order) {
        riderMapPanel.setRoute(system.plannedLegRouteTo(order));
        riderMapPanel.setCurrentNode(system.plannedLegStartNode(order));
        DeliveryOrder previousOrder = system.previousOpenOrderBefore(order);
        if (previousOrder == null) {
            riderMapPanel.setPreviewWarning(null, false);
            riderPlanHintText.setText(order.displayId() + " เป็นงานถัดไปตามแผน รับงานนี้ได้เลย");
            return;
        }
        riderMapPanel.setPreviewWarning("ต้องส่ง " + previousOrder.displayId() + " ก่อน จึงจะไป " + order.displayId(), true);
        riderPlanHintText.setText("ดูเส้นทางล่วงหน้าได้ แต่ต้องส่ง " + previousOrder.displayId()
                + " ก่อนจึงจะไป " + order.displayId());
    }

    // ซิงก์เส้นทางเดียวกันไปยังแผนที่ในหน้าลูกค้าและหน้าผู้จัดส่ง
    void setRouteOnMaps(RouteResult route) {
        customerMapPanel.setRoute(route);
        riderMapPanel.setRoute(route);
        customerMapPanel.setPreviewWarning(null, false);
        riderMapPanel.setPreviewWarning(null, false);
    }

    // อัปเดตตารางร้านค้า รายการคิวผู้จัดส่ง และตัวเลขสถิติบน header
    void refreshAll() {
        List<DeliveryOrder> sorted = system.sortedOrders();
        orderTableModel.setRowCount(0);
        for (DeliveryOrder order : sorted) {
            orderTableModel.addRow(new Object[]{
                    order.displayId(),
                    order.item().name(),
                    order.item().category(),
                    order.location().name(),
                    system.displayRouteForOrder(order).distance() + " นาที",
                    order.orderTimeLabel(),
                    order.statusLabel()
            });
        }

            riderQueueModel.clear();
            for (DeliveryOrder order : system.riderQueueOrders()) {
                riderQueueModel.addElement(order);
            }
            if (riderQueueModel.isEmpty() && activeDelivery == null) {
                riderPlanHintText.setText("เลือกออเดอร์ในคิวเพื่อดูเส้นทางตามแผน");
            }

        queueCountLabel.setText(String.valueOf(system.waitingCount()));
        topOrderLabel.setText(system.nextOpenOrderLabel());
        currentRoundLabel.setText(system.currentRoundLabel());
        updateOpenElapsedTime();
        riderMapPanel.setCurrentNode(system.riderCurrentNode());
        customerMapPanel.repaint();
        riderMapPanel.repaint();
    }

    // อัปเดตเวลานับตั้งแต่เปิดร้านบน header โดยไม่ไปกระทบ layout ส่วนอื่น
    void updateOpenElapsedTime() {
        openElapsedLabel.setText(system.elapsedSinceOpenLabel());
    }
}
