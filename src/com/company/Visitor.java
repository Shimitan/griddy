package com.company;

import com.company.codegen.*;
import com.company.parser.*;
import com.company.codegen.GriddyStructure.SetupStruct;

import java.util.*;

/**
 * Griddy visitor for C targets.
 */
public class Visitor extends GriddyDefaultVisitor {
    OutputTemplates templates = new TargetC();
    GriddyStructure generator = new GriddyStructure(templates);

    /** Root */
    public StringBuilder visit(ASTStart node, StringBuilder data){
        node.childrenAccept(this, generator.setupStruct.body);
        return data.append(generator);
    }

    /**
     * {@code output} print statement, which maps to C's {@code printf}.
     * */
    public StringBuilder visit(ASTOutput node, StringBuilder data) {
        var arg = node.jjtGetChild(0);
        String argType = GriddyTreeConstants.jjtNodeName[arg.getId()];
        String value = switch (argType) {
            case "FuncCall" -> {
                var val = arg.jjtGetChild(0).jjtGetValue().toString();
                argType = Util.getFunctionReturnType(node, val);
                yield val + "()";
            }
            case "String" -> templates.typeString(arg.jjtGetValue().toString());
            case "Ident" -> {
                var val = arg.jjtGetValue().toString();
                if (val.startsWith("_")) {
                    argType = Util.getGriddyGlobalType.apply(val.replaceFirst("_", "@"));
                    yield Util.getGriddyGlobal.apply(val);
                }
                if (val.startsWith("@")) {
                    argType = Util.getGriddyGlobalType.apply(val);
                    yield Util.getGriddyGlobal.apply(val);
                }
                argType = Util.getIdentifierType(node, val);
                yield val;
            }
            case "Boolean" -> templates.typeBoolean("true".equals(arg.jjtGetValue().toString()));
            default -> null;
        };

        return data.append(switch (argType) {
            case "Integer", "Boolean" -> templates.outputNumber(value);
            case "Expr" -> templates.outputNumber(arg.jjtAccept(this, new StringBuilder()).toString());
            case "String" -> templates.outputString(value);
            case "Tile" -> templates.outputString(value + "->name");
            case "Board" -> templates.outputTable(
                    generator.setupStruct.boardWidth,
                    generator.setupStruct.boardHeight
            );
            default -> throw new RuntimeException("Can't echo value of unknown type: " + argType);
        });
    }

    public StringBuilder visit(ASTGame node, StringBuilder data){
        var winCond = node.jjtGetChild(0).jjtAccept(this, new StringBuilder()).toString();
        generator.gameStruct.winCondition = winCond;

        for (int i = 1; i < node.getNumChildren(); i++)
            node.jjtGetChild(i).jjtAccept(this, generator.gameStruct.body);

        return data;
    }

