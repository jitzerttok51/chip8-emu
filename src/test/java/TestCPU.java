import org.example.core.CPUBuilder;
import org.example.core.CPUv2;
import org.example.core.agent.CPUAgent;
import org.example.core.agent.CPUAgentContext;
import org.example.core.components.Controls;
import org.example.core.components.Display;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

public class TestCPU {

    private final AtomicInteger randomNumber = new AtomicInteger();

    @Test
    public void testIFCond() {
        var program = new short[] {
                0x640A, // 200
                0x340A, // 202
                0x120A, // 204
                0x6420, // 206
                0x0000, // 208
                0x6521, // 20A
        };

        testCPU(program, ctx -> {
            if (ctx.getPCRegisterValue() == 0x208) {
                assertEquals(ctx.getRegisterValue(4),0x20);
                return true;
            }
            if (ctx.getPCRegisterValue() > 0x208) {
                fail();
                return true;
            }
            return false;
        });
    }

    @Test
    public void testSubroutine() {
        var program = new short[] {
                0x120A, // 200
                0x7501, // 202
                0x00EE, // 204
                0x0000, // 206
                0x0000, // 208
                0x6520, // 20A
                0x2202, // 20C
                0x2202, // 20E
                0x2202, // 210
        };

        testCPU(program, ctx -> {
            if (ctx.getPCRegisterValue() == 0x212) {
                assertEquals(ctx.getRegisterValue(5),0x23);
                return true;
            }
            return false;
        });
    }

    @Test
    public void testSetRegValues() {
        var program = new short[] {
                0x6533,
                0x6421
        };

        testCPU(program, ctx -> {
            if (ctx.getPCRegisterValue() == 0x200 + 4) {
                assertEquals(ctx.getRegisterValue(5), 0x33);
                assertEquals(ctx.getRegisterValue(4), 0x21);
                return true;
            }
            return false;
        });
    }

    @Test
    public void testAssignment() {
        var program = new short[] {
                0x6533,
                (short) 0x8450
        };

        testCPU(program, ctx -> {
            if (ctx.getPCRegisterValue() == 0x200 + 4) {
                assertEquals(ctx.getRegisterValue(5), 0x33);
                assertEquals(ctx.getRegisterValue(4), 0x33);
                return true;
            }
            return false;
        });
    }

    @Test
    public void testBitwiseOR() {
        var program = new short[] {
                0x6523,
                0x6413,
                (short) 0x8451
        };

        testCPU(program, ctx -> {
            if (ctx.getPCRegisterValue() == 0x200 + 6) {
                assertEquals(ctx.getRegisterValue(5), 0x23);
                assertEquals(ctx.getRegisterValue(4), 0x23|0x13);
                return true;
            }
            return false;
        });
    }

    @Test
    public void testBitwiseAND() {
        var program = new short[] {
                0x6523,
                0x6413,
                (short) 0x8452
        };

        testCPU(program, ctx -> {
            if (ctx.getPCRegisterValue() == 0x200 + 6) {
                assertEquals(ctx.getRegisterValue(5), 0x23);
                assertEquals(ctx.getRegisterValue(4), 0x23&0x13);
                return true;
            }
            return false;
        });
    }

    @Test
    public void testBitwiseXOR() {
        var program = new short[] {
                0x6523,
                0x6413,
                (short) 0x8453
        };

        testCPU(program, ctx -> {
            if (ctx.getPCRegisterValue() == 0x200 + 6) {
                assertEquals(ctx.getRegisterValue(5), 0x23);
                assertEquals(ctx.getRegisterValue(4), 0x23^0x13);
                return true;
            }
            return false;
        });
    }

    @Test
    public void testAddition() {
        var program = new short[] {
                0x6523,
                0x6413,
                (short) 0x8454
        };

        testCPU(program, ctx -> {
            if (ctx.getPCRegisterValue() == 0x200 + 6) {
                assertEquals(ctx.getRegisterValue(5), 0x23);
                assertEquals(ctx.getRegisterValue(4), 0x23+0x13);
                assertEquals(ctx.getRegisterValue(0xF), 0);
                return true;
            }
            return false;
        });
    }

