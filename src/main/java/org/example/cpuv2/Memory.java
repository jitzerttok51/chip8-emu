package org.example.cpuv2;

public class Memory {

    public static final int CAPACITY = 4096;
    private final byte[] memory = new byte[CAPACITY];

    // TODO: Add logging and exception handling

    public byte read(int index) {
        return memory[index];
    }

    public void write(int index, byte data) {
        memory[index] = data;
    }

    public void insertData(int index, byte[] data) {
        System.arraycopy(data, 0, memory, index, data.length);
    }
}
