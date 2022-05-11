package com.company.target;

import com.company.codegen.*;
import com.company.parser.*;

import java.util.*;
import java.util.function.Function;

import com.company.Util;

/**
 * Griddy visitor for C targets.
 */
public class CVisitor extends GriddyDefaultVisitor {

    TargetFormat targetFormat = new TargetC();

    GriddyStructure generator = new GriddyStructure(targetFormat);

    Function<String, String> getGriddyGlobal = k -> switch (k) {
            case "@player1" -> "_p1";
            case "@player2" -> "_p2";
            case "@board" -> "_board";
            case "@current_player" -> "_current_player";
            default -> throw new RuntimeException("Unknown identifier: '" + k + "'");
    };

    Function<String, String> getGriddyGlobalType = k -> switch (k) {
        case "@player1", "@player2" -> "Player";
        case "@board" -> "Board";
        default -> throw new RuntimeException("Unknown identifier: '" + k + "'");
    };

    /** Throw error in case a base AST node is encountered. */
    @Override
    public Object visit(SimpleNode node, Object data){
        throw new RuntimeException("Encountered SimpleNode");
    }

    /** Root */
    @Override
    public Object visit(ASTStart node, Object data){
        var output = (StringBuilder) data;

        node.childrenAccept(this, data);

        return output.append(generator);
    }

    /**
     * {@code output} print statement, which maps to C's {@code printf}.
     * */
    @Override
    public Object visit(ASTOutput node, Object data) {
        var output = (StringBuilder) data;
        var arg = node.jjtGetChild(0);
        String argType = GriddyTreeConstants.jjtNodeName[arg.getId()];
        Node assocNode = null;
        var ident = arg.jjtGetValue().toString();

        if (argType.equals("String")) {
            ident = "\"" + ident + "\"";

        } else if (argType.equals("Ident") && ident.startsWith("@")) {
            argType = getGriddyGlobalType.apply(ident);
            ident = getGriddyGlobal.apply(ident);

        } else if (argType.equals("Ident")) {
            ArrayList<Node> prevAssign = Util.getAssignedInScope(node, ident);
            if (prevAssign.isEmpty()) throw new RuntimeException("Identifier '" + ident + "' unknown.");
            assocNode = prevAssign
                    .get(prevAssign.size() - 1)
                    .jjtGetChild(1);
            argType = GriddyTreeConstants.jjtNodeName[assocNode.getId()];
        }

        return switch (argType) {
            case "Integer", "Expr" -> output.append(targetFormat.outputNumber(ident));
            case "Boolean" -> output
                    .append("printf(\"%d\\n\", ")
                    .append(ident.equals("true") ? 1 : 0)
                    .append(");\n");
            case "String" -> output.append(targetFormat.outputString(ident));
            case "Access" -> output
                    .append("printf(\"%s\\n\", ")
                    .append(ident).append(".name")
                    .append(");\n");
            case "Board" -> output.append(targetFormat.outputTable(
                    ident,
                    generator.setupStruct.boardWidth,
                    generator.setupStruct.boardHeight
            ));
            default -> throw new RuntimeException("Can't echo value of unknown type: " + argType);
        };
    }

    @Override
    public Object visit(ASTGame node, Object data){
        // 1. /*  GAME    */
        // 2. int i = 5;
        // 3. do {
        // ...
        // n. } while (0 < i--);
        node.childrenAccept(this, generator.gameStruct.body);
        return data;
    }

    public Object visit(ASTBoard node, Object data){
        var boardSize = (ASTPosition) node.jjtGetChild(0);
        generator.setupStruct.initBoard(
                generator.setupStruct.boardWidth = (int) boardSize.jjtGetChild(0).jjtGetValue(),
                generator.setupStruct.boardHeight = (int) boardSize.jjtGetChild(1).jjtGetValue()
        );
        return data;
    }

