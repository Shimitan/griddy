package com.company.parser;

public class Visitor implements GriddyVisitor{

    public Object visit(ASTEcho node, Object data) {
        System.out.println("printf(\"%s\",");
        node.jjtGetChild(0).jjtAccept(this, data);
        System.out.print(");");

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
        return null;
    }

    public Object visit(ASTAssign node, Object data){

        return null;
    }

    public Object visit(ASTIdent node, Object data){
        return null;
    }

    public Object visit(ASTExpr node, Object data){
        System.out.println("(");
        node.jjtGetChild(0).jjtAccept(this, data);
        System.out.print(")");

        return data;
    }

    public Object visit(ASTAdd node, Object data){
        node.jjtGetChild(0).jjtAccept(this, data);
        node.jjtGetChild(1).jjtAccept(this, data);


        return data;
    }

    public Object visit(ASTMult node, Object data){

        return null;
    }

    public Object visit(ASTInteger node, Object data){
        return null;
    }

    public Object visit(ASTString node, Object data){
        return null;
    }

    public Object visit(ASTMinus node, Object data){
        System.out.println("-");
        node.jjtGetChild(0).jjtAccept(this, data);

        return data;
    }

    public Object visit(ASTPlus node, Object data){
        System.out.println("+");
        node.jjtGetChild(0).jjtAccept(this, data);
        return data;
    }
}
