@file:Suppress("TestFunctionName", "NonAsciiCharacters")

package jp.takke.mfm_kt

import jp.takke.mfm_kt.syntax_parser.MfmSyntaxParser
import jp.takke.mfm_kt.syntax_parser.MfmNode
import jp.takke.mfm_kt.token_parser.MfmTokenParser
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class MfmSyntaxParserTest {

    private val optionAll = MfmSyntaxParser.Option()

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
            enableBig = false,
            enableBold = false,
            enableItalic = false,
            enableStrike = false,
            enableCenter = false,
            enableSmall = false,
            enableQuote = false,
            enableFunction = false,
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
            "big",
            "hoge***big***",
            option,
            listOf(
                MfmNode.Text("hoge***big***"),
            )
        )

        checkSyntaxParser(
            "strike",
            "hoge<s>big</s>",
            option,
            listOf(
                MfmNode.Text("hoge<s>big</s>"),
            )
        )

        checkSyntaxParser(
            "bold",
            "hoge**bold**",
            option,
            listOf(
                MfmNode.Text("hoge**bold**"),
            )
        )

        checkSyntaxParser(
            "bold+italic",
            "hoge**bold**and*italic*",
            option,
            listOf(
                MfmNode.Text("hoge**bold**and*italic*"),
            )
        )

        checkSyntaxParser(
            "bold+italic2",
            "hoge**bold**and*italic*_a_<i>hoge</i>",
            option,
            listOf(
                MfmNode.Text("hoge**bold**and*italic*_a_<i>hoge</i>"),
            )
        )

        checkSyntaxParser(
            "bold+italic+center",
            "<center>hoge**bold**and*italic*</center>",
            option,
            listOf(
                MfmNode.Text("<center>hoge**bold**and*italic*</center>"),
            )
        )

        checkSyntaxParser(
            "bold+italic+center+small",
            "<center>hoge**bold**and*italic*</center><small>ちいさい</small>",
            option,
            listOf(
                MfmNode.Text("<center>hoge**bold**and*italic*</center><small>ちいさい</small>"),
            )
        )

        checkSyntaxParser(
            "bold+italic+center+small+quote",
            "<center>hoge**bold**and*italic*</center><small>ちいさい</small>\n>a\n>>b\n",
            option,
            listOf(
                MfmNode.Text("<center>hoge**bold**and*italic*</center><small>ちいさい</small>\n>a\n>>b\n"),
            )
        )

        checkSyntaxParser(
            "bold+italic+center+small+quote+fn+big",
            "<center>hoge**bold**$[x2 and]*italic*</center><small>ちいさい</small>\n>a\n>>b\n***hou***",
            option,
            listOf(
                MfmNode.Text("<center>hoge**bold**$[x2 and]*italic*</center><small>ちいさい</small>\n>a\n>>b\n***hou***")
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
                MfmNode.Text("<center>hoge"),
            )
        )

        checkSyntaxParser(
            "center 複数行1",
            "a\n<center>hoge\nb",
            option,
            listOf(
                MfmNode.Text("a\n<center>hoge\nb"),
            )
        )

        checkSyntaxParser(
            "center 複数行+bold",
            "a\n<center>\n**hoge1**\nhoge2\n\nb",
            option,
            listOf(
                MfmNode.Text("a\n<center>\n"),
                MfmNode.Bold(
                    listOf(MfmNode.Text("hoge1"))
                ),
                MfmNode.Text("\nhoge2\n\nb"),
            )
        )
    }

    @Test
    fun parse_big() {

        val option = optionAll

        checkSyntaxParser(
            "big1",
            "***hoge***",
            option,
            listOf(
                MfmNode.Big(
                    listOf(MfmNode.Text("hoge"))
                ),
            )
        )

        checkSyntaxParser(
            "big2",
            "aaa***hoge***bbb",
            option,
            listOf(
                MfmNode.Text("aaa"),
                MfmNode.Big(
                    listOf(MfmNode.Text("hoge"))
                ),
                MfmNode.Text("bbb"),
            )
        )

        // *** の間は いろいろOK
        checkSyntaxParser(
            "big + tag",
            "aaa***<i>hoge</i>***bbb",
            option,
            listOf(
                MfmNode.Text("aaa"),
                MfmNode.Big(
                    listOf(
                        MfmNode.Italic(
                            listOf(MfmNode.Text("hoge"))
                        )
                    )
                ),
                MfmNode.Text("bbb"),
            )
        )
    }

    @Test
    fun parse_bold() {

        val option = MfmSyntaxParser.Option(
            enableBold = true,
            enableItalic = true,
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

        checkSyntaxParser(
            "bold tag",
            "aaa<b>hoge</b>bbb",
            option,
            listOf(
                MfmNode.Text("aaa"),
                MfmNode.Bold(
                    listOf(MfmNode.Text("hoge"))
                ),
                MfmNode.Text("bbb"),
            )
        )

        checkSyntaxParser(
            "bold tag 2",
            "aaa<b><i>hoge</i></b>bbb",
            option,
            listOf(
                MfmNode.Text("aaa"),
                MfmNode.Bold(
                    listOf(
                        MfmNode.Italic(
                            listOf(MfmNode.Text("hoge"))
                        )
                    )
                ),
                MfmNode.Text("bbb"),
            )
        )

        checkSyntaxParser(
            "bold under",
            "aaa__hoge__bbb",
            option,
            listOf(
                MfmNode.Text("aaa"),
                MfmNode.Bold(
                    listOf(MfmNode.Text("hoge"))
                ),
                MfmNode.Text("bbb"),
            )
        )

        checkSyntaxParser(
            "bold under",
            "aaa__ho ge__bbb",
            option,
            listOf(
                MfmNode.Text("aaa"),
                MfmNode.Bold(
                    listOf(MfmNode.Text("ho ge"))
                ),
                MfmNode.Text("bbb"),
            )
        )

        // __ の間は alpha+num+sp
        checkSyntaxParser(
            "bold under 2",
            "aaa__<i>hoge</i>__bbb",
            option,
            listOf(
                MfmNode.Text("aaa__"),
                MfmNode.Italic(
                    listOf(MfmNode.Text("hoge"))
                ),
                MfmNode.Text("__bbb"),
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
                MfmNode.Text("aaa**hoge"),
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
                MfmNode.Text("<small>hoge"),
            )
        )

        checkSyntaxParser(
            "small 複数行1",
            "a\n<small>hoge\nb",
            option,
            listOf(
                MfmNode.Text("a\n<small>hoge\nb"),
            )
        )

        checkSyntaxParser(
            "small 複数行+bold",
            "a\n<small>\n**hoge1**\nhoge2\n\nb",
            option,
            listOf(
                MfmNode.Text("a\n<small>\n"),
                MfmNode.Bold(
                    listOf(MfmNode.Text("hoge1"))
                ),
                MfmNode.Text("\nhoge2\n\nb"),
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

        // *...* と <i>...</i>, _..._ の3パターンある

        checkSyntaxParser(
            "italic*",
            "*hoge1*",
            option,
            listOf(
                MfmNode.Italic(
                    listOf(MfmNode.Text("hoge1"))
                ),
            )
        )

        // *と*の間はローマ字と数値のみ許可
        checkSyntaxParser(
            "italic* ローマ字と数値以外",
            "*ほげ*",
            option,
            listOf(
                MfmNode.Text("*ほげ*"),
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

        checkSyntaxParser(
            "italic_",
            "_hoge1_",
            option,
            listOf(
                MfmNode.Italic(
                    listOf(MfmNode.Text("hoge1"))
                ),
            )
        )

        // _と_の間はローマ字と数値のみ許可
        checkSyntaxParser(
            "italic_ ローマ字と数値以外",
            "_ほげ_",
            option,
            listOf(
                MfmNode.Text("_ほげ_"),
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
                MfmNode.Text("*hoge"),
            )
        )

        checkSyntaxParser(
            "italic tag 閉じず",
            "<i>hoge",
            optionAll,
            listOf(
                MfmNode.Text("<i>hoge"),
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
    fun parse_strike() {

        val option = optionAll

        checkSyntaxParser(
            "<s>strike</s>",
            "<s>strike</s>",
            option,
            listOf(
                MfmNode.Strike(
                    listOf(MfmNode.Text("strike"))
                ),
            )
        )
    }

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

    @Test
    fun parse_function_閉じず() {

        checkSyntaxParser(
            "Function x2",
            "$[x2 hoge",
            optionAll,
            listOf(
                MfmNode.Text("$[x2 hoge"),
            )
        )

        checkSyntaxParser(
            "Function + Bold",
            "$[x4 **hoge**",
            optionAll,
            listOf(
                MfmNode.Text("$[x4 "),
                MfmNode.Bold(
                    listOf(MfmNode.Text("hoge"))
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
                is MfmNode.Quote -> {
                    println("Quote: (${spr.level})")
                    traverse(spr.children, level + 1)
                }
                is MfmNode.Center -> {
                    println("Center: ")
                    traverse(spr.children, level + 1)
                }
                is MfmNode.Big -> {
                    println("Big: ")
                    traverse(spr.children, level + 1)
                }
                is MfmNode.Bold -> {
                    println("Bold: ")
                    traverse(spr.children, level + 1)
                }
                is MfmNode.Small -> {
                    println("Small: ")
                    traverse(spr.children, level + 1)
                }
                is MfmNode.Italic -> {
                    println("Italic: ")
                    traverse(spr.children, level + 1)
                }
                is MfmNode.Strike -> {
                    println("Strike: ")
                    traverse(spr.children, level + 1)
                }
                is MfmNode.Function -> {
                    println("Function: ${spr.name} ${spr.args.joinToString(", ")}")
                    traverse(spr.children, level + 1)
                }
            }
        }
    }

}