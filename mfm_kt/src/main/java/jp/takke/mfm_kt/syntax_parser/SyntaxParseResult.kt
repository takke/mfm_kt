package jp.takke.mfm_kt.syntax_parser

sealed class SyntaxParseResult(val isInline: Boolean) {

    data class Text(val value: String) : SyntaxParseResult(true)

    data class Bold(val children: List<SyntaxParseResult>) : SyntaxParseResult(true)

    data class Italic(val children: List<SyntaxParseResult>) : SyntaxParseResult(true)

    data class Center(val children: List<SyntaxParseResult>) : SyntaxParseResult(false)

    data class Small(val children: List<SyntaxParseResult>) : SyntaxParseResult(false)

    enum class QuoteLevel {
        Level1,
        Level2,
    }

    data class Quote(val level: QuoteLevel, val children: List<SyntaxParseResult>) : SyntaxParseResult(true)

    data class Function(val props: String, val children: List<SyntaxParseResult>) : SyntaxParseResult(false) {
        // TODO name+propsに変換すること
        val name: String
            get() = props
        val args: List<String>
            get() = emptyList()
    }

}