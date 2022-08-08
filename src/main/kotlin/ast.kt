package org.anotherkit.interpreter.ast

import kotlin.reflect.full.memberProperties
import org.antlr.v4.runtime.*
import org.anotherkit.interpreter.*
import org.anotherkit.interpreter.AkParser.*

// TODO: type information
open class AstContext (ctx: ParserRuleContext) : SourceContext(ctx)

sealed class Node (val ctx: AstContext) {
  override fun toString (): String {
    val className = this::class.simpleName
    val fields = this::class.memberProperties.filter {
      it.name != "ctx"
    }.map {
      it as kotlin.reflect.KProperty1<Node, *>
      "${it.name}=${it.get(this)}"
    }.joinToString().indent(2)
    return "$className(\n$fields)"
  }
}

fun astFromParseTree (input: ParserRuleContext): Node {
  val ctx = AstContext(input)
  return when (input) {
    is ModuleContext -> Module(ctx,
      astFromExprSeq(input.expressionSequence()),
    )
    is BlockExprContext -> BlockExpression.from(input.blockExpression())
    is IdentiferExprContext -> astFromId(input.identifier())
    is FpLiteralExprContext -> FpLiteral(ctx,
      parseFloat(input.fpLiteral()),
    )
    is IntegerLiteralExprContext -> IntegerLiteral(ctx,
      parseInt(input.integerLiteral()),
    )
    is SingleStringLiteralExprContext -> StringLiteral(ctx,
      parseSingleQuote(input.singleStringLiteral().text),
    )
    is DoubleStringLiteralExprContext -> {
      val atoms = input.doubleStringLiteral().doubleQuoteAtom()
      val leading = if (atoms.size == 0 || !(atoms[0] is DoubleQuoteAtomLiteralContext)) listOf("") else listOf()
      TemplateLiteral(ctx,
        leading + atoms.filter { it is DoubleQuoteAtomLiteralContext }.map { unescape(it.text) },
        atoms.filter { !(it is DoubleQuoteAtomLiteralContext) }.map {
          when (it) {
            is DoubleQuoteAtomIdentifierContext -> Identifier(AstContext(it), it.text.substring(1))
            is DoubleQuoteAtomExpressionContext -> astFromExpr(it.expression())
            else -> error("unknown double quote atom $it")
          }
        },
      )
    }
    is HeredocLiteralExprContext -> StringLiteral(ctx,
      removeHeredocDelim(input.heredocLiteral().text),
    )
    is HeredocTemplateExprContext -> {
      val atoms = input.heredocTemplate().heredocAtom()
      val leading = if (atoms.size == 0 || !(atoms[0] is HeredocAtomLiteralContext)) listOf("") else listOf()
      TemplateLiteral(ctx,
        leading + atoms.filter { it is HeredocAtomLiteralContext }.map { unescape(it.text) },
        atoms.filter { !(it is HeredocAtomLiteralContext) }.map {
          when (it) {
            is HeredocAtomIdentifierContext -> Identifier(AstContext(it), it.text.substring(1))
            is HeredocAtomExpressionContext -> astFromExpr(it.expression())
            else -> error("unknown double quote atom $it")
          }
        },
      )
    }
    is UnaryExprContext -> UnaryExpression(ctx,
      UnaryOperator.from(input.unaryop().text),
      astFromExpr(input.expression()),
    )
    is BinaryExprContext -> BinaryExpression(ctx,
      BinaryOperator.from(input.op.text),
      astFromExpr(input.l),
      astFromExpr(input.r),
    )
    is MemberExprContext -> MemberExpression(ctx,
      astFromExpr(input.`object`),
      astFromId(input.prop),
    )
    is ComputedMemberExprContext -> ComputedMemberExpression(ctx,
      astFromExpr(input.`object`),
      astFromExpr(input.prop),
    )
    is CallExprContext -> CallExpression(ctx,
      astFromExpr(input.callee),
      astFromExprList(input.args),
    )
    is AssignExprContext -> AssignmentExpression(ctx,
      AssignmentOperator.from(input.op.text),
      astFromExpr(input.l),
      astFromExpr(input.r),
    )
    is VarExprContext -> VariableDeclaration(ctx,
      input.variableExpression().variableDeclaration()!!.map { VariableDeclarator.from(it) },
      input.variableExpression().kind.text == "var",
    )
    is ArrayExprContext -> ArrayExpression(ctx,
      input.arrayExpression().expression()?.map { astFromExpr(it) } ?: listOf(),
    )
    is IfExprContext -> IfExpression(ctx,
      astFromExpr(input.ifExpression().test),
      astFromExpr(input.ifExpression().consequent),
      input.ifExpression().alternate?.let { astFromExpr(it) },
    )
    is WhileExprContext -> WhileExpression(ctx,
      astFromExpr(input.whileExpression().test),
      astFromExpr(input.whileExpression().body),
    )
    is ForExprContext -> ForInExpression(ctx,
      astFromId(input.forExpression().left),
      astFromExpr(input.forExpression().right),
      astFromExpr(input.forExpression().body),
    )
    is ReturnExprContext -> ReturnExpression(ctx,
      input.returnExpression().expression()?.let { astFromExpr(it) },
    )
    is ThrowExprContext -> ThrowExpression(ctx,
      astFromExpr(input.throwExpression().expression()),
    )
    is BreakExprContext -> BreakExpression(ctx,
      input.breakExpression().identifier()?.let { astFromId(it) },
    )
    is ContinueExprContext -> ContinueExpression(ctx,
      input.continueExpression().identifier()?.let { astFromId(it) },
    )
    is TypedefExprContext ->
      TODO()
    is FunctionExprContext -> FunctionDeclaration(ctx,
      astFromId(input.functionExpression().identifier()),
      input.functionExpression().functionParam()?.map { FunctionParam.from(it) } ?: listOf(),
      when (val body = input.functionExpression().functionBody()) {
        is FunctionBodyAssignContext -> astFromExpr(body.expression())
        is FunctionBodyBlockContext -> BlockExpression.from(body.blockExpression())
        else -> error("unknown function body $body")
      },
    )
    else -> error("unknown parse tree node ${input.toStringTree()}")
  }
}

