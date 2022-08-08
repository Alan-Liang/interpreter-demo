package org.anotherkit.interpreter;

import org.antlr.v4.runtime.*;

public abstract class AkParserBase extends Parser {
  public AkParserBase (TokenStream input) {
    super(input);
  }
  // public boolean closeBrace () {
  //   return _input.LA(1) == AkParser.BraceClose;
  // }
  public boolean lineTerminatorAhead () {
    var prevIndex = getCurrentToken().getTokenIndex() - 1;
    if (prevIndex < 0) return false;
    var prevType = _input.get(prevIndex).getType();
    return prevType == AkParser.LineTerminator ||
      prevType == AkParser.HeredocLiteral ||
      prevType == AkParser.HeredocTemplateEnd;
  }
}
