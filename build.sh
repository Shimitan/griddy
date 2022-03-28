# Build script for the Griddy parser

java -cp javacc.jar jjtree -tree src/com/company/griddy.jjt

java -cp javacc.jar javacc gen/griddy.jj

javac -cp gen gen/Griddy.java