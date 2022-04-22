package com.company;

import com.company.parser.GriddyTreeConstants;
import com.company.parser.Node;

import javax.management.remote.TargetedNotification;
import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;

public class Util {
    /**
     * Check if identifier name has been declared in scope.
     * @param node start
     * @param name identifier
     * @return status
     */
    public static boolean isDeclaredInScope(Node node, String name) {
        if (node.jjtGetParent() == null) return false;

        for (Node c : node.jjtGetParent().getChildren()) {
            if (c == node) break;

            if (GriddyTreeConstants.jjtNodeName[c.getId()].equals("Assign")) {
                if (c.jjtGetChild(0).jjtGetValue().equals(name))
                    return true;
            }
        }

        return isDeclaredInScope(node.jjtGetParent(), name);
    }



    /**
     * Get all previous assignments of identifier name.
     * @param node start
     * @param name identifier
     * @return previous assignment nodes
     */
    public static ArrayList<Node> getAssignedInScope(Node node, String name) {
        var output = new ArrayList<Node>();

        if (node.jjtGetParent() != null)
            for (Node c : node.jjtGetParent().getChildren()) {
                if (c == node) break;

                if (GriddyTreeConstants.jjtNodeName[c.getId()].equals("Assign")
                        && c.jjtGetChild(0).jjtGetValue().equals(name)
                ) output.add(c);
                else output.addAll(getAssignedInScope(node.jjtGetParent(), name));
            }

        return output;
    }

    public static void cli(String args[], FileInputStream inputStream, FileOutputStream outputStream, StringBuilder output) {
        cliFlags flags = new cliFlags();
        cli(args, flags, 0);

        if (flags.help) {
            System.out.println("help is near but not here... yet");
            return;
        }

        if (flags.file == null) {
            throw new RuntimeException("no input file given");
        }

        try {
            inputStream = new FileInputStream(flags.file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Griddy.main(flags.target, flags.tree, inputStream, output);
        File outFile = new File(flags.output != null? flags.output: (flags.target != Target.JS? flags.file+".c": flags.file+".js"));
        try {
            outFile.createNewFile();
            outputStream = new FileOutputStream(outFile);
            outputStream.write(output.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (inputStream != null)
            System.out.println(output); //delete later
    }

    public static class cliFlags {
        boolean help = false;
        String file = null;
        String output = null;
        boolean tree = false;
        Target target = Target.C;
    }

    protected static void cli(String args[], cliFlags flags, int i) {
        if(args.length < 1) return;

        switch (args[i]) {
            case "-h", "--help" -> {
                flags.help = true;

                if(i+2 <= args.length)
                    cli(args, flags, i+1);
            }
            case "-f", "--file" -> {
                flags.file = args[i+1];

                if(i+3 <= args.length)
                    cli(args, flags, i+2);
            }
            case "-o", "--output" -> {
                flags.output = args[i+1];

                if(i+3 <= args.length)
                    cli(args, flags, i+2);
            }
            case "--tree" -> {
                flags.tree = true;

                if(i+2 <= args.length)
                    cli(args, flags, i+1);
            }
            case "-t", "--target" -> {
                if(args[i+1].equalsIgnoreCase("c")){
                    flags.target = Target.C;
                }else if(args[i+1].equalsIgnoreCase("js")){
                    flags.target = Target.JS;
                }

                if(i+3 <= args.length)
                    cli(args, flags, i+2);
            }
            default -> {
                System.out.println("Unknown argument: " + args[i]);
                System.out.println("type -h or --help for help");
            }
        }
    }


}