    @Test
    public void testAdditionWithCarry() {
        var program = new short[] {
                0x65FF,
                0x6402,
                (short) 0x8454
        };

        testCPU(program, ctx -> {
            if (ctx.getPCRegisterValue() == 0x200 + 6) {
                assertEquals(Byte.toUnsignedInt(ctx.getRegisterValue(5)), 0xFF);
                assertEquals(Byte.toUnsignedInt(ctx.getRegisterValue(4)), 0x02);
                assertEquals(ctx.getRegisterValue(0xF), 1);
                return true;
            }
            return false;
        });
    }

    @Test
    public void testSubtract() {
        var program = new short[] {
                0x6505,
                0x6404,
                (short) 0x8545
        };

        testCPU(program, ctx -> {
            if (ctx.getPCRegisterValue() == 0x200 + 6) {
                assertEquals(Byte.toUnsignedInt(ctx.getRegisterValue(5)), 0x01);
                assertEquals(Byte.toUnsignedInt(ctx.getRegisterValue(4)), 0x04);
                assertEquals(ctx.getRegisterValue(0xF), 1);
                return true;
            }
            return false;
        });
    }

    @Test
    public void testSubtractReverse() {
        var program = new short[] {
                0x6505,
                0x6404,
                (short) 0x8547
        };

        testCPU(program, ctx -> {
            if (ctx.getPCRegisterValue() == 0x200 + 6) {
                assertEquals(Byte.toUnsignedInt(ctx.getRegisterValue(5)), 254);
                assertEquals(Byte.toUnsignedInt(ctx.getRegisterValue(4)), 0x04);
                assertEquals(ctx.getRegisterValue(0xF), 0);
                return true;
            }
            return false;
        });
    }

    @Test
    public void testShiftLeft() {
        var program = new short[] {
                0x6502,
                (short) 0x8546
        };

        testCPU(program, ctx -> {
            if (ctx.getPCRegisterValue() == 0x200 + 4) {
                assertEquals(1, Byte.toUnsignedInt(ctx.getRegisterValue(5)));
                assertEquals(0, ctx.getRegisterValue(0xF));
                return true;
            }
            return false;
        });
    }

    @Test
    public void testShiftLeftVF() {
        var program = new short[] {
                0x6501,
                (short) 0x8546
        };

        testCPU(program, ctx -> {
            if (ctx.getPCRegisterValue() == 0x200 + 4) {
                assertEquals(0, Byte.toUnsignedInt(ctx.getRegisterValue(5)));
                assertEquals(1, ctx.getRegisterValue(0xF));
                return true;
            }
            return false;
        });
    }

    @Test
    public void testShiftRight() {
        var program = new short[] {
                0x6501,
                (short) 0x854E
        };

        testCPU(program, ctx -> {
            if (ctx.getPCRegisterValue() == 0x200 + 4) {
                assertEquals(2, Byte.toUnsignedInt(ctx.getRegisterValue(5)));
                assertEquals(0, ctx.getRegisterValue(0xF));
                return true;
            }
            return false;
        });
    }

    @Test
    public void testShiftRightVF() {
        var program = new short[] {
                0x65FF,
                (short) 0x854E
        };

        testCPU(program, ctx -> {
            if (ctx.getPCRegisterValue() == 0x200 + 4) {
                assertEquals(0xFE, Byte.toUnsignedInt(ctx.getRegisterValue(5)));
                assertEquals(1, ctx.getRegisterValue(0xF));
                return true;
            }
            return false;
        });
    }

    @Test
    public void testIFRegsMatch() {
        var program = new short[] {
                0x6412,
                0x6512,
                0x5540,
                0x7101,
                0x7102

        };

        testCPU(program, ctx -> {
            if (ctx.getPCRegisterValue() == 0x200 + program.length*2) {
                assertEquals(0x12, Byte.toUnsignedInt(ctx.getRegisterValue(4)));
                assertEquals(0x12, Byte.toUnsignedInt(ctx.getRegisterValue(5)));
                assertEquals(0x02, Byte.toUnsignedInt(ctx.getRegisterValue(1)));
                return true;
            }
            return false;
        });
    }

