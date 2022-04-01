package com.company.parser;

import com.company.*;

public
class ASTString extends SimpleNode {
  public ASTString(int id) {
    super(id);
  }

  public ASTString(Griddy p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(GriddyVisitor visitor, Object data) {

    return
    visitor.visit(this, data);
  }

  public String toString() {
    return super.toString() + ": " + value.toString();
  }

}
