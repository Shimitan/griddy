# Griddy

A programming language for board games.

## Requirements

- JDK 17 (Java Development Kit)
- JRE (Java Runtime Environment)
- GCC (GNU C compiler)

## Usage

```shell
# Build Griddy compiler
sh build.sh

# Compile to binary
java -cp output com.company.Main --file examples/noughts_and_crosses.griddy --compile

# Run binary
./a.out
```

## Files

*Primary files:*

- Parsing *(JJTree)*: `src/com/company/griddy.jjt`
- AST node interface: `src/com/company/parser/Node.java`
- Base AST node: `src/com/company/parser/SimpleNode.java`
- Code generation: 
  - Visitor: `src/com/company/target/CVisitor.java`
- Program entry: `src/com/company/Main.java`