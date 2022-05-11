package com.company.codegen;

import java.util.List;

public class PieceDef implements Cloneable {
    protected String identifier;
    public PieceProps props;
    public TargetFormat formatter;
    public String ownerPrefix = "";
    public Integer[] startPos = new Integer[2];
    public int count = 0;

    public PieceDef(String ident, String name, int limit, List<Object> moves, TargetFormat targetFormat) {
        identifier = ident;
        props = new PieceProps(name, limit, moves);
        formatter = targetFormat;
    }

    public void setStartPos(int x, int y) {
        startPos[0] = x;
        startPos[1] = y;
    }

    public void setOwnerPrefix(String prefix) {
        ownerPrefix = prefix;
    }

    @Override
    public String toString() {
        return formatter.formatPieceDef(ownerPrefix + "." + identifier, this);
    }

    @Override
    public PieceDef clone() {
        try {
            PieceDef clone = (PieceDef) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public record PieceProps(
            String name,
            Integer limit,
            List<Object> moveSet
    ) {}
}
