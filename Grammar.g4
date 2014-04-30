grammar Grammar;

program
    : statement+
    ;

statement
    : assignmentR ';' #AssignmentStatement
    | varDeclR ';' #VarDeclarationStatement
    | 'print' '(' printable=printableR ')' ';' #Print
    | 'if' '(' eval=boolexpr ')' '{' stmntThenList=statementList '}' ('else' '{' stmntElseList=statementList '}')? #If
    ;

statementList
    : statement+
    ;

printableR
    : string ('+' string)* #PrintableL
    ;

string
    : integer=expression #IntegerPrint
    ;

varDeclR
    : ('int'|'bool') var=VAR #varDecl
    ;

assignmentR
    : ('int'|'bool') var=VAR ':=' expr=expression #DeclAssi
    | var=VAR ':=' expr=expression #Assignment
    ;

boolexpr
    : '(' boolexpr ')' #KlammerBool
    | left=expression '==' right=expression #Equals
    | left=expression '!=' right=expression #NotEquals
    | left=expression '<=' right=expression #LessEquals
    | left=expression '>=' right=expression #GreaterEquals
    | left=expression '<' right=expression #Less
    | left=expression '>' right=expression #Greater
    | left=boolexpr '&&' right=boolexpr #And
    | 'true' #True
    | 'false' #False
    ;

expression
    : '(' expression ')' #Klammer
    | expression operator=('*'|'/') expression #Mult
    | expression operator=('+'|'-') expression #Add
    | zahl=ZAHL #Zahl
    | var=VAR #Var
    ;

faktor
    : zahl=ZAHL #Zahl2
    | '(' + expr=expression + ')' #Verschachtelung
    ;


additionalt: links=additionalt '+' rechts=ZAHL #Plus
    | zahl=ZAHL #Zahl1
    ;

ZAHL: [0-9]+;
VAR: [a-zA-Z][a-zA-Z_0-9]*;
WS : [ \t\r\n]+ -> skip ;