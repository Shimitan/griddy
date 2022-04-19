package com.company;

import com.company.parser.GriddyTreeConstants;
import com.company.parser.Node;

import java.util.ArrayList;

public class Util {
    /**
     * Check if identifier name has been declared in scope.
     * @param node start
     * @param name identifier
     * @return status
     */
    public static boolean isDeclaredInScope(Node node, String name) {
        if (node.jjtGetParent() == null) return false;

        for (Node c : node.jjtGetParent().getChildren()) {
            if (c == node) break;

            if (GriddyTreeConstants.jjtNodeName[c.getId()].equals("Assign")) {
                if (c.jjtGetChild(0).jjtGetValue().equals(name))
                    return true;
            }
        }

        return isDeclaredInScope(node.jjtGetParent(), name);
    }

    /**
     * Get all previous assignments of identifier name.
     * @param node start
     * @param name identifier
     * @return previous assignment nodes
     */
    public static ArrayList<Node> getAssignedInScope(Node node, String name) {
        var output = new ArrayList<Node>();

        if (node.jjtGetParent() != null)
            for (Node c : node.jjtGetParent().getChildren()) {
                if (c == node) break;

                if (GriddyTreeConstants.jjtNodeName[c.getId()].equals("Assign")
                        && c.jjtGetChild(0).jjtGetValue().equals(name)
                ) output.add(c);
                else output.addAll(getAssignedInScope(node.jjtGetParent(), name));
            }

        return output;
    }
}
