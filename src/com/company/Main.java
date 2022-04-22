package com.company;

import java.io.*;

public class Main {
    public static void main(String[] args) {
        FileInputStream inputStream = null;
        StringBuilder output = new StringBuilder();
        FileOutputStream outputStream = null;

        Util.cli(args, inputStream, outputStream, output);
    }
}