    @Test
    public void testIFRegsDoNotMatch() {
        var program = new short[] {
                0x6412,
                0x6513,
                (short) 0x9540,
                0x7101,
                0x7102

        };

        testCPU(program, ctx -> {
            if (ctx.getPCRegisterValue() == 0x200 + program.length*2) {
                assertEquals(0x12, Byte.toUnsignedInt(ctx.getRegisterValue(4)));
                assertEquals(0x13, Byte.toUnsignedInt(ctx.getRegisterValue(5)));
                assertEquals(0x02, Byte.toUnsignedInt(ctx.getRegisterValue(1)));
                return true;
            }
            return false;
        });
    }

    @Test
    public void testSetI() {
        var program = new short[] {
                (short) 0xAFFF
        };

        testCPU(program, ctx -> {
            if (ctx.getPCRegisterValue() == 0x200 + program.length*2) {
                assertEquals(0xFFF, Short.toUnsignedInt(ctx.getIRegisterValue()));
                return true;
            }
            return false;
        });
    }

    @Test
    public void testJumpWithV0() {
        var program = new short[] {
                0x6002,
                (short) 0xB111
        };

        testCPU(program, ctx -> {
            if (ctx.getPCRegisterValue() == 0x2+0x111) {
                assertEquals(0x2+0x111, Short.toUnsignedInt(ctx.getPCRegisterValue()));
                return true;
            }
            return false;
        });
    }

    @Test
    public void testTestRandom() {
        var program = new short[] {
                (short) 0xC011
        };

        randomNumber.set(0x33);

        testCPU(program, ctx -> {
            if (ctx.getPCRegisterValue() == 0x200 + program.length*2) {
                assertEquals(0x33 & 0x11, Byte.toUnsignedInt(ctx.getRegisterValue(0)));
                return true;
            }
            return false;
        });
    }

    @Test
    public void testJumpInstr() {
        var program = new short[] {
                0x120A, // 200
                0x0000, // 202
                0x0000, // 204
                0x6420, // 206
                0x0000, // 208
                0x6421, // 20A
        };

        testCPU(program, ctx -> {
            if (ctx.getPCRegisterValue() == 0x200 + 12) {
                assertEquals(ctx.getRegisterValue(4),0x21);
                return true;
            }
            return false;
        });
    }

    private void testCPU(short[] program, Function<CPUAgentContext, Boolean> testFunc) {
        byte[] rom = new byte[program.length*2+1];
        for(int i=0; i<program.length; i++) {
            addOperation(rom, i, program[i]);
        }
        CPUv2 cpu = CPUBuilder.build(rom, new Display() {
            @Override
            public int getPixel(int x, int y) {
                return 0;
            }

            @Override
            public void setPixel(int x, int y, int p) {

            }
        }, new Controls() {
            @Override
            public boolean isKeyPressed(byte v) {
                return false;
            }

            @Override
            public boolean isKeyNotPressed(byte v) {
                return false;
            }

            @Override
            public byte waitForKeyPress() {
                return 0;
            }
        });

        cpu.addAgents( new TestAgent(testFunc));
        cpu.setRandom(new FakeRandom());
        cpu.run();
    }

    private class FakeRandom extends Random {

        @Override
        public int nextInt(int bound) {
            return randomNumber.get();
        }
    }

    private static class TestAgent implements CPUAgent {

        private boolean halt = false;

        private final Function<CPUAgentContext, Boolean> testFunc;

        public TestAgent(Function<CPUAgentContext, Boolean> testFunc) {
            this.testFunc = testFunc;
        }

        @Override
        public void postCycle(CPUAgentContext ctx) {
            halt = testFunc.apply(ctx);
        }

        @Override
        public boolean isHalt() {
            return halt;
        }
    }

    private static void addOperation(byte[] rom, int pos, short op) {
        rom[pos * 2] = (byte) (op >> 8);
        rom[pos * 2 + 1] = (byte) (op & 0x00FF);
    }


}
