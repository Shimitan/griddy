import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.company.codegen.*;

import java.util.Arrays;

public class CodeGenTest {
    TargetFormat formatter = new TargetC();

    @Test
    void pieceDef() {
        Object[] moves = { new Object() };
        var pd = new GriddyStructure.SetupStruct.PieceDef("x_piece", "X", 3, Arrays.stream(moves).toList(), formatter);
        var expected = """
                .x_piece.name = calloc(2, sizeof(char));
                strcpy(.x_piece.name, "X");
                .x_piece.limit = 3;
                .x_piece.count = 0;
                """;

        assertEquals(expected, pd.toString());
    }

    @Test
    void playerDef() {
        var pldef = new GriddyStructure.SetupStruct.PlayerDef(formatter);

        Object[] moves = { new Object() };
        pldef.addPiece("x_piece", new GriddyStructure.SetupStruct.PieceDef("x_piece", "X", 3, Arrays.stream(moves).toList(), formatter));
        pldef.addPiece("y_piece", new GriddyStructure.SetupStruct.PieceDef("y_piece", "Y", 3, Arrays.stream(moves).toList(), formatter));

        var expected = """
                struct Player {
                  struct Piece x_piece;
                  struct Piece y_piece;
                } _p1, _p2;
                _p1.x_piece.name = calloc(2, sizeof(char));
                strcpy(_p1.x_piece.name, "X");
                _p1.x_piece.limit = 3;
                _p1.x_piece.count = 0;
                _p1.y_piece.name = calloc(2, sizeof(char));
                strcpy(_p1.y_piece.name, "Y");
                _p1.y_piece.limit = 3;
                _p1.y_piece.count = 0;
                _p2.x_piece.name = calloc(2, sizeof(char));
                strcpy(_p2.x_piece.name, "X");
                _p2.x_piece.limit = 3;
                _p2.x_piece.count = 0;
                _p2.y_piece.name = calloc(2, sizeof(char));
                strcpy(_p2.y_piece.name, "Y");
                _p2.y_piece.limit = 3;
                _p2.y_piece.count = 0;
                """;

        assertEquals(expected, pldef.toString());
    }

    @Test
    void condIf() {
        var condStr = "true == false";
        var body = "printf(\"True!\");\n";

        var expected = """
                if (true == false)
                printf("True!");
                """;

        assertEquals(expected, formatter.condStmt(condStr, body));
    }

    @Test
    void condIfElse() {
        var condStr = "true == false";
        var primary = "printf(\"True!\");\n";
        var secondary = "printf(\"False!\");\n";

        var expected = """
                if (true == false)
                printf("True!");
                else
                printf("False!");
                """;

        assertEquals(expected, formatter.condStmt(condStr, primary + formatter.condElse(secondary)));
    }

    @Test
    void condIfElseChain() {
        var condStr1 = "true == false";
        var condStr2 = "1 != 2";
        var body1 = "printf(\"True!\");\n";
        var body2 = "printf(\"Maybe true!\");\n";
        var body3 = "printf(\"False!\");\n";

        var expected = """
                if (true == false)
                printf("True!");
                else
                if (1 != 2)
                printf("Maybe true!");
                else
                printf("False!");
                """;

        assertEquals(expected, formatter.condStmt(condStr1, body1 + formatter.condElse(formatter.condStmt(condStr2, body2 + formatter.condElse(body3)))));
    }
}
