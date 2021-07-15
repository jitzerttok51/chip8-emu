package org.example.core.instructions;

import org.example.core.components.Controls;
import org.example.core.components.Display;
import org.example.core.components.Memory;
import org.example.core.components.Registers;

import java.util.Deque;
import java.util.Random;

public interface ActionContext {

    Display display();
    Controls controls();
    Random random();
    Deque<Short> stack();
    Registers registers();
    Memory memory();

    default short getAddress(short opcode) {
        return (short) (opcode & 0x0FFF);
    }

    default byte getRegisterX(short opcode) {
        return (byte) ((opcode >> 8) & 0x0F);
    }

    default byte getRegisterY(short opcode) {
        return (byte) ((opcode >> 4) & 0x00F);
    }

    default byte getValue(short opcode) {
        return (byte) (opcode & 0x00FF);
    }

}
