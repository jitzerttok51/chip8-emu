package org.example.core.agent;

public interface CPUAgentContext {

    byte[] getMemorySnapshot();
    byte getRegisterValue(int v);
    short getIRegisterValue();
    short getPCRegisterValue();
    short getCurrentOpcode();
}
