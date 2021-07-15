package org.example.core.instructions;

@FunctionalInterface
public interface Action {
    void execute(short opcode, ActionContext ctx);
}
