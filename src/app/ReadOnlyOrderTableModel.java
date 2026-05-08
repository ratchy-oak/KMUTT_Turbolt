package app;

import javax.swing.table.DefaultTableModel;

final class ReadOnlyOrderTableModel extends DefaultTableModel {
    ReadOnlyOrderTableModel(String[] columns) {
        super(columns, 0);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
