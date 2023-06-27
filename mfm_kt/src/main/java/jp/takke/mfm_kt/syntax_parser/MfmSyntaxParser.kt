package jp.takke.mfm_kt.syntax_parser

import jp.takke.mfm_kt.token_parser.TokenParseResult
import jp.takke.mfm_kt.token_parser.TokenType


/**
 * 構文解析を行い、構文解析木を返す
 */
class MfmSyntaxParser(tokenizedResult: TokenParseResult, private val option: Option) {

    private val tokenList = tokenizedResult.holder.tokenList

    data class Option(
        val enableBold: Boolean = true,
        val enableItalic: Boolean = true,
        val enableCenter: Boolean = true,
        val enableSmall: Boolean = true,
        val enableQuote: Boolean = true,
        val enableFunction: Boolean = true,
    )

    enum class ParseState {
        Normal,
        Bold,
        ItalicAsterisk,
        ItalicTag,
//        InlineCode,
//        Quote,
        Function,
        Center,
        Small,
    }

    private var tokenPos = 0

    fun parse(): List<SyntaxParseResult> {

        return parse(ParseState.Normal)
    }

    private fun parse(state: ParseState): List<SyntaxParseResult> {

        val result = ArrayList<SyntaxParseResult>(tokenList.size)

        while (tokenPos < tokenList.size) {
            val token = tokenList[tokenPos]
            tokenPos++

            // 初期状態
            when (token.type) {
                TokenType.Bold1 -> {
                    if (option.enableBold) {
                        if (state == ParseState.Bold) {
                            // Bold 終了
                            return result
                        } else {
                            // Bold 開始
                            val boldResult = parse(ParseState.Bold)
                            result.add(SyntaxParseResult.Bold(boldResult))
                        }
                    } else {
                        // Bold 無効
                        result.add(SyntaxParseResult.Text(token.wholeText))
                    }
                }

                TokenType.Italic1 -> {
                    if (option.enableItalic) {
                        if (state == ParseState.ItalicAsterisk) {
                            // Italic 終了
                            return result
                        } else {
                            // Italic 開始
                            val italicResult = parse(ParseState.ItalicAsterisk)
                            result.add(SyntaxParseResult.Italic(italicResult))
                        }
                    } else {
                        // Italic 無効
                        result.add(SyntaxParseResult.Text(token.wholeText))
                    }
                }

                TokenType.ItalicTagStart -> {
                    if (option.enableItalic) {
                        // Italic 開始
                        val italicResult = parse(ParseState.ItalicTag)
                        result.add(SyntaxParseResult.Italic(italicResult))
                    } else {
                        // Italic 無効
                        result.add(SyntaxParseResult.Text(token.wholeText))
                    }
                }

                TokenType.ItalicTagEnd -> {
                    if (option.enableItalic) {
                        // Italic 終了
                        if (state == ParseState.ItalicTag) {
                            return result
                        } else {
                            // <i>じゃないところで</i>が来たので無視する
                            result.add(SyntaxParseResult.Text(token.wholeText))
                        }
                    } else {
                        // Italic 無効
                        result.add(SyntaxParseResult.Text(token.wholeText))
                    }
                }

                TokenType.CenterStart -> {
                    if (option.enableCenter) {
                        // Center 開始
                        val centerResult = parse(ParseState.Center)
                        result.add(SyntaxParseResult.Center(centerResult))
                    } else {
                        // Center 無効
                        result.add(SyntaxParseResult.Text(token.wholeText))
                    }
                }

                TokenType.CenterEnd -> {
                    if (option.enableCenter) {
                        // Center 終了
                        if (state == ParseState.Center) {
                            return result
                        } else {
                            // <center>じゃないところで</center>が来たので無視する
                            result.add(SyntaxParseResult.Text(token.wholeText))
                        }
                    } else {
                        // Center 無効
                        result.add(SyntaxParseResult.Text(token.wholeText))
                    }
                }

                TokenType.SmallStart -> {
                    if (option.enableSmall) {
                        // Small 開始
                        val smallResult = parse(ParseState.Small)
                        result.add(SyntaxParseResult.Small(smallResult))
                    } else {
                        // Small 無効
                        result.add(SyntaxParseResult.Text(token.wholeText))
                    }
                }

                TokenType.SmallEnd -> {
                    if (option.enableSmall) {
                        // Small 終了
                        if (state == ParseState.Small) {
                            return result
                        } else {
                            // <small>じゃないところで</small>が来たので無視する
                            result.add(SyntaxParseResult.Text(token.wholeText))
                        }
                    } else {
                        // Small 無効
                        result.add(SyntaxParseResult.Text(token.wholeText))
                    }
                }

                TokenType.FunctionStart -> {
                    if (option.enableFunction) {
                        // Function 開始
                        val functionResult = parse(ParseState.Function)
                        result.add(SyntaxParseResult.Function(token.extractedValue, functionResult))
                    } else {
                        // Function 無効
                        result.add(SyntaxParseResult.Text(token.wholeText))
                    }
                }

                TokenType.FunctionEnd -> {
                    if (option.enableFunction) {
                        // Function 終了
                        if (state == ParseState.Function) {
                            return result
                        } else {
                            // "$[" じゃないところで "]" が来たので無視する
                            result.add(SyntaxParseResult.Text(token.wholeText))
                        }
                    } else {
                        // Function 無効
                        result.add(SyntaxParseResult.Text(token.wholeText))
                    }
                }

                TokenType.QuoteLine1 -> {
                    if (option.enableQuote) {
                        // Quote
                        // TODO 本来はここで extractedValue をさらに字句解析から行う必要がある
                        result.add(SyntaxParseResult.Quote(SyntaxParseResult.QuoteLevel.Level1, listOf(SyntaxParseResult.Text(token.extractedValue))))
                    } else {
                        // Quote 無効
                        result.add(SyntaxParseResult.Text(token.wholeText))
                    }
                }

                TokenType.QuoteLine2 -> {
                    if (option.enableQuote) {
                        // Quote
                        // TODO 本来はここで extractedValue をさらに字句解析から行う必要がある
                        result.add(SyntaxParseResult.Quote(SyntaxParseResult.QuoteLevel.Level2, listOf(SyntaxParseResult.Text(token.extractedValue))))
                    } else {
                        // Quote 無効
                        result.add(SyntaxParseResult.Text(token.wholeText))
                    }
                }

                else -> {
                    result.add(SyntaxParseResult.Text(token.wholeText))
                }
            }
        }

        return result
    }

}
