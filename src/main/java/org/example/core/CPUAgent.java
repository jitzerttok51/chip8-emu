package org.example.core;

public interface CPUAgent {

    void postCycle(CPUAgentContext ctx);

    boolean isHalt();
}
