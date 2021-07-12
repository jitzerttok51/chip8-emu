package org.example.cpuv2;

import org.example.core.Controls;
import org.example.core.Display;

import java.util.ArrayDeque;
import java.util.Random;
import java.util.function.Supplier;

public class CPUv2 {

    private final Registers registers;
    private final Memory memory;
    private final Clock clock;
    private final Clock soundClock;
    private final Clock delayClock;

    private final Display display;
    private final Controls controls;
    private final Random random = new Random();

    private final ArrayDeque<Short> stack = new ArrayDeque<>();


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

    private void execute(Instructions instruction, short opcode) {
        switch (instruction) {
            case CLS:  clearScreen(opcode);       break;
            case LDA:  loadAddress(opcode);       break;
            case SET:  setRegister(opcode);       break;
            case DRAW: draw(opcode);              break;
            case ADD:  addRegister(opcode);       break;
            case JMP:  jump(opcode);              break;
            case SE:   skipEquals(opcode);        break;
            case SNE:  skipNotEquals(opcode);     break;
            case ADDA: addAddress(opcode);        break;
            case CALL: call(opcode);              break;
            case RET:  ret(opcode);               break;
            case STDT: storeInDelayReg(opcode);   break;
            case STST: storeInSoundReg(opcode);   break;
            case LDDT: loadFromDelayReg(opcode);  break;
            case ADDR: addRegisters(opcode);      break;
            case SHL:  shiftLeft(opcode);         break;
            case SHR:  shiftRight(opcode);        break;
            case LDRD: loadRegisterDump(opcode);  break;
            case STRD: storeRegisterDump(opcode); break;
            case LDSA: loadSpriteAddress(opcode); break;
            case STDR: binaryCodedDec(opcode);    break;
            case LDKP: waitForKeyPress(opcode);   break;
            case SKP:  skIfKeyPressed(opcode);    break;
            case SKNP: skIfKeyNotPressed(opcode); break;
            case RAND: randomNumber(opcode);      break;
            case SETR: setRegToReg(opcode);       break;
            case SER:  skipEqualsReg(opcode);     break;
            case SNER: skipNotEqualsReg(opcode);  break;
            case AND:  andRegisters(opcode);      break;
            case OR:   orRegisters(opcode);       break;
            case XOR:  xorRegisters(opcode);      break;
            case SUB:  subRegisters(opcode);      break;
            case SUBR: revSubRegisters(opcode);   break;
            case JMPO: jumpWithReg(opcode);       break;
            case USI:
            default:
                throw new IllegalStateException("Unsupported instruction "+instruction.toString()
                        + " Opcode: " + String.format("0x%04X", opcode));
        }
    }

    public Display getDisplay() {
        return display;
    }

    // Instruction implementations
    private void clearScreen(short opcode) {
        display.clear();
    }

    private void loadAddress(short opcode) {
        var res = getAddress(opcode);
        registers.setI(res);
    }

    private void addAddress(short opcode) {
        var reg = getRegisterX(opcode);
        var res = registers.getRegister(reg);
        registers.addToI(res);
    }

    private void jump(short opcode) {
        var res = getAddress(opcode);
        registers.setPC(res);
    }

    private void jumpWithReg(short opcode) {
        var res = getAddress(opcode);
        var reg0 = registers.getRegister(0);
        registers.setPC((short) (reg0 + res));
    }

    private void call(short opcode) {
        var res = getAddress(opcode);
        stack.push(registers.getPC());
        registers.setPC(res);
    }

    private void ret(short opcode) {
        var res = stack.pop();
        registers.setPC(res);
    }

    private void skipEquals(short opcode) {
        var reg = getRegisterX(opcode);
        var val = getValue(opcode);
        var regVal = registers.getRegister(reg);
        if(regVal==val) {
            registers.skipNextInstruction();
        }
    }

    private void skipNotEquals(short opcode) {
        var reg = getRegisterX(opcode);
        var val = getValue(opcode);
        var regVal = registers.getRegister(reg);
        if(regVal!=val) {
            registers.skipNextInstruction();
        }
    }