    /**
     * Variable assignment nodes.
     * <br>
     * Example: {@code my_var = 42}.
     */
    @Override
    public Object visit(ASTAssign node, Object data) {
        var output = (StringBuilder) data;

        Node identNode = node.jjtGetChild(0);
        Node valueNode = node.jjtGetChild(1);
        String ident = identNode.jjtGetValue().toString();
        Object value = valueNode.jjtGetValue();
        String valueType = GriddyTreeConstants.jjtNodeName[valueNode.getId()];

        if (ident.startsWith("@")) {
            ident = getGriddyGlobal.apply(ident);
        }

        if (valueType.equals("Ident") && value.toString().startsWith("@")) {
            value = getGriddyGlobal.apply(value.toString());
        }

        // Generate code based on whether the identifier being assigned, has already been declared or not:
        if (Util.isDeclaredInScope(identNode, ident))
            return switch (valueType) {
                // 1. str_ptr = realloc(str_ptr, str_size);
                // 2. strcpy(str_ptr, str_val);
                case "String" ->
                    output.append(ident)
                            .append(" = realloc(")
                            .append(ident)
                            .append(", ")
                            .append(value.toString().length() + 1)
                            .append(");\n")
                            .append("strcpy(")
                            .append(ident)
                            .append(", ")
                            .append(value)
                            .append(");\n");
                // Integer values 0 and >0 used in C boolean expressions instead of bool literals.
                // 1. var_name = int_val;
                case "Integer", "Boolean", "Expr" -> output
                        .append(ident)
                        .append(" = ")
                        .append(value)
                        .append(";\n");
                default -> throw new RuntimeException("Encountered invalid value type in assignment: " + valueNode);
            };

        return switch (valueType) {
            // 1. char *str_ptr;
            // 2. str_ptr = calloc(str_size, sizeof(char));
            // 3. strcpy(str_ptr, str_val);
            case "String" -> output
                    .append("char *")
                    .append(ident)
                    .append(";\n")
                    .append(ident)
                    .append(" = calloc(")
                    .append(value.toString().length() + 1)
                    .append(", sizeof(char));\n")
                    .append("strcpy(")
                    .append(ident)
                    .append(", \"")
                    .append(value)
                    .append("\");\n");

            // Integer values 0 and >0 used in C boolean expressions instead of bool literals.
            // 1. int var_name = int_value;
            case "Integer", "Boolean", "Expr" -> output
                    .append("int ")
                    .append(ident)
                    .append(" = ")
                    .append(value)
                    .append(";\n");

            case "Empty" -> {
                valueNode.jjtAccept(this, data);
                identNode.jjtAccept(this, data);
                yield output.append(";\n");
            }
            case "Access" -> output
                    .append("struct Piece ")
                    .append(ident)
                    .append(";\n")
                    .append(ident)
                    .append(" = ")
                    .append(value)
                    .append(";\n");
            default -> throw new RuntimeException("Encountered invalid value type in assignment: " + valueNode);
        };
    }

    @Override
    public Object visit(ASTExpr node, Object data) {
        var output = (StringBuilder) data;
        output.append("(");
        for (Node c : node.getChildren())
            c.jjtAccept(this, data);
        return output.append(")");
    }

    @Override
    public Object visit(ASTEmpty node, Object data) {
        var type = (String) node.jjtGetValue();

        return ((StringBuilder) data).append(switch (type) {
            case "number", "boolean" -> "int ";
            case "string" -> "char *";
            default -> throw new RuntimeException("Unknown type in empty assignment: '" + type + "'");
        });
    }

    @Override
    public Object visit(ASTOperator node, Object data) {
        return ((StringBuilder) data).append(node.jjtGetValue());
    }

    @Override
    public Object visit(ASTString node, Object data) {
        return ((StringBuilder) data)
                .append("\"")
                .append(node.jjtGetValue())
                .append("\"");
    }

    @Override
    public Object visit(ASTIdent node, Object data) {
        return ((StringBuilder) data).append(node.jjtGetValue());
    }

