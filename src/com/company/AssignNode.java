package com.company;

public class AssignNode implements Node {
    protected Node parent;
    protected Node[] children;
    protected int id;
    protected String left;
    protected Object right;
    protected Griddy parser;

    public AssignNode(int i) {
        id = i;
    }

    public AssignNode(Griddy p, int i) {
        this(i);
        parser = p;
    }

    public void jjtOpen() {
    }

    public void jjtClose() {
    }

    public void jjtSetParent(Node n) { parent = n; }
    public Node jjtGetParent() { return parent; }

    public void jjtAddChild(Node n, int i) {
        if (children == null) {
            children = new Node[i + 1];
        } else if (i >= children.length) {
            Node c[] = new Node[i + 1];
            System.arraycopy(children, 0, c, 0, children.length);
            children = c;
        }
        children[i] = n;
    }

    public Node jjtGetChild(int i) {
        return children[i];
    }

    public int jjtGetNumChildren() {
        return (children == null) ? 0 : children.length;
    }

    public void setLeft(Object value) { this.left = value; }
    public String getLeft() { return left; }

    public void setRight(Object value) { this.right = value }
    public Object getRight() { return right }

    /** Accept the visitor. **/
    public Object jjtAccept(GriddyVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }

    public String toString() {
        return GriddyTreeConstants.jjtNodeName[id] + (value != null ? (": " + value) : "");
    }
    public String toString(String prefix) { return prefix + toString(); }

    public void dump(String prefix) {
        System.out.println(toString(prefix));
        if (children != null) {
            for (int i = 0; i < children.length; ++i) {
                SimpleNode n = (SimpleNode)children[i];
                if (n != null) {
                    n.dump(prefix + " ");
                }
            }
        }
    }

    public int getId() {
        return id;
    }
}