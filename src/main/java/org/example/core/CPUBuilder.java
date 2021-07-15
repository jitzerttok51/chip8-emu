package org.example.core;

import org.example.gui.components.SwingDisplay;
import org.example.utils.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CPUBuilder {

    private static final short[] FONT = {
            0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
            0x20, 0x60, 0x20, 0x20, 0x70, // 1
            0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
            0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
            0x90, 0x90, 0xF0, 0x10, 0x10, // 4
            0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
            0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
            0xF0, 0x10, 0x20, 0x40, 0x40, // 7
            0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
            0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
            0xF0, 0x90, 0xF0, 0x90, 0x90, // A
            0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
            0xF0, 0x80, 0x80, 0x80, 0xF0, // C
            0xE0, 0x90, 0x90, 0x90, 0xE0, // D
            0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
            0xF0, 0x80, 0xF0, 0x80, 0x80  // F
    };

    private static final int REFRESH_RATE = 300; //Hz

    public static CPUv2 build(byte[] rom) {
        var display = new SwingDisplay();
        return build(rom, display, display);
    }

    public static CPUv2 build(byte[] rom, Display display, Controls controls) {
        var memory = new Memory();
        memory.insertData(0x200, rom);
        memory.insertData(0, Utils.shortArrAsByte(FONT));

        var registers = new Registers();
        var mainClock = new Clock(REFRESH_RATE);
        var delayClock =  new Clock(REFRESH_RATE);
        var soundClock =  new Clock(REFRESH_RATE);

        return new CPUv2 (
                registers, memory,
                mainClock, delayClock, soundClock,
                display, controls);
    }

    public static CPUv2 build(Path program) throws IOException {
        return build(loadProgram(program));
    }

    private static byte[] loadProgram(Path path) throws IOException {
        if(Files.notExists(path)) {
            throw new IllegalArgumentException("Provided path does not exists");
        }

        if(Files.isDirectory(path)) {
            throw new IllegalArgumentException("Provided path points to a directory");
        }

        return Files.readAllBytes(path);
    }
}
