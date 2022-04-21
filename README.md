# Griddy

A programming language for board games.

## Requirements

- JDK 17 (Java Development Kit)
- JRE (Java Runtime Environment)

## Usage

*Generate, compile, and run parser:*

```shell
# Run build script
sh build.sh

# Parse example_1 and output generated code to terminal
java -cp output com.company.Main -f examples/example_1.griddy

# Compile generated code
gcc examples/example_1.griddy.c
```

## Files

*Primary files:*

- Parsing *(JJTree)*: `src/com/company/griddy.jjt`
- AST node interface: `src/com/company/parser/Node.java`
- Base AST node: `src/com/company/parser/SimpleNode.java`
- Code generation: 
  - C target: `src/com/company/target/CVisitor.java`
  - JS target: `src/com/company/target/JSVisitor.java`
- Program entry: `src/com/company/Main.java`