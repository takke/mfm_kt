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
        Function,
        Center,
        Small,
    }

    private var tokenPos = 0

    fun parse(): List<MfmNode> {

        return parse(ParseState.Normal).nodes
    }

    data class ParseResult(
        val success: Boolean,
        val nodes: List<MfmNode>,
    )

    private fun parse(state: ParseState): ParseResult {

        val nodes = ArrayList<MfmNode>(tokenList.size)

        while (tokenPos < tokenList.size) {
            val token = tokenList[tokenPos]
            tokenPos++

            // 初期状態
            when (token.type) {
                TokenType.Bold1 -> {
                    if (option.enableBold) {
                        if (state == ParseState.Bold) {
                            // Bold 終了
                            return ParseResult(true, nodes)
                        } else {
                            // Bold 開始
                            val boldResult = parse(ParseState.Bold)
                            if (boldResult.success) {
                                nodes.add(MfmNode.Bold(boldResult.nodes))
                            } else {
                                // Bold が終了しないまま終端に達した
                                nodes.add(MfmNode.Text(token.wholeText))
                                nodes.addAll(boldResult.nodes)
                            }
                        }
                    } else {
                        // Bold 無効
                        nodes.add(MfmNode.Text(token.wholeText))
                    }
                }

                TokenType.Italic1 -> {
                    if (option.enableItalic) {
                        if (state == ParseState.ItalicAsterisk) {
                            // Italic 終了
                            return ParseResult(true, nodes)
                        } else {
                            // Italic 開始
                            val italicResult = parse(ParseState.ItalicAsterisk)
                            if (italicResult.success) {
                                nodes.add(MfmNode.Italic(italicResult.nodes))
                            } else {
                                // Italic が終了しないまま終端に達した
                                nodes.add(MfmNode.Text(token.wholeText))
                                nodes.addAll(italicResult.nodes)
                            }
                        }
                    } else {
                        // Italic 無効
                        nodes.add(MfmNode.Text(token.wholeText))
                    }
                }

                TokenType.ItalicTagStart -> {
                    if (option.enableItalic) {
                        // Italic 開始
                        val italicResult = parse(ParseState.ItalicTag)
                        if (italicResult.success) {
                            nodes.add(MfmNode.Italic(italicResult.nodes))
                        } else {
                            // Italic が終了しないまま終端に達した
                            nodes.add(MfmNode.Text(token.wholeText))
                            nodes.addAll(italicResult.nodes)
                        }
                    } else {
                        // Italic 無効
                        nodes.add(MfmNode.Text(token.wholeText))
                    }
                }

                TokenType.ItalicTagEnd -> {
                    if (option.enableItalic) {
                        // Italic 終了
                        if (state == ParseState.ItalicTag) {
                            return ParseResult(true, nodes)
                        } else {
                            // <i>じゃないところで</i>が来たので無視する
                            nodes.add(MfmNode.Text(token.wholeText))
                        }
                    } else {
                        // Italic 無効
                        nodes.add(MfmNode.Text(token.wholeText))
                    }
                }

                TokenType.CenterStart -> {
                    if (option.enableCenter) {
                        // Center 開始
                        val centerResult = parse(ParseState.Center)
                        if (centerResult.success) {
                            nodes.add(MfmNode.Center(centerResult.nodes))
                        } else {
                            // Center が終了しないまま終端に達した
                            nodes.add(MfmNode.Text(token.wholeText))
                            nodes.addAll(centerResult.nodes)
                        }
                    } else {
                        // Center 無効
                        nodes.add(MfmNode.Text(token.wholeText))
                    }
                }

                TokenType.CenterEnd -> {
                    if (option.enableCenter) {
                        // Center 終了
                        if (state == ParseState.Center) {
                            return ParseResult(true, nodes)
                        } else {
                            // <center>じゃないところで</center>が来たので無視する
                            nodes.add(MfmNode.Text(token.wholeText))
                        }
                    } else {
                        // Center 無効
                        nodes.add(MfmNode.Text(token.wholeText))
                    }
                }

                TokenType.SmallStart -> {
                    if (option.enableSmall) {
                        // Small 開始
                        val smallResult = parse(ParseState.Small)
                        if (smallResult.success) {
                            nodes.add(MfmNode.Small(smallResult.nodes))
                        } else {
                            // Small が終了しないまま終端に達した
                            nodes.add(MfmNode.Text(token.wholeText))
                            nodes.addAll(smallResult.nodes)
                        }
                    } else {
                        // Small 無効
                        nodes.add(MfmNode.Text(token.wholeText))
                    }
                }

                TokenType.SmallEnd -> {
                    if (option.enableSmall) {
                        // Small 終了
                        if (state == ParseState.Small) {
                            return ParseResult(true, nodes)
                        } else {
                            // <small>じゃないところで</small>が来たので無視する
                            nodes.add(MfmNode.Text(token.wholeText))
                        }
                    } else {
                        // Small 無効
                        nodes.add(MfmNode.Text(token.wholeText))
                    }
                }

                TokenType.FunctionStart -> {
                    if (option.enableFunction) {
                        // Function 開始
                        val functionResult = parse(ParseState.Function)
                        if (functionResult.success) {
                            nodes.add(MfmNode.Function(token.extractedValue, functionResult.nodes))
                        } else {
                            // Function が終了しないまま終端に達した
                            nodes.add(MfmNode.Text(token.wholeText))
                            nodes.addAll(functionResult.nodes)
                        }
                    } else {
                        // Function 無効
                        nodes.add(MfmNode.Text(token.wholeText))
                    }
                }

                TokenType.FunctionEnd -> {
                    if (option.enableFunction) {
                        // Function 終了
                        if (state == ParseState.Function) {
                            return ParseResult(true, nodes)
                        } else {
                            // "$[" じゃないところで "]" が来たので無視する
                            nodes.add(MfmNode.Text(token.wholeText))
                        }
                    } else {
                        // Function 無効
                        nodes.add(MfmNode.Text(token.wholeText))
                    }
                }

                TokenType.QuoteLine1 -> {
                    if (option.enableQuote) {
                        // Quote
                        // TODO 本来はここで extractedValue をさらに字句解析から行う必要がある
                        nodes.add(MfmNode.Quote(MfmNode.QuoteLevel.Level1, listOf(MfmNode.Text(token.extractedValue))))
                    } else {
                        // Quote 無効
                        nodes.add(MfmNode.Text(token.wholeText))
                    }
                }

                TokenType.QuoteLine2 -> {
                    if (option.enableQuote) {
                        // Quote
                        // TODO 本来はここで extractedValue をさらに字句解析から行う必要がある
                        nodes.add(MfmNode.Quote(MfmNode.QuoteLevel.Level2, listOf(MfmNode.Text(token.extractedValue))))
                    } else {
                        // Quote 無効
                        nodes.add(MfmNode.Text(token.wholeText))
                    }
                }

                else -> {
                    nodes.add(MfmNode.Text(token.wholeText))
                }
            }
        }

        // Normal 以外で終端に達したらエラー
        return ParseResult(state == ParseState.Normal, nodes)
    }

}
