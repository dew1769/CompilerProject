package lexer;

import token.Token;
import token.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
    private final List<Token> tokens = new ArrayList<>();
    private final String input;

    public Lexer(String input) {
        this.input = input;
    }

    public List<Token> getTokens() {
        Long pos = 0L;
        String s = input;

        while (!s.isEmpty()) {
            boolean matched = false;

            for (TokenType tokenType : TokenType.values()) {
                Pattern pattern = Pattern.compile("^(" + tokenType.pattern + ")");
                Matcher matcher = pattern.matcher(s);

                if (matcher.find()) {
                    String lex = matcher.group().trim();

                    if (tokenType != TokenType.WHITESPACE) {
                        tokens.add(new Token(tokenType, lex, pos));
                    }

                    s = s.substring(matcher.end());
                    pos += matcher.end();
                    matched = true;
                    break;
                }
            }

            if (!matched) {
                throw new RuntimeException("Unknown token: " + s);
            }
        }

        for (Token token : tokens) {
                System.out.println(token);
        }
        
        return tokens;
    }
}