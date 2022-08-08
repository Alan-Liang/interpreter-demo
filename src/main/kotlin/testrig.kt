package org.anotherkit.interpreter

import org.antlr.v4.runtime.*
import org.antlr.v4.gui.TestRig

fun invokeTestRig (input: CharStream) {
  class TR : TestRig(arrayOf("AkParser", "module", "-gui")) {
    fun invoke (input: CharStream) {
      val lexer = AkLexer(input)
      val tokens = CommonTokenStream(lexer)
      val parser = AkParser(tokens)
      process(lexer, AkParser::class.java, parser, input)
    }
  }
  val tr = TR()
  tr.invoke(input)
}