    @Override
    public Object visit(ASTInteger node, Object data) {
        return ((StringBuilder) data).append(node.jjtGetValue());
    }

    @Override
    public Object visit(ASTBoolean node, Object data) {
        return ((StringBuilder) data).append("true".equals(node.jjtGetValue()) ? 1 : 0);
    }

    /**
     * Generates:
     * <br>
     * {@code 1. some_board[y][x]}
     * @param node an access AST node
     * @param data String Builder
     * @return StringBuilder
     */
    public Object visit(ASTAccess node, Object data) {
        var out = (StringBuilder)data;

        out.append("*");
        node.jjtGetChild(1).jjtAccept(this, data);  //  board
        out.append("[");
        node.jjtGetChild(0).jjtGetChild(1).jjtAccept(this, data);   //  Y
        out.append("-1][");
        node.jjtGetChild(0).jjtGetChild(0).jjtAccept(this, data);   //  X
        return out.append("-1]");
    }

    @Override
    public Object visit(ASTPlace node, Object data) {
        var out = (StringBuilder) data;
        var piece = node.jjtGetChild(0);
        var pos = node.jjtGetChild(2);

        out.append("if (");
        piece.jjtAccept(this, data);
        out.append(".count < ");
        piece.jjtAccept(this, data);
        out.append(".limit) {\n");
        node.jjtGetChild(1).jjtAccept(this, data);  //  board
        out.append("[");
        pos.jjtGetChild(1).jjtAccept(this, data);   //  Y
        out.append("-1][");
        pos.jjtGetChild(0).jjtAccept(this, data);   //  X
        out.append("-1] = &");
        piece.jjtAccept(this, data);
        out.append(";\n");
        piece.jjtAccept(this, data);
        out.append(".count++;\n}\n");

        return data;
    }

    @Override
    public Object visit(ASTPiece node, Object data) {
        @SuppressWarnings("unchecked")
        var pieceProps = (HashMap<String, Node>) node.jjtGetValue();

        var ident = node.jjtGetChild(0).jjtGetValue().toString();
        String name = pieceProps.get("name").jjtGetValue().toString();
        Integer limit = (Integer) pieceProps.get("limit").jjtGetValue();
        Object[] moveset = { new Object() };
        Node startPos = pieceProps.get("StartPosition");

        PieceDef pieceDef = new PieceDef(ident, name, limit, List.of(moveset), targetFormat);

        if (startPos != null) pieceDef.setStartPos(
                (Integer) startPos.jjtGetChild(0).jjtGetValue(),
                (Integer) startPos.jjtGetChild(1).jjtGetValue()
        );

        generator.setupStruct.addPiece(pieceDef);

        return data;
    }
  
    public Object visit(ASTInput node, Object data){
        var output = (StringBuilder) data;
        var arg = node.jjtGetChild(0);
        Node assocNode;

        ArrayList<Node> prevAssign = Util.getAssignedInScope(node, arg.jjtGetValue().toString());
        if (prevAssign.isEmpty()) throw new RuntimeException("Identifier '" + arg.jjtGetValue() + "' unknown.");
        assocNode = prevAssign
                .get(prevAssign.size() - 1)
                .jjtGetChild(1);
        String argType = GriddyTreeConstants.jjtNodeName[assocNode.getId()];
        
        return switch (argType) {
            case "Integer", "Expr", "Boolean" -> {
                output.append("scanf(\"%d\", &");
                arg.jjtAccept(this, data);
                yield output.append(");\n");
            }
            case "String" -> {
                output.append("scanf(\"%s\", &");
                arg.jjtAccept(this, data);
                yield output.append(");\n");
            }
            case "Piece", "Access" -> {
                output.append("scanf(\"%s\", &");
                arg.jjtAccept(this, data);
                yield output.append(".name")
                        .append(");\n");
            }
            default -> throw new RuntimeException("Can't scan value of unknown type: " + argType);
        };
    }
}
