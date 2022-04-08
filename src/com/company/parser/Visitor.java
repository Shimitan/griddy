package com.company.parser;

import java.util.ArrayList;

import static java.lang.System.out;

public class Visitor extends GriddyDefaultVisitor {
    public Object visit(ASTEcho node, Object data) {
        String argType = GriddyTreeConstants.jjtNodeName[node.jjtGetChild(0).getId()];
        Object argValue = node.jjtGetChild(0).jjtGetValue();

        if (argType.equals("Ident")) {
            ArrayList<Node> prevAssign = getAssignedInScope(node, node.jjtGetChild(0).getName());
            Node assocNode = prevAssign
                    .get(prevAssign.toArray().length - 1)
                    .jjtGetChild(1);
            argType = GriddyTreeConstants.jjtNodeName[assocNode.getId()];
            argValue = node
                    .jjtGetChild(0)
                    .getName();
        }

        switch (argType) {
            case "Integer" -> out.println("printf(\"%d\", " + argValue + ");");
            case "String" -> {
                out.print("printf(\"%s\", ");
                node.jjtGetChild(0).jjtAccept(this, data);
                out.println(");");
            }
            default -> throw new RuntimeException("Can't echo value of unknown type");
        }

        return data;
    }

    public Object visit(SimpleNode node, Object data){
        throw new RuntimeException("encountered SimpleNode");
    }

    public Object visit(ASTStart node, Object data){
        // Include C libraries that might be needed:
        out.println("#include <stdio.h>");
        out.println("#include <stdlib.h>");
        out.println("#include <string.h>");

        // Wrap generated C code in the target entry function:
        out.println("\nint main(int argc, char *argv[]){");
        node.jjtGetChild(0).jjtAccept(this, data);
        node.jjtGetChild(1).jjtAccept(this, data);
        out.println("return 0;");
        out.println("}");

        return data;
    }

    public Object visit(ASTSetup node, Object data){
        out.println("/*  SETUP   */");
        for (Node child : node.children)
            child.jjtAccept(this, data);

        out.println();

        return data;
    }

    public Object visit(ASTGame node, Object data){
        out.println("/*  GAME    */");
        for (Node child : node.children)
            child.jjtAccept(this, data);

        return data;
    }

    public Object visit(ASTBoard node, Object data){
        return data;
    }

    boolean isAssignedInScope(Node n, String k) {
        if (n.jjtGetParent() != null) {
            for (Node c : n.jjtGetParent().getChildren()) {
                if (c == n) {
                    break;
                }

                if (GriddyTreeConstants.jjtNodeName[c.getId()].equals("Assign")) {
                    if (c.jjtGetChild(0).getName().equals(k))
                        return true;
                }
            }

            return isAssignedInScope(n.jjtGetParent(), k);
        }

        return false;
    }

    ArrayList<Node> getAssignedInScope(Node n, String k) {
        ArrayList<Node> output = new ArrayList<>();


        if (n.jjtGetParent() != null)
            for (Node c : n.jjtGetParent().getChildren()) {
                if (c == n) return output;

                if (GriddyTreeConstants.jjtNodeName[c.getId()].equals("Assign")) {
                    if (c.jjtGetChild(0).getName().equals(k))
                        output.add(c);

                } else output.addAll(getAssignedInScope(n.jjtGetParent(), k));

            }

        return output;
    }

    public Object visit(ASTAssign node, Object data) {
        Node identNode = node.jjtGetChild(0);
        Node valueNode = node.jjtGetChild(1);
        String ident = identNode.getName();
        Object value = valueNode.jjtGetValue();
        String valueType = GriddyTreeConstants.jjtNodeName[valueNode.getId()];

        // Generate code based on whether the identifier being assigned, has already been declared or not:
        if (isAssignedInScope(identNode, ident)) {
            switch (valueType) {
                case "String" -> {
                    identNode.jjtAccept(this, data);
                    out.print(" = realloc(");
                    identNode.jjtAccept(this, data);
                    out.println(", " + (value.toString().length() + 1) + ");");

                    out.print("strcpy(");
                    identNode.jjtAccept(this, data);
                    out.print(", ");
                    valueNode.jjtAccept(this, data);
                    out.println(");");
                }
                case "Integer" -> {
                    identNode.jjtAccept(this, data);
                    out.print(" = ");
                    valueNode.jjtAccept(this, data);
                    out.println(";");
                }
                case "Board" -> out.println("/* Board declarations not yet implemented... */");
                default -> throw new RuntimeException("Encountered invalid value type in assignment.");
            }
        } else {
            switch (valueType) {
                case "String" -> {
                    out.print("char *");
                    identNode.jjtAccept(this, data);
                    out.println(";");
                    identNode.jjtAccept(this, data);
                    out.println(" = calloc(" + (value.toString().length() + 1) + ", sizeof(char));");
                    out.print("strcpy(");
                    identNode.jjtAccept(this, data);
                    out.print(", ");
                    valueNode.jjtAccept(this, data);
                    out.println(");");
                }
                case "Integer" -> {
                    out.print("int ");
                    identNode.jjtAccept(this, data);
                    out.print(" = ");
                    valueNode.jjtAccept(this, data);
                    out.println(";");
                }
                case "Board" -> out.println("/* Board declarations not yet implemented... */");
                default -> throw new RuntimeException("Encountered invalid value type in assignment.");
            }
        }

        return data;
    }

    public Object visit(ASTAdd node, Object data){
        node.jjtGetChild(0).jjtAccept(this, data);
        out.print("+");
        node.jjtGetChild(1).jjtAccept(this, data);

        return data;
    }

    public Object visit(ASTSub node, Object data){
        node.jjtGetChild(0).jjtAccept(this, data);
        out.print("-");
        node.jjtGetChild(1).jjtAccept(this, data);

        return data;
    }

    public Object visit(ASTString node, Object data) {
        out.print("\"" + node.jjtGetValue() + "\"");

        return data;
    }
    
    public Object visit(ASTIdent node, Object data) {
        out.print(node.getName());

        return data;
    }

    public Object visit(ASTInteger node, Object data) {
        out.print(node.jjtGetValue());

        return data;
    }

    public Object visit(ASTDiv node, Object data) {
        node.jjtGetChild(0).jjtAccept(this, data);
        System.out.print("/");
        node.jjtGetChild(1).jjtAccept(this, data);

        return data;
    }

    public Object visit(ASTMod node, Object data) {
        node.jjtGetChild(0).jjtAccept(this, data);
        System.out.print("%");
        node.jjtGetChild(1).jjtAccept(this, data);

        return data;
    }

    public Object visit(ASTMul node, Object data) {
        node.jjtGetChild(0).jjtAccept(this, data);
        System.out.print("*");
        node.jjtGetChild(1).jjtAccept(this, data);

        return data;
    }
}
