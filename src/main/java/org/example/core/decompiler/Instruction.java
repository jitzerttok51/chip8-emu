package org.example.core.decompiler;

@FunctionalInterface
public interface Instruction {
    String decompile(short opcode);
}
