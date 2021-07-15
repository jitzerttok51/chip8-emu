package org.example.core.agent;

public interface CPUAgent {

    void postCycle(CPUAgentContext ctx);

    boolean isHalt();
}
