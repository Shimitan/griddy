package com.company.codegen;

public class GameStruct {
    protected TargetFormat formatter;
    public StringBuilder body = new StringBuilder();

    public GameStruct(TargetFormat targetFormat) {
        formatter = targetFormat;
    }

    @Override
    public String toString() {
        return formatter.formatGame(body.toString());
    }
}
