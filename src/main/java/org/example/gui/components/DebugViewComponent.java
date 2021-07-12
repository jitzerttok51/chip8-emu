package org.example.gui.components;

import org.example.core.decompiler.CPUDecompiler;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static java.awt.BorderLayout.CENTER;

public class DebugViewComponent extends JPanel {

    private final JList<String> view;
    private int index = -1;

    public DebugViewComponent(short[] rom) {
        setLayout(new BorderLayout());
        view = new JList<>();
        view.setFixedCellWidth(200);
        view.setCellRenderer(new ListCellRenderer(()->this.index));
        // view.setSelectionModel(new NoSelectionModel());

        setRom(rom);
        add(new JScrollPane(view), CENTER);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
    }

    public void setRom(short[] rom) {
        view.setListData(testDecompiler(rom));
        view.repaint();
    }

    private String[] mapOpcodeToString(short[] rom) {
        List<String> list = new ArrayList<>();
        for(var opcode : rom) {
            list.add(String.format("    0x%04X",opcode));
        }
        return list.toArray(String[]::new);
    }

    private String[] mapInstrToString(short[] rom) {
        List<String> list = new ArrayList<>();
        for(var opcode : rom) {
            var mainOp = Short.toUnsignedInt(opcode) >> 12;

            var result = String.format("    0x%04X",opcode);
            switch (mainOp) {
                case 0x6: {
                    byte register = (byte) ((opcode >> 8) & 0x0F);
                    byte value = (byte) (opcode & 0x00FF);
                    result = "    V"+String.format("%X",register)+" := "+String.format("%X",value);
                } break;
                case 0xA: {
                    short value = (short) (opcode & 0x0FFF);
                    result = "    I"+" := "+String.format("%X",value);
                } break;
            }
            list.add(result);
        }
        return list.toArray(String[]::new);
    }

    private String[] testDecompiler(short[] rom) {
        var decompiler = CPUDecompiler.getDefault();
        return decompiler
                .run(rom)
                .stream()
                .map(s->"    "+s)
                .toArray(String[]::new);
    }

    public void setSelectedOpcode(int index) {
        this.index = index;
        view.repaint();
        index+=10;
        if(index>=view.getModel().getSize()) {
            index = view.getModel().getSize() - 1;
        }
        view.ensureIndexIsVisible(index);
    }

    private static class ListCellRenderer extends DefaultListCellRenderer {

        private final Supplier<Integer> index;
        public ListCellRenderer(Supplier<Integer> index) {
            this.index = index;
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if(index == this.index.get()) {
                setBackground(Color.YELLOW);
            }
            return this;
        }
    }
}
