package app;

import java.awt.Color;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.border.EmptyBorder;

final class PlaceholderComboBoxRenderer extends DefaultListCellRenderer {
    final String placeholder;

    PlaceholderComboBoxRenderer(String placeholder) {
        this.placeholder = placeholder;
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        setBorder(new EmptyBorder(4, 10, 4, 10));
        if (value == null) {
            setText(placeholder);
            setForeground(Theme.MUTED);
        } else {
            setText(value.toString());
            setForeground(isSelected ? Color.WHITE : Theme.INK);
        }
        return this;
    }
}
