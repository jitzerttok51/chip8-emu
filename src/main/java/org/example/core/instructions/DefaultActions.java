package org.example.core.instructions;

import org.example.core.components.Display;

import java.util.HashMap;
import java.util.Map;
import static org.example.core.instructions.Instructions.*;

public class DefaultActions {

    private static final Map<Instructions, Action> actions = new HashMap<>();

    static {
        actions.put(CLS, (opcode, ctx) -> ctx.display().clear());
        actions.put(LDA, (opcode, ctx) -> {
            var res = ctx.getAddress(opcode);
            ctx.registers().setI(res);
        });
        actions.put(ADDA, (opcode, ctx) -> {
            var reg = ctx.getRegisterX(opcode);
            var res = ctx.registers().getRegister(reg);
            ctx.registers().addToI(res);
        });
        actions.put(JMP, (opcode, ctx) -> {
            var res = ctx.getAddress(opcode);
            ctx.registers().setPC(res);
        });
        actions.put(JMPO, (opcode, ctx) -> {
            var res = ctx.getAddress(opcode);
            var reg0 = ctx.registers().getRegister(0);
            ctx.registers().setPC((short) (reg0 + res));
        });
        actions.put(CALL, (opcode, ctx) -> {
            var res = ctx.getAddress(opcode);
            ctx.stack().push(ctx.registers().getPC());
            ctx.registers().setPC(res);
        });
        actions.put(RET, (opcode, ctx) -> {
            var res = ctx.stack().pop();
            ctx.registers().setPC(res);
        });
        actions.put(SE, (opcode, ctx) -> {
            var reg = ctx.getRegisterX(opcode);
            var val = ctx.getValue(opcode);
            var regVal = ctx.registers().getRegister(reg);
            if(regVal==val) {
                ctx.registers().skipNextInstruction();
            }
        });
        actions.put(SNE, (opcode, ctx) -> {
            var reg = ctx.getRegisterX(opcode);
            var val = ctx.getValue(opcode);
            var regVal = ctx.registers().getRegister(reg);
            if(regVal!=val) {
                ctx.registers().skipNextInstruction();
            }
        });
        actions.put(SER, (opcode, ctx) -> {
            var registers = ctx.registers();
            var regX = ctx.getRegisterX(opcode);
            var regY = ctx.getRegisterX(opcode);
            var x = registers.getRegister(regX);
            var y = registers.getRegister(regY);
            if(x==y) {
                registers.skipNextInstruction();
            }
        });
        actions.put(SNER, (opcode, ctx) -> {
            var registers = ctx.registers();
            var regX = ctx.getRegisterX(opcode);
            var regY = ctx.getRegisterX(opcode);
            var x = registers.getRegister(regX);
            var y = registers.getRegister(regY);
            if(x!=y) {
                registers.skipNextInstruction();
            }
        });
        actions.put(SET, (opcode, ctx) -> {
            var reg = ctx.getRegisterX(opcode);
            var val = ctx.getValue(opcode);
            ctx.registers().setRegister(reg, val);
        });
        actions.put(SETR, (opcode, ctx) -> {
            var regX = ctx.getRegisterX(opcode);
            var regY = ctx.getRegisterY(opcode);
            var val = ctx.registers().getRegister(regY);
            ctx.registers().setRegister(regX, val);
        });
        actions.put(RAND, (opcode, ctx) -> {
            var reg = ctx.getRegisterX(opcode);
            byte n = (byte) (opcode & 0x00FF);
            var res = (byte) (ctx.random().nextInt(0x100) & n);
            ctx.registers().setRegister(reg, res);
        });
        actions.put(ADD, (opcode, ctx) -> {
            var reg = ctx.getRegisterX(opcode);
            var val = ctx.getValue(opcode);
            ctx.registers().addRegister(reg, val);
        });
        actions.put(ADDR, (opcode, ctx) -> {
            var registers = ctx.registers();
            var regX = ctx.getRegisterX(opcode);
            var regY = ctx.getRegisterY(opcode);
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
        });
        actions.put(SUB, (opcode, ctx) -> {
            var registers = ctx.registers();
            var regX = ctx.getRegisterX(opcode);
            var regY = ctx.getRegisterY(opcode);
            var x = registers.getRegister(regX);
            var z = registers.getRegister(regY);
            var result = Byte.toUnsignedInt(x) - Byte.toUnsignedInt(z);

            registers.setRegister(0xF, (byte) 0);
            if(x>=z) {
                registers.setRegister(0xF, (byte) 1);
            }
            if(result<0) {
                result+=256;
            }
            registers.setRegister(regX, (byte) result);
        });

        actions.put(SUBR, (opcode, ctx) -> {
            var registers = ctx.registers();
            var regX = ctx.getRegisterX(opcode);
            var regY = ctx.getRegisterY(opcode);
            var x = registers.getRegister(regX);
            var z = registers.getRegister(regY);
            var result = Byte.toUnsignedInt(z) - Byte.toUnsignedInt(x);

            registers.setRegister(0xF, (byte) 0);
            if(x<z) {
                registers.setRegister(0xF, (byte) 1);
            }
            if(result<0) {
                result+=256;
            }
            registers.setRegister(regX, (byte) result);
        });
        actions.put(AND, (opcode, ctx) -> {
            var registers = ctx.registers();
            var regX = ctx.getRegisterX(opcode);
            var regY = ctx.getRegisterY(opcode);
            var x = registers.getRegister(regX);
            var y = registers.getRegister(regY);
            var res = (byte) (x&y);

            registers.setRegister(regX, res);
        });
        actions.put(OR, (opcode, ctx) -> {
            var registers = ctx.registers();
            var regX = ctx.getRegisterX(opcode);
            var regY = ctx.getRegisterY(opcode);
            var x = registers.getRegister(regX);
            var y = registers.getRegister(regY);
            var res = (byte) (x|y);

            registers.setRegister(regX, res);
        });
        actions.put(XOR, (opcode, ctx) -> {
            var registers = ctx.registers();
            var regX = ctx.getRegisterX(opcode);
            var regY = ctx.getRegisterY(opcode);
            var x = registers.getRegister(regX);
            var y = registers.getRegister(regY);
            var res = (byte) (x^y);

            registers.setRegister(regX, res);
        });
        actions.put(SHR, (opcode, ctx) -> {
            var registers = ctx.registers();
            var regX = ctx.getRegisterX(opcode);
            var x = registers.getRegister(regX);

            registers.setRegister(0xF, (byte) 0);
            if((x & 0x01) != 0) {
                registers.setRegister(0xF, (byte) 1);
            }
            var res = (byte) (Byte.toUnsignedInt(x) >> 1);
            registers.setRegister(regX, res);
        });
        actions.put(SHL, (opcode, ctx) -> {
            var registers = ctx.registers();
            var regX = ctx.getRegisterX(opcode);
            var x = registers.getRegister(regX);

            registers.setRegister(0xF, (byte) 0);
            if((x & 0x80) != 0) {
                registers.setRegister(0xF, (byte) 1);
            }
            var res = (byte) (Byte.toUnsignedInt(x) << 1);
            registers.setRegister(regX, res);
        });
        actions.put(DRAW, (opcode, ctx) -> {
            var registers = ctx.registers();
            var n = opcode & 0x000F;
            byte registerX = ctx.getRegisterX(opcode);
            byte registerY = ctx.getRegisterY(opcode);

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
                var part = ctx.memory().read(I+i);
                for(int j=0; j<8; j++) {
                    if(x+j>=Display.WIDTH) {
                        continue;
                    }
                    var mask = 0x80 >> j;
                    var p = ctx.display().getPixel(x+j, y+i);
                    var res = ((part & mask) != 0) ? 1 : 0;
                    var np = p ^ res;
                    ctx.display().setPixel(x+j, y+i, np);
                    if(p == 1 && np == 0) {
                        registers.setRegister(0xF, (byte) 1);
                    }
                }
            }
        });
        actions.put(STDT, (opcode, ctx) -> {
            var registers = ctx.registers();
            var reg = ctx.getRegisterX(opcode);
            var regVal = registers.getRegister(reg);
            registers.setDelayTimer(regVal);
        });
        actions.put(STST, (opcode, ctx) -> {
            var registers = ctx.registers();
            var reg = ctx.getRegisterX(opcode);
            var regVal = registers.getRegister(reg);
            registers.setSoundTimer(regVal);
        });
        actions.put(LDDT, (opcode, ctx) -> {
            var registers = ctx.registers();
            var reg = ctx.getRegisterX(opcode);
            var regVal = registers.getDelayTimer();
            registers.setRegister(reg, regVal);
        });
        actions.put(STDR, (opcode, ctx) -> {
            var regX =  ctx.getRegisterX(opcode);
            var val = ctx.registers().getRegister(regX);
            var x = Byte.toUnsignedInt(val);
            var f = (byte) (x / 100);
            var s = (byte) (x / 10 % 10);
            var t = (byte) (x % 10);
            var I = ctx.registers().getI();
            ctx.memory().write(I, f);
            ctx.memory().write(I + 1, s);
            ctx.memory().write(I + 2, t);
        });
        actions.put(LDSA, (opcode, ctx) -> {
            var regX =  ctx.getRegisterX(opcode);
            var val = ctx.registers().getRegister(regX);
            var res = (short) (val * 5);
            ctx.registers().setI(res);
        });
        actions.put(LDRD, (opcode, ctx) -> {
            var registerRange =  ctx.getRegisterX(opcode);
            var I = ctx.registers().getI();

            for(int i=0; i<=registerRange; i++) {
                var res = ctx.memory().read(I + i);
                ctx.registers().setRegister(i, res);
            }
        });
        actions.put(STRD, (opcode, ctx) -> {
            var registerRange =  ctx.getRegisterX(opcode);
            var I = ctx.registers().getI();

            for(int i=0; i<=registerRange; i++) {
                var res = ctx.registers().getRegister(i);
                ctx.memory().write(I+i, res);
            }
        });
        actions.put(LDKP, (opcode, ctx) -> {
            var regX =  ctx.getRegisterX(opcode);
            var key = ctx.controls().waitForKeyPress();
            if(key<0) {
                var PC = ctx.registers().getPC();
                ctx.registers().setPC((short) (PC - 2));
            } else {
                ctx.registers().setRegister(regX, key);
            }
        });
        actions.put(SKP, (opcode, ctx) -> {
            var regX =  ctx.getRegisterX(opcode);
            var val = ctx.registers().getRegister(regX);
            if(ctx.controls().isKeyPressed(val)) {
                ctx.registers().skipNextInstruction();
            }
        });
        actions.put(SKNP, (opcode, ctx) -> {
            var regX =  ctx.getRegisterX(opcode);
            var val = ctx.registers().getRegister(regX);
            if(ctx.controls().isKeyNotPressed(val)) {
                ctx.registers().skipNextInstruction();
            }
        });
    }

    public static Map<Instructions, Action> getActions() {
        return actions;
    }
}
