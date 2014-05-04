grammar Grammar;

program
    : functionR+
    ;

functionR
    : 'int main' '(' ')' '{' stmntList=statementList '}' #Main
    | type=types functionname=VAR '(' paramList=parameterList ')' '{' stmntList=statementList '}' #Function
    ;

statement
    : assignmentR ';' #AssignmentStatement
    | varDeclR ';' #VarDeclarationStatement
    | expression ';' #ExpressionCall
    | 'print' '(' printable=stringRec ')' ';' #Print
    | 'return' expr=expression ';' #Return
    | 'if' '(' eval=boolexpr ')' '{' stmntThenList=statementList '}' ('else' '{' stmntElseList=statementList '}')? #If
    | 'if' '(' eval=boolexpr ')' stmnt=statement #IfSingle
    | 'while' '(' eval=boolexpr ')' '{' stmntList=statementList '}' #While
    ;

statementList
    : statement+
    ;

parameterList
    : paramList+=varDeclR (',' paramList+=varDeclR)*
    |
    ;
expressionList
    : paramList+=expression (',' paramList+=expression)*
    |
    ;

stringRec
    : stringList+=string ('.' stringList+=string)* #RecursiveString
    ;

string
    : stringContent = STRING #StringString
    | integer=expression #IntegerString
    ;

types
    : 'int'
    | 'bool'
    | 'string'
    | 'void'
    ;

varDeclR
    : type=types var=VAR #varDecl
    ;

assignmentR
    : type=types var=VAR ':=' expr=variables #DeclAssi
    | var=VAR ':=' expr=variables #Assignment
    ;

variables
    : expression
    | stringRec
    ;

boolexpr
    : '(' boolexpr ')' #BracketsBool
    | left=expression '=' right=expression #Equals
    | left=expression '!=' right=expression #NotEquals
    | left=expression '<=' right=expression #LessEquals
    | left=expression '>=' right=expression #GreaterEquals
    | left=expression '<' right=expression #Less
    | left=expression '>' right=expression #Greater
    | left=boolexpr '&&' right=boolexpr #And
    | expression #ExpressionBool
    ;

expression
    : functionname=VAR '(' paramList=expressionList ')' #Functioncall
    | '(' expression ')' #Brackets
    | expression operator=('*'|'/') expression #Mult
    | expression operator=('+'|'-') expression #Add
    | number=NUMBER #Number
    | var=VAR #Var
    | 'true' #True
    | 'false' #False
    ;

ML_COMMENT : (NESTED_COMMENTARY) -> channel(HIDDEN) ;

fragment NESTED_COMMENTARY :
      '/*'
      .*?
      ( NESTED_COMMENTARY .*? )*
      '*/'
   ;

NUMBER: [0-9]+;
VAR: [a-zA-Z][a-zA-Z_0-9]*;
COMMENT: '//' ~('\r'|'\n')* -> skip;
STRING: '"' [a-zA-Z_-\^=0-9,. ]* '"';
WS : ([ \t\r\n]+) -> channel(HIDDEN) ;