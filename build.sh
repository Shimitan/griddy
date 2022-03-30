# Build script for the Griddy parser

java -cp javacc.jar jjtree -tree src/com/company/griddy.jjt

cp -r src/com/company/* src/com/company/

java -cp javacc.jar javacc src/com/company/parser/griddy.jj

javac -cp src src/com/company/Main.java
