package jp.takke.mfm_kt.syntax_parser

sealed class MfmNode(val isInline: Boolean) {

    data class Text(val value: String) : MfmNode(true)

    enum class QuoteLevel {
        Level1,
        Level2,
    }

    data class Quote(val level: QuoteLevel, val children: List<MfmNode>) : MfmNode(false)

    data class Center(val children: List<MfmNode>) : MfmNode(false)

    data class Big(val children: List<MfmNode>) : MfmNode(true)

    data class Bold(val children: List<MfmNode>) : MfmNode(true)

    data class Small(val children: List<MfmNode>) : MfmNode(true)

    data class Italic(val children: List<MfmNode>) : MfmNode(true)

    data class Strike(val children: List<MfmNode>) : MfmNode(true)

    data class Function(val props: String, val children: List<MfmNode>) : MfmNode(true) {
        // TODO name+propsに変換すること
        val name: String
            get() = props
        val args: List<String>
            get() = emptyList()
    }

    data class InlineCode(val children: List<MfmNode>) : MfmNode(true)

}