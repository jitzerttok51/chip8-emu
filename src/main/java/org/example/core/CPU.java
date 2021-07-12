package org.example.core;

import java.util.ArrayDeque;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class CPU {


    private final byte[] V = new byte[0x10];
    private short I = 0;
    private short PC = 0x200;

    private final byte[] memory = new byte[4096];
    private boolean halt = false;
    private static final int REFRESH_RATE = 60; //Hz
    private Random random = new Random();

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

    private final ArrayDeque<Short> stack = new ArrayDeque<>();

    private CPUAgent[] cpuAgents = new CPUAgent[0];

    private final Display display;
    private final Controls controls;

    private final AtomicInteger delayRegister = new AtomicInteger();
    private final AtomicInteger soundRegister = new AtomicInteger();

    public CPU(byte[] rom, Display display, Controls controls) {

        int totalSize = memory.length - 0x200;
        if (rom.length > totalSize) {
            throw new IllegalArgumentException("Program size to big for CPU. Total size should be: "+totalSize);
        }
        for(int i=0; i<FONT.length; i++) {
            memory[i] = (byte) FONT[i];
        }
        System.arraycopy(rom, 0, memory, 0x200, rom.length);
        this.display = display;
        this.controls = controls;

        Timer timer = new Timer("Timers");
        timer.scheduleAtFixedRate(wrap(()->{
            if(delayRegister.get() > 0) {
                delayRegister.decrementAndGet();
            }
        }), 1000L / REFRESH_RATE, 1000L / REFRESH_RATE);
        timer.scheduleAtFixedRate(wrap(()->{
            if(soundRegister.get() > 0) {
                soundRegister.decrementAndGet();
            }
        }), 0,1000L / REFRESH_RATE);

    }

    private static TimerTask wrap(Runnable r) {
        return new TimerTask() {

            @Override
            public void run() {
                r.run();
            }
        };
    }

    public CPU(byte[] rom, Display display, Controls controls, CPUAgent... agents) {
        this(rom, display, controls);
        this.cpuAgents = agents;
    }

    public void run() {
        long cycleLength = Math.round((1.0f / REFRESH_RATE) * 1e9);
        while (!halt) {
            long before = System.nanoTime();
            short opcode = fetch();
            execute(opcode);
            var ctx = new CPUContext(this, opcode);
            for(var agent : cpuAgents) {
                agent.postCycle(ctx);
                halt = agent.isHalt();
            }
            long now = System.nanoTime();

            long timeToWait = (cycleLength - (now - before));
            if (timeToWait>0) {
                long start = System.nanoTime();
                while((System.nanoTime() - start) > timeToWait);
            }
        }
    }

    private short fetch() {
        byte upper = memory[PC++];
        byte lower = memory[PC++];
        return (short)((Byte.toUnsignedInt(upper) << 8) + Byte.toUnsignedInt(lower));
    }

    private void execute(short opcode) {
        int opByte = Short.toUnsignedInt(opcode) >> 12;

        switch (opByte) {
            case 0: {
                if(opcode == 0x00EE) {
                    PC = stack.pop();
                }
                if(opcode == 0x00E0) {
                    display.clear();
                }
            } break;
            case 1:
                PC = (short) (opcode & 0x0FFF);
                break;
            case 2:
                stack.push(PC);
                PC = (short) (opcode & 0x0FFF);
                break;
            case 3: {
                byte register = (byte) ((opcode >> 8) & 0x0F);
                byte value = (byte) (opcode & 0x00FF);
                if (V[register] == value) {
                    PC+=2;
                }
            } break;
            case 4: {
                byte register = (byte) ((opcode >> 8) & 0x0F);
                byte value = (byte) (opcode & 0x00FF);
                if (V[register] != value) {
                    PC+=2;
                }
            } break;
            case 5: {
                byte registerX = (byte) ((opcode >> 8) & 0x0F);
                byte registerY = (byte) ((opcode >> 4) & 0x00F);
                if (V[registerX] == V[registerY]) {
                    PC+=2;
                }
            } break;
            case 9: {
                byte registerX = (byte) ((opcode >> 8) & 0x0F);
                byte registerY = (byte) ((opcode >> 4) & 0x00F);
                if (V[registerX] != V[registerY]) {
                    PC+=2;
                }
            } break;
            case 6: {
                byte register = (byte) ((opcode >> 8) & 0x0F);
                byte value = (byte) (opcode & 0x00FF);
                V[register] = value;
            } break;
            case 7: {
                byte register = (byte) ((opcode >> 8) & 0x0F);
                byte value = (byte) (opcode & 0x00FF);
                V[register] += value;
            } break;
            case 8: {
                var lastDigit = opcode & 0x000F;
                byte registerX = (byte) ((opcode >> 8) & 0x0F);
                byte registerY = (byte) ((opcode >> 4) & 0x00F);
                switch (lastDigit) {
                    case 0:
                        V[registerX] = V[registerY];
                        break;
                    case 1:
                        V[registerX] = (byte) (V[registerX]|V[registerY]);
                        break;
                    case 2:
                        V[registerX] = (byte) (V[registerX]&V[registerY]);
                        break;
                    case 3:
                        V[registerX] = (byte) (V[registerX]^V[registerY]);
                        break;
                    case 4: {
                        var result =
                                Byte.toUnsignedInt(V[registerX])
                                        +Byte.toUnsignedInt(V[registerY]);

                        V[0xF] = 0;
                        if(result > 255) {
                            V[0xF] = 1;
                            result -= 256;
                        }
                        V[registerX] = (byte) result;
                    } break;
                    case 5: {
                        var result =
                                Byte.toUnsignedInt(V[registerX]) - Byte.toUnsignedInt(V[registerY]);

                        V[0xF] = 0;
                        if(V[registerX]>=V[registerY]) {
                            V[0xF] = 1;
                        }
                        if(result<0) {
                            result+=256;
                        }
                        V[registerX] = (byte) result;
                    } break;
                    case 7: {
                        var result =
                                Byte.toUnsignedInt(V[registerY]) - Byte.toUnsignedInt(V[registerX]);

                        V[0xF] = 0;
                        if(V[registerY]>=V[registerX]) {
                            V[0xF] = 1;
                        }
                        if(result<0) {
                            result+=255;
                        }
                        V[registerX] = (byte) result;
                    } break;
                    case 6: {
                        V[0xF] = 0;
                        if((V[registerX] & 0x01) != 0) {
                            V[0xF] = 1;
                        }
                        V[registerX] = (byte) (Byte.toUnsignedInt(V[registerX]) >> 1);
                    } break;
                    case 0xE: {
                        V[0xF] = 0;
                        if((V[registerX] & 0x80) != 0) {
                            V[0xF] = 1;
                        }
                        V[registerX] = (byte) (Byte.toUnsignedInt(V[registerX]) << 1);
                    } break;
                }

            } break;
            case 0xA:
                I = (short) (opcode & 0x0FFF);
                break;
            case 0xB:
                PC = (short) (V[0] + (short) (opcode & 0x0FFF));
                break;
            case 0xC: {
                byte registerX = (byte) ((opcode >> 8) & 0x0F);
                byte n = (byte) (opcode & 0x00FF);
                V[registerX] = (byte) (random.nextInt(0x100) & n);
            } break;
            case 0xD: {
                var n = opcode & 0x000F;
                byte registerX = (byte) ((opcode >> 8) & 0x0F);
                byte registerY = (byte) ((opcode >> 4) & 0x00F);

                var x = V[registerX];
                var y = V[registerY];

                x = (byte) (x % (byte) Display.WIDTH);
                y = (byte) (y % (byte) Display.HEIGHT);

                V[0xF] = 0;
                for(int i=0; i<n; i++) {
                    if(y+i>=Display.HEIGHT) {
                        continue;
                    }
                    var part = memory[I+i];
                    for(int j=0; j<8; j++) {
                        if(x+j>=Display.WIDTH) {
                            continue;
                        }
                        var mask = 0x80 >> j;
                        var p = display.getPixel(x+j, y+i);
                        var res = ((part & mask) != 0) ? 1 : 0;
                        var np = p ^ res;
                        display.setPixel(x+j, y+i, np);
                        if(p == 1 && np == 0) {
                            V[0xF] = 1;
                        }
                    }
                }
            } break;
            case 0xE: {
                var operation = opcode & 0xFF;
                var registerX = (opcode >> 8) & 0xF;
                switch (operation) {
                    case 0x9E:
                        if(controls.isKeyPressed(V[registerX])) {
                            PC+=2;
                        }
                        break;
                    case 0xA1:
                        if(controls.isKeyNotPressed(V[registerX])) {
                            PC+=2;
                        }
                        break;
                }
            } break;
            case 0xF: {
                var operation = opcode & 0xFF;
                var registerRange = (opcode >> 8) & 0xF;
                switch (operation) {
                    case 0x07:
                        V[registerRange] = delayRegister.byteValue();
                        break;
                    case 0x15:
                        delayRegister.set(V[registerRange]);
                        break;
                    case 0x18:
                        soundRegister.set(V[registerRange]);
                        break;
                    case 0x0A: {
                        var key = controls.waitForKeyPress();
                        if(key<0) {
                            PC-=2;
                        } else {
                            V[registerRange] = key;
                        }
                    } break;
                    case 0x1E:
                        I+=V[registerRange];
                        break;
                    case 0x29:
                        I= (short) (V[registerRange] * 5);
                        break;
                    case 0x33: {
                        var x = Byte.toUnsignedInt(V[registerRange]);
                        var f = (byte) (x / 100);
                        var s = (byte) (x / 10 % 10);
                        var t = (byte) (x % 10);
                        memory[I] = f;
                        memory[I + 1] = s;
                        memory[I + 2] = t;
                    } break;
                    case 0x55:
                        System.arraycopy(V, 0, memory, I, registerRange + 1);
                        break;
                    case 0x65:
                        System.arraycopy(memory, I, V, 0, registerRange + 1);
                        break;
                }
            } break;
        }
    }

    public void halt() {
        halt = true;
    }

    public void setRandom(Random random) {
        this.random = random;
    }

    public Display getDisplay() {
        return display;
    }

    private static class CPUContext implements CPUAgentContext {

        private final byte[] memory;
        private final byte[] registers;
        private final short PC, I, opcode;


        public CPUContext(CPU cpu, short opcode) {
            PC = cpu.PC;
            I = cpu.I;
            this.opcode = opcode;
            memory = new byte[cpu.memory.length];
            System.arraycopy(cpu.memory, 0, memory, 0, memory.length);
            registers = new byte[cpu.V.length];
            System.arraycopy(cpu.V, 0, registers, 0, cpu.V.length);
        }

        @Override
        public byte[] getMemorySnapshot() {
            return memory;
        }

        @Override
        public byte getRegisterValue(int v) {
            return registers[v];
        }

        @Override
        public short getIRegisterValue() {
            return I;
        }

        @Override
        public short getPCRegisterValue() {
            return PC;
        }

        @Override
        public short getCurrentOpcode() {
            return opcode;
        }
    }
}
