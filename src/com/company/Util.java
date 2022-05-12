package com.company;

import com.company.parser.GriddyTreeConstants;
import com.company.parser.Node;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Util {
    /**
     * Check if identifier name has been declared in scope.
     * @param node start
     * @param name identifier
     * @return status
     */
    public static boolean isDeclaredInScope(Node node, String name) {
        if (node.getParent() == null) return false;

        for (Node c : node.getParent().getChildren()) {
            if (c == node) break;

            if (GriddyTreeConstants.jjtNodeName[c.getId()].equals("Assign")) {
                if (c.jjtGetChild(0).jjtGetValue().equals(name))
                    return true;
            }
        }

        return isDeclaredInScope(node.getParent(), name);
    }

    /**
     * Get all previous assignments of identifier name.
     * @param node start
     * @param name identifier
     * @return previous assignment nodes
     */
    public static ArrayList<Node> getAssignedInScope(Node node, String name) {
        var output = new ArrayList<Node>();

        if (node.getParent() != null) {
            for (Node c : node.getParent().getChildren()) {
                if (c == node) break;

                if (c.toString().equals("Assign") && c.jjtGetChild(0).jjtGetValue().equals(name))
                    output.add(c);
            }
            output.addAll(getAssignedInScope(node.getParent(), name));
        }

        return output;
    }

    public static String getIdentifierType(Node node, String name, boolean debug) {
        Node assocNode;

        ArrayList<Node> prevAssign = Util.getAssignedInScope(node, name);
        if (prevAssign.isEmpty()) throw new RuntimeException("Identifier '" + name + "' unknown.");

        assocNode = prevAssign.get(prevAssign.size() - 1).jjtGetChild(1);

        if (debug) System.out.println();

        var type = GriddyTreeConstants.jjtNodeName[assocNode.getId()];
        if (type.equals("Ident"))
            return getIdentifierType(assocNode, prevAssign.get(prevAssign.size() - 1).jjtGetChild(0).jjtGetValue().toString());

        return type;
    }

    public static String getIdentifierType(Node node, String name) {
        return getIdentifierType(node, name, false);
    }

    public static void cli(String[] args, StringBuilder output) {
        CLI_Flags flags = new CLI_Flags();
        cli(args, flags, 0);

        // Print help message if help flag is set:
        if (flags.help) {
            System.out.println("""
                    Available args:
                        -h, --help                  =>  Display help message.
                        -f <path>, --file <path>    =>  Set input filepath (required).
                        -o <path>, --output <path>  =>  Set output filepath.
                        -t <target>, --target <target>  =>  Set compilation target (default = C).
                        -c, --compile               =>  Compile output with gcc (target: C).
                        --tree                      =>  Dump AST to stdout.
                        
                    Compilation targets:
                        C   =>  C kode compatible with GCC version 11.
                        JS  =>  JavaScript for usage with Node.js.
                    """);
            return;
        }

        if (flags.compile && flags.target != Target.C) {
            System.out.println("Wrong usage: '-c/--compile' option only valid for 'C' target.");
            return;
        }

        if (flags.file == null) throw new RuntimeException("Missing input filepath.");

        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(flags.file);

            Griddy.main(flags.target, flags.tree, inputStream, output);
            File outFile = new File(flags.output != null
                    ? flags.output
                    : (flags.target != Target.JS  ? flags.file+".c" : flags.file+".js"));

            if (outFile.createNewFile()) System.out.println("File '" + outFile.getName() + "' successfully created!");

            try (FileOutputStream outputStream = new FileOutputStream(outFile)) {
                outputStream.write(output.toString().getBytes(StandardCharsets.UTF_8));
            }

            if (flags.compile) {
                Runtime runtime = Runtime.getRuntime();
                String[] cmdArgs = {"gcc", outFile.getPath()};
                runtime.exec(cmdArgs);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class CLI_Flags {
        boolean help = false;
        String file = null;
        String output = null;
        boolean tree = false;
        Target target = Target.C;
        boolean compile = false;
    }

    protected static void cli(String[] args, CLI_Flags flags, int i) {
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
                if (args[i+1].equalsIgnoreCase("c")){
                    flags.target = Target.C;
                }else if(args[i+1].equalsIgnoreCase("js")){
                    flags.target = Target.JS;
                }

                if(i+3 <= args.length)
                    cli(args, flags, i+2);
            }
            case "-c", "--compile" -> {
                flags.compile = true;
                if (i+2 <= args.length)
                    cli(args, flags, i+1);
            }
            default -> {
                System.out.println("Unknown argument: " + args[i]);
                System.out.println("type -h or --help for help");
            }
        }
    }

    public static int[] range(int begin, int end) {
        int[] arr = new int[(end + 1) - begin];

        for (int i = 0; i < end; i++)
            arr[i] = i + begin;

        return arr;
    }
}
