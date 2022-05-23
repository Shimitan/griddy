package com.company;

import com.company.parser.GriddyTreeConstants;
import com.company.parser.Node;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.function.Function;

public class Util {
    public static final class ANSI {
        public static final String RESET = "\u001B[0m";
        public static final String FG_RED = "\u001B[38;5;9m";
        public static final String FG_GREEN = "\u001B[38;5;10m";
        public static final String FG_BLUE = "\u001B[38;5;12m";
        public static final String FG_YELLOW = "\u001B[38;5;11m";
        public static final String STYLE_UNDERLINE = "\u001B[4m";
        public static final String STYLE_BOLD = "\u001B[1m";
        public static final String STYLE_ITALIC = "\u001B[3m";
    }

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

                if (GriddyTreeConstants.jjtNodeName[c.getId()].equals("Assign") && c.jjtGetChild(0).jjtGetValue().equals(name))
                    output.add(c);
            }
            output.addAll(getAssignedInScope(node.getParent(), name));
        }

        return output;
    }

    public static String getIdentifierType(Node node, String name) {
        Node assocNode;

        ArrayList<Node> prevAssign = Util.getAssignedInScope(node, name);
        if (prevAssign.isEmpty()) throw new RuntimeException("Can't get type of '" + name + "'. Identifier unknown.");

        assocNode = prevAssign.get(prevAssign.size() - 1).jjtGetChild(1);

        var type = GriddyTreeConstants.jjtNodeName[assocNode.getId()];
        if (type.equals("Ident"))
            return getIdentifierType(assocNode, prevAssign.get(prevAssign.size() - 1).jjtGetChild(0).jjtGetValue().toString());

        return type;
    }

    public static String getFunctionReturnType(Node node, String name) {
        var root = node;
        while (root.getParent() != null) root = root.getParent();

        for (Node c : root.getChildren()) {
            if (c.toString().equals("FuncDecl") && c.jjtGetChild(0).jjtGetValue().toString().equals(name)) {
                var retType = GriddyTreeConstants.jjtNodeName[c.jjtGetChild(3).getId()];
                if (retType.equals("Ident")) {
                    return getIdentifierType(c.jjtGetChild(2).jjtGetChild(c.getNumChildren() - 1), c.jjtGetChild(3).jjtGetValue().toString());
                }

                return retType;
            }
        }
        throw new RuntimeException("Declaration for function '" + name + "' not found.");
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
                        -c, --compile               =>  Compile output with gcc.
                        --tree                      =>  Dump AST to stdout.
                    """);
            return;
        }

        if (flags.file == null) throw new RuntimeException("Missing input filepath.");

        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(flags.file);

            Griddy.main(flags.tree, inputStream, output);
            File outFile = new File(flags.output != null
                    ? flags.output
                    : flags.file+".c");

            if (outFile.createNewFile()) System.out.println("File '" + outFile.getName() + "' successfully created!");

            try (FileOutputStream outputStream = new FileOutputStream(outFile)) {
                outputStream.write(output.toString().getBytes(StandardCharsets.UTF_8));
            }

            if (flags.compile) {
                Runtime runtime = Runtime.getRuntime();
                String[] cmdArgs = {"gcc", "-std=c99", outFile.getPath()};

                Process cmdProc = runtime.exec(cmdArgs);

                try {
                    if (cmdProc.waitFor() == 0) {
                        System.out.println("Successfully compiled: " + ANSI.FG_GREEN + ANSI.STYLE_BOLD + "\u001B[52m" + flags.file + ANSI.RESET + "!");
                    } else {
                        System.out.println("Failed to compile: " + ANSI.FG_YELLOW + ANSI.STYLE_BOLD + flags.file + ANSI.RESET + ".");

                        InputStream procStdErr = cmdProc.getErrorStream();
                        String errStr = new BufferedReader(
                                new InputStreamReader(procStdErr, StandardCharsets.UTF_8)
                        ).lines().collect(Collectors.joining("\n"));

                        System.out.println(ANSI.FG_RED + errStr + ANSI.RESET);
                    }
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
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

    public static Function<String, String> getGriddyGlobal = k -> switch (k) {
        case "@player_one" -> "&_p1";
        case "@player_two" -> "&_p2";
        case "@board" -> "_board";
        case "@current_player" -> "_current_player";
        case "@turn_count" -> "_turn_count";
        case "@win_condition" -> "_win_condition";
        default -> throw new RuntimeException("Unknown identifier: '" + k + "'");
    };

    public static Function<String, String> getGriddyGlobalType = k -> switch (k) {
        case "@player_one", "@player_two" -> "Player";
        case "@board" -> "Board";
        case "@win_condition" -> "Boolean";
        case "@turn_count" -> "Integer";
        case "@placeable", "@capture", "@can_jump" -> "MetaBoolean";
        case "@limit" -> "MetaInteger";
        default -> throw new RuntimeException("Unknown identifier: '" + k + "'");
    };
}

