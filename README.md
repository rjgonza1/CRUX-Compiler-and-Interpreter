# Compiler-and-Interpreter
Scanner, parser, symbol table, and abstract syntax tree for "CRUX" language

Grammar Sepcifications
literal := INTEGER | FLOAT | TRUE | FALSE .
 
designator := IDENTIFIER { "[" expression0 "]" } .
type := IDENTIFIER .
 
op0 := ">=" | "<=" | "!=" | "==" | ">" | "<" .
op1 := "+" | "-" | "or" .
op2 := "*" | "/" | "and" .
 
expression0 := expression1 [ op0 expression1 ] .
expression1 := expression2 { op1  expression2 } .
expression2 := expression3 { op2 expression3 } .
expression3 := "not" expression3
       | "(" expression0 ")"
       | designator
       | call-expression
       | literal .
call-expression := "::" IDENTIFIER "(" expression-list ")" .
expression-list := [ expression0 { "," expression0 } ] .
 
parameter := IDENTIFIER ":" type .
parameter-list := [ parameter { "," parameter } ] .
 
variable-declaration := "var" IDENTIFIER ":" type ";" .
array-declaration := "array" IDENTIFIER ":" type "[" INTEGER "]" { "[" INTEGER "]" } ";" .
function-definition := "func" IDENTIFIER "(" parameter-list ")" ":" type statement-block .
declaration := variable-declaration | array-declaration | function-definition .
declaration-list := { declaration } .
 
assignment-statement := "let" designator "=" expression0 ";" .
call-statement := call-expression ";" .
if-statement := "if" expression0 statement-block [ "else" statement-block ] .
while-statement := "while" expression0 statement-block .
return-statement := "return" expression0 ";" .
statement := variable-declaration
           | call-statement
           | assignment-statement
           | if-statement
           | while-statement
           | return-statement .
statement-list := { statement } .
statement-block := "{" statement-list "}" .
 
program := declaration-list EOF .


/src/crux 
Contains scanner, parser, and symbol table

/src/ast
Contains abstract syntax tree

/src/mips
Contains code generation for MIPS interpreter, SPIM

/src/types
Contains language data types

.asm files
Output of code generation

.crx files
Sample CRUX code to test output

.out files
CRUX output files
