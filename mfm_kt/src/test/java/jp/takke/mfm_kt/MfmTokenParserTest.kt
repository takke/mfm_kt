@file:Suppress("TestFunctionName", "NonAsciiCharacters")

package jp.takke.mfm_kt

import jp.takke.mfm_kt.token_parser.MfmTokenParser
import jp.takke.mfm_kt.token_parser.TokenResult
import jp.takke.mfm_kt.token_parser.TokenType
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class MfmTokenParserTest {

    @Test
    fun tokenize_String() {

        MfmTokenParser.tokenize("")
            .let {
                assertThat(it.success).isEqualTo(true)
                assertThat(it.holder.tokenList).containsExactly()
            }

        MfmTokenParser.tokenize("hoge")
            .let {
                assertThat(it.success).isEqualTo(true)
                assertThat(it.holder.tokenList).containsExactly(
                    TokenResult.string("hoge")
                )
            }

        MfmTokenParser.tokenize("hoge\nfuga")
            .let {
                assertThat(it.success).isEqualTo(true)
                assertThat(it.holder.tokenList).containsExactly(
                    TokenResult.string("hoge\nfuga")
                )
            }
    }

    @Test
    fun tokenize_InlineCode() {

        MfmTokenParser.tokenize("hoge\ntest=>`fuga`")
            .let {
                assertThat(it.success).isEqualTo(true)
                assertThat(it.holder.tokenList).containsExactly(
                    TokenResult.string("hoge\ntest=>"),
                    TokenResult.inlineCode("fuga")
                )
            }
    }

    @Test
    fun tokenize_引用() {

        MfmTokenParser.tokenize(">")
            .let {
                assertThat(it.success).isEqualTo(true)
                assertThat(it.holder.tokenList).containsExactly(
                    TokenResult.string(">")
                )
            }

        MfmTokenParser.tokenize("> ")
            .let {
                assertThat(it.success).isEqualTo(true)
                assertThat(it.holder.tokenList).containsExactly(
                    TokenResult.string("> ")
                )
            }

        // 末尾が改行コードなしの場合はマッチしないので注意
        MfmTokenParser.tokenize("> a")
            .let {
                assertThat(it.success).isEqualTo(true)
                assertThat(it.holder.tokenList).containsExactly(
                    TokenResult.string("> a")
                )
            }

        MfmTokenParser.tokenize("> a\n")
            .let {
                assertThat(it.success).isEqualTo(true)
                assertThat(it.holder.tokenList).containsExactly(
                    TokenResult(TokenType.QuoteLine1, "a\n", "> a\n")
                )
            }

        MfmTokenParser.tokenize(">> a\n")
            .let {
                assertThat(it.success).isEqualTo(true)
                assertThat(it.holder.tokenList).containsExactly(
                    TokenResult(TokenType.QuoteLine2, "a\n", ">> a\n")
                )
            }

        // 連続する引用はマージされること
        MfmTokenParser.tokenize("> a\n> b\n")
            .let {
                assertThat(it.success).isEqualTo(true)
                assertThat(it.holder.tokenList).containsExactly(
                    TokenResult(TokenType.QuoteLine1, "a\nb\n", "> a\n> b\n")
                )
            }

        // その後ろに別のデータがある場合
        MfmTokenParser.tokenize("> a\n> b\nhoge")
            .let {
                assertThat(it.success).isEqualTo(true)
                assertThat(it.holder.tokenList).containsExactly(
                    TokenResult(TokenType.QuoteLine1, "a\nb\n", "> a\n> b\n"),
                    TokenResult.string("hoge")
                )
            }

        MfmTokenParser.tokenize(">> a\n>> b\n")
            .let {
                assertThat(it.success).isEqualTo(true)
                assertThat(it.holder.tokenList).containsExactly(
                    TokenResult(TokenType.QuoteLine2, "a\nb\n", ">> a\n>> b\n")
                )
            }

    }

    @Test
    fun tokenize_BoldItalic() {

        MfmTokenParser.tokenize("*abc*")
            .let {
                assertThat(it.success).isEqualTo(true)
                assertThat(it.holder.tokenList).containsExactly(
                    TokenResult.italic1(),
                    TokenResult.string("abc"),
                    TokenResult.italic1()
                )
            }

        MfmTokenParser.tokenize("**abc**")
            .let {
                assertThat(it.success).isEqualTo(true)
                assertThat(it.holder.tokenList).containsExactly(
                    TokenResult.bold1(),
                    TokenResult.string("abc"),
                    TokenResult.bold1()
                )
            }

        MfmTokenParser.tokenize("**Hello**, *World*!")
            .let {
                assertThat(it.success).isEqualTo(true)
                assertThat(it.holder.tokenList).containsExactly(
                    TokenResult.bold1(),
                    TokenResult.string("Hello"),
                    TokenResult.bold1(),
                    TokenResult.string(", "),
                    TokenResult.italic1(),
                    TokenResult.string("World"),
                    TokenResult.italic1(),
                    TokenResult.string("!")
                )
            }
    }

    @Test
    fun tokenize_CenterBoldItalic() {

        MfmTokenParser.tokenize("<center>abc</center>")
            .let {
                assertThat(it.success).isEqualTo(true)
                assertThat(it.holder.tokenList).containsExactly(
                    TokenResult.centerStart(),
                    TokenResult.string("abc"),
                    TokenResult.centerEnd()
                )
            }

        MfmTokenParser.tokenize("<center>**Hello**, *World*!</center>")
            .let {
                assertThat(it.success).isEqualTo(true)
                assertThat(it.holder.tokenList).containsExactly(
                    TokenResult.centerStart(),
                    TokenResult.bold1(),
                    TokenResult.string("Hello"),
                    TokenResult.bold1(),
                    TokenResult.string(", "),
                    TokenResult.italic1(),
                    TokenResult.string("World"),
                    TokenResult.italic1(),
                    TokenResult.string("!"),
                    TokenResult.centerEnd()
                )
            }
    }

    @Test
    fun tokenize_Small() {

        MfmTokenParser.tokenize("<small>abc</small>")
            .let {
                assertThat(it.success).isEqualTo(true)
                assertThat(it.holder.tokenList).containsExactly(
                    TokenResult.smallStart(),
                    TokenResult.string("abc"),
                    TokenResult.smallEnd()
                )
            }

        MfmTokenParser.tokenize("<small>**Hello**, *World*!</small>")
            .let {
                assertThat(it.success).isEqualTo(true)
                assertThat(it.holder.tokenList).containsExactly(
                    TokenResult.smallStart(),
                    TokenResult.bold1(),
                    TokenResult.string("Hello"),
                    TokenResult.bold1(),
                    TokenResult.string(", "),
                    TokenResult.italic1(),
                    TokenResult.string("World"),
                    TokenResult.italic1(),
                    TokenResult.string("!"),
                    TokenResult.smallEnd()
                )
            }
    }

    @Test
    fun tokenize_Function() {

        MfmTokenParser.tokenize("$[x2 abc]")
            .let {
                assertThat(it.success).isEqualTo(true)
                assertThat(it.holder.tokenList).containsExactly(
                    TokenResult.functionStart("x2"),
                    TokenResult.string("abc"),
                    TokenResult.functionEnd()
                )
            }

        MfmTokenParser.tokenize("$[x2 $[rotate.deg=340 abc]]")
            .let {
                assertThat(it.success).isEqualTo(true)
                assertThat(it.holder.tokenList).containsExactly(
                    TokenResult.functionStart("x2"),
                    TokenResult.functionStart("rotate.deg=340"),
                    TokenResult.string("abc"),
                    TokenResult.functionEnd(),
                    TokenResult.functionEnd()
                )
            }
    }


}