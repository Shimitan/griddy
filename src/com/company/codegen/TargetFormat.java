package com.company.codegen;

public interface TargetFormat {
    String formatPieceDef(String ident, GriddyStructure.SetupStruct.PieceDef pieceDef);
    String formatPlayerDef(GriddyStructure.SetupStruct.PlayerDef playerDef);
    String formatSetup(GriddyStructure.SetupStruct setupStruct);
    String formatGame(String body, String winCond);
    String formatTurn(String playerPrefix, String body);
    String format(GriddyStructure.SetupStruct setupStruct, GriddyStructure.GameStruct gameStruct);

    String outputString(String body);
    String outputNumber(String body);
    String outputTable(String ident, int w, int h);

    String condStmt(String condition, String body);
    String condElse(String body);

    String assignPieceRef(String ident, int x, int y);
    String formatPieceRef(int x, int y);
    String reAssignVar(String ident, String body);

    String assignString(String ident, String body);
    String reAssignString(String ident, String body);

    String assignNumber(String ident, String body);
    String reAssignNumber(String ident, String body);

    String assignBoolean(String ident, String body);
    String reAssignBoolean(String ident, String body);

    String formatPlace(String pieceIdent);

    String formatLogicalOperator(String token);
}
