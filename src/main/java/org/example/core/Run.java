package org.example.core;

import org.example.gui.components.SwingDisplay;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Run {

    public static void main(String[] args) throws IOException {
        if(args.length<1) {
            throw new IllegalArgumentException("You must provide a path to the rom");
        }
        Path program = Paths.get(args[0]);

        var cpu = CPUBuilder.build(program);
        ((SwingDisplay) cpu.getDisplay()).exitOnClose(cpu::halt);
        cpu.run();
    }
}
