package jp.takke.mfm_kt.syntax_parser

import jp.takke.mfm_kt.token_parser.TokenParseResult
import jp.takke.mfm_kt.token_parser.TokenType


/**
 * 構文解析を行い、構文解析木を返す
 */
class MfmSyntaxParser(tokenizedResult: TokenParseResult, private val option: Option) {

    // 解析対象
    private val tokenList = tokenizedResult.holder.tokenList
    private var tokenPos = 0

    data class Option(
        val enableBold: Boolean = true,
        val enableItalic: Boolean = true,
        val enableCenter: Boolean = true,
        val enableSmall: Boolean = true,
        val enableQuote: Boolean = true,
        val enableFunction: Boolean = true,
    )

    fun parse(): List<MfmNode> {
        return parse(ParseState.Normal).nodes
    }

    private enum class ParseState {
        Normal,
        Center,
        BoldAsta,
        BoldTag,
        BoldUnder,
        Small,
        ItalicTag,
        ItalicAsta,
        Function,
    }

    private data class ParseResult(
        val success: Boolean,
        val nodes: List<MfmNode>,
    )

    private fun parse(state: ParseState): ParseResult {

        val nodes = ArrayList<MfmNode>(tokenList.size)

        while (tokenPos < tokenList.size) {
            val token = tokenList[tokenPos]
            tokenPos++

            if (!isOptionEnabled(token.type)) {
                // オプションで無効化されたタグなのでそのまま出力する
                nodes.addOrMergeText(token.wholeText)
                continue
            }

            // 初期状態
            when (token.type) {
                TokenType.Char,
                TokenType.String -> {
                    nodes.addOrMergeText(token.wholeText)
                }

                TokenType.QuoteLine1 -> {
                    // Quote
                    // TODO 本来はここで extractedValue をさらに字句解析から行う必要がある
                    nodes.add(MfmNode.Quote(MfmNode.QuoteLevel.Level1, listOf(MfmNode.Text(token.extractedValue))))
                }

                TokenType.QuoteLine2 -> {
                    // Quote
                    // TODO 本来はここで extractedValue をさらに字句解析から行う必要がある
                    nodes.add(MfmNode.Quote(MfmNode.QuoteLevel.Level2, listOf(MfmNode.Text(token.extractedValue))))
                }

                TokenType.CenterStart -> {
                    // Center 開始
                    val centerResult = parse(ParseState.Center)
                    if (centerResult.success) {
                        nodes.add(MfmNode.Center(centerResult.nodes))
                    } else {
                        // Center が終了しないまま終端に達した
                        nodes.addOrMergeText(token.wholeText)
                        nodes.addAllWithMergeText(centerResult.nodes)
                    }
                }

                TokenType.CenterEnd -> {
                    // Center 終了
                    if (state == ParseState.Center) {
                        return ParseResult(true, nodes)
                    } else {
                        // <center>じゃないところで</center>が来たので無視する
                        nodes.addOrMergeText(token.wholeText)
                    }
                }

                TokenType.BoldAsta -> {
                    if (state == ParseState.BoldAsta) {
                        // Bold 終了
                        return ParseResult(true, nodes)
                    } else {
                        // Bold 開始
                        val boldResult = parse(ParseState.BoldAsta)
                        if (boldResult.success) {
                            nodes.add(MfmNode.Bold(boldResult.nodes))
                        } else {
                            // Bold が終了しないまま終端に達した
                            nodes.addOrMergeText(token.wholeText)
                            nodes.addAllWithMergeText(boldResult.nodes)
                        }
                    }
                }

                TokenType.BoldTagStart -> {
                    // Bold 開始
                    val boldTagResult = parse(ParseState.BoldTag)
                    if (boldTagResult.success) {
                        nodes.add(MfmNode.Bold(boldTagResult.nodes))
                    } else {
                        // Bold が終了しないまま終端に達した
                        nodes.addOrMergeText(token.wholeText)
                        nodes.addAllWithMergeText(boldTagResult.nodes)
                    }
                }

                TokenType.BoldTagEnd -> {
                    // Bold 終了
                    if (state == ParseState.BoldTag) {
                        return ParseResult(true, nodes)
                    } else {
                        // <bold>じゃないところで</bold>が来たので無視する
                        nodes.addOrMergeText(token.wholeText)
                    }
                }

                TokenType.BoldUnder -> {
                    if (state == ParseState.BoldUnder) {
                        // __ 終了
                        return ParseResult(true, nodes)
                    } else {
                        // __ 開始
                        val boldResult = parse(ParseState.BoldUnder)
                        if (boldResult.success) {
                            // __ の間は[a-zA-Z0-9_ ]のみ許可
                            // https://github.com/misskey-dev/mfm.js/blob/develop/src/internal/parser.ts#L354
                            if (boldResult.nodes.size == 1 &&
                                (boldResult.nodes[0] as? MfmNode.Text)?.value?.matches(Regex("^[a-zA-Z0-9 ]+")) == true
                            ) {
                                nodes.add(MfmNode.Bold(boldResult.nodes))
                            } else {
                                // __ の間に対象外の文字がある場合は無視する
                                nodes.addOrMergeText("__")
                                nodes.addAllWithMergeText(boldResult.nodes)
                                nodes.addOrMergeText("__")
                            }
                        } else {
                            // __ が終了しないまま終端に達した
                            nodes.addOrMergeText(token.wholeText)
                            nodes.addAllWithMergeText(boldResult.nodes)
                        }
                    }
                }

                TokenType.SmallStart -> {
                    // Small 開始
                    val smallResult = parse(ParseState.Small)
                    if (smallResult.success) {
                        nodes.add(MfmNode.Small(smallResult.nodes))
                    } else {
                        // Small が終了しないまま終端に達した
                        nodes.addOrMergeText(token.wholeText)
                        nodes.addAllWithMergeText(smallResult.nodes)
                    }
                }

                TokenType.SmallEnd -> {
                    // Small 終了
                    if (state == ParseState.Small) {
                        return ParseResult(true, nodes)
                    } else {
                        // <small>じゃないところで</small>が来たので無視する
                        nodes.addOrMergeText(token.wholeText)
                    }
                }

                TokenType.ItalicTagStart -> {
                    // Italic 開始
                    val italicResult = parse(ParseState.ItalicTag)
                    if (italicResult.success) {
                        nodes.add(MfmNode.Italic(italicResult.nodes))
                    } else {
                        // Italic が終了しないまま終端に達した
                        nodes.addOrMergeText(token.wholeText)
                        nodes.addAllWithMergeText(italicResult.nodes)
                    }
                }

                TokenType.ItalicTagEnd -> {
                    // Italic 終了
                    if (state == ParseState.ItalicTag) {
                        return ParseResult(true, nodes)
                    } else {
                        // <i>じゃないところで</i>が来たので無視する
                        nodes.addOrMergeText(token.wholeText)
                    }
                }

                TokenType.ItalicAsta -> {
                    if (state == ParseState.ItalicAsta) {
                        // Italic 終了
                        return ParseResult(true, nodes)
                    } else {
                        // Italic 開始
                        val italicResult = parse(ParseState.ItalicAsta)

                        if (italicResult.success) {
                            // * の間は[a-zA-Z0-9_]のみ許可
                            // https://github.com/misskey-dev/mfm.js/blob/develop/src/internal/parser.ts#L354
                            if (italicResult.nodes.size == 1 &&
                                (italicResult.nodes[0] as? MfmNode.Text)?.value?.matches(Regex("^[a-zA-Z0-9]+")) == true
                            ) {
                                nodes.add(MfmNode.Italic(italicResult.nodes))
                            } else {
                                // * の間に対象外の文字がある場合は無視する
                                nodes.addOrMergeText("*")
                                nodes.addAllWithMergeText(italicResult.nodes)
                                nodes.addOrMergeText("*")
                            }
                        } else {
                            // Italic が終了しないまま終端に達した
                            nodes.addOrMergeText(token.wholeText)
                            nodes.addAllWithMergeText(italicResult.nodes)
                        }
                    }
                }

                TokenType.FunctionStart -> {
                    // Function 開始
                    val functionResult = parse(ParseState.Function)
                    if (functionResult.success) {
                        nodes.add(MfmNode.Function(token.extractedValue, functionResult.nodes))
                    } else {
                        // Function が終了しないまま終端に達した
                        nodes.addOrMergeText(token.wholeText)
                        nodes.addAllWithMergeText(functionResult.nodes)
                    }
                }

                TokenType.FunctionEnd -> {
                    // Function 終了
                    if (state == ParseState.Function) {
                        return ParseResult(true, nodes)
                    } else {
                        // "$[" じゃないところで "]" が来たので無視する
                        nodes.addOrMergeText(token.wholeText)
                    }
                }

                TokenType.InlineCode -> {
                    nodes.addOrMergeText(token.wholeText)
                }
            }
        }

        // Normal 以外で終端に達したらエラー
        return ParseResult(state == ParseState.Normal, nodes)
    }

