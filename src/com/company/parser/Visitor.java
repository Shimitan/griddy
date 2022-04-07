package com.company.parser;

import static java.lang.System.out;

public class Visitor extends GriddyDefaultVisitor {
    public Object visit(ASTEcho node, Object data) {
        out.println("printf(\"%s\", " + node.jjtGetChild(0).getName() + ");");

        return data;
    }

    public Object visit(SimpleNode node, Object data){
        throw new RuntimeException("encountered SimpleNode");
    }

    public Object visit(ASTStart node, Object data){
        // Include C libraries that might be needed:
        out.println("#include <stdio.h>");
        out.println("#include <stdlib.h");
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
        return null;
    }

    public Object visit(ASTAssign node, Object data) {
        Node identNode = node.jjtGetChild(0);
        Node valueNode = node.jjtGetChild(1);
        String ident = identNode.getName();
        Object value = valueNode.jjtGetValue();
        String valueType = GriddyTreeConstants.jjtNodeName[valueNode.getId()];

        // Generate code based on whether the identifier being assigned, has already been declared or not:
        if (identNode.keyInScope(ident)) {
            switch (valueType) {
                case "String" -> {
                    identNode.jjtAccept(this, data);
                    out.println(" = realloc(" + value.toString().length() + ", sizeof(char));");

                    out.print("strcpy(");
                    identNode.jjtAccept(this, data);
                    out.print(", ");
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
                    out.println(" = calloc(" + value.toString().length() + ", sizeof(char));");
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

    public Object visit(ASTDiv node, Object data) {

        return null;
    }

    public Object visit(ASTMod node, Object data) {
        return null;
    }

    public Object visit(ASTMul node, Object data) {
        return null;
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
}
