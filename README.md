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
