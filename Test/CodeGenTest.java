import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.company.codegen.*;

import java.util.Arrays;

public class CodeGenTest {
    TargetFormat formatter = new TargetC();

    @Test
    void pieceDef() {
        Object[] moves = { new Object() };
        var pd = new PieceDef("x_piece", "X", 3, Arrays.stream(moves).toList(), formatter);
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
        var pldef = new PlayerDef(formatter);

        Object[] moves = { new Object() };
        pldef.addPiece("x_piece", new PieceDef("x_piece", "X", 3, Arrays.stream(moves).toList(), formatter));
        pldef.addPiece("y_piece", new PieceDef("y_piece", "Y", 3, Arrays.stream(moves).toList(), formatter));

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
}
