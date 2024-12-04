import ast.ASTNode;
import lexer.Lexer;
import syntax.SyntaxAnalyzer;
import analysis.SemanticAnalyzer;
import token.Token;
import jvm.Compiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File("C:/Users/joker/Project/test_cases/test7.txt"));
        StringBuilder codeLines = new StringBuilder();
        while (scanner.hasNextLine()) {
            codeLines.append(scanner.nextLine());
            codeLines.append("\n");
        }

        String code = codeLines.toString();

        try {
            Lexer lexer = new Lexer(code);
            List<Token> tokens = lexer.getTokens();

            SyntaxAnalyzer syntax = new SyntaxAnalyzer(tokens);
            ASTNode ast = syntax.parse();
            System.out.println(ast.toString());

            SemanticAnalyzer analyzer = new SemanticAnalyzer(ast);
            analyzer.analyze();

            System.out.println("Converting to bytecode...");
            Compiler compiler = new Compiler();
            String res = compiler.convertation(ast);
            try (FileWriter writer = new FileWriter("C:/Users/joker//Project/JVMbytecode.j")) {
                writer.write(res);
            }
            
            //System.out.println("\nResult:\n " + res);
            //compiler.generate(ast, "output.j");
            System.out.println("Bytecode saved");
        } catch (IOException e) {
            System.err.println("Error writing bytecode file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
