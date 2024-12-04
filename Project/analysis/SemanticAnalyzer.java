package analysis;

import ast.ASTNode;

import java.util.*;

public class SemanticAnalyzer {
    private final ASTNode astRoot;
    private final Stack<Map<String, String>> scopeStack = new Stack<>();
    private final Set<String> globalUsedVariables = new HashSet<>();
    private final Set<String> declaredVariables = new HashSet<>();
    private final Map<String, String> genericTypeMap = new HashMap<>();

    public SemanticAnalyzer(ASTNode astRoot) {
        this.astRoot = astRoot;
        scopeStack.push(new HashMap<>());
    }

    public void analyze() {
        System.out.println("Starting semantic analysis...\n");

        performSemanticChecks(astRoot);
        applyOptimizations(astRoot);

        System.out.println("\n ---------- Updated AST ---------");
        System.out.println(astRoot.toString());
    }

    // -------- SEMANTIC CHECKS --------

    private void performSemanticChecks(ASTNode node) {
        if (node == null) return;

        switch (node.getNodeType()) {
            case "Class":
                handleClass(node);
                break;

            case "VarDeclaration":
                handleVarDeclaration(node);
                break;

            case "Assignment":
                handleAssignment(node);
                break;

            case "Identifier":
                handleIdentifier(node);
                break;

            case "ArrayAccess":
                handleArrayAccess(node);
                break;

            case "ReturnStatement":
                handleReturnStatement(node);
                break;

            case "IfStatement":
                handleIfStatement(node);
                break;

            case "BreakStatement":
                handleBreakStatement(node);
                break;
        }

        for (ASTNode child : node.getChildren()) {
            performSemanticChecks(child);
        }
    }

    private void handleClass(ASTNode classNode) {
        genericTypeMap.clear();

        // Handle generic parameters (e.g., T in `Container<T>`)
        for (ASTNode child : classNode.getChildren()) {
            if ("GenericType".equals(child.getNodeType())) {
                genericTypeMap.put(child.getNodeName(), "Generic");
            }
        }

        // Check class body
        for (ASTNode child : classNode.getChildren()) {
            if (!"GenericType".equals(child.getNodeType())) {
                performSemanticChecks(child);
            }
        }
    }

    private void handleVarDeclaration(ASTNode node) {
        String varName = node.getNodeName();
        String varType = node.getNodeTypeInfo();

        if (varName != null) {
            if (genericTypeMap.containsKey(varType)) {
                // Generic type declared
                currentScope().put(varName, "Generic");
            } else {
                currentScope().put(varName, varType);
            }
            System.out.println("Declaration check: Variable '" + varName + "' declared.");
        }
    }

    private void handleAssignment(ASTNode node) {
        ASTNode target = node.getChildren().get(0);
        String varName = target.getNodeName();

        if (!isDeclared(varName)) {
            System.err.println("Error: Variable '" + varName + "' used before declaration.");
        } else {
            globalUsedVariables.add(varName);
        }
    }

    private void handleIdentifier(ASTNode node) {
        String varUsage = node.getNodeName();
        if (!isDeclared(varUsage)) {
            System.err.println("Error: Variable '" + varUsage + "' used before declaration.");
        } else {
            globalUsedVariables.add(varUsage);
        }
    }

    private void handleArrayAccess(ASTNode node) {
        ASTNode arrayVar = node.getChildren().get(0);
        ASTNode indexExpr = node.getChildren().get(1);

        int arraySize = getArraySize(arrayVar);
        if (arraySize != -1 && "NumberLiteral".equals(indexExpr.getNodeType())) {
            int indexValue = Integer.parseInt(indexExpr.getNodeName());
            if (indexValue < 0 || indexValue >= arraySize) {
                System.err.println("Error: Array index " + indexValue + " out of bounds for array of size " + arraySize);
            }
        }
    }

    private void handleReturnStatement(ASTNode node) {
        if (!isInsideFunction(node)) {
            System.err.println("Error: Return statement used outside of a function or method.");
        }
    }

    private void handleIfStatement(ASTNode node) {
        ASTNode condition = node.getChildren().get(0);
        if ("ArrayAccess".equals(condition.getNodeType())) {
            handleArrayAccess(condition);
        }
    }

    private void handleBreakStatement(ASTNode node) {
        if (!isInsideLoop(node)) {
            System.err.println("Error: 'break' statement used outside of a loop.");
        }
    }

    private boolean isDeclared(String varName) {
        for (Map<String, String> scope : scopeStack) {
            if (scope.containsKey(varName)) return true;
        }
        return false;
    }

    private boolean isInsideFunction(ASTNode node) {
        ASTNode parent = node.getParent();
        while (parent != null) {
            if ("Method".equals(parent.getNodeType()) || "Function".equals(parent.getNodeType())) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }

    private boolean isInsideLoop(ASTNode node) {
        ASTNode parent = node.getParent();
        while (parent != null) {
            if ("WhileStatement".equals(parent.getNodeType())) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }

    private int getArraySize(ASTNode arrayNode) {
        for (ASTNode child : arrayNode.getChildren()) {
            if ("Size".equals(child.getNodeType())) {
                return Integer.parseInt(child.getNodeName());
            }
        }
        return -1;
    }

    private Map<String, String> currentScope() {
        return scopeStack.peek();
    }

    // -------- OPTIMIZATIONS --------

    private void applyOptimizations(ASTNode node) {
        if (node == null) return;

        if ("VarDeclaration".equals(node.getNodeType())) {
            foldConstants(node);
        } else if ("IfStatement".equals(node.getNodeType())) {
            simplifyConditionals(node);
        } else if ("Method".equals(node.getNodeType())) {
            removeUnreachableCode(node);
        }

        for (ASTNode child : node.getChildren()) {
            applyOptimizations(child);
        }
    }

    private void foldConstants(ASTNode node) {
        if (node.getChildren().size() < 1) return;

        ASTNode expression = node.getChildren().get(0);

        if ("MethodCall".equals(expression.getNodeType())) {
            String operation = expression.getNodeName();
            ASTNode left = expression.getChildren().get(0);
            ASTNode right = expression.getChildren().get(1);

            if ("NumberLiteral".equals(left.getNodeType()) && "NumberLiteral".equals(right.getNodeType())) {
                int leftValue = Integer.parseInt(left.getNodeName());
                int rightValue = Integer.parseInt(right.getNodeName());
                int result;

                switch (operation) {
                    case "Plus":
                        result = leftValue + rightValue;
                        break;
                    case "Minus":
                        result = leftValue - rightValue;
                        break;
                    case "Multiply":
                        result = leftValue * rightValue;
                        break;
                    case "Divide":
                        if (rightValue == 0) {
                            System.err.println("Error: Division by zero in constant folding.");
                            return;
                        }
                        result = leftValue / rightValue;
                        break;
                    default:
                        return;
                }

                node.getChildren().set(0, new ASTNode("NumberLiteral", Integer.toString(result)));
                System.out.println("Optimization: Simplified " + leftValue + " " + operation + " " + rightValue + " to " + result);
            }
        }
    }

    private void simplifyConditionals(ASTNode node) {
        if ("IfStatement".equals(node.getNodeType())) {
            ASTNode condition = node.getChildren().get(0);

            if ("Identifier".equals(condition.getNodeType()) && "true".equals(condition.getNodeName())) {
                ASTNode thenBlock = node.getChildren().get(1);
                node.getParent().getChildren().set(node.getParent().getChildren().indexOf(node), thenBlock);
                System.out.println("Optimization: Simplified if-statement with constant condition 'true'.");

            } else if ("Identifier".equals(condition.getNodeType()) && "false".equals(condition.getNodeName())) {
                if (node.getChildren().size() > 2) {
                    ASTNode elseBlock = node.getChildren().get(2);
                    node.getParent().getChildren().set(node.getParent().getChildren().indexOf(node), elseBlock);
                } else {
                    node.getParent().getChildren().remove(node);
                }
                System.out.println("Optimization: Simplified if-statement with constant condition 'false'.");
            }
        }
    }

    private void removeUnreachableCode(ASTNode node) {
        boolean returnFound = false;
        for (ASTNode child : new ArrayList<>(node.getChildren())) {
            if (returnFound) {
                node.getChildren().remove(child);
                System.out.println("Optimization: Removed unreachable code after return statement.");
            }
            if ("ReturnStatement".equals(child.getNodeType())) {
                returnFound = true;
            }
        }
    }
}
