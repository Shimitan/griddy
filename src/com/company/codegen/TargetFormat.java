package com.company.codegen;

import java.util.Map;
import java.util.function.Function;
import java.util.function.BiFunction;

public interface TargetFormat {
    String formatPieceDef(String ident, PieceDef pieceDef);
    String formatPlayerDef(PlayerDef playerDef);
    String formatSetup(SetupStruct setupStruct);
    String formatGame(String body);
    String formatTurn(String playerPrefix, String body);
    String format(SetupStruct setupStruct, GameStruct gameStruct);

    String outputString(String body);
    String outputNumber(String body);
    String outputTable(String ident, int w, int h);
}
