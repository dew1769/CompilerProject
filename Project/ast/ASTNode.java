package ast;

import java.util.ArrayList;
import java.util.List;

public class ASTNode {
    private String nodeType;
    private String nodeName;
    private String nodeTypeInfo;
    private List<ASTNode> children;
    private ASTNode parent;

    public ASTNode(String nodeType) {
        this(nodeType, null, null);
    }

    public ASTNode(String nodeType, String nodeName) {
        this(nodeType, nodeName, null);
    }

    public ASTNode(String nodeType, String nodeName, String nodeTypeInfo) {
        this.nodeType = nodeType;
        this.nodeName = nodeName;
        this.nodeTypeInfo = nodeTypeInfo;
        this.children = new ArrayList<>();
    }

    public void addChild(ASTNode child) {
        child.setParent(this);
        this.children.add(child);
    }

    public void addChildren(List<ASTNode> children) {
        for (ASTNode child : children) {
            child.setParent(this);
        }
        this.children.addAll(children);
    }

    // Getter methods
    public String getNodeType() {
        return nodeType;
    }

    public String getNodeName() {
        return nodeName;
    }

    public List<ASTNode> getChildren() {
        return children;
    }

    public ASTNode getParent() {
        return parent;
    }

    public void setParent(ASTNode parent) {
        this.parent = parent;
    }

    public String getNodeTypeInfo() {
        return this.nodeTypeInfo;
    }

    @Override
    public String toString() {
        return toString(0);
    }

    private String toString(int indent) {
        StringBuilder sb = new StringBuilder();
        String prefix = " ".repeat(indent * 2);
        sb.append(prefix).append(nodeType);

        if (nodeName != null) {
            sb.append(": ").append(nodeName);
        }

        if (nodeTypeInfo != null) {
            sb.append(" (").append(nodeTypeInfo).append(")");
        }

        sb.append("\n");

        for (ASTNode child : children) {
            sb.append(child.toString(indent + 1));
        }

        return sb.toString();
    }
}
