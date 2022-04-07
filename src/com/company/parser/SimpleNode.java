package com.company.parser;

import com.company.*;

public
class SimpleNode implements Node {

  protected Node parent;
  protected Node[] children;
  protected int id;
  protected Object value;
  protected Griddy parser;
  protected String name;

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

  public void jjtSetValue(Object value) { this.value = value; }
  public Object jjtGetValue() { return value; }

  /** Accept the visitor. **/
  public Object jjtAccept(GriddyVisitor visitor, Object data)
{
    return visitor.visit(this, data);
  }

  /** Accept the visitor. **/
  public Object childrenAccept(GriddyVisitor visitor, Object data)
{
    if (children != null) {
      for (int i = 0; i < children.length; ++i) {
        children[i].jjtAccept(visitor, data);
      }
    }
    return data;
  }

  public String toString() {
    return GriddyTreeConstants.jjtNodeName[id];
  }
  public String toString(String prefix) { return prefix + toString(); }

  /* Override this method if you want to customize how the node dumps
     out its children. */

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

  public String getName() {
    return name;
  }

  public void setName(String k) {
    name = k;
  }

  public boolean neighborHasKey(String k) {
    if (jjtGetNumChildren() > 1)
      for (Node n : children)
        if (n.getId() != this.getId() && n.getName().equals(k))
          return true;

    return false;
  }

  public boolean keyInParentScope(String k) {
    return parent.neighborHasKey(k);
  }

  public boolean keyInOuterScope(String k) {
    if (parent != null && !keyInParentScope(k))
      return parent.keyInOuterScope(k);

    return false;
  }

  public boolean keyInScope(String k) {
    return neighborHasKey(k) || keyInOuterScope(k);
  }
}
