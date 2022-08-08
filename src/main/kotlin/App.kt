package org.anotherkit.interpreter

import org.antlr.v4.runtime.*
import org.anotherkit.interpreter.ast.astFromParseTree
import java.io.FileInputStream
import java.io.InputStream

fun main (args: Array<String>) {
  if (args.size == 0) {
    error("Usage: app <command> [file]")
  }
  val inputStream = if (args.size > 1) { FileInputStream(args[1]) } else { System.`in` }
  val input = CharStreams.fromStream(inputStream)
  when (args[0]) {
    "testrig" -> invokeTestRig(input)
    "parse" -> {
      val lexer = AkLexer(input)
      val parser = AkParser(CommonTokenStream(lexer))
      val rules = AkParser.ruleNames.toList()
      parser.module().expressionSequence().expression()!!.forEach {
        println(it.toStringTree(rules))
      }
    }
    "ast" -> {
      val lexer = AkLexer(input)
      val parser = AkParser(CommonTokenStream(lexer))
      val ast = astFromParseTree(parser.module())
      println(ast)
    }
    else -> error("unknown command ${args[0]}")
  }
}