private fun astFromExprSeq (input: ExpressionSequenceContext?): List<Expression> =
  input?.expression()?.map { astFromExpr(it) } ?: listOf()
private fun astFromExprList (input: ExpressionListContext): List<Expression> =
  input.expression()?.map { astFromExpr(it) } ?: listOf()
private fun astFromId (input: IdentifierContext): Identifier =
  Identifier(AstContext(input), input.IdentifierName().text)

private fun astFromExpr (input: ExpressionContext): Expression {
  return astFromParseTree(input) as Expression
}

private fun parseFloat (input: FpLiteralContext): Double {
  // TODO
  return input.text.toDouble()
}
private fun parseInt (input: IntegerLiteralContext): Int {
  // TODO
  return input.text.toInt()
}

private fun parseSingleQuote (input: String): String {
  val flag = if (input[0] != '\'') input[0] else null
  val start = if (flag != null) 2 else 1
  val unquoted = input.slice(start..(input.length - 2))
  return when (flag) {
    null -> unquoted
    '$' -> unescape(unquoted)
    else -> error("unsupported single string flag $flag")
  }
}
private fun unescape (input: String): String {
  // TODO
  return input
}
private fun removeHeredocDelim (input: String): String {
  // TODO: remove indent if heredoc is <<<-
  var start = 0
  while (!lineTerminator.contains(input[start])) ++start
  ++start
  var end = input.length - 2
  while (!lineTerminator.contains(input[end])) --end
  return input.slice(start..end)
}

sealed interface ExpressionOrPattern
sealed interface Pattern : ExpressionOrPattern
sealed interface Expression : ExpressionOrPattern
sealed interface Declaration : Expression

class Module (ctx: AstContext, val body: List<Expression>) : Node(ctx)
class BlockExpression (ctx: AstContext, val body: List<Expression>) : Node(ctx), Expression {
  companion object {
    fun from (input: BlockExpressionContext): BlockExpression = BlockExpression(AstContext(input),
      astFromExprSeq(input.expressionSequence())
    )
  }
}

class Identifier (ctx: AstContext, val name: String) : Node(ctx), Expression, Pattern
class FpLiteral (ctx: AstContext, val value: Double) : Node(ctx), Expression
class IntegerLiteral (ctx: AstContext, val value: Int) : Node(ctx), Expression
class StringLiteral (ctx: AstContext, val value: String) : Node(ctx), Expression
class TemplateLiteral (ctx: AstContext, val quasis: List<String>, val expressions: List<Expression>) : Node(ctx), Expression

