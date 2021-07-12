package org.example.core.decompiler;

public class Instructions {

    public static Instruction goTo = opcode -> {
      var N = opcode & 0x0FFF;
      return "goto: " + N;
    };

    public static Instruction call = opcode -> {
        var N = opcode & 0x0FFF;
        return "call: " + N;
    };

    public static Instruction iF3 = opcode -> {
        var N = opcode & 0x00FF;
        var reg = (opcode >> 8) & 0xF;
        return String.format("if(V%X==%X)", reg, N);
    };

    public static Instruction iF4 = opcode -> {
        var N = opcode & 0x00FF;
        var reg = (opcode >> 8) & 0xF;
        return String.format("if(V%X!=%X)", reg, N);
    };

    public static Instruction iF5 = opcode -> {
        var regX = (opcode >> 8) & 0xF;
        var regY = (opcode >> 4) & 0xF;
        return String.format("if(V%X==V%X)", regX, regY);
    };

    public static Instruction assign = opcode -> {
        var N = opcode & 0x00FF;
        var reg = (opcode >> 8) & 0xF;
        return String.format("V%X=%X", reg, N);
    };

    public static Instruction append = opcode -> {
        var N = opcode & 0x00FF;
        var reg = (opcode >> 8) & 0xF;
        return String.format("V%X+=%X", reg, N);
    };

    public static Instruction iF9 = opcode -> {
        var regX = (opcode >> 8) & 0xF;
        var regY = (opcode >> 4) & 0xF;
        return String.format("if(V%X!=V%X)", regX, regY);
    };

    public static Instruction assignI = opcode -> {
        var N = opcode & 0x0FFF;
        return String.format("I=%X", N);
    };

    public static Instruction goToVX = opcode -> {
        var N = opcode & 0x0FFF;
        return String.format("PC=V0+%X", N);
    };

    public static Instruction random = opcode -> {
        var N = opcode & 0x00FF;
        var reg = (opcode >> 8) & 0xF;
        return String.format("V%X=rand()&%X", reg, N);
    };

    public static Instruction def = opcode -> String.format("0x%04X", opcode);
}
