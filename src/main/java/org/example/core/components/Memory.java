package org.example.core.components;

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

    public byte[] snapshot() {
        var buff = new byte[CAPACITY];
        System.arraycopy(memory, 0, buff, 0, CAPACITY);
        return buff;
    }
}
