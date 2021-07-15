package org.example.core;

import org.example.core.agent.CPUAgent;
import org.example.core.agent.CPUAgentContext;
import org.example.core.components.*;
import org.example.core.instructions.Action;
import org.example.core.instructions.ActionContext;
import org.example.core.instructions.DefaultActions;
import org.example.core.instructions.Instructions;

import java.util.*;
import java.util.function.Supplier;

public class CPUv2 implements ActionContext {

    private final Registers registers;
    private final Memory memory;
    private final Clock clock;
    private final Clock soundClock;
    private final Clock delayClock;

    private final Display display;
    private final Controls controls;
    private Random random = new Random();

    private final ArrayDeque<Short> stack = new ArrayDeque<>();
    private final List<CPUAgent> agents = new ArrayList<>();


    public CPUv2(Registers registers, Memory memory, Clock clock, Clock soundClock, Clock delayClock, Display display, Controls controls) {
        this.registers = registers;
        this.memory = memory;
        this.clock = clock;
        this.soundClock = soundClock;
        this.delayClock = delayClock;
        this.display = display;
        this.controls = controls;
    }

    public void run() {
        populateClock(delayClock, registers::getDelayTimer, registers::decDelayTimer).start();
        populateClock(soundClock, registers::getSoundTimer, registers::decSoundTimer).start();
        this.clock.setTask(this::runInt);
        this.clock.start();
    }

    public void halt() {
        try {
            this.delayClock.stop();
            this.soundClock.stop();
            this.clock.stop();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Clock populateClock(Clock clock, Supplier<Byte> getReg, Runnable decReg) {
        clock.setTask(()->{
            if(getReg.get()>0) {
                decReg.run();
            }
        });
        return clock;
    }

    private void runInt() {
        try {
            var opcode = fetch();
            var instruction = Instructions.decode(opcode);
            execute(instruction, opcode);
            updateAgents(opcode);
        } catch (Exception e) {
            this.halt();
            e.printStackTrace();
        }
    }

    private short fetch() {
        byte upper = memory.read(registers.getAndIncPC());
        byte lower = memory.read(registers.getAndIncPC());
        return (short)((Byte.toUnsignedInt(upper) << 8) + Byte.toUnsignedInt(lower));
    }

    public void addAgents(CPUAgent ...agent) {
        agents.addAll(Arrays.asList(agent));
    }

    private void execute(Instructions instruction, short opcode) {
        Action action = DefaultActions.getActions().get(instruction);
        if(action != null) {
            action.execute(opcode, this);
            return;
        }
        throw new IllegalStateException("Unsupported instruction "+instruction.toString()
                        + " Opcode: " + String.format("0x%04X", opcode));
    }

    public Display getDisplay() {
        return display;
    }

    public void setRandom(Random random) {
        this.random = random;
    }

    @Override
    public Display display() {
        return display;
    }

    @Override
    public Controls controls() {
        return controls;
    }

    @Override
    public Random random() {
        return random;
    }

    @Override
    public Deque<Short> stack() {
        return stack;
    }

    @Override
    public Registers registers() {
        return registers;
    }

    @Override
    public Memory memory() {
        return memory;
    }


    private void updateAgents(short opcode) {
        for(var agent : agents) {
            var ctx = new Context(opcode);
            agent.postCycle(ctx);
            if(agent.isHalt()) {
                halt();
            }
        }
    }

    private class Context implements CPUAgentContext {

        private final short opcode;

        private Context(short opcode) {
            this.opcode = opcode;
        }

        @Override
        public byte[] getMemorySnapshot() {
            return memory.snapshot();
        }

        @Override
        public byte getRegisterValue(int v) {
            return registers.getRegister(v);
        }

        @Override
        public short getIRegisterValue() {
            return registers.getI();
        }

        @Override
        public short getPCRegisterValue() {
            return registers.getPC();
        }

        @Override
        public short getCurrentOpcode() {
            return 0;
        }
    }
}
