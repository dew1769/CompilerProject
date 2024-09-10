%%

%class MyLexer
%unicode
%public
%final
%type String

WHITESPACE = [ \t\r\n]+
DIGIT = [0-9]
LETTER = [a-zA-Z]
ID = {LETTER}({LETTER}|{DIGIT})*
INT_NUMBER = {DIGIT}+
FLOAT_NUMBER = {DIGIT}+\.[0-9]*([eE][+-]?[0-9]+)?
DOUBLE_NUMBER = {FLOAT_NUMBER}
STRING = \"[^\"]*\"

%%

"int"         { return "TOKEN_INT"; }
"class"       { return "TOKEN_CLASS"; }
"public"      { return "TOKEN_PUBLIC"; }
"void"        { return "TOKEN_VOID"; }
"return"      { return "TOKEN_RETURN"; }

"{"           { return "TOKEN_LEFT_BRACE"; }
"}"           { return "TOKEN_RIGHT_BRACE"; }
"("           { return "TOKEN_LEFT_PAREN"; }
")"           { return "TOKEN_RIGHT_PAREN"; }
";"           { return "TOKEN_SEMICOLON"; }
"="           { return "TOKEN_ASSIGN"; }
"+"           { return "TOKEN_PLUS"; }
"-"           { return "TOKEN_MINUS"; }
"*"           { return "TOKEN_MULTIPLICATION"; }
"/"           { return "TOKEN_DIVISION"; }
"."           { return "TOKEN_DOT"; }

{ID}          { return "TOKEN_ID(" + yytext() + ")"; }
{INT_NUMBER}     { return "TOKEN_INT_NUMBER(" + yytext() + ")"; }
{DOUBLE_NUMBER}   { return "TOKEN_FLOAT_NUMBER(" + yytext() + ")"; }
{STRING} { return "TOKEN_STRING(" + yytext() + ")"; }

{WHITESPACE}  { /* whitespace */ }

.             { return "ERROR: Unrecognized character '" + yytext() + "'"; }

