import java.io.FileReader;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java Main <source_file>");
            return;
        }

        try {

            FileReader reader = new FileReader(args[0]);

            MyLexer lexer = new MyLexer(reader);

            // Get tokens until the end of file
            String token;
            while ((token = lexer.yylex()) != null) {
                System.out.println(token);
            }

        } catch (IOException e) {
            System.err.println("Error reading the file");
        }
    }
}

