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

        // 全てオフにするとただのテキストになること
        MfmSyntaxParser(
            MfmTokenParser.tokenize("hoge"), MfmSyntaxParser.Option(
                enableBold = false,
                enableItalic = false,
                enableCenter = false,
                enableSmall = false,
                enableQuote = false,
                enableFunction = false
            )
        )
            .parse()
            .let {
                assertThat(it).containsExactly(
                    SyntaxParseResult.Text("hoge")
                )
            }
    }

}