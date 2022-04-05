# Build script for the Griddy parser

java -cp javacc.jar jjtree -tree src/com/company/griddy.jjt

java -cp javacc.jar javacc src/com/company/parser/griddy.jj

javac -verbose -cp src src/com/company/Main.java
