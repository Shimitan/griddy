package com.company.codegen;

public interface TargetFormat {
    String formatPieceDef(String ident, PieceDef pieceDef);
    String formatPlayerDef(PlayerDef playerDef);
    String formatSetup(SetupStruct setupStruct);
    String formatGame(String body, String winCond);
    String formatTurn(String playerPrefix, String body);
    String format(SetupStruct setupStruct, GameStruct gameStruct);

    String outputString(String body);
    String outputNumber(String body);
    String outputTable(String ident, int w, int h);

    String condStmt(String condition, String body);
    String condElse(String body);
}
