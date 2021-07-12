package org.example.cpuv2;

import org.example.gui.components.SwingDisplay;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Run {

    public static void main(String[] args) throws IOException {
        Path roms = Paths.get("C:\\Users\\y509215\\Documents\\roms");
        Path program = roms.resolve("Zero Demo [zeroZshadow, 2007].ch8");

        var cpu = CPUBuilder.build(program);
        ((SwingDisplay) cpu.getDisplay()).exitOnClose(cpu::halt);
        cpu.run();
    }
}