    private void skipEqualsReg(short opcode) {
        var regX = getRegisterX(opcode);
        var regY = getRegisterX(opcode);
        var x = registers.getRegister(regX);
        var y = registers.getRegister(regY);
        if(x==y) {
            registers.skipNextInstruction();
        }
    }

    private void skipNotEqualsReg(short opcode) {
        var regX = getRegisterX(opcode);
        var regY = getRegisterX(opcode);
        var x = registers.getRegister(regX);
        var y = registers.getRegister(regY);
        if(x!=y) {
            registers.skipNextInstruction();
        }
    }

    private void setRegister(short opcode) {
        var reg = getRegisterX(opcode);
        var val = getValue(opcode);
        registers.setRegister(reg, val);
    }
    private void setRegToReg(short opcode) {
        var regX = getRegisterX(opcode);
        var regY = getRegisterY(opcode);
        var val = registers.getRegister(regY);
        registers.setRegister(regX, val);
    }

    private void randomNumber(short opcode) {
        var reg = getRegisterX(opcode);

        byte n = (byte) (opcode & 0x00FF);
        var res = (byte) (random.nextInt(0x100) & n);
        registers.setRegister(reg, res);
    }

    private void addRegister(short opcode) {
        var reg = getRegisterX(opcode);
        var val = getValue(opcode);
        registers.addRegister(reg, val);
    }

    private void addRegisters(short opcode) {
        var regX = getRegisterX(opcode);
        var regY = getRegisterY(opcode);
        var val = registers.getRegister(regY);
        var xVal = registers.getRegister(regX);
        var result = Byte.toUnsignedInt(xVal)
                + Byte.toUnsignedInt(val);

        registers.setRegister(0xF, (byte) 0);
        if(result > 255) {
            registers.setRegister(0xF, (byte) 1);
            result -= 256;
        }

        registers.setRegister(regX, (byte) result);
    }

    @SuppressWarnings("all")
    private void subRegisters(short opcode) {
        var regX = getRegisterX(opcode);
        var regY = getRegisterY(opcode);
        var x = registers.getRegister(regX);
        var y = registers.getRegister(regY);
        var result = Byte.toUnsignedInt(x) - Byte.toUnsignedInt(y);

        registers.setRegister(0xF, (byte) 0);
        if(x>=y) {
            registers.setRegister(0xF, (byte) 1);
        }
        if(result<0) {
            result+=256;
        }
        registers.setRegister(regX, (byte) result);
    }

    @SuppressWarnings("all")
    private void revSubRegisters(short opcode) {
        var regX = getRegisterX(opcode);
        var regY = getRegisterY(opcode);
        var x = registers.getRegister(regX);
        var y = registers.getRegister(regY);
        var result = Byte.toUnsignedInt(y) - Byte.toUnsignedInt(x);

        registers.setRegister(0xF, (byte) 0);
        if(x<y) {
            registers.setRegister(0xF, (byte) 1);
        }
        if(result<0) {
            result+=256;
        }
        registers.setRegister(regX, (byte) result);
    }

    private void andRegisters(short opcode) {
        var regX = getRegisterX(opcode);
        var regY = getRegisterY(opcode);
        var x = registers.getRegister(regX);
        var y = registers.getRegister(regY);
        var res = (byte) (x&y);

        registers.setRegister(regX, res);
    }

    private void orRegisters(short opcode) {
        var regX = getRegisterX(opcode);
        var regY = getRegisterY(opcode);
        var x = registers.getRegister(regX);
        var y = registers.getRegister(regY);
        var res = (byte) (x|y);

        registers.setRegister(regX, res);
    }

    private void xorRegisters(short opcode) {
        var regX = getRegisterX(opcode);
        var regY = getRegisterY(opcode);
        var x = registers.getRegister(regX);
        var y = registers.getRegister(regY);
        var res = (byte) (x^y);

        registers.setRegister(regX, res);
    }

    private void shiftRight(short opcode) {
        var regX = getRegisterX(opcode);
        var x = registers.getRegister(regX);

        registers.setRegister(0xF, (byte) 0);
        if((x & 0x01) != 0) {
            registers.setRegister(0xF, (byte) 1);
        }
        var res = (byte) (Byte.toUnsignedInt(x) >> 1);
        registers.setRegister(regX, res);
    }

    private void shiftLeft(short opcode) {
        var regX = getRegisterX(opcode);
        var x = registers.getRegister(regX);

        registers.setRegister(0xF, (byte) 0);
        if((x & 0x80) != 0) {
            registers.setRegister(0xF, (byte) 1);
        }
        var res = (byte) (Byte.toUnsignedInt(x) << 1);
        registers.setRegister(regX, res);
    }

    private void draw(short opcode) {
        var n = opcode & 0x000F;
        byte registerX = getRegisterX(opcode);
        byte registerY = getRegisterY(opcode);

        var x = registers.getRegister(registerX);
        var y = registers.getRegister(registerY);

        x = (byte) (x % (byte) Display.WIDTH);
        y = (byte) (y % (byte) Display.HEIGHT);

        registers.setRegister(0xF, (byte) 0);
        for(int i=0; i<n; i++) {
            if(y+i>=Display.HEIGHT) {
                continue;
            }
            var I = registers.getI();
            var part = memory.read(I+i);
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
                    registers.setRegister(0xF, (byte) 1);
                }
            }
        }
    }

    private void storeInDelayReg(short opcode) {
        var reg = getRegisterX(opcode);
        var regVal = registers.getRegister(reg);
        registers.setDelayTimer(regVal);
    }

    private void storeInSoundReg(short opcode) {
        var reg = getRegisterX(opcode);
        var regVal = registers.getRegister(reg);
        registers.setSoundTimer(regVal);
    }

    private void loadFromDelayReg(short opcode) {
        var reg = getRegisterX(opcode);
        var regVal = registers.getDelayTimer();
        registers.setRegister(reg, regVal);
    }

    private void binaryCodedDec(short opcode) {
        var regX =  getRegisterX(opcode);
        var val = registers.getRegister(regX);
        var x = Byte.toUnsignedInt(val);
        var f = (byte) (x / 100);
        var s = (byte) (x / 10 % 10);
        var t = (byte) (x % 10);
        var I = registers.getI();
        memory.write(I, f);
        memory.write(I + 1, s);
        memory.write(I + 2, t);
    }

    private void loadSpriteAddress(short opcode) {
        var regX =  getRegisterX(opcode);
        var val = registers.getRegister(regX);
        var res = (short) (val * 5);
        registers.setI(res);
    }

    private void loadRegisterDump(short opcode) {
        var registerRange =  getRegisterX(opcode);
        var I = registers.getI();

        for(int i=0; i<=registerRange; i++) {
            var res = memory.read(I+i);
            registers.setRegister(i, res);
        }
    }

    private void storeRegisterDump(short opcode) {
        var registerRange =  getRegisterX(opcode);
        var I = registers.getI();

        for(int i=0; i<=registerRange; i++) {
            var res = registers.getRegister(i);
            memory.write(I+i, res);
        }
    }

    private void waitForKeyPress(short opcode) {
        var regX =  getRegisterX(opcode);
        var key = controls.waitForKeyPress();
        if(key<0) {
            var PC = registers.getPC();
            registers.setPC((short) (PC - 2));
        } else {
            registers.setRegister(regX, key);
        }
    }

    private void skIfKeyPressed(short opcode) {
        var regX =  getRegisterX(opcode);
        var val = registers.getRegister(regX);
        if(controls.isKeyPressed(val)) {
            registers.skipNextInstruction();
        }
    }

    private void skIfKeyNotPressed(short opcode) {
        var regX =  getRegisterX(opcode);
        var val = registers.getRegister(regX);
        if(controls.isKeyNotPressed(val)) {
            registers.skipNextInstruction();
        }
    }

    // Helpers
    private short getAddress(short opcode) {
        return (short) (opcode & 0x0FFF);
    }

    private byte getRegisterX(short opcode) {
        return (byte) ((opcode >> 8) & 0x0F);
    }

    private byte getRegisterY(short opcode) {
        return (byte) ((opcode >> 4) & 0x00F);
    }

    private byte getValue(short opcode) {
        return (byte) (opcode & 0x00FF);
    }
}
