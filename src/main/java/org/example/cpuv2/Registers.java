package org.example.cpuv2;

public class Registers {

    private final byte[] V = new byte[0x10];
    private short I = 0;
    private short PC = 0x200;

    private byte delayTimer = 0;
    private byte soundTimer = 0;

    public short getI() {
        return I;
    }

    public void setI(short i) {
        I = i;
    }

    public void addToI(short i) {
        I += i;
    }

    public short getPC() {
        return PC;
    }

    public void setPC(short PC) {
        this.PC = PC;
    }

    public byte getDelayTimer() {
        return delayTimer;
    }

    public void decDelayTimer() { delayTimer--; }

    public void setDelayTimer(byte delayTimer) {
        this.delayTimer = delayTimer;
    }

    public byte getSoundTimer() {
        return soundTimer;
    }

    public void setSoundTimer(byte soundTimer) {
        this.soundTimer = soundTimer;
    }

    public void decSoundTimer() { soundTimer--; }

    public byte[] getRegisters() {
        return V;
    }

    public void setRegister(int index, byte data) {
        this.V[index] = data;
    }

    public byte addRegister(int index, byte data) {
        return this.V[index] += data;
    }

    public byte getRegister(int index) {
        return this.V[index];
    }

    public short getAndIncPC() {
        return PC++;
    }

    public void skipNextInstruction() {
        PC+=2;
    }
}
