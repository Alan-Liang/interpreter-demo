package org.anotherkit.interpreter

import org.antlr.v4.runtime.*

data class Position (val line: Int, val col: Int)
data class SourceLocation
  (val source: String, val start: Position, val end: Position)

open class SourceContext (val parsed: ParserRuleContext) {
  val loc: SourceLocation
  get () {
    return SourceLocation(
      parsed.text,
      Position(parsed.start.line, parsed.start.charPositionInLine),
      endPosition(parsed.stop),
    )
  }
}

private fun endPosition (token: Token): Position {
  val lines = token.text.split('\n')
  return if (lines.size == 1) {
    Position(token.line, token.charPositionInLine + token.text.length)
  } else {
    Position(token.line + lines.size - 1, lines.last().length)
  }
}
