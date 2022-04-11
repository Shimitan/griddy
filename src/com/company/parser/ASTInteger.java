package com.company.parser;

import com.company.*;

public class ASTInteger extends SimpleNode {
    public ASTInteger(int id) {
        super(id);
    }
    public ASTInteger(Griddy p, int id) {
        super(p, id);
    }

    @Override
    public String toString() {
        return super.toString() + ": " + value.toString();
    }
    @Override
    public String toString(String prefix) { return prefix + toString(); }

    @Override
    public void dump(String prefix) {
        System.out.println(toString(prefix));
    }

    /** Accept the visitor. **/
    public Object jjtAccept(GriddyVisitor visitor, Object data) {

        return
                visitor.visit(this, data);
    }
}