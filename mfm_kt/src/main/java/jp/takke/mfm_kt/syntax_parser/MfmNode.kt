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
        internal constructor(props: String, vararg children: MfmNode) : this(props, children.toList())

        val name: String by lazy { props.substringBefore('.') }
        val args: Map<String, String> by lazy {
            // $[x2 ] => []
            // $[font.serif ] => [("serif", "")]
            // $[bg.color=00ee22 ] => (["color", "00ee22")]
            // $[scale.x=1.2,y=1.5 ] => [("x", "1.2"), ("y", "1.5")]

            val after = props.substringAfter('.', "")
            if (after.isEmpty()) {
                return@lazy emptyMap<String, String>()
            }

            val result = HashMap<String, String>()
            after.split(',').forEach {
                if (it.contains('=')) {
                    val (key, value) = it.split('=')
                    result[key] = value
                } else {
                    result[it] = ""
                }
            }

            result
        }
    }

    data class InlineCode(val children: List<MfmNode>) : MfmNode(true)

    data class EmojiCode(val value: String) : MfmNode(true)

}