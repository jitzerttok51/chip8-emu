package org.example.cpuv2;

public enum Instructions {
    USI, // Unsupported operation
    CLS, // Clear screen
    RET, // Return
    JMP, // Jump to address
    CALL, // Call sub-routine at address
    SE,   // Skip next instruction if Vx equals N
    SNE,  // Skip next instruction if Vx does not equal N
    SER,  // Skip next instruction if Vx equals Vy
    SET,  // Set Vx to the value of N
    ADD,  // Add N to Vx
    SETR, // Set Vx to the value of Vy
    OR,   // Vx = Vx OR Vy
    AND,  // Vx = Vx AND Vy
    XOR,  // Vx = Vx XOR Vy
    ADDR, // Vx = Vx + Vy (affects VF)
    SUB,  // Vx = Vx - Vy (affects VF)
    SHR,  // Vx = Vx >> 1 (affects VF)
    SUBR, // Vx = Vy - Vx (affects VF)
    SHL,  // Vx = Vx << 1 (affects VF)
    SNER, // Skip next instruction if Vx does not equal Vy
    LDA,  // Loads address N to register I
    JMPO, // Jump to address N + V0
    RAND, // Vx = rand() & N
    DRAW, // Draw at Vx Vy
    SKP,  // Skip next instruction of key in Vx is pressed
    SKNP, // Skip next instruction of key in Vx is not pressed
    LDDT, // Load the value of the delay timer to Vx
    LDKP, // Load the key press to Vx
    STDT, // Store the value of Vx to the delay timer
    STST, // Store the value of Vx to the sound timer
    ADDA, // Add N to the address stored in I
    LDSA, // Loads the sprite address for a given character in Vx to I
    STDR, // Stores Vx as a binary-coded-decimal in memory at address stored in register I
    LDRD, // Dumps registers from V0 to Vx in memory at address stored in register I
    STRD; // Loads registers from V0 to Vx from memory at address stored in register I

    public static Instructions decode(short opcode) {
        var operation = getMainOperation(opcode);
        switch (operation) {
            case 0x0000: switch (Short.toUnsignedInt(opcode)) {
                case 0x00E0: return CLS;
                case 0x00EE: return RET;
                default: return USI;
            }
            case 0x1000: return JMP;
            case 0x2000: return CALL;
            case 0x3000: return SE;
            case 0x4000: return SNE;
            case 0x5000: return SER;
            case 0x6000: return SET;
            case 0x7000: return ADD;
            case 0x8000: switch (getArithmeticOperation(opcode)) {
                case 0x0: return SETR;
                case 0x1: return OR;
                case 0x2: return AND;
                case 0x3: return XOR;
                case 0x4: return ADDR;
                case 0x5: return SUB;
                case 0x6: return SHR;
                case 0x7: return SUBR;
                case 0x8: return SHL;
                default: return USI;
            }
            case 0x9000: return SNER;
            case 0xA000: return LDA;
            case 0xB000: return JMPO;
            case 0xC000: return RAND;
            case 0xD000: return DRAW;
            case 0xE000: switch (getSubOperation(opcode)) {
                case 0x9E: return SKP;
                case 0xA1: return SKNP;
                default: return USI;
            }
            case 0xF000: switch (getSubOperation(opcode)) {
                case 0x07: return LDDT;
                case 0x0A: return LDKP;
                case 0x15: return STDT;
                case 0x18: return STST;
                case 0x1E: return ADDA;
                case 0x29: return LDSA;
                case 0x33: return STDR;
                case 0x55: return STRD;
                case 0x65: return LDRD;
                default: return USI;
            }
            default: return USI;
        }
    }

    private static int getMainOperation(short opcode) {
        return Short.toUnsignedInt(opcode) & 0xF000;
    }

    private static int getSubOperation(short opcode) {
        return Short.toUnsignedInt(opcode) & 0x00FF;
    }

    private static int getArithmeticOperation(short opcode) {
        return Short.toUnsignedInt(opcode) & 0x000F;
    }
}
