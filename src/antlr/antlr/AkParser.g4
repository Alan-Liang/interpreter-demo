parser grammar AkParser;

options {
  tokenVocab=AkLexer;
  superClass=AkParserBase;
}

module: expressionSequence? EOF;

expressionSequence: (expression eoe)* expression;
expressionList: (expression ',')* expression?;

expression
  : blockExpression      # BlockExpr

  | identifier           # IdentiferExpr
  | fpLiteral            # FpLiteralExpr
  | integerLiteral       # IntegerLiteralExpr
  | singleStringLiteral  # SingleStringLiteralExpr
  | doubleStringLiteral  # DoubleStringLiteralExpr
  | heredocLiteral       # HeredocLiteralExpr
  | heredocTemplate      # HeredocTemplateExpr

  | unaryop expression   # UnaryExpr
  | object=expression '.' prop=identifier         # MemberExpr
  | object=expression '[' prop=expression ']'     # ComputedMemberExpr
  | callee=expression '(' args=expressionList ')' # CallExpr
  | l=expression op=('=' | '+=' | '-=' | '*=' | '/=' | '%=' | '**=' | '&=' | '|=' | '^=') r=expression # AssignExpr
  | <assoc=right> l=expression op='**'                      r=expression # BinaryExpr
  | l=expression op=('*' | '/' | '%')                       r=expression # BinaryExpr
  | l=expression op=('+' | '-')                             r=expression # BinaryExpr
  | l=expression op=('<<' | '>>')                           r=expression # BinaryExpr
  | l=expression op=('<=' | '>=' | '<' | '>' | '==' | '!=') r=expression # BinaryExpr
  | l=expression op=('&' | '^' | '|')                       r=expression # BinaryExpr
  | l=expression op='&&'                                    r=expression # BinaryExpr
  | l=expression op='||'                                    r=expression # BinaryExpr

  | variableExpression   # VarExpr
  | arrayExpression      # ArrayExpr

  | ifExpression         # IfExpr
  // | matchExpression      # MatchExpr
  | whileExpression      # WhileExpr
  | forExpression        # ForExpr
  // | tryExpression        # TryExpr

  | returnExpression     # ReturnExpr
  | throwExpression      # ThrowExpr
  | breakExpression      # BreakExpr
  | continueExpression   # ContinueExpr

  | typedefExpression    # TypedefExpr
  | functionExpression   # FunctionExpr
  // | classExpression      # ClassExpr
  ;

blockExpression: '{' expressionSequence? '}';

identifier: IdentifierName;
fpLiteral
  : DecimalFPLiteral
  | BinaryFPLiteral
  | OctalFPLiteral
  | HexFPLiteral
  ;
integerLiteral
  : DecimalIntegerLiteral
  | BinaryIntegerLiteral
  | OctalIntegerLiteral
  | HexIntegerLiteral
  ;
singleStringLiteral: SingleStringLiteral;
doubleStringLiteral: DoubleQuote doubleQuoteAtom* DoubleQuoteEnd;
doubleQuoteAtom
  : DoubleQuoteAnythingElse # DoubleQuoteAtomLiteral
  | DoubleQuoteIdentifier # DoubleQuoteAtomIdentifier
  | DoubleQuoteStartExpression expression TemplateParenClose # DoubleQuoteAtomExpression
  ;
heredocLiteral: HeredocLiteral;
heredocTemplate: HeredocTemplateBegin heredocAtom* HeredocTemplateEnd;
heredocAtom
  : (HeredocAnythingElse | HeredocLineTerminator)+ # HeredocAtomLiteral
  | HeredocIdentifier # HeredocAtomIdentifier
  | HeredocStartExpression expression TemplateParenClose # HeredocAtomExpression
  ;

unaryop
  : Inc
  | Dec
  | Add
  | Sub
  | Not
  | BitNot
  ;

variableExpression: kind=('var' | 'let') variableDeclaration (',' variableDeclaration)*;
variableDeclaration
  : identifier ':' type ('=' expression)?
  | identifier '=' expression
  ;
arrayExpression: '[' (expression ',')* expression? ']';

ifExpression: 'if' '(' test=expression ')' consequent=expression ('else' alternate=expression)?;
matchExpression: 'match' '(' expression ')'; // TODO
whileExpression: 'while' '(' test=expression ')' body=expression;
forExpression: 'for' '(' left=identifier 'in' right=expression ')' body=expression;
// TODO
// tryExpression: 'try' expression;
returnExpression: 'return' expression?;
throwExpression: 'throw' expression;
breakExpression: 'break' identifier?;
continueExpression: 'continue' identifier?;

// TODO: loop labels

typedefExpression: 'type' identifier '=' type;
// TODO: lambdas
functionExpression: 'fn' identifier ('(' (functionParam ',')* functionParam? ')')? ('->' type)? functionBody;
// TODO: default parameters
functionParam: identifier ':' type;
functionBody
  : '=' expression  # FunctionBodyAssign
  | blockExpression # FunctionBodyBlock
  ;

type
  : IdentifierName
  | type type+
  ;

eoe
  : Semicolon+
  // | EOF
  | {lineTerminatorAhead()}?
  ;