sealed class Function (ctx: AstContext, val id: Identifier?, val params: List<FunctionParam>, val body: Expression) : Node(ctx), Expression
// TODO: type, default value
class FunctionParam (ctx: AstContext, val name: Pattern) : Node(ctx) {
  companion object {
    fun from (input: FunctionParamContext): FunctionParam = FunctionParam(AstContext(input),
      astFromId(input.identifier()),
    )
  }
}

class ReturnExpression (ctx: AstContext, val argument: Expression?) : Node(ctx), Expression
class ThrowExpression (ctx: AstContext, val argument: Expression) : Node(ctx), Expression
class BreakExpression (ctx: AstContext, val label: Identifier?) : Node(ctx), Expression
class ContinueExpression (ctx: AstContext, val label: Identifier?) : Node(ctx), Expression

class IfExpression (ctx: AstContext, val test: Expression, val consequent: Expression, val alternate: Expression?) : Node(ctx), Expression
// TODO: match, try
class WhileExpression (ctx: AstContext, val test: Expression, val body: Expression) : Node(ctx), Expression
class ForInExpression (ctx: AstContext, val left: Pattern, val right: Expression, val body: Expression) : Node(ctx), Expression

class FunctionDeclaration (ctx: AstContext, id: Identifier, params: List<FunctionParam>, body: Expression) : Function(ctx, id, params, body), Declaration
class VariableDeclaration (ctx: AstContext, val declarations: List<VariableDeclarator>, val mutable: Boolean) : Node(ctx), Declaration
// TODO: type
class VariableDeclarator (ctx: AstContext, val id: Pattern, val init: Expression?) : Node(ctx) {
  companion object {
    fun from (input: VariableDeclarationContext): VariableDeclarator =
      VariableDeclarator(AstContext(input), astFromId(input.identifier()), input.expression()?.let { astFromExpr(it) })
  }
}

// TODO: class, this

class ArrayExpression (ctx: AstContext, val elements: List<Expression>) : Node(ctx), Expression

enum class UnaryOperator {
  INC, DEC, POS, NEG, NOT, BIT_NOT;
  companion object {
    fun from (input: String): UnaryOperator = when (input) {
      "++" -> INC
      "--" -> DEC
      "+" -> POS
      "-" -> NEG
      "!" -> NOT
      "~" -> BIT_NOT
      else -> error("unknown unary op $input")
    }
  }
}
class UnaryExpression (ctx: AstContext, val operator: UnaryOperator, val argument: Expression) : Node(ctx), Expression
enum class BinaryOperator {
  POW,
  MUL, DIV, MOD,
  ADD, SUB,
  SHL, SHR,
  LE, GE, LT, GT, EQ, NE,
  BIT_AND, BIT_OR, BIT_XOR,
  AND,
  OR;
  companion object {
    fun from (input: String): BinaryOperator = when (input) {
      "**" -> POW
      "*" -> MUL
      "/" -> DIV
      "%" -> MOD
      "+" -> ADD
      "-" -> SUB
      "<<" -> SHL
      ">>" -> SHR
      "<=" -> LE
      ">=" -> GE
      "<" -> LT
      ">" -> GT
      "==" -> EQ
      "!=" -> NE
      "&" -> BIT_AND
      "|" -> BIT_OR
      "^" -> BIT_XOR
      "&&" -> AND
      "||" -> OR
      else -> error("unknown binary op $input")
    }
  }
}
class BinaryExpression (ctx: AstContext, val operator: BinaryOperator, val left: Expression, val right: Expression) : Node(ctx), Expression
enum class AssignmentOperator {
  ASSIGN, ADD, SUB, MUL, DIV, MOD, POW, BIT_AND, BIT_OR, BIT_XOR;
  companion object {
    fun from (input: String): AssignmentOperator = when (input) {
      "=" -> ASSIGN
      "+=" -> ADD
      "-=" -> SUB
      "*=" -> MUL
      "/=" -> DIV
      "%=" -> MOD
      "**=" -> POW
      "&=" -> BIT_AND
      "|=" -> BIT_OR
      "^=" -> BIT_XOR
      else -> error("unknown assignment op $input")
    }
  }
}
class AssignmentExpression (ctx: AstContext, val operator: AssignmentOperator, val left: ExpressionOrPattern, val right: Expression) : Node(ctx), Expression
class MemberExpression (ctx: AstContext, val `object`: Expression, val property: Identifier) : Node(ctx), Expression
class ComputedMemberExpression (ctx: AstContext, val `object`: Expression, val property: Expression) : Node(ctx), Expression

class CallExpression (ctx: AstContext, val callee: Expression, val arguments: List<Expression>) : Node(ctx), Expression
