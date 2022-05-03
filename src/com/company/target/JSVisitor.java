package com.company.target;

import com.company.parser.*;
import java.util.ArrayList;
import com.company.Util;

/**
 * Griddy visitor for JavaScript targets.
 * */
public class JSVisitor extends GriddyDefaultVisitor {
    /** Throw error in case a base AST node is encountered. */
    public Object visit(SimpleNode node, Object data){
        throw new RuntimeException("Encountered SimpleNode");
    }

    /** Root */
    public Object visit(ASTStart node, Object data){
        var output = (StringBuilder) data;

        output.append("\nfunction main(){\n"); // main begin.

        // Setup phase:
        node.jjtGetChild(0).jjtAccept(this, data);
        output.append("\n");

        // Game phase:
        node.jjtGetChild(1).jjtAccept(this, data);

        return output.append("\nreturn 0;\n}\n")
                .append("if (require.main ===  module) {\nmain();\n}\n"); // main end.
    }

    public Object visit(ASTOutput node, Object data) {
        var output = (StringBuilder) data;

        var arg = node.jjtGetChild(0);
        String argType = GriddyTreeConstants.jjtNodeName[arg.getId()];

        if (argType.equals("Ident")) {
            ArrayList<Node> prevAssign = Util.getAssignedInScope(node, arg.jjtGetValue().toString());
            Node assocNode = prevAssign
                    .get(prevAssign.toArray().length - 1)
                    .jjtGetChild(1);
            argType = GriddyTreeConstants.jjtNodeName[assocNode.getId()];
        }

        return switch (argType) {
            case "Integer", "Expr", "String" -> {
                output.append("console.log(");
                arg.jjtAccept(this, data);
                yield output.append(");\n");
            }
            default -> throw new RuntimeException("Can't echo value of unknown type");
        };
    }

    public Object visit(ASTSetup node, Object data){
        ((StringBuilder) data).append("/*  SETUP   */\n");
        for (Node child : node.getChildren())
            child.jjtAccept(this, data);

        return data;
    }

    public Object visit(ASTGame node, Object data){
        // 1. /*  GAME    */
        // 2. let i = 5;
        // 3. do {
        // ...
        // n. } while (0 < i--);

        ((StringBuilder) data)
                .append("/*  GAME    */\n")
                .append("let i = 5;") // for testing while win condition is unimplemented.
                .append("do {\n");

        for (Node child : node.getChildren())
            child.jjtAccept(this, data);

        ((StringBuilder) data).append("\n} while (0 < i--);");

        return data;
    }

    public Object visit(ASTBoard node, Object data){
        return data;
    }

    /**
     * Variable assignment nodes, e.g. `my_var = 42`.
     */
    public Object visit(ASTAssign node, Object data) {
        var output = (StringBuilder) data;

        Node identNode = node.jjtGetChild(0);
        Node valueNode = node.jjtGetChild(1);
        String ident = identNode.jjtGetValue().toString();
        Object value = valueNode.jjtGetValue();
        String valueType = GriddyTreeConstants.jjtNodeName[valueNode.getId()];

        // Generate code based on whether the identifier being assigned, has already been declared or not:
        if (Util.isDeclaredInScope(identNode, ident))
            return switch (valueType) {
                case "String", "Integer", "Expr", "Bool" -> {
                    identNode.jjtAccept(this, data);
                    output.append(" = ");
                    valueNode.jjtAccept(this, data);
                    yield output.append(";\n");
                }
                // NOTE: Board might not be re-assignable
                case "Board" -> output.append("/* Board declarations not yet implemented... */\n");
                default -> throw new RuntimeException("Encountered invalid value type in assignment: " + valueNode);
            };

        return switch (valueType) {
            case "String", "Integer", "Bool", "Expr" -> {
                output.append("let ");
                identNode.jjtAccept(this, data);
                output.append(" = ");
                valueNode.jjtAccept(this, data);
                yield output.append(";\n");
            }
            // TODO: Implement assignable board type.
            case "Board" -> output.append("/* Board declarations not yet implemented... */\n");
            default -> throw new RuntimeException("Encountered invalid value type in assignment: " + valueNode);
        };
    }

    public Object visit(ASTExpr node, Object data) {
        var output = (StringBuilder) data;
        output.append("(");
        for (Node c : node.getChildren())
            c.jjtAccept(this, data);
        return output.append(")");
    }

    public Object visit(ASTOperator node, Object data) {
        return ((StringBuilder) data).append(node.jjtGetValue());
    }

    public Object visit(ASTString node, Object data) {
        return ((StringBuilder) data)
                .append("\"")
                .append(node.jjtGetValue())
                .append("\"");
    }

    public Object visit(ASTIdent node, Object data) {
        return ((StringBuilder) data).append(node.jjtGetValue());
    }

    public Object visit(ASTInteger node, Object data) {
        return ((StringBuilder) data).append(node.jjtGetValue());
    }

    public Object visit(ASTBool node, Object data) {
        return ((StringBuilder) data).append(node.jjtGetValue());
    }
}