    public StringBuilder visit(ASTBoard node, StringBuilder data){
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
    public StringBuilder visit(ASTAssign node, StringBuilder data) {
        Node identNode = node.jjtGetChild(0);
        Node valueNode = node.jjtGetChild(1);
        String ident = identNode.jjtGetValue().toString();
        Object value = valueNode.jjtGetValue();
        String valueType = GriddyTreeConstants.jjtNodeName[valueNode.getId()];

        if (ident.startsWith("@")) {
            switch (ident) {
                case "@can_jump" ->
                    SetupStruct.PresetGlobals.canJump = value.toString().equals("true");
                case "@limit" ->
                    SetupStruct.PresetGlobals.limit = (int) value;
                case "@placeable" ->
                    SetupStruct.PresetGlobals.placeable = value.toString().equals("true");
                case "@capture" ->
                    SetupStruct.PresetGlobals.capture = value.toString().equals("true");
                default -> throw new RuntimeException("Unable to assign a value to global: " + ident);
            }
            return data;
        }

        if (valueType.equals("Ident"))
            if (value.toString().startsWith("@")) {
                value = Util.getGriddyGlobal.apply(value.toString());
            } else valueType = Util.getIdentifierType(node, value.toString());

        // Generate code based on whether the identifier being assigned, has already been declared or not:
        if (Util.isDeclaredInScope(identNode, ident))
            return data.append(switch (valueType) {
                case "String" -> templates.reAssignString(ident, value.toString());
                case "Integer", "Expr" -> templates.reAssignNumber(ident, value.toString());
                case "Boolean" -> templates.reAssignBoolean(ident, value.toString());
                default -> throw new RuntimeException("Encountered invalid value type in assignment: " + valueType);
            });

        value = valueNode.jjtAccept(this, new StringBuilder()).toString();

        return data.append(switch (valueType) {
            case "String" -> templates.assignString(ident, value.toString());
            case "Integer", "Expr" -> templates.assignNumber(ident, value.toString());
            case "Boolean" -> templates.assignBoolean(ident, value.toString());
            case "Tile" -> templates.assignPieceRef(ident,
                    (int)valueNode.jjtGetChild(0).jjtGetChild(0).jjtGetValue(),
                    (int)valueNode.jjtGetChild(0).jjtGetChild(1).jjtGetValue()
            );
            default -> throw new RuntimeException("Encountered invalid value type in assignment: " + valueNode);
        });
    }

    public StringBuilder visit(ASTExpr node, StringBuilder data) {
        data.append("(");
        for (Node c : node.getChildren())
            c.jjtAccept(this, data);
        return data.append(")");
    }

    public StringBuilder visit(ASTOperator node, StringBuilder data) {
        var op = node.jjtGetValue().toString();
        return data.append(templates.logicalOperator(op));
    }

    public StringBuilder visit(ASTString node, StringBuilder data) {
        return data.append(templates.typeString(node.jjtGetValue().toString()));
    }

    public StringBuilder visit(ASTIdent node, StringBuilder data) {
        var ident = node.jjtGetValue().toString();
        if (ident.startsWith("@")) ident = Util.getGriddyGlobal.apply(ident);

        return data.append(ident);
    }

    public StringBuilder visit(ASTInteger node, StringBuilder data) {
        return data.append(node.jjtGetValue());
    }

    public StringBuilder visit(ASTBoolean node, StringBuilder data) {
        return data.append(templates.typeBoolean(
                "true".equals(node.jjtGetValue())
        ));
    }

    public StringBuilder visit(ASTPlace node, StringBuilder data) {
        var ident = node.jjtGetChild(0).jjtGetValue().toString();
        return data.append(templates.place(ident));
    }

    public StringBuilder visit(ASTPiece node, StringBuilder data) {
        @SuppressWarnings("unchecked")
        var pieceProps = (HashMap<String, Node>) node.jjtGetValue();

        var ident = node.jjtGetChild(0).jjtGetValue().toString();
        GriddyStructure.SetupStruct.PieceDef pieceDef = new GriddyStructure.SetupStruct.PieceDef(ident, templates);

        Node limitNode = pieceProps.get("limit");
        if (limitNode != null) pieceDef.pieceProps.limit = (Integer) limitNode.jjtGetValue();

        Node captureNode = pieceProps.get("capture");
        if (captureNode != null) pieceDef.pieceProps.capture = captureNode.jjtGetValue().toString().equals("true");

        Node startPos = pieceProps.get("start_position");
        if (startPos != null)
            for (Node n : startPos.getChildren()) pieceDef.addStartPos(
                (Integer) n.jjtGetChild(0).jjtGetValue(),
                (Integer) n.jjtGetChild(1).jjtGetValue()
            );

        Node placeableNode = pieceProps.get("placeable");
        if (placeableNode != null) pieceDef.pieceProps.placeable = placeableNode.jjtGetValue().toString().equals("true");

        Node canJumpNode = pieceProps.get("can_jump");
        if (canJumpNode != null) pieceDef.pieceProps.canJump = canJumpNode.jjtGetValue().toString().equals("true");

        generator.setupStruct.addPiece(pieceDef);

        return data;
    }
  
    public StringBuilder visit(ASTInput node, StringBuilder data) throws RuntimeException {
        var arg = node.jjtGetChild(0);
        String argType = Util.getIdentifierType(node, arg.jjtGetValue().toString());
        
        return data.append(switch (argType) {
            case "Integer", "Expr", "Boolean" -> "scanf(\"%d\", &" + arg.jjtAccept(this, new StringBuilder()).toString() + ");\n";
            default -> throw new RuntimeException("Can't scan value of unknown type: " + argType);
        });
    }

    public StringBuilder visit(ASTCondStmt node, StringBuilder data) {
        var ifCond = node.jjtGetChild(0).jjtAccept(this, new StringBuilder());
        var body = new StringBuilder();
        var multiStmt = node.getNumChildren() > 2 && !node.jjtGetChild(2).toString().equals("CondElse");

        for (int i = 1; i < node.getNumChildren(); i++) {
            node.jjtGetChild(i).jjtAccept(this, body);
        }

        return data.append(templates.condStmt(ifCond.toString(), body.toString()));
    }

    public StringBuilder visit(ASTCondElse node, StringBuilder data) {
        var body = node.childrenAccept(this, new StringBuilder());
        return data.append(templates.condElse(body.toString()));
    }

    public StringBuilder visit(ASTFuncDecl node, StringBuilder data) {
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
                .append("() {\n");

        node.jjtGetChild(2).jjtAccept(this, body);
        body.append("return ");
        retNode.jjtAccept(this, body);
        body.append(";\n}\n");

        return data.append(body);
    }
    
    public StringBuilder visit(ASTFuncCall node, StringBuilder data) {
        return node.jjtGetChild(0).jjtAccept(this, data).append("()");
    }

    public StringBuilder visit(ASTStmt node, StringBuilder data) {
        return node.jjtGetChild(0).jjtAccept(this, data).append(";\n");
    }

    public StringBuilder visit(ASTGetPiece node, StringBuilder data) {
        var pieceIdent = node.jjtGetChild(0);
        var player = node.jjtGetChild(1);
        player.jjtAccept(this, data).append("->");
        return pieceIdent.jjtAccept(this, data);
    }

    public StringBuilder visit(ASTTileEmpty node, StringBuilder data) {
        var pos = (ASTPosition) node.jjtGetChild(0);
        int x = (int) pos.jjtGetChild(0).jjtGetValue();
        int y = (int) pos.jjtGetChild(1).jjtGetValue();
        return data.append(templates.tile(x, y))
                .append("== NULL");
    }

    public StringBuilder visit(ASTTile node, StringBuilder data) {
        var pos = (ASTPosition) node.jjtGetChild(0);
        int x = (int) pos.jjtGetChild(0).jjtGetValue();
        int y = (int) pos.jjtGetChild(1).jjtGetValue();
        return data.append(templates.tile(x, y));
    }

    public StringBuilder visit(ASTBoolNot node, StringBuilder data) {
        return data.append(templates.unaryNot(
                node.jjtGetChild(0).jjtAccept(this, new StringBuilder()).toString()
        ));
    }
}
