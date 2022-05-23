    import static org.junit.jupiter.api.Assertions.assertEquals;

    import org.junit.jupiter.api.Test;

    import com.company.codegen.*;
    import com.company.codegen.GriddyStructure.*;

    public class CodeGenTest {
        OutputTemplates templates = new TargetC();

        @Test
        void outputTable() {
            int boardWidth = 3, boardHeight = 4;
            var output = templates.outputTable(boardWidth, boardHeight);
            var expected = """
                    printf("┌───┬───┬───┐\\n");
                    for (int _i = 3; _i >= 0; _i--) {
                    for (int _j = 0; _j < 3; _j++)
                    if (_board[_i][_j]) {
                    if (_board[_i][_j]->player == &_p1) {
                    printf("│ \\x1b[33m\\x1b[1m%c\\x1b[0m ", *_board[_i][_j]->name);
                    } else printf("│ %c ", *_board[_i][_j]->name);
                    } else printf("│   ");
                    printf("│ %d\\n", _i + 1);
                    if (_i > 0) printf("├───┼───┼───┤\\n");
                    }
                    printf("└───┴───┴───┘\\n");
                    printf("  a   b   c \\n");
                    """;

            assertEquals(expected, output);
        }

        @Test
        void pieceDef() {
            var pd = new SetupStruct.PieceDef("pd", templates);
            pd.pieceProps.limit = 3;
            pd.pieceProps.count = 1;
            pd.setOwnerPrefix("_p1");
            pd.pieceProps.placeable = true;

            var output = templates.pieceDef("_p1.pd", pd);
            var expected = """
                    _p1.pd.name = calloc(3, sizeof(char));
                    strcpy(_p1.pd.name, "pd");
                    _p1.pd.limit = 3;
                    _p1.pd.count = 1;
                    _p1.pd.capture = 0;
                    _p1.pd.can_jump = 0;
                    _p1.pd.placeable = 1;
                    _p1.pd.player = &_p1;
                    """;

            assertEquals(expected, output);
        }
    }
