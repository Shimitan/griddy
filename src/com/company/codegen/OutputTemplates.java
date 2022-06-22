package com.company.codegen;

import com.company.codegen.GriddyStructure.*;

public interface OutputTemplates {
    String pieceDef(String ident, SetupStruct.PieceDef pieceDef);
    String playerDef(SetupStruct.PlayerDef playerDef);
    String setup(SetupStruct setupStruct);
    String game(String body, String winCond);
    String wrapper(SetupStruct setupStruct, GameStruct gameStruct);

    String outputString(String body);
    String outputNumber(String body);
    String outputTable(int w, int h);

    String condStmt(String condition, String body);
    String condElse(String body);

    String assignPieceRef(String ident, int x, int y);
    String pieceRef(int x, int y);
    String reAssignVar(String ident, String body);

    String assignString(String ident, String body);
    String reAssignString(String ident, String body);

    String assignNumber(String ident, String body);
    String reAssignNumber(String ident, String body);

    String assignBoolean(String ident, String body);
    String reAssignBoolean(String ident, String body);

    String place(String pieceIdent);

    String logicalOperator(String token);

    String typeString(String str);
    String typeNumber(int num);
    String typeBoolean(boolean bool);
    String tile(int x, int y);
    String unaryNot(String body);
}
