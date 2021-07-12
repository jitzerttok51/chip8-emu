package org.example.gui.controllers;

import org.example.gui.panels.EditorPanel;
import org.example.utils.Utils;

import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;

public class EditorController {

    private final EditorPanel panel;

    public EditorController() {
        this.panel = new EditorPanel(this);
    }

    public void saveFile(ActionEvent e) {
        var text = panel.getText();
        var opt = panel.chooseFile();
        opt.ifPresent(path -> {
            try(var in = textToStream(text)) {
                Files.copy(in, path);
            } catch (Exception ex) {
                ex.printStackTrace();
                panel.showException(ex);
            }
        });
    }

    private InputStream textToStream(String text) {
        var lines = text.split("\n");
        int[] opcodes = Arrays
                .stream(lines)
                .filter(line->!line.isBlank())
                .map(line->line.replace("0x", ""))
                .mapToInt(line-> Integer.parseInt(line, 16))
                .toArray();

        var bytes = Utils.shortArrToBytes(Utils.intArrAsShort(opcodes));
        return new ByteArrayInputStream(bytes);
    }
}
