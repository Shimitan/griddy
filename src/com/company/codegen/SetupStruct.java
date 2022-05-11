package com.company.codegen;

public class SetupStruct {
    public TargetFormat formatter;
    public PlayerDef playerDef;

    protected PieceDef[][] board;
    public StringBuilder body = new StringBuilder();
    public int boardWidth;
    public int boardHeight;

    public SetupStruct(TargetFormat targetFormat) {
        formatter = targetFormat;
        playerDef = new PlayerDef(formatter);
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
        p.count++;
    }

    public void addPiece(PieceDef pieceDef) {
        playerDef.addPiece(pieceDef.identifier, pieceDef);
    }

    public void placeAllPieces() {
        playerDef.player1.forEach(
                (k, v) -> {
                    if (v.startPos[0] != null)
                        placePiece(v, v.startPos[0] - 1, v.startPos[1] - 1);
                }
        );
        playerDef.player2.forEach(
                (k, v) -> {
                    if (v.startPos[0] != null)
                        placePiece(v, v.startPos[0] - 1, boardHeight - v.startPos[1]);
                }
        );
    }

    @Override
    public String toString() {
        placeAllPieces();
        return formatter.formatSetup(this);
    }
}
