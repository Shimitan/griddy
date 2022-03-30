package com.company.parser;

import com.company.Griddy;

public class ASTInteger extends SimpleNode {
    protected Node parent;
    protected Node[] children;
    protected int id;
    protected Object value;

    public ASTInteger(int id) {
        super(id);
    }
    public ASTInteger(Griddy p, int id) {
        super(p, id);
    }

    public void setValue(Object v) { value = v; }
    public Object getValue() { return value; }

    public void jjtOpen() {}
    public void jjtClose() {}

    public void jjtSetParent(Node n) {}
    public Node jjtGetParent() { return parent; }

    public void jjtAddChild(Node n, int i) {
        if (children == null) {
            children = new Node[i + 1];
        } else if (i >= children.length) {
            Node[] c = new Node[i + 1];
            System.arraycopy(children, 0, c, 0, children.length);
            children = c;
        }
        children[i] = n;
    }
    public Node jjtGetChild(int i) { return children[i]; }

    public int jjtGetNumChildren() { return children.length; }

    public int getId() { return id; }

    public String toString() {
        return super.toString() + ": " + value.toString();
    }
    public String toString(String prefix) { return prefix + toString(); }

    public void dump(String prefix) {
        System.out.println(toString(prefix));
    }
}