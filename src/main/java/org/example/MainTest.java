package org.example;

public class MainTest {

    public static void main(String[] args) {
        System.out.println(Short.parseShort("0xF".replace("0x", ""), 16));
    }
}
