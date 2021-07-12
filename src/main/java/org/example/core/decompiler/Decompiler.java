package org.example.core.decompiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Decompiler {

    public enum DigitMatch {
        FIRST (0xF000),
        SECOND(0x0F00),
        THIRD (0x00F0),
        FORTH (0x000F);

        public final int mask;

        DigitMatch(int mask) {
            this.mask = mask;
        }

        public int getMask() {
            return mask;
        }
    }

    public static class Builder {
        private int mask;
        private Instruction def;
        private final Map<Integer, Instruction> map = new HashMap<>();

        public Builder setMask(int mask) {
            this.mask = mask;
            return this;
        }

        public Builder setMask(DigitMatch match1) {
            this.mask = match1.getMask();
            return this;
        }

        public Builder setMask(DigitMatch match1, DigitMatch match2) {
            this.mask = match1.getMask() | match2.getMask();
            return this;
        }

        public Builder setMask(DigitMatch match1, DigitMatch match2, DigitMatch match3) {
            this.mask = match1.getMask() | match2.getMask() | match3.getMask();
            return this;
        }

        public Builder setDefInstr(Instruction in) {
            this.def = in;
            return this;
        }

        public Builder setInstr(int matcher, Instruction in) {
            this.map.put(matcher, in);
            return this;
        }

        public Decompiler build() {
            return new Decompiler(mask, def, map);
        }
    }

    private final Map<Integer, Instruction> map = new HashMap<>();
    private final int mask;
    private final Instruction def;

    public Decompiler(int mask, Instruction def, Map<Integer, Instruction> map) {
        this.mask = mask;
        this.def = def;
        this.map.putAll(map);
    }

    public List<String> run(short[] opcodes) {
        List<String> list = new ArrayList<>();

        for(var opcode : opcodes) {
            var match = opcode & mask;
            var instr = map.getOrDefault(match, def);
            var result = instr.decompile(opcode);
            list.add(result);
        }

        return list;
    }
}
