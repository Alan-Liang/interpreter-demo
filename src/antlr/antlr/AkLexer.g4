lexer grammar AkLexer;

options {
  superClass=AkLexerBase;
}

fragment SourceChar: '\u0000'..'\u{10ffff}';

// operators
Arrow: '=>';
SingleArrow: '->';
Inc: '++';
Dec: '--';
AddEq: '+=';
SubEq: '-=';
MulEq: '*=';
DivEq: '/=';
ModEq: '%=';
PowEq: '**=';
Add: '+';
Sub: '-';
Pow: '**';
Mul: '*';
Div: '/';
Mod: '%';
Shl: '<<';
Shr: '>>';
Ge: '>=';
Le: '<=';
Gt: '>';
Lt: '<';
Eq: '==';
Ne: '!=';
Not: '!';
And: '&&';
Or: '||';
BitAndEq: '&=';
BitOrEq: '|=';
BitXorEq: '^=';
BitAnd: '&';
BitOr: '|';
BitXor: '^';
BitNot: '~';
Assign: '=';
Range: '..';
PropAccess: '.';
BracketOpen: '[';
BracketClose: ']';
ParenOpen: '(' {parenIn();};
TemplateParenClose: ')' {!isInParen() && isInTemplate()}? -> popMode;
ParenClose: ')' {parenOut();};
BraceOpen: '{';
BraceClose: '}';
Colon: ':';
Semicolon: ';';
Comma: ',';

// keywords: types
Int: 'U'? 'Int' ('8' | '32' | '64')?;
Char: 'Char';
Byte: 'Byte';
Float: 'Float' ('32' | '64');
Bool: 'Bool';
UnitType: 'Unit';
Any: 'Any';
Never: 'Never';

// keywords: builtin constants
True: 'true';
False: 'false';
UnitValue: 'unit';

// keywords: visibility modifiers
Pub: 'pub';
Priv: 'priv';
Prot: 'prot';

// keywords: constructs
Class: 'class';
Enum: 'enum';
Type: 'type';
Interface: 'interface';
Extends: 'extends';
Implements: 'implements';
Throws: 'throws'; // reserved
Fn: 'fn';
If: 'if';
In: 'in';
Else: 'else';
Match: 'match';
While: 'while';
For: 'for';
Try: 'try'; // reserved
Catch: 'catch'; // reserved
Finally: 'finally'; // reserved
Package: 'package'; // reserved
Import: 'import'; // reserved
Export: 'export'; // reserved

// keywords: statements / expressions
Val: 'val'; // reserved
Var: 'var';
Const: 'const'; // reserved
Let: 'let';
New: 'new'; // reserved
Break: 'break';
Continue: 'continue';
Async: 'async'; // reserved
Await: 'await'; // reserved
Return: 'return';
Yield: 'yield'; // reserved
Throw: 'throw'; // reserved
This: 'this';
Super: 'super';


// Comment
//   : MultiLineComment
//   | SingleLineComment
//   ;
MultiLineComment: '/*' .*? '*/' -> channel(HIDDEN);
SingleLineComment: '//' ~[\r\n\u2028\u2029]* -> channel(HIDDEN);

// CommonToken
//   : IdentifierName
//   | IntegerLiteral
//   | FPLiteral
//   | SingleStringLiteral
//   ;

// IntegerLiteral
//   : DecimalIntegerLiteral
//   | BinaryIntegerLiteral
//   | OctalIntegerLiteral
//   | HexIntegerLiteral
//   ;
// FPLiteral
//   : DecimalFPLiteral
//   | BinaryFPLiteral
//   | OctalFPLiteral
//   | HexFPLiteral
//   ;
fragment NumericLiteralSeperator: '\'';
fragment ExponentPart: ExponentIndicator SignedInteger;
fragment ExponentIndicator: [eE];
fragment SignedInteger: [+\-]? DecimalDigits;
DecimalFPLiteral
  : ('0' [dD])? DecimalDigits '.' DecimalDigits? ExponentPart?
  | ('0' [dD])? '.' DecimalDigits ExponentPart?
  ;
DecimalIntegerLiteral: ('0' [dD])? DecimalDigits ExponentPart?;
fragment DecimalDigits: DecimalDigit (DecimalDigit | NumericLiteralSeperator)*;
fragment DecimalDigit: [0-9];
BinaryFPLiteral
  : ('0' [bB])? BinaryDigits '.' BinaryDigits? ExponentPart?
  | ('0' [bB])? '.' BinaryDigits ExponentPart?
  ;
BinaryIntegerLiteral: ('0' [bB])? BinaryDigits ExponentPart?;
fragment BinaryDigits: BinaryDigit (BinaryDigit | NumericLiteralSeperator)*;
fragment BinaryDigit: [01];
OctalFPLiteral
  : ('0' [oO])? OctalDigits '.' OctalDigits? ExponentPart?  | ('0' [oO])? '.' OctalDigits ExponentPart?
  ;
OctalIntegerLiteral: ('0' [oO])? OctalDigits ExponentPart?;
fragment OctalDigits: OctalDigit (OctalDigit | NumericLiteralSeperator)*;
fragment OctalDigit: [0-7];
HexFPLiteral
  : ('0' [xX])? HexDigits '.' HexDigits? ExponentPart?
  | ('0' [xX])? '.' HexDigits ExponentPart?
  ;
HexIntegerLiteral: ('0' [xX])? HexDigits ExponentPart?;
fragment HexDigits: HexDigit (HexDigit | NumericLiteralSeperator)*;
fragment HexDigit: [0-9a-fA-F];

SingleStringLiteral
  : [b]? '\'' SingleStringChars '\''
  | '$' '\'' EscapedSingleStringChars '\''
  ;
fragment SingleStringChars: SingleStringChar+?;
fragment SingleStringChar: ~['];
fragment EscapedSingleStringChars: EscapedSingleStringChar+?;
fragment EscapedSingleStringChar
  : ~['\\]
  | '\\' EscapeSequence
  ;

fragment EscapeSequence
  : CharacterEscapeSequence
  | HexEscapeSequence
  | UnicodeEscapeSequence
  ;
fragment CharacterEscapeSequence: ['"\\abrntefv0$];
fragment HexEscapeSequence: [xX] HexDigit HexDigit;
fragment UnicodeEscapeSequence
  : [uU] HexDigit HexDigit HexDigit HexDigit
  | [uU] '{' HexDigit HexDigit+ '}';

DoubleQuote: '"' {templateIn();} -> pushMode(TEMPLATE);
HeredocLiteral
  : '<<<' '-'? WS+ '\'' IdentifierName? '\'' WS* LineTerminator
  (.+? LineTerminator)?
  IdentifierName? WS* LineTerminator {heredocValid(getText())}?;
HeredocTemplateBegin
  : '<<<' '-'? (WS+ IdentifierName)? WS* LineTerminator
  {heredocTemplateIn(getText());} -> pushMode(HEREDOC_TEMPLATE);

IdentifierName: IdentifierStart IdentifierPart* {identifierNameValid(getText())}?;
// please keep lexer-base.kt in sync
fragment IdentifierExtras: [`~!@#$%^&*\-_+=<>,?/];
fragment IdentifierStart: [\p{XID_Start}] | IdentifierExtras;
fragment IdentifierPart: [\p{XID_Continue}] | IdentifierExtras;

WS
  :(' '
  | '\t'
  | '\u000B'
  | '\u000C'
  | '\u00A0'
  ) -> channel(HIDDEN);
LineTerminator
  :('\r'
  | '\n'
  | '\u2028'
  | '\u2029'
  ) -> channel(HIDDEN);

mode TEMPLATE;

DoubleQuoteEnd: '"' {templateOut();} -> popMode;
DoubleQuoteIdentifier: '$' IdentifierName;
DoubleQuoteStartExpression: '$(' -> pushMode(DEFAULT_MODE);
DoubleQuoteAnythingElse: (~["$\\] | '\\' EscapeSequence)+;

mode HEREDOC_TEMPLATE;

HeredocTemplateEnd
  : LineTerminator WS* IdentifierName? WS* LineTerminator
  {heredocEndValid(getText())}? {heredocTemplateOut();} -> popMode;
HeredocIdentifier: '$' IdentifierName;
HeredocStartExpression: '$(' -> pushMode(DEFAULT_MODE);
HeredocAnythingElse: (~["$\\\r\n\u2028\u2029] | '\\' EscapeSequence)+;
HeredocLineTerminator: LineTerminator;
