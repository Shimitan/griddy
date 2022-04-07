package com.company.parser;

import com.company.*;

public
class ASTIdent extends SimpleNode {
  public ASTIdent(int id) {
    super(id);
  }

  public ASTIdent(Griddy p, int id) {
    super(p, id);
  }

  @Override
  public String toString() {
    return super.toString() + ": " + name;
  }
  @Override
  public String toString(String prefix) { return prefix + toString(); }

  /** Accept the visitor. **/
  public Object jjtAccept(GriddyVisitor visitor, Object data) {

    return
    visitor.visit(this, data);
  }
}
