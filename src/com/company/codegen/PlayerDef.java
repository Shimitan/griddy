package com.company.codegen;

import java.util.LinkedHashMap;
import java.util.Map;
public class PlayerDef {
    protected Map<String, PieceDef> player1 = new LinkedHashMap<>();
    protected Map<String, PieceDef> player2 = new LinkedHashMap<>();
    public TargetFormat formatter;
    public PlayerDef(TargetFormat targetFormat) {
        formatter = targetFormat;
    }

    public void addPiece(String ident, PieceDef piece) {
        var piece1 = piece.clone();
        piece1.setOwnerPrefix("_p1");
        player1.put(ident, piece1);

        var piece2 = piece.clone();
        piece2.setOwnerPrefix("_p2");
        player2.put(ident, piece2);
    }

    @Override
    public String toString() {
        return formatter.formatPlayerDef(this);
    }
}
