package com.company.parser;

import com.company.*;

public class ASTBool extends SimpleNode {
    public ASTBool(int id) {super(id);}

    public ASTBool(Griddy p, int id) {super(p, id);}

    /** Accept the visitor. **/
    public Object jjtAccept(GriddyVisitor visitor, Object data) {

        return
                visitor.visit(this, data);
    }
}