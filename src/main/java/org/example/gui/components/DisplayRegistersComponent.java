package org.example.gui.components;

import org.example.utils.Pair;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;

public class DisplayRegistersComponent extends JPanel {

    private List<Consumer<String>> registerSetters = new ArrayList<>();

    private Consumer<String> IReg, PCReg;

    public DisplayRegistersComponent() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        var gb = new GridBagConstraints();

        gb.gridy = 0;
        for(int i=0; i<16/2; i++) {
            gb.gridx = 0;
            gb.insets = new Insets(0,0, 0, 5);
            var name = String.format("V%X", (byte)(i*2));
            var result = registerBox(name);
            registerSetters.add(result.getSecond());
            panel.add(result.getFirst(), gb);

            gb.gridx = 1;
            gb.insets = new Insets(0,5, 0, 0);
            name = String.format("V%X", (byte)(i*2+1));
            result = registerBox(name);
            registerSetters.add(result.getSecond());
            panel.add(result.getFirst(), gb);

            gb.gridy++;
        }

        gb.gridx = 0;
        gb.insets = new Insets(0,0, 0, 5);
        var result = registerBox("PC", shortToStr((short) 0));
        this.PCReg = result.getSecond();
        panel.add(result.getFirst(), gb);

        gb.gridx = 1;
        gb.insets = new Insets(0,5, 0, 0);
        result = registerBox("I", shortToStr((short) 0));
        this.IReg = result.getSecond();
        panel.add(result.getFirst(), gb);

        setLayout(new GridBagLayout());
        gb.gridx = 0;
        gb.gridy = 0;
        gb.insets = new Insets(10, 40, 10, 40);
        add(panel, gb);
    }

    private Pair<Component, Consumer<String>> registerBox(String name, String value) {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.add(new JLabel(name));
        var field = new JTextField(3);
        field.setHorizontalAlignment(SwingConstants.CENTER);
        field.setEditable(false);
        field.setText(value);
        panel.add(field);
        return new Pair<>(panel, field::setText);
    }

    private Pair<Component, Consumer<String>> registerBox(String name) {
        return registerBox(name, byteToStr((byte) 0));
    }

    private Pair<Component, Consumer<String>> registerBox(String name, short value) {
        return registerBox(name, shortToStr(value));
    }

    public void setRegisterValue(int index, byte value) {
        registerSetters.get(index).accept(byteToStr(value));
    }

    public void setPCValue(short value) {
        this.PCReg.accept(shortToStr(value));
    }

    public void setIValue(short value) {
        this.IReg.accept(shortToStr(value));
    }

    public void resetRegisters() {
        registerSetters.forEach(setter->setter.accept(byteToStr((byte) 0)));
        this.PCReg.accept(shortToStr((short) 0));
        this.IReg.accept(shortToStr((short) 0));
    }

    private String byteToStr(byte b) {
        return String.format("%02X", b);
    }

    private String shortToStr(short b) {
        return String.format("%04X", b);
    }
}
