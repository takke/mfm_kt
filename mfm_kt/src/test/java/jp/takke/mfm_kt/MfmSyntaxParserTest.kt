@file:Suppress("TestFunctionName", "NonAsciiCharacters")

package jp.takke.mfm_kt

import jp.takke.mfm_kt.syntax_parser.MfmSyntaxParser
import jp.takke.mfm_kt.syntax_parser.MfmNode
import jp.takke.mfm_kt.token_parser.MfmTokenParser
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class MfmSyntaxParserTest {

    @Test
    fun parse_simple() {

        MfmSyntaxParser(MfmTokenParser.tokenize(""), MfmSyntaxParser.Option())
            .parse()
            .let {
                assertThat(it).containsExactly()
            }

        MfmSyntaxParser(MfmTokenParser.tokenize("hoge"), MfmSyntaxParser.Option())
            .parse()
            .let {
                assertThat(it).containsExactly(
                    MfmNode.Text("hoge")
                )
            }
    }

    @Test
    fun parse_option_all_off() {

        // オプションを全てオフにするとただのテキストになること
        val option = MfmSyntaxParser.Option(
            enableBold = false,
            enableItalic = false,
            enableCenter = false,
            enableSmall = false,
            enableQuote = false,
            enableFunction = false
        )

        checkSyntaxParser(
            "1 word",
            "hoge",
            option,
            listOf(
                MfmNode.Text("hoge"),
            )
        )

        checkSyntaxParser(
            "bold",
            "hoge**bold**",
            option,
            listOf(
                MfmNode.Text("hoge"),
                MfmNode.Text("**"),
                MfmNode.Text("bold"),
                MfmNode.Text("**"),
            )
        )

        checkSyntaxParser(
            "bold+italic",
            "hoge**bold**and*italic*",
            option,
            listOf(
                MfmNode.Text("hoge"),
                MfmNode.Text("**"),
                MfmNode.Text("bold"),
                MfmNode.Text("**"),
                MfmNode.Text("and"),
                MfmNode.Text("*"),
                MfmNode.Text("italic"),
                MfmNode.Text("*"),
            )
        )

        checkSyntaxParser(
            "bold+italic+center",
            "<center>hoge**bold**and*italic*</center>",
            option,
            listOf(
                MfmNode.Text("<center>"),
                MfmNode.Text("hoge"),
                MfmNode.Text("**"),
                MfmNode.Text("bold"),
                MfmNode.Text("**"),
                MfmNode.Text("and"),
                MfmNode.Text("*"),
                MfmNode.Text("italic"),
                MfmNode.Text("*"),
                MfmNode.Text("</center>"),
            )
        )

        checkSyntaxParser(
            "bold+italic+center+small",
            "<center>hoge**bold**and*italic*</center><small>ちいさい</small>",
            option,
            listOf(
                MfmNode.Text("<center>"),
                MfmNode.Text("hoge"),
                MfmNode.Text("**"),
                MfmNode.Text("bold"),
                MfmNode.Text("**"),
                MfmNode.Text("and"),
                MfmNode.Text("*"),
                MfmNode.Text("italic"),
                MfmNode.Text("*"),
                MfmNode.Text("</center>"),
                MfmNode.Text("<small>"),
                MfmNode.Text("ちいさい"),
                MfmNode.Text("</small>"),
            )
        )

        checkSyntaxParser(
            "bold+italic+center+small+quote",
            "<center>hoge**bold**and*italic*</center><small>ちいさい</small>\n>a\n>>b\n",
            option,
            listOf(
                MfmNode.Text("<center>"),
                MfmNode.Text("hoge"),
                MfmNode.Text("**"),
                MfmNode.Text("bold"),
                MfmNode.Text("**"),
                MfmNode.Text("and"),
                MfmNode.Text("*"),
                MfmNode.Text("italic"),
                MfmNode.Text("*"),
                MfmNode.Text("</center>"),
                MfmNode.Text("<small>"),
                MfmNode.Text("ちいさい"),
                MfmNode.Text("</small>"),
                MfmNode.Text("\n"),
                MfmNode.Text(">a\n"),
                MfmNode.Text(">>b\n"),
            )
        )

        checkSyntaxParser(
            "bold+italic+center+small+quote+fn",
            "<center>hoge**bold**$[x2 and]*italic*</center><small>ちいさい</small>\n>a\n>>b\n",
            option,
            listOf(
                MfmNode.Text("<center>"),
                MfmNode.Text("hoge"),
                MfmNode.Text("**"),
                MfmNode.Text("bold"),
                MfmNode.Text("**"),
                MfmNode.Text("$[x2 "),
                MfmNode.Text("and"),
                MfmNode.Text("]"),
                MfmNode.Text("*"),
                MfmNode.Text("italic"),
                MfmNode.Text("*"),
                MfmNode.Text("</center>"),
                MfmNode.Text("<small>"),
                MfmNode.Text("ちいさい"),
                MfmNode.Text("</small>"),
                MfmNode.Text("\n"),
                MfmNode.Text(">a\n"),
                MfmNode.Text(">>b\n"),
            )
        )

    }

    @Test
    fun parse_bold() {

        val option = MfmSyntaxParser.Option(
            enableBold = true,
            enableItalic = false,
            enableCenter = false,
            enableSmall = false,
            enableQuote = false,
            enableFunction = false
        )

        checkSyntaxParser(
            "1 word",
            "hoge",
            option,
            listOf(
                MfmNode.Text("hoge"),
            )
        )

        checkSyntaxParser(
            "bold1",
            "**hoge**",
            option,
            listOf(
                MfmNode.Bold(
                    listOf(MfmNode.Text("hoge"))
                ),
            )
        )

        checkSyntaxParser(
            "bold2",
            "aaa**hoge**bbb",
            option,
            listOf(
                MfmNode.Text("aaa"),
                MfmNode.Bold(
                    listOf(MfmNode.Text("hoge"))
                ),
                MfmNode.Text("bbb"),
            )
        )
    }

    @Test
    fun parse_bold_閉じず() {

        checkSyntaxParser(
            "bold 閉じず",
            "aaa**hoge",
            optionAll,
            listOf(
                MfmNode.Text("aaa"),
                MfmNode.Text("**"),
                MfmNode.Text("hoge"),
            )
        )
    }

    @Test
    fun parse_italic() {

        val option = MfmSyntaxParser.Option(
            enableBold = true,
            enableItalic = true,
            enableCenter = false,
            enableSmall = false,
            enableQuote = false,
            enableFunction = false
        )

        // *...* と <i>...</i> の2パターンある

        checkSyntaxParser(
            "italic*",
            "*hoge*",
            option,
            listOf(
                MfmNode.Italic(
                    listOf(MfmNode.Text("hoge"))
                ),
            )
        )

        checkSyntaxParser(
            "italic tag",
            "<i>hoge</i>",
            option,
            listOf(
                MfmNode.Italic(
                    listOf(MfmNode.Text("hoge"))
                ),
            )
        )

        checkSyntaxParser(
            "italic + bold",
            "<i>**hoge**</i>",
            option,
            listOf(
                MfmNode.Italic(
                    listOf(
                        MfmNode.Bold(
                            listOf(MfmNode.Text("hoge"))
                        )
                    )
                ),
            )
        )
    }

    @Test
    fun parse_italic_閉じず() {

        checkSyntaxParser(
            "italic* 閉じず",
            "*hoge",
            optionAll,
            listOf(
                MfmNode.Text("*"),
                MfmNode.Text("hoge"),
            )
        )

        checkSyntaxParser(
            "italic tag 閉じず",
            "<i>hoge",
            optionAll,
            listOf(
                MfmNode.Text("<i>"),
                MfmNode.Text("hoge"),
            )
        )

        checkSyntaxParser(
            "italic + bold 閉じず",
            "<i>**hoge**",
            optionAll,
            listOf(
                MfmNode.Text("<i>"),
                MfmNode.Bold(
                    listOf(MfmNode.Text("hoge"))
                )
            )
        )
    }

    @Test
    fun parse_center() {

        val option = MfmSyntaxParser.Option(
            enableBold = true,
            enableItalic = true,
            enableCenter = true,
            enableSmall = false,
            enableQuote = false,
            enableFunction = false
        )

        // <center>...</center>

        checkSyntaxParser(
            "center",
            "<center>hoge</center>",
            option,
            listOf(
                MfmNode.Center(
                    listOf(MfmNode.Text("hoge"))
                ),
            )
        )

        checkSyntaxParser(
            "center 複数行1",
            "a\n<center>hoge</center>\nb",
            option,
            listOf(
                MfmNode.Text("a\n"),
                MfmNode.Center(
                    listOf(MfmNode.Text("hoge"))
                ),
                MfmNode.Text("\nb"),
            )
        )

        checkSyntaxParser(
            "center 複数行2",
            "a\n<center>\nhoge1\nhoge2\n</center>\nb",
            option,
            listOf(
                MfmNode.Text("a\n"),
                MfmNode.Center(
                    listOf(
                        MfmNode.Text("hoge1\nhoge2")
                    )
                ),
                MfmNode.Text("\nb"),
            )
        )

        checkSyntaxParser(
            "center 複数行+bold",
            "a\n<center>\n**hoge1**\nhoge2\n</center>\nb",
            option,
            listOf(
                MfmNode.Text("a\n"),
                MfmNode.Center(
                    listOf(
                        MfmNode.Bold(
                            listOf(MfmNode.Text("hoge1"))
                        ),
                        MfmNode.Text("\nhoge2")
                    )
                ),
                MfmNode.Text("\nb"),
            )
        )
    }

    @Test
    fun parse_center_閉じず() {

        val option = MfmSyntaxParser.Option(
            enableBold = true,
            enableItalic = true,
            enableCenter = true,
            enableSmall = false,
            enableQuote = false,
            enableFunction = false
        )

        checkSyntaxParser(
            "center",
            "<center>hoge",
            option,
            listOf(
                MfmNode.Text("<center>"),
                MfmNode.Text("hoge"),
            )
        )

        checkSyntaxParser(
            "center 複数行1",
            "a\n<center>hoge\nb",
            option,
            listOf(
                MfmNode.Text("a\n"),
                MfmNode.Text("<center>"),
                MfmNode.Text("hoge\nb"),
            )
        )

        checkSyntaxParser(
            "center 複数行+bold",
            "a\n<center>\n**hoge1**\nhoge2\n\nb",
            option,
            listOf(
                MfmNode.Text("a\n"),
                MfmNode.Text("<center>\n"),
                MfmNode.Bold(
                    listOf(MfmNode.Text("hoge1"))
                ),
                MfmNode.Text("\nhoge2\n\nb"),
            )
        )
    }

    @Test
    fun parse_small() {

        val option = MfmSyntaxParser.Option(
            enableBold = true,
            enableItalic = true,
            enableCenter = true,
            enableSmall = true,
            enableQuote = false,
            enableFunction = false
        )

        // <small>...</small>

        checkSyntaxParser(
            "small",
            "<small>hoge</small>",
            option,
            listOf(
                MfmNode.Small(
                    listOf(MfmNode.Text("hoge"))
                ),
            )
        )

        checkSyntaxParser(
            "small 複数行1",
            "a\n<small>hoge</small>\nb",
            option,
            listOf(
                MfmNode.Text("a\n"),
                MfmNode.Small(
                    listOf(MfmNode.Text("hoge"))
                ),
                MfmNode.Text("\nb"),
            )
        )

        checkSyntaxParser(
            "small 複数行2",
            "a\n<small>\nhoge1\nhoge2\n</small>\nb",
            option,
            listOf(
                MfmNode.Text("a\n"),
                MfmNode.Small(
                    listOf(
                        MfmNode.Text("\nhoge1\nhoge2\n")
                    )
                ),
                MfmNode.Text("\nb"),
            )
        )

        checkSyntaxParser(
            "small 複数行+bold",
            "a\n<small>\n**hoge1**\nhoge2\n</small>\nb",
            option,
            listOf(
                MfmNode.Text("a\n"),
                MfmNode.Small(
                    listOf(

                        MfmNode.Text("\n"),
                        MfmNode.Bold(
                            listOf(MfmNode.Text("hoge1"))
                        ),
                        MfmNode.Text("\nhoge2\n")
                    )
                ),
                MfmNode.Text("\nb"),
            )
        )
    }

    @Test
    fun parse_small_閉じず() {

        val option = MfmSyntaxParser.Option(
            enableBold = true,
            enableItalic = true,
            enableCenter = true,
            enableSmall = false,
            enableQuote = false,
            enableFunction = false
        )

        checkSyntaxParser(
            "small",
            "<small>hoge",
            option,
            listOf(
                MfmNode.Text("<small>"),
                MfmNode.Text("hoge"),
            )
        )

        checkSyntaxParser(
            "small 複数行1",
            "a\n<small>hoge\nb",
            option,
            listOf(
                MfmNode.Text("a\n"),
                MfmNode.Text("<small>"),
                MfmNode.Text("hoge\nb"),
            )
        )

        checkSyntaxParser(
            "small 複数行+bold",
            "a\n<small>\n**hoge1**\nhoge2\n\nb",
            option,
            listOf(
                MfmNode.Text("a\n"),
                MfmNode.Text("<small>"),
                MfmNode.Text("\n"),
                MfmNode.Bold(
                    listOf(MfmNode.Text("hoge1"))
                ),
                MfmNode.Text("\nhoge2\n\nb"),
            )
        )
    }

    @Test
    fun parse_quote() {

        val option = MfmSyntaxParser.Option(
            enableBold = true,
            enableItalic = true,
            enableCenter = true,
            enableSmall = true,
            enableQuote = true,
            enableFunction = false
        )

        // >aaa
        // > aaa
        // >bbb
        // >>a
        // >> b

        checkSyntaxParser(
            "quote1 1行(改行なし)",
            ">aaa",
            option,
            listOf(
                MfmNode.Text(">aaa"),
            )
        )

        checkSyntaxParser(
            "quote1 1行",
            ">aaa\n",
            option,
            listOf(
                MfmNode.Quote(
                    MfmNode.QuoteLevel.Level1,
                    listOf(MfmNode.Text("aaa\n"))
                ),
            )
        )

        checkSyntaxParser(
            "quote1 2行 => 結合されること",
            ">aaa\n>bbb\n",
            option,
            listOf(
                MfmNode.Quote(
                    MfmNode.QuoteLevel.Level1,
                    listOf(MfmNode.Text("aaa\nbbb\n"))
                ),
            )
        )

        checkSyntaxParser(
            "quote1 2行 => 行頭のスペースは無視されること",
            ">aaa\n> bbb\n",
            option,
            listOf(
                MfmNode.Quote(
                    MfmNode.QuoteLevel.Level1,
                    listOf(MfmNode.Text("aaa\nbbb\n"))
                ),
            )
        )

        checkSyntaxParser(
            "quote2 2行 => 行頭のスペースは無視されること",
            ">>aaa\n>> bbb\n",
            option,
            listOf(
                MfmNode.Quote(
                    MfmNode.QuoteLevel.Level2,
                    listOf(MfmNode.Text("aaa\nbbb\n"))
                ),
            )
        )

        checkSyntaxParser(
            "quote1+2 2行 => 行頭のスペースは無視されること",
            ">>aaa\n>> bbb\n>c\n",
            option,
            listOf(
                MfmNode.Quote(
                    MfmNode.QuoteLevel.Level2,
                    listOf(MfmNode.Text("aaa\nbbb\n"))
                ),
                MfmNode.Quote(
                    MfmNode.QuoteLevel.Level1,
                    listOf(MfmNode.Text("c\n"))
                ),
            )
        )
    }

    private val optionAll = MfmSyntaxParser.Option()

    @Test
    fun parse_function() {

        checkSyntaxParser(
            "Function x2",
            "$[x2 hoge]",
            optionAll,
            listOf(
                MfmNode.Function(
                    "x2",
                    listOf(MfmNode.Text("hoge"))
                ),
            )
        )
        checkSyntaxParser(
            "Function x3",
            "$[x3 hoge]",
            optionAll,
            listOf(
                MfmNode.Function(
                    "x3",
                    listOf(MfmNode.Text("hoge"))
                ),
            )
        )
        checkSyntaxParser(
            "Function x4",
            "$[x4 hoge]",
            optionAll,
            listOf(
                MfmNode.Function(
                    "x4",
                    listOf(MfmNode.Text("hoge"))
                ),
            )
        )

        // 実際にはなくてもパースはできること
        checkSyntaxParser(
            "Function x1",
            "$[x1 hoge]",
            optionAll,
            listOf(
                MfmNode.Function(
                    "x1",
                    listOf(MfmNode.Text("hoge"))
                ),
            )
        )
        checkSyntaxParser(
            "Function x5",
            "$[x5 hoge]",
            optionAll,
            listOf(
                MfmNode.Function(
                    "x5",
                    listOf(MfmNode.Text("hoge"))
                ),
            )
        )

        checkSyntaxParser(
            "Function + Bold",
            "$[x4 **hoge**]",
            optionAll,
            listOf(
                MfmNode.Function(
                    "x4",
                    listOf(
                        MfmNode.Bold(
                            listOf(MfmNode.Text("hoge"))
                        )
                    )
                ),
            )
        )
    }

    //--------------------------------------------------
    // custom checker
    //--------------------------------------------------

    private fun checkSyntaxParser(scenarioName: String, inputText: String, option: MfmSyntaxParser.Option, expected: List<MfmNode>) {

        val result = MfmSyntaxParser(MfmTokenParser.tokenize(inputText), option).parse()

        println("---- [$scenarioName] start")
        println("input: [$inputText]")
        println("actual:")
        dump(result)
        println("expected:")
        dump(expected)
        println("---- [$scenarioName] end")

        assertThat(result).containsExactlyElementsOf(expected)
    }

    private fun dump(it: List<MfmNode>) {
        traverse(it, 1)
    }

    private fun traverse(parsedResult: List<MfmNode>, level: Int) {

        parsedResult.forEach { spr ->

            print("   ".repeat(level))
            when (spr) {
                is MfmNode.Text -> {
                    println("Text: \"${spr.value.replace("\n", "\\n")}\"")
                }
                is MfmNode.Bold -> {
                    println("Bold: ")
                    traverse(spr.children, level + 1)
                }
                is MfmNode.Italic -> {
                    println("Italic: ")
                    traverse(spr.children, level + 1)
                }
                is MfmNode.Center -> {
                    println("Center: ")
                    traverse(spr.children, level + 1)
                }
                is MfmNode.Small -> {
                    println("Small: ")
                    traverse(spr.children, level + 1)
                }
                is MfmNode.Function -> {
                    println("Function: ${spr.name} ${spr.args.joinToString(", ")}")
                    traverse(spr.children, level + 1)
                }
                is MfmNode.Quote -> {
                    println("Quote: (${spr.level})")
                    traverse(spr.children, level + 1)
                }
            }
        }
    }

}