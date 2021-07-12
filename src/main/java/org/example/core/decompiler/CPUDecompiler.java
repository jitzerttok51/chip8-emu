package org.example.core.decompiler;

public class CPUDecompiler {

    public static Decompiler getDefault() {
        var builder = new Decompiler.Builder();
        builder.setMask(Decompiler.DigitMatch.FIRST);
        builder.setDefInstr(Instructions.def);

        builder.setInstr(0x1000, Instructions.goTo);
        builder.setInstr(0x2000, Instructions.call);
        builder.setInstr(0x3000, Instructions.iF3);
        builder.setInstr(0x4000, Instructions.iF4);
        builder.setInstr(0x5000, Instructions.iF5);
        builder.setInstr(0x6000, Instructions.assign);
        builder.setInstr(0x7000, Instructions.append);
        builder.setInstr(0x9000, Instructions.iF9);
        builder.setInstr(0xA000, Instructions.assignI);
        builder.setInstr(0xB000, Instructions.goToVX);
        builder.setInstr(0xC000, Instructions.random);


        return builder.build();
    }
}
