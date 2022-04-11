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
java -cp output com.company.Main < examples/example_1.griddy

# Parse example_1 and output AST to terminal
java -cp output com.company.Main < examples/example_1.griddy t
```

## Files

*Primary files:*

- Parsing *(JJTree)*: `src/com/company/griddy.jjt`
- AST node interface: `src/com/company/parser/Node.java`
- Base AST node: `src/com/company/parser/SimpleNode.java`
- Code generation: `src/com/company/parser/Visitor.java`
- Program entry: `src/com/company/Main.java`

*Modified AST nodes:*

- `src/com/company/parser/ASTInteger.java`
- `src/com/company/parser/ASTIdent.java`