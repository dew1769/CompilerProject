package jvm;

import ast.ASTNode;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Compiler {
    private final StringBuilder bytecode;
    private String className;
    public Compiler() {
        this.bytecode = new StringBuilder();
    }

    public String convertation(ASTNode root) {
        if (!"Program".equals(root.getNodeType())) {
            throw new IllegalArgumentException("Error during converting");
        }

        for (ASTNode child : root.getChildren()) {
            if ("Class".equals(child.getNodeType())) {
                classGen(child);
            }
        }

        return bytecode.toString();
    }

    private void classGen(ASTNode classNode) {
        className = classNode.getNodeName();
        bytecode.append(".class public ").append(className).append("\n");
        bytecode.append(".super java/lang/Object\n\n");

        for (ASTNode child : classNode.getChildren()) {
            switch (child.getNodeType()) {
                case "VarDeclaration":
                    field(className, child);
                    break;
                case "Constructor":
                    constructor(className, child);
                    break;
                case "Method":
                    methodGen(className, child);
                    break;
                case "extends":
                    continue;
                default:
                    throw new UnsupportedOperationException("Unknown class element: " + child.getNodeType());
            }
        }
        saveClassToFile();
    }

    private void saveClassToFile() {
        String filePath = "C:/Users/joker/Project/jasmin_classes/" + className + ".j";
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(bytecode.toString().getBytes());
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }

        bytecode.setLength(0);
    }


    private void field(String className, ASTNode varNode) {
        String fieldName = varNode.getNodeName();
        String fieldType = ofType(varNode.getNodeTypeInfo());
        if (fieldType == null) {
            bytecode.append("new ").append(className).append("\n");
            bytecode.append("dup\n");
            if (varNode.getChildren().get(0).getNodeType().equals("ConstructorCall")) {
                bytecode.append("iconst_1\n").append("bipush 10\n")
                        .append("ldc \"text\"\n")
                        .append("invokespecial ").append(className).append("/<init>(ILjava/lang/String;)V\n");
            }
        } else {
            bytecode.append(".field private ").append(fieldName).append(" ").append(fieldType).append("\n");
        }
    }

    private void constructor(String className, ASTNode constructorNode) {
        bytecode.append("\n.method public <init>(");

        Map<String, String> types = new HashMap<>();
        for (ASTNode arg : constructorNode.getChildren()) {
            if ("Argument".equals(arg.getNodeType())) {
                bytecode.append(ofType(arg.getNodeTypeInfo()));
                types.put(arg.getNodeName(), ofType(arg.getNodeTypeInfo()));
            }
        }

        bytecode.append(")V\n");
        bytecode.append("    .limit stack 2\n");
        bytecode.append("    .limit locals ").append(constructorNode.getChildren().size() + 1).append("\n");
        bytecode.append("    aload_0\n");
        bytecode.append("    invokespecial java/lang/Object/<init>()V\n");

        int localIndex = 1;
        for (ASTNode child : constructorNode.getChildren()) {
            if ("Assignment".equals(child.getNodeType())) {
                ASTNode targetNode = child.getChildren().get(0);
                if ("this".equals(child.getNodeTypeInfo())) {
                    bytecode.append("    aload_0\n");
                    bytecode.append("    iload_").append(localIndex++).append("\n");
                    bytecode.append("    putfield ").append(className).append("/").append(targetNode.getNodeName()).append(" ").append(types.get(targetNode.getNodeName())).append("\n");
                }
            }
        }

        bytecode.append("    return\n");
        bytecode.append(".end method\n");
    }

    private void methodGen(String className, ASTNode methodNode) {
        String returnType = "V";
        String methodName = methodNode.getNodeName();
        if (methodName.equals("main")) {
            bytecode.append(".method public static main([Ljava/lang/String;)V\n");
        } else {
            bytecode.append("\n.method public ").append(methodName).append("(");
        }

        for (ASTNode arg : methodNode.getChildren()) {
            if ("Argument".equals(arg.getNodeType())) {
                bytecode.append(ofType(arg.getNodeTypeInfo()));
            }
        }
        for (ASTNode child : methodNode.getChildren()) {
            if ("ReturnType".equals(child.getNodeType())) {
                returnType = ofType(child.getNodeName());
            }
        }
        if (!methodName.equals("main"))
            bytecode.append(")").append(returnType).append("\n");
        bytecode.append("    .limit stack 3\n");
        bytecode.append("    .limit locals 10\n");

        for (ASTNode child : methodNode.getChildren()) {
            switch (child.getNodeType()) {
                case "Assignment":
                    generateAssignment(child, className);
                    break;
                case "MethodCall":
                    generateMethodCall(child);
                    break;
                case "Argument":
                    break;
                case "ReturnType":
                    break;
                case "ReturnStatement":
                    generateReturnStatement(child, returnType);
                    break;
                case "IfStatement":
                    generateIfStatement(child);
                    break;
                case "WhileStatement":
                    generateWhileStatement(child);
                    break;
                case "VarDeclaration":
                    generateVarDeclaration(child);
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown method element: " + child.getNodeType());
            }
        }
        bytecode.append("    return\n");
        bytecode.append(".end method\n");
    }
    private void generateVarDeclaration(ASTNode varNode) {
        String varName = varNode.getNodeName();
        int localVarIndex = getLocalVarIndex(varName);
        String varType = ofType(varNode.getNodeTypeInfo());
    
        if ("Z".equals(varType)) {
            bytecode.append("    iconst_0\n");
            bytecode.append("    istore ").append(localVarIndex).append("\n");
        } else if (varNode.getChildren().size() > 0) {
            ASTNode initialValue = varNode.getChildren().get(0);
            generateExpression(initialValue);
            bytecode.append("    ").append(storeInstruction(varType)).append(" ").append(localVarIndex).append("\n");
        }
    }
    private String storeInstruction(String varType) {
        switch (varType) {
            case "I": return "istore";
            case "D": return "dstore";
            case "Z": return "istore"; // Boolean uses the same as int
            default: throw new UnsupportedOperationException("Unsupported type for store instruction: " + varType);
        }
    }

    private void generateReturnStatement(ASTNode returnNode, String returnType) {
        if ("V".equals(returnType)) {
            // Void return
            bytecode.append("    return\n");
        } else {
            // Non-void return
            ASTNode returnExpression = returnNode.getChildren().get(0);
            generateExpression(returnExpression);
    
            switch (returnType) {
                case "I":
                    bytecode.append("    ireturn\n");
                    break;
                case "Ljava/lang/String;":
                case "[I":
                    bytecode.append("    areturn\n");
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported return type: " + returnType);
            }
        }
    }

    private void generateAssignment(ASTNode assignNode, String className) {
        ASTNode targetNode = assignNode.getChildren().get(0);
        ASTNode expressionNode = assignNode.getChildren().get(1);

        if ("FieldAccess".equals(targetNode.getNodeType())) {
            generateExpression(expressionNode);
            bytecode.append("    aload_0\n");
            bytecode.append("    swap\n");
            bytecode.append("    putfield ").append(className).append("/").append(targetNode.getNodeName()).append(" I\n");
        } else if ("Identifier".equals(targetNode.getNodeType())) {
            String varName = targetNode.getNodeName();
            int localVarIndex = getLocalVarIndex(varName);
            generateExpression(expressionNode);
            bytecode.append("    istore ").append(localVarIndex).append("\n");
        } else {
            throw new IllegalArgumentException("Unknown assignment target type: " + targetNode.getNodeType());
        }
    }

    private final Map<String, Integer> localVarIndices = new HashMap<>();
    private int currentLocalVarIndex = 1;

    private int getLocalVarIndex(String varName) {
        return localVarIndices.computeIfAbsent(varName, k -> currentLocalVarIndex++);
    }

    private void generateMethodCall(ASTNode methodCallNode) {
        String methodName = methodCallNode.getNodeName();

        if ("print".equals(methodName)) {
            bytecode.append("getstatic java/lang/System/out Ljava/io/PrintStream;\n");
            for (ASTNode arg : methodCallNode.getChildren()) {
                generateExpression(arg);
            }
            bytecode.append("invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V\n");
        }
    }

    private void generateExpression(ASTNode exprNode) {
        switch (exprNode.getNodeType()) {
            case "NumberLiteral":
                bytecode.append("    ldc ").append(exprNode.getNodeName()).append("\n");
                break;
            case "RealLiteral":
                bytecode.append("    ldc2_w ").append(exprNode.getNodeName()).append("\n"); // Use ldc2_w for double precision values
                break;
            case "Identifier":
                int localVarIndex = getLocalVarIndex(exprNode.getNodeName());
                bytecode.append("    iload ").append(localVarIndex).append("\n");
                break;
            case "FieldAccess":
                bytecode.append("    aload_0\n");
                bytecode.append("    getfield ").append(className).append("/").append(exprNode.getNodeName()).append(" I\n");
                break;
            case "MethodCall":
                generateMethodCall(exprNode);
                break;
            case "StringLiteral":
                bytecode.append("    ldc ").append(exprNode.getNodeName()).append("\n");
                break;
            default:
                throw new UnsupportedOperationException("Unknown expression type: " + exprNode.getNodeType());
        }
    }

    private void generateIfStatement(ASTNode ifStatementNode) {
        String labelTrue = "L" + System.nanoTime();
        String labelFalse = "L" + System.nanoTime();
        String labelEnd = "L" + System.nanoTime();
    
        // Condition
        ASTNode conditionNode = ifStatementNode.getChildren().get(0);
        generateExpression(conditionNode);
    
        // Jump to "true" block if condition is true, otherwise to "false"
        bytecode.append("  ifne ").append(labelTrue).append("\n");
        bytecode.append("  goto ").append(labelFalse).append("\n");
    
        // "True" block (ThenBlock)
        bytecode.append(labelTrue).append(":\n");
        if (ifStatementNode.getChildren().size() > 1) {
            ASTNode thenBlockNode = ifStatementNode.getChildren().get(1);
            for (ASTNode child : thenBlockNode.getChildren()) {
                processStatement(child);
            }
        }
        bytecode.append("  goto ").append(labelEnd).append("\n");
    
        // "False" block (ElseBlock, if present)
        bytecode.append(labelFalse).append(":\n");
        if (ifStatementNode.getChildren().size() > 2) {
            ASTNode elseBlockNode = ifStatementNode.getChildren().get(2);
            for (ASTNode child : elseBlockNode.getChildren()) {
                processStatement(child);
            }
        }
    
        // End label
        bytecode.append(labelEnd).append(":\n");
    }

    private void processStatement(ASTNode statementNode) {
        switch (statementNode.getNodeType()) {
            case "VarDeclaration":
                generateVarDeclaration(statementNode);
                break;
            case "Assignment":
                generateAssignment(statementNode, className);
                break;
            case "MethodCall":
                generateMethodCall(statementNode);
                break;
            case "IfStatement":
                generateIfStatement(statementNode);
                break;
            case "WhileStatement":
                generateWhileStatement(statementNode);
                break;
            case "ReturnStatement":
                generateReturnStatement(statementNode, "V"); // Default return type
                break;
            default:
                throw new UnsupportedOperationException("Unknown statement type: " + statementNode.getNodeType());
        }
    }

    private void generateWhileStatement(ASTNode whileStatementNode) {
        String labelStart = "L" + System.nanoTime();
        String labelEnd = "L" + System.nanoTime();

        bytecode.append(labelStart).append(":\n");
        ASTNode conditionNode = whileStatementNode.getChildren().get(0);
        generateExpression(conditionNode);

        bytecode.append("  ifeq ").append(labelEnd).append("\n");

        ASTNode bodyNode = whileStatementNode.getChildren().get(1);
        generateExpression(bodyNode);

        bytecode.append("  goto ").append(labelStart).append("\n");
        bytecode.append(labelEnd).append(":\n");
    }

    private String ofType(String type) {
        switch (type) {
            case "Integer":
                return "I";
            case "String":
                return "Ljava/lang/String;";
            case "Real":
                return "D";
            case "Boolean":
                return "Z";
            case "Array[Integer]":
                return "[I";
            default:
                return "V";
        }
    }
}