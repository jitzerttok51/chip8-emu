package org.example.gui.components;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class Editor extends JPanel {

    private Supplier<String> text;

    public Editor() {
        this.add(init());
    }

    private Component init() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        JTextArea area = new JTextArea(20,50);
        area.setLineWrap(true);

        text = area::getText;

        var gb = new GridBagConstraints();
        gb.gridx = 0;
        gb.anchor = GridBagConstraints.PAGE_START;
        JLabel label = new JLabel("<html>1<br/></html>");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBorder(BorderFactory.createEmptyBorder(0,5,0,10));
        panel.add(label, gb);

        gb.gridx = 1;
        panel.add(area, gb);

        area.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void removeUpdate(DocumentEvent e) {
                onUpdate();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                onUpdate();
            }

            @Override
            public void changedUpdate(DocumentEvent arg0) {

            }

            private void onUpdate() {
                var lineData = getLines(area.getText())
                        .stream()
                        .mapToInt(String::length)
                        .map(len->len/area.getColumns()+1)
                        .toArray();
                label.setText(regenerateLineNumber(lineData));
            }
        });

        return new JScrollPane(panel);

    }

    private static String regenerateLineNumber(int[] lineData) {
        var sb = new StringBuilder("<html>");
        for(int i=0; i<lineData.length; i++) {
            sb.append(i+1);
            for(int j=0; j<lineData[i]; j++) {
                sb.append("<br/>");
            }
        }
        sb.append("</html>");
        return sb.toString();
    }


    private List<String> getLines(String text) {
        List<String> lines = new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        for(var c : text.toCharArray()) {
            if (c=='\n') {
                lines.add(sb.toString());
                sb.delete(0, sb.length());
            } else {
                sb.append(c);
            }
        }
        lines.add("");
        return lines;
    }

    public String getText() {
        return text.get();
    }
}
