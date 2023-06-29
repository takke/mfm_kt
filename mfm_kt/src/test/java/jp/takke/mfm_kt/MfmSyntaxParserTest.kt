@file:Suppress("TestFunctionName", "NonAsciiCharacters")

package jp.takke.mfm_kt

import jp.takke.mfm_kt.syntax_parser.MfmSyntaxParser
import jp.takke.mfm_kt.syntax_parser.SyntaxParseResult
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
                    SyntaxParseResult.Text("hoge")
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
                SyntaxParseResult.Text("hoge"),
            )
        )

        checkSyntaxParser(
            "bold",
            "hoge**bold**",
            option,
            listOf(
                SyntaxParseResult.Text("hoge"),
                SyntaxParseResult.Text("**"),
                SyntaxParseResult.Text("bold"),
                SyntaxParseResult.Text("**"),
            )
        )

        checkSyntaxParser(
            "bold+italic",
            "hoge**bold**and*italic*",
            option,
            listOf(
                SyntaxParseResult.Text("hoge"),
                SyntaxParseResult.Text("**"),
                SyntaxParseResult.Text("bold"),
                SyntaxParseResult.Text("**"),
                SyntaxParseResult.Text("and"),
                SyntaxParseResult.Text("*"),
                SyntaxParseResult.Text("italic"),
                SyntaxParseResult.Text("*"),
            )
        )

        checkSyntaxParser(
            "bold+italic+center",
            "<center>hoge**bold**and*italic*</center>",
            option,
            listOf(
                SyntaxParseResult.Text("<center>"),
                SyntaxParseResult.Text("hoge"),
                SyntaxParseResult.Text("**"),
                SyntaxParseResult.Text("bold"),
                SyntaxParseResult.Text("**"),
                SyntaxParseResult.Text("and"),
                SyntaxParseResult.Text("*"),
                SyntaxParseResult.Text("italic"),
                SyntaxParseResult.Text("*"),
                SyntaxParseResult.Text("</center>"),
            )
        )

        checkSyntaxParser(
            "bold+italic+center+small",
            "<center>hoge**bold**and*italic*</center><small>ちいさい</small>",
            option,
            listOf(
                SyntaxParseResult.Text("<center>"),
                SyntaxParseResult.Text("hoge"),
                SyntaxParseResult.Text("**"),
                SyntaxParseResult.Text("bold"),
                SyntaxParseResult.Text("**"),
                SyntaxParseResult.Text("and"),
                SyntaxParseResult.Text("*"),
                SyntaxParseResult.Text("italic"),
                SyntaxParseResult.Text("*"),
                SyntaxParseResult.Text("</center>"),
                SyntaxParseResult.Text("<small>"),
                SyntaxParseResult.Text("ちいさい"),
                SyntaxParseResult.Text("</small>"),
            )
        )

        checkSyntaxParser(
            "bold+italic+center+small+quote",
            "<center>hoge**bold**and*italic*</center><small>ちいさい</small>\n>a\n>>b\n",
            option,
            listOf(
                SyntaxParseResult.Text("<center>"),
                SyntaxParseResult.Text("hoge"),
                SyntaxParseResult.Text("**"),
                SyntaxParseResult.Text("bold"),
                SyntaxParseResult.Text("**"),
                SyntaxParseResult.Text("and"),
                SyntaxParseResult.Text("*"),
                SyntaxParseResult.Text("italic"),
                SyntaxParseResult.Text("*"),
                SyntaxParseResult.Text("</center>"),
                SyntaxParseResult.Text("<small>"),
                SyntaxParseResult.Text("ちいさい"),
                SyntaxParseResult.Text("</small>"),
                SyntaxParseResult.Text("\n"),
                SyntaxParseResult.Text(">a\n"),
                SyntaxParseResult.Text(">>b\n"),
            )
        )

        checkSyntaxParser(
            "bold+italic+center+small+quote+fn",
            "<center>hoge**bold**$[x2 and]*italic*</center><small>ちいさい</small>\n>a\n>>b\n",
            option,
            listOf(
                SyntaxParseResult.Text("<center>"),
                SyntaxParseResult.Text("hoge"),
                SyntaxParseResult.Text("**"),
                SyntaxParseResult.Text("bold"),
                SyntaxParseResult.Text("**"),
                SyntaxParseResult.Text("$[x2 "),
                SyntaxParseResult.Text("and"),
                SyntaxParseResult.Text("]"),
                SyntaxParseResult.Text("*"),
                SyntaxParseResult.Text("italic"),
                SyntaxParseResult.Text("*"),
                SyntaxParseResult.Text("</center>"),
                SyntaxParseResult.Text("<small>"),
                SyntaxParseResult.Text("ちいさい"),
                SyntaxParseResult.Text("</small>"),
                SyntaxParseResult.Text("\n"),
                SyntaxParseResult.Text(">a\n"),
                SyntaxParseResult.Text(">>b\n"),
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
                SyntaxParseResult.Text("hoge"),
            )
        )

        checkSyntaxParser(
            "bold1",
            "**hoge**",
            option,
            listOf(
                SyntaxParseResult.Bold(
                    listOf(SyntaxParseResult.Text("hoge"))
                ),
            )
        )

        checkSyntaxParser(
            "bold2",
            "aaa**hoge**bbb",
            option,
            listOf(
                SyntaxParseResult.Text("aaa"),
                SyntaxParseResult.Bold(
                    listOf(SyntaxParseResult.Text("hoge"))
                ),
                SyntaxParseResult.Text("bbb"),
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
                SyntaxParseResult.Text("aaa"),
                SyntaxParseResult.Bold(
                    listOf(SyntaxParseResult.Text("hoge"))
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
                SyntaxParseResult.Italic(
                    listOf(SyntaxParseResult.Text("hoge"))
                ),
            )
        )

        checkSyntaxParser(
            "italic tag",
            "<i>hoge</i>",
            option,
            listOf(
                SyntaxParseResult.Italic(
                    listOf(SyntaxParseResult.Text("hoge"))
                ),
            )
        )

        checkSyntaxParser(
            "italic + bold",
            "<i>**hoge**</i>",
            option,
            listOf(
                SyntaxParseResult.Italic(
                    listOf(
                        SyntaxParseResult.Bold(
                            listOf(SyntaxParseResult.Text("hoge"))
                        )
                    )
                ),
            )
        )
    }

    private fun checkSyntaxParser(scenarioName: String, inputText: String, option: MfmSyntaxParser.Option, expected: List<SyntaxParseResult>) {

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

    private fun dump(it: List<SyntaxParseResult>) {
        traverse(it, 1)
    }

    private fun traverse(parsedResult: List<SyntaxParseResult>, level: Int) {

        parsedResult.forEach { spr ->

            print("   ".repeat(level))
            when (spr) {
                is SyntaxParseResult.Text -> {
                    println("Text: \"${spr.value.replace("\n", "\\n")}\"")
                }
                is SyntaxParseResult.Bold -> {
                    println("Bold: ")
                    traverse(spr.children, level + 1)
                }
                is SyntaxParseResult.Italic -> {
                    println("Italic: ")
                    traverse(spr.children, level + 1)
                }
                is SyntaxParseResult.Center -> {
                    println("Center: ")
                    traverse(spr.children, level + 1)
                }
                is SyntaxParseResult.Small -> {
                    println("Small: ")
                    traverse(spr.children, level + 1)
                }
                is SyntaxParseResult.Function -> {
                    println("Function: ${spr.name} ${spr.args.joinToString(", ")}")
                    traverse(spr.children, level + 1)
                }
                is SyntaxParseResult.Quote -> {
                    println("Quote: (${spr.level})")
                    traverse(spr.children, level + 1)
                }
            }
        }
    }

}