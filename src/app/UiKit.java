package app;

import java.awt.Color;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

final class UiKit {
    static final String UI_FONT = chooseUiFont();
    static final int LEFT_COLUMN_WIDTH = 390;

    static JPanel paddedPage(BorderLayout layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(Theme.BACKGROUND);
        panel.setBorder(new EmptyBorder(18, 22, 22, 22));
        return panel;
    }

    static JPanel transparent(java.awt.LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setOpaque(false);
        return panel;
    }

    static JLabel label(String text, int size, int style, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(UI_FONT, style, size));
        label.setForeground(color);
        label.setBorder(new EmptyBorder(2, 0, 4, 0));
        return label;
    }

    static JLabel sectionTitle(String text) {
        JLabel label = label(text, 22, Font.BOLD, Theme.INK);
        label.setBorder(new EmptyBorder(4, 0, 12, 0));
        return label;
    }

    static JLabel fieldLabel(String text) {
        return label(text, 13, Font.BOLD, Theme.MUTED);
    }

    static JTextArea textBlock(String text, int size, Color color) {
        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setFocusable(false);
        area.setOpaque(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setForeground(color);
        area.setFont(new Font(UI_FONT, Font.PLAIN, size));
        area.setBorder(new EmptyBorder(2, 0, 2, 0));
        return area;
    }

    static JPanel statCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(new Color(255, 255, 255, 185));
        card.setBorder(new EmptyBorder(10, 14, 10, 14));
        card.setPreferredSize(new Dimension(160, 92));
        JLabel titleLabel = label(title, 11, Font.BOLD, Theme.MUTED);
        valueLabel.setFont(new Font(UI_FONT, Font.BOLD, 19));
        valueLabel.setForeground(Theme.INK);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        valueLabel.setHorizontalAlignment(JLabel.CENTER);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 8, 0);
        card.add(titleLabel, c);

        c.gridy = 1;
        c.weighty = 1;
        c.insets = new Insets(0, 0, 0, 0);
        c.anchor = GridBagConstraints.CENTER;
        card.add(valueLabel, c);
        return card;
    }

    static JButton primaryButton(String text) {
        return button(text, Theme.LEAF, Color.WHITE);
    }

    static JButton secondaryButton(String text) {
        return button(text, new Color(244, 241, 229), Theme.INK);
    }

    static JButton button(String text, Color bg, Color fg) {
        JButton button = new PillButton(text, bg, fg);
        int textWidth = button.getFontMetrics(button.getFont()).stringWidth(text);
        button.setPreferredSize(new Dimension(Math.max(132, textWidth + 48), 48));
        button.setMinimumSize(new Dimension(Math.max(118, textWidth + 36), 46));
        return button;
    }

    static void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(new Font(UI_FONT, Font.PLAIN, 14));
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(Theme.INK);
        comboBox.setPreferredSize(new Dimension(0, 44));
        comboBox.setMinimumSize(new Dimension(0, 44));
        comboBox.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(Theme.LINE),
                new EmptyBorder(4, 10, 4, 10)
        ));
    }

    static Color lighten(Color color, int amount) {
        return new Color(
                Math.min(255, color.getRed() + amount),
                Math.min(255, color.getGreen() + amount),
                Math.min(255, color.getBlue() + amount)
        );
    }

    // เลือก font ที่รองรับภาษาไทยก่อน เพื่อให้สระและวรรณยุกต์แสดงไม่เพี้ยน
    static String chooseUiFont() {
        List<String> availableFonts = List.of(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        for (String font : List.of("Thonburi", "Noto Sans Thai", "Tahoma", "Arial Unicode MS", "Dialog")) {
            if (availableFonts.contains(font)) {
                return font;
            }
        }
        return "Dialog";
    }

    // ลดขนาด font ของปุ่มอัตโนมัติเมื่อข้อความยาวกว่าพื้นที่ปุ่ม
    static Font fontThatFits(Graphics2D g2, String text, Font baseFont, int maxWidth, int maxHeight) {
        int size = baseFont.getSize();
        while (size > 10) {
            Font candidate = baseFont.deriveFont((float) size);
            FontMetrics metrics = g2.getFontMetrics(candidate);
            if (metrics.stringWidth(text) <= maxWidth && metrics.getHeight() <= maxHeight) {
                return candidate;
            }
            size--;
        }
        return baseFont.deriveFont(10f);
    }

    // ตัดข้อความพร้อม ... เป็นทางเลือกสุดท้าย ถ้าลด font แล้วยังยาวเกิน
    static String textThatFits(Graphics2D g2, String text, Font font, int maxWidth) {
        FontMetrics metrics = g2.getFontMetrics(font);
        if (metrics.stringWidth(text) <= maxWidth) {
            return text;
        }

        String ellipsis = "...";
        int end = text.length();
        while (end > 0 && metrics.stringWidth(text.substring(0, end) + ellipsis) > maxWidth) {
            end--;
        }
        return end == 0 ? ellipsis : text.substring(0, end) + ellipsis;
    }

    // ไล่ตั้ง font หลักให้ทุก component เพื่อให้หน้าตาไทยสม่ำเสมอทั้งแอป
    static void applyBaseFont(Component component) {
        Font current = component.getFont();
        if (current != null) {
            component.setFont(new Font(UI_FONT, current.getStyle(), current.getSize()));
        }
        if (component instanceof JComponent jComponent) {
            jComponent.setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        if (component instanceof java.awt.Container container) {
            for (Component child : container.getComponents()) {
                applyBaseFont(child);
            }
        }
    }


    UiKit() {
    }
}
