package com.company.parser;

import com.company.*;

public
class SimpleNode implements Node {

  protected Node parent = null;
  protected Node[] children;
  protected int id;
  protected Object value;
  protected Griddy parser;

  public SimpleNode(int i) {
    id = i;
  }

  public SimpleNode(Griddy p, int i) {
    this(i);
    parser = p;
  }

  public void jjtOpen() {
  }

  public void jjtClose() {
  }

  public void jjtSetParent(Node n) { parent = n; }
  public Node getParent() { return parent; }

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
    return this.children[i];
  }

  public Node[] getChildren() {
    return this.children;
  }

  public int getNumChildren() {
    return (this.children == null) ? 0 : this.children.length;
  }

  public void jjtSetValue(Object value) { this.value = value; }
  public Object jjtGetValue() { return this.value; }

  /** Accept the visitor. **/
  public StringBuilder jjtAccept(GriddyVisitor visitor, StringBuilder data)
{
    return visitor.visit(this, data);
  }

  /** Accept the visitor. **/
  public StringBuilder childrenAccept(GriddyVisitor visitor, StringBuilder data)
{
    if (this.children != null) {
      for (Node child : this.children) {
        child.jjtAccept(visitor, data);
      }
    }
    return data;
  }

  public String toString() {
    return GriddyTreeConstants.jjtNodeName[id] + ((value != null) ? ": " + value : "");
  }
  public String toString(String prefix) { return prefix + toString(); }

  /* Override this method if you want to customize how the node dumps
     out its children. */

  public void dump(String prefix) {
    System.out.println(toString(prefix));

    if (this.children != null)
      for (Node child : this.children) {
        SimpleNode n = (SimpleNode) child;
        if (n != null) n.dump(prefix + " ");
      }
  }

  public int getId() {
    return id;
  }
}
