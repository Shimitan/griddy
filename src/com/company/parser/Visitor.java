package com.company.parser;

public class Visitor implements GriddyVisitor {
    public Object visit(ASTEcho node, Object data) {
        System.out.print("printf(\"%s\",");
        node.jjtGetChild(0).jjtAccept(this, data);
        System.out.println(");");

        return data;
    }

    public Object visit(SimpleNode node, Object data){
        throw new RuntimeException("encountered SimpleNode");
    }

    public Object visit(ASTStart node, Object data){
        System.out.println("#include <stdio.h>");
        System.out.println("int main(int argc, char *argv[]){");
        node.jjtGetChild(0).jjtAccept(this, data);
        node.jjtGetChild(1).jjtAccept(this, data);
        System.out.println("return 0;");
        System.out.println("}");

        return data;
    }

    public Object visit(ASTSetup node, Object data){
        System.out.println("/*  SETUP   */");

        return data;
    }

    public Object visit(ASTGame node, Object data){
        System.out.println("/*  GAME    */");
        for (Node child : node.children) {
            child.jjtAccept(this, data);
        }

        return data;
    }

    public Object visit(ASTBoard node, Object data){
        return data;
    }

    public Object visit(ASTAssign node, Object data) {
        String valueType = node.jjtGetChild(1).getClass().getSimpleName();

        if (valueType.equals("ASTInteger")) {
            System.out.print("int ");
        } else if (valueType.equals("ASTString")) {
            System.out.print("char ");
            node.jjtGetChild(0).jjtAccept(this, data);
            System.out.print("[] = ");
            node.jjtGetChild(1).jjtAccept(this, data);
        } else {
            throw new RuntimeException("Encountered invalid value type in assignment.");
        }

        return data;
    }

    public Object visit(ASTIdent node, Object data){
        System.out.print(node.jjtGetValue());

        return data;
    }

    public Object visit(ASTAdd node, Object data){
        node.jjtGetChild(0).jjtAccept(this, data);
        System.out.print("+");
        node.jjtGetChild(1).jjtAccept(this, data);

        return data;
    }

    public Object visit(ASTSub node, Object data){
        node.jjtGetChild(0).jjtAccept(this, data);
        System.out.print("-");
        node.jjtGetChild(1).jjtAccept(this, data);

        return data;
    }

    public Object visit(ASTInteger node, Object data){
        System.out.print(node.getValue().toString());

        return data;
    }

    public Object visit(ASTString node, Object data){
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
