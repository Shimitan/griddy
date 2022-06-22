package com.company.codegen;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GriddyStructure {
    public SetupStruct setupStruct;
    public GameStruct gameStruct;
    protected OutputTemplates templates;

    public GriddyStructure(OutputTemplates targetTemplates) {
        templates = targetTemplates;
        setupStruct = new SetupStruct(targetTemplates);
        gameStruct = new GameStruct(targetTemplates);
    }

    @Override
    public String toString() {
        return templates.wrapper(setupStruct, gameStruct);
    }

    public static class GameStruct {
        protected OutputTemplates templates;
        public StringBuilder body = new StringBuilder();
        public String winCondition;

        public GameStruct(OutputTemplates targetTemplates) {
            templates = targetTemplates;
        }

        @Override
        public String toString() {
            return templates.game(body.toString(), winCondition);
        }
    }

    public static class SetupStruct {
        public static class PresetGlobals {
            public static int limit = Integer.MAX_VALUE;
            public static boolean capture = false;
            public static boolean placeable = true;
            public static boolean canJump = false;
        }

        public OutputTemplates templates;
        public PlayerDef playerDef;

        protected PieceDef[][] board;
        public StringBuilder body = new StringBuilder();
        public int boardWidth;
        public int boardHeight;

        public SetupStruct(OutputTemplates targetTemplates) {
            templates = targetTemplates;
            playerDef = new PlayerDef(templates);
        }

        public PieceDef[][] getBoard() {
            return board;
        }

        public void initBoard(int w, int h) {
            board = new PieceDef[h][w];
            boardWidth = w;
            boardHeight = h;
        }

        public void placePiece(PieceDef p, int x, int y) {
            board[y][x] = p;
            p.pieceProps.count++;
            if (p.pieceProps.count > p.pieceProps.limit)
                throw new RuntimeException("Start positions for piece: " + p.pieceProps.limit);
        }

        public void addPiece(PieceDef pieceDef) {
            playerDef.addPiece(pieceDef.pieceProps.name, pieceDef);
        }

        public void placeAllPieces() {
            playerDef.player1.forEach(
                    (k, v) -> {
                        if (!v.pieceProps.startPos.isEmpty())
                            for (Integer[] pos : v.pieceProps.startPos) {
                                v.pieceProps.count++;
                                placePiece(v, pos[0] - 1, pos[1] - 1);
                            }
                    }
            );
            playerDef.player2.forEach(
                    (k, v) -> {
                        if (!v.pieceProps.startPos.isEmpty())
                            for (Integer[] pos : v.pieceProps.startPos) {
                                v.pieceProps.count++;
                                placePiece(v, pos[0] - 1, boardHeight - pos[1]);
                            }
                    }
            );
        }

        @Override
        public String toString() {
            placeAllPieces();
            return templates.setup(this);
        }

        public static class PlayerDef {
            protected Map<String, PieceDef> player1 = new LinkedHashMap<>();
            protected Map<String, PieceDef> player2 = new LinkedHashMap<>();
            public OutputTemplates templates;
            public PlayerDef(OutputTemplates targetTemplates) {
                templates = targetTemplates;
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
                return templates.playerDef(this);
            }
        }

        public static class PieceDef implements Cloneable {
            public static class PieceProps {
                public String name;
                public Integer limit = PresetGlobals.limit;
                public boolean placeable = PresetGlobals.placeable;
                public boolean capture = PresetGlobals.capture;
                public boolean canJump = PresetGlobals.canJump;
                public ArrayList<Integer[]> startPos = new ArrayList<>();
                public int count = 0;
                public List<Object> moveSet;
            }

            public PieceProps pieceProps = new PieceProps();

            public OutputTemplates templates;
            public String ownerPrefix = "";

            public PieceDef(String name, OutputTemplates targetTemplates) {
                pieceProps.name = name;
                templates = targetTemplates;
            }

            public void addStartPos(int x, int y) {
                var pos = new Integer[2];
                pos[0] = x;
                pos[1] = y;
                pieceProps.startPos.add(pos);
            }

            public void setOwnerPrefix(String prefix) {
                ownerPrefix = prefix;
            }

            @Override
            public String toString() {
                return templates.pieceDef(ownerPrefix + "." + pieceProps.name, this);
            }

            @Override
            public PieceDef clone() {
                try {
                    PieceDef clone = (PieceDef) super.clone();
                    return clone;
                } catch (CloneNotSupportedException e) {
                    throw new AssertionError();
                }
            }
        }
    }
}

