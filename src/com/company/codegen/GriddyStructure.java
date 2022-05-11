package com.company.codegen;

public class GriddyStructure {
    public SetupStruct setupStruct;
    public GameStruct gameStruct;
    protected TargetFormat formatter;

    public GriddyStructure(TargetFormat targetFormat) {
        formatter = targetFormat;
        setupStruct = new SetupStruct(targetFormat);
        gameStruct = new GameStruct(targetFormat);
    }

    @Override
    public String toString() {
        return formatter.format(setupStruct, gameStruct);
    }
}

