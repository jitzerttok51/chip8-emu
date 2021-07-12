package org.example.gui.components;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class MemoryComponent extends JPanel {

    private JTable table;
    private MemoryModel model;

    private static final byte[] ZERO_MEMORY = new byte[4096];

    public MemoryComponent() {
        add(init());
    }

    private Component init() {
        model = new MemoryModel(ZERO_MEMORY);
        table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setDefaultRenderer(Object.class, new HexRenderer());

        table
                .getColumnModel()
                .getColumn(0)
                .setCellRenderer(new ColorRenderer(table.getTableHeader().getBackground()));

        for(int i=0; i<=0xF; i++) {
            table.getColumnModel().getColumn(i+1).setHeaderValue(String.format("%X", i));
        }
        table.getColumnModel().getColumn(0).setHeaderValue("");
        return new JScrollPane(table);

    }

    public void setMemory(byte[] memory) {
        if(memory.length != 4096) {
            throw new IllegalArgumentException("CPU memory must be 4096 bytes");
        }
        model.setMemory(memory);
        table.repaint();
    }

    public void resetMemory() {
        setMemory(ZERO_MEMORY);
    }

    private static class HexRenderer extends DefaultTableCellRenderer {

        @Override
        protected void setValue(Object value) {
            if(value instanceof Byte) {
                Byte v = (Byte) value;
                setText(String.format("%02X", v));
            } else {
                setText(value.toString());
            }
        }
    }

    private static class ColorRenderer extends DefaultTableCellRenderer {
        private final Color color;

        private ColorRenderer(Color color) {
            this.color = color;
        }

        @Override
        protected void setValue(Object value) {
            super.setValue(value);
            if(value instanceof Byte) {
                Byte v = (Byte) value;
                setText(String.format("%02X", v));
            } else {
                setText(value.toString());
            }
            setBackground(color);
        }
    }

    private static class MemoryModel extends AbstractTableModel {

        private byte[] memory;

        private MemoryModel(byte[] memory) {
            this.memory = memory;
            memory[1] = 0x4F;
        }

        @Override
        public int getRowCount() {
            return this.memory.length / 16;
        }

        @Override
        public int getColumnCount() {
            return 17;
        }

        @Override
        public Object getValueAt(int y, int x) {
            if(x==0) {
                return (byte) y;
            } else {
                return this.memory[16*y + x - 1];
            }

        }

        public void setMemory(byte[] memory) {
            this.memory = memory;
        }
    }
}
