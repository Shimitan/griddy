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
                if (c.getChild(0).getValue().equals(name))
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

        if (node.getParent() != null)
            for (Node c : node.getParent().getChildren()) {
                if (c == node) break;

                if (GriddyTreeConstants.jjtNodeName[c.getId()].equals("Assign")
                        && c.getChild(0).getValue().equals(name)
                ) output.add(c);
                else output.addAll(getAssignedInScope(node.getParent(), name));
            }

        return output;
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
                        --tree                      =>  Dump AST to stdout.
                        
                    Compilation targets:
                        C   =>  C kode compatible with GCC version 11.
                        JS  =>  JavaScript for usage with Node.js.
                    """);
            return;
        }

        if (flags.file == null) throw new RuntimeException("Missing input filepath.");

        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(flags.file);

            Griddy.main(flags.target, flags.tree, inputStream, output);
            File outFile = new File(flags.output != null? flags.output: (flags.target != Target.JS? flags.file+".c": flags.file+".js"));

            if (outFile.createNewFile()) System.out.println("File '" + outFile.getName() + "' successfully created!");

            try (FileOutputStream outputStream = new FileOutputStream(outFile)) {
                outputStream.write(output.toString().getBytes(StandardCharsets.UTF_8));
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
