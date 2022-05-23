#!/usr/bin/env sh

# Build script for the Griddy parser

java -cp javacc.jar jjtree src/com/company/griddy.jjt

java -cp javacc.jar javacc src/com/company/parser/griddy.jj

javac -cp src -d output src/com/company/Main.java
