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
            case "@player_one" -> "&_p1";
            case "@player_two" -> "&_p2";
            case "@board" -> "_board";
            case "@current_player" -> "_current_player";
            case "@turn_count" -> "_turn_count";
            case "@win_condition" -> "_win_condition";
            default -> throw new RuntimeException("Unknown identifier: '" + k + "'");
    };

    Function<String, String> getGriddyGlobalType = k -> switch (k) {
        case "@player_one", "@player_two" -> "Player";
        case "@board" -> "Board";
        case "@win_condition" -> "int";
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

        node.childrenAccept(this, generator.setupStruct.body);

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
        String value = switch (argType) {
            case "FuncCall" -> {
                var retNode = arg.jjtGetChild(0);
                var val = retNode.jjtGetValue().toString();
                argType = Util.getFunctionReturnType(node, val);
                yield val + "()";
            }
            case "String" -> "\"" + arg.jjtGetValue() + "\"";
            case "Ident" -> {
                var val = arg.jjtGetValue().toString();

                if (val.startsWith("_")) {
                    argType = getGriddyGlobalType.apply(val.replaceFirst("_", "@"));
                    yield getGriddyGlobal.apply(val);
                }

                if (val.startsWith("@")) {
                    argType = getGriddyGlobalType.apply(val);
                    yield getGriddyGlobal.apply(val);
                }

                argType = Util.getIdentifierType(node, val);
                yield val;
            }
            case "Boolean" -> arg.jjtGetValue().toString().equals("true") ? "1" : "0";
            default -> null;
        };


        return switch (argType) {
            case "Integer", "Boolean" -> output.append(targetFormat.outputNumber(value));
            case "Expr" -> {
                output.append("printf(\"%d\\n\", ");
                arg.jjtAccept(this, output);
                yield output.append(");\n");
            }
            case "String" -> output.append(targetFormat.outputString(value));
            case "Tile" -> output
                    .append("printf(\"%s\\n\", ")
                    .append(value).append("->name")
                    .append(");\n");
            case "Board" -> output.append(targetFormat.outputTable(
                    value,
                    generator.setupStruct.boardWidth,
                    generator.setupStruct.boardHeight
            ));
            default -> throw new RuntimeException("Can't echo value of unknown type: " + argType);
        };
    }

    @Override
    public Object visit(ASTGame node, Object data){
        var winCond = new StringBuilder();
        node.jjtGetChild(0).jjtAccept(this, winCond);
        generator.gameStruct.winCondition = winCond.toString();

        for (int i = 1; i < node.getNumChildren(); i++)
            node.jjtGetChild(i).jjtAccept(this, generator.gameStruct.body);

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


        if (valueType.equals("Ident")) {
            if (value.toString().startsWith("@")) {
                value = getGriddyGlobal.apply(value.toString());
            } else {
                valueType = Util.getIdentifierType(node, value.toString());
            }
        }

        // Generate code based on whether the identifier being assigned, has already been declared or not:
        if (Util.isDeclaredInScope(identNode, ident))
            return switch (valueType) {
                // 1. str_ptr = realloc(str_ptr, str_size);
                // 2. strcpy(str_ptr, str_val);
                case "String" -> output.append(targetFormat.reAssignString(ident, value.toString()));
                // Integer values 0 and >0 used in C boolean expressions instead of bool literals.
                // 1. var_name = int_val;
                case "Integer", "Boolean", "Expr" -> output
                        .append(ident)
                        .append(" = ")
                        .append(value)
                        .append(";\n");
                default -> throw new RuntimeException("Encountered invalid value type in assignment: " + valueType);
            };

        var tmp = new StringBuilder();
        valueNode.jjtAccept(this, tmp);
        value = tmp.toString();

        return switch (valueType) {
            // 1. char *str_ptr;
            // 2. str_ptr = calloc(str_size, sizeof(char));
            // 3. strcpy(str_ptr, str_val);
            case "String" -> output.append(targetFormat.assignString(ident, value.toString()));

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
            case "Tile" -> output.append(targetFormat.assignPieceRef(
                    ident,
                    (int)valueNode.jjtGetChild(0).jjtGetChild(0).jjtGetValue(),
                    (int)valueNode.jjtGetChild(0).jjtGetChild(1).jjtGetValue()
            ));
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
        var ident = node.jjtGetValue().toString();
        if (ident.startsWith("@")) ident = getGriddyGlobal.apply(ident);

        return ((StringBuilder) data).append(ident);
    }

    @Override
    public Object visit(ASTInteger node, Object data) {
        return ((StringBuilder) data).append(node.jjtGetValue());
    }

    @Override
    public Object visit(ASTBoolean node, Object data) {
        return ((StringBuilder) data).append("true".equals(node.jjtGetValue()) ? 1 : 0);
    }

    @Override
    public Object visit(ASTPlace node, Object data) {
        var out = (StringBuilder) data;
        var piece = node.jjtGetChild(0);
        var pos = node.jjtGetChild(1);

        out.append("if (");
        piece.jjtAccept(this, data);
        out.append(".count < ");
        piece.jjtAccept(this, data);
        out.append(".limit) {\n");
        out.append("_board[");
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
        Node limitNode = pieceProps.get("limit");
        int limit = limitNode == null ? Integer.MAX_VALUE : (Integer) limitNode.jjtGetValue();
        Object[] moveset = { new Object() };
        Node startPos = pieceProps.get("StartPosition");

        PieceDef pieceDef = new PieceDef(ident, ident, limit, List.of(moveset), targetFormat);

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

        String argType = Util.getIdentifierType(node, arg.jjtGetValue().toString());
        
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

    @Override
    public Object visit(ASTCondStmt node, Object data) {
        var out = (StringBuilder) data;
        var ifCond = (StringBuilder) node.jjtGetChild(0).jjtAccept(this, new StringBuilder());
        var body = new StringBuilder();
        var multiStmt = node.getNumChildren() > 2 && !node.jjtGetChild(2).toString().equals("CondElse");

        if (multiStmt) body.append("{\n");
        node.jjtGetChild(1).jjtAccept(this, body);
        for (int i = 2; i < node.getNumChildren(); i++) {
            var c = node.jjtGetChild(i);
            if (c.toString().equals("CondElse") && multiStmt) {
                body.append("}\n");
                multiStmt = false;
            }
            c.jjtAccept(this, body);
        }
        if (multiStmt) body.append("}\n");

        return out.append(targetFormat.condStmt(ifCond.toString(), body.toString()));
    }

    @Override
    public Object visit(ASTCondElse node, Object data) {
        var body = new StringBuilder();

        if (node.getNumChildren() > 1) body.append("{\n");
        node.childrenAccept(this, body);
        if (node.getNumChildren() > 1) body.append("}\n");

        return ((StringBuilder) data)
                .append(targetFormat.condElse(body.toString()));
    }

    @Override
    public Object visit(ASTFuncDecl node, Object data) {
        var body = new StringBuilder();
        Node retNode = node.jjtGetChild(3);
        String retType = GriddyTreeConstants.jjtNodeName[retNode.getId()];
        var bodyLen = node.jjtGetChild(2).getNumChildren();

        if (retType.equals("Ident"))
            retType = Util.getIdentifierType(node.jjtGetChild(2).jjtGetChild(bodyLen - 1), retNode.jjtGetValue().toString());

        body.append(switch (retType) {
            case "String" -> "char * ";
            case "Integer", "Boolean" -> "int ";
            default -> throw new RuntimeException("Unknown return type: " + retType);
        }).append(node.jjtGetChild(0).jjtGetValue())
                .append("(").append(") {\n");

        node.jjtGetChild(2).jjtAccept(this, body);
        body.append("return ");
        retNode.jjtAccept(this, body);
        body.append(";\n}\n");

        return ((StringBuilder) data).append(body);
    }

    @Override
    public Object visit(ASTFuncCall node, Object data) {
        var out = (StringBuilder) data;
        node.jjtGetChild(0).jjtAccept(this, data);

        return out.append("()");
    }

    @Override
    public Object visit(ASTStmt node, Object data) {
        node.jjtGetChild(0).jjtAccept(this, data);
        return ((StringBuilder) data).append(";\n");
    }

    @Override
    public Object visit(ASTGetPiece node, Object data) {
        var output = (StringBuilder) data;

        var pieceIdent = node.jjtGetChild(0);
        var player = node.jjtGetChild(1);

        player.jjtAccept(this, output);
        output.append("->");
        return pieceIdent.jjtAccept(this, output);
    }

    @Override
    public Object visit(ASTTileEmpty node, Object data) {
        var output = (StringBuilder) data;
        var pos = (ASTPosition) node.jjtGetChild(0);

        output.append("_board[");
        pos.jjtGetChild(1).jjtAccept(this, data);
        output.append("-1][");
        pos.jjtGetChild(0).jjtAccept(this, data);
        return output.append("-1] == NULL");
    }

    @Override
    public Object visit(ASTTile node, Object data) {
        var output = (StringBuilder) data;
        var pos = (ASTPosition) node.jjtGetChild(0);

        output.append("_board[");
        pos.jjtGetChild(1).jjtAccept(this, data);
        output.append("-1][");
        pos.jjtGetChild(0).jjtAccept(this, data);
        return output.append("-1]");
    }

    @Override
    public Object visit(ASTBoolNot node, Object data) {
        var output = (StringBuilder) data;
        output.append("!(");
        node.jjtGetChild(0).jjtAccept(this, data);
        return output.append(")");
    }
}