    private fun isOptionEnabled(type: TokenType): Boolean {
        return when (type) {
            TokenType.Char -> true
            TokenType.String -> true
            TokenType.QuoteLine1 -> option.enableQuote
            TokenType.QuoteLine2 -> option.enableQuote
            TokenType.CenterStart -> option.enableCenter
            TokenType.CenterEnd -> option.enableCenter
            TokenType.BoldAsta -> option.enableBold
            TokenType.BoldTagStart -> option.enableBold
            TokenType.BoldTagEnd -> option.enableBold
            TokenType.BoldUnder -> option.enableBold
            TokenType.SmallStart -> option.enableSmall
            TokenType.SmallEnd -> option.enableSmall
            TokenType.ItalicTagStart -> option.enableItalic
            TokenType.ItalicTagEnd -> option.enableItalic
            TokenType.ItalicAsta -> option.enableItalic
            TokenType.FunctionStart -> option.enableFunction
            TokenType.FunctionEnd -> option.enableFunction
            TokenType.InlineCode -> true
        }
    }

    private fun ArrayList<MfmNode>.addOrMergeText(s: String) {
        // 末尾が Text なら結合する
        if (this.isNotEmpty() && this.last() is MfmNode.Text) {
            val text = this.last() as MfmNode.Text
            this[this.size - 1] = MfmNode.Text(text.value + s)
        } else {
            this.add(MfmNode.Text(s))
        }
    }

    private fun ArrayList<MfmNode>.addAllWithMergeText(nodes: List<MfmNode>) {
        for (node in nodes) {
            if (node is MfmNode.Text) {
                this.addOrMergeText(node.value)
            } else {
                this.add(node)
            }
        }
    }

}

