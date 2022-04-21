package com.company;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args){
        FileInputStream input = null;
        StringBuilder output = new StringBuilder();

        if(args.length < 1) return;
        switch (args[0]){
            case "-h", "--help" -> System.out.println("yaaa boiiii");
            case "-f", "--file" -> {
                try {
                    input = new FileInputStream(args[1]);
                    File outFile = new File(args[1]+".c");
                    try {
                        outFile.createNewFile();
                        Griddy.main(Target.C, false, input, output);
                        FileOutputStream outStream = new FileOutputStream(outFile);
                        outStream.write(output.toString().getBytes(StandardCharsets.UTF_8));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            default -> System.out.println("no known argument");
        }

        if(input != null) System.out.println(output);

    }
}