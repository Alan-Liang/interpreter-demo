package org.anotherkit.interpreter

import org.antlr.v4.runtime.*

val ws = arrayOf(' ', '\t', '\u000b', '\u000c', '\u00a0')
val lineTerminator = arrayOf('\r', '\n', '\u2028', '\u2029')
val idExtras = "`~!@#$%^&*-_+=<>,?/"

abstract class AkLexerBase(input: CharStream?) : Lexer(input) {
  enum class TemplateType { NONE, DOUBLE_QUOTE, HEREDOC };
  data class TemplateStackFrame(val type: TemplateType, val heredocDelimiter: String? = null, var parens: Int = 0);
  private var lastToken = null as Token?
  private var parenStack = arrayListOf(TemplateStackFrame(TemplateType.NONE))
  override fun nextToken (): Token {
    val next = super.nextToken()
    if (next.channel == Token.DEFAULT_CHANNEL) {
      this.lastToken = next
    }
    return next
  }
  fun templateIn () {
    parenStack.add(TemplateStackFrame(TemplateType.DOUBLE_QUOTE))
  }
  fun heredocTemplateIn (token: String) {
    var pos = 4
    val delim = if (token.length <= 4) "" else {
      while (ws.contains(token[pos])) ++pos
      val start = pos
      while (!ws.contains(token[pos]) && !lineTerminator.contains(token[pos])) ++pos
      token.slice(start..(pos - 1))
    }
    parenStack.add(TemplateStackFrame(TemplateType.HEREDOC, delim))
  }
  fun heredocEndValid (token: String): Boolean {
    var pos = 1
    while (ws.contains(token[pos])) ++pos
    val start = pos
    while (!ws.contains(token[pos]) && !lineTerminator.contains(token[pos])) ++pos
    val delim = token.slice(start..(pos - 1))
    val expected = parenStack.last().heredocDelimiter
    // println("calling heredocEndValid with delim $delim expected $expected")
    return expected == delim
  }
  fun templateOut () {
    parenStack.removeLastOrNull()
  }
  fun heredocTemplateOut () {
    templateOut()
  }
  fun isInTemplate (): Boolean {
    return parenStack.size > 1
  }
  fun parenIn () {
    ++parenStack[parenStack.size - 1].parens
  }
  fun parenOut () {
    --parenStack[parenStack.size - 1].parens
  }
  fun isInParen (): Boolean {
    return parenStack.last().parens > 0
  }
  fun heredocValid (token: String): Boolean {
    var pos = 4
    while (ws.contains(token[pos])) ++pos
    val start = pos + 1
    while (!ws.contains(token[pos]) && !lineTerminator.contains(token[pos])) ++pos
    val delim = token.slice(start..(pos - 2))
    pos = token.length - 2
    while (ws.contains(token[pos])) --pos
    val end = pos
    while (!ws.contains(token[pos]) && !lineTerminator.contains(token[pos])) --pos
    return token.slice((pos + 1)..end) == delim
  }
  fun identifierNameValid (token: String): Boolean {
    for (char in token) {
      if (!idExtras.contains(char)) return true
    }
    return false
  }
}

// abstract class AkParserBase(input: TokenStream?): Parser(input) {
//   // https://github.com/antlr/grammars-v4/blob/master/javascript/javascript/Java/JavaScriptParserBase.java
//   fun closeBrace (): Boolean {
//     return _input.LT(1).type == AkParser.BraceClose
//   }
//   fun lineTerminatorAhead (): Boolean {
//     val next = _input.get(currentToken.tokenIndex + 1) ?: return true
//     return next.type == AkParser.LineTerminator
//   }
// }
