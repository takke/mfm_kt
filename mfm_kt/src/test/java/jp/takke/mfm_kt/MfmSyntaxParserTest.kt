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

        checkSyntaxParser(
            "bold 閉じず",
            "aaa**hoge",
            option,
            // TODO 閉じていない場合は、そのままテキストとして扱うべき
//            listOf(
//                SyntaxParseResult.Text("aaa"),
//                SyntaxParseResult.Text("**"),
//                SyntaxParseResult.Text("hoge"),
//            )
            listOf(
                MfmNode.Text("aaa"),
                MfmNode.Bold(
                    listOf(MfmNode.Text("hoge"))
                ),
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

        checkSyntaxParser(
            "italic* 閉じず",
            "*hoge",
            option,
            listOf(
                MfmNode.Italic(
                    listOf(MfmNode.Text("hoge"))
                ),
            )
        )

        checkSyntaxParser(
            "italic tag  閉じず",
            "<i>hoge",
            option,
            listOf(
                MfmNode.Italic(
                    listOf(MfmNode.Text("hoge"))
                ),
            )
        )

        checkSyntaxParser(
            "italic + bold",
            "<i>**hoge**",
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

    //--------------------------------------------------
    // custom checker
    //--------------------------------------------------

    private fun checkSyntaxParser(scenarioName: String, inputText: String, option: MfmSyntaxParser.Option, expected: List<MfmNode>) {

        val result = MfmSyntaxParser(MfmTokenParser.tokenize(inputText), option).parse()

        println("---- [$scenarioName] start")
        println("input: [$inputText]")
        println("result:")
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