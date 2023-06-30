package jp.takke.mfm_kt.token_parser

data class Token(
    // 識別結果
    val type: TokenType,
    // 抽出した文字列
    val extractedValue: String,
    // マッチした全体の文字列(これを連結することで元の文字列が復元できること)
    val wholeText: String = extractedValue,
) {
    companion object {
        // テスト用
        fun string(string: String) = Token(TokenType.String, string)
        fun italicAsta() = Token(TokenType.ItalicAsta, "*")
        fun italicTagStart() = Token(TokenType.ItalicTagStart, "<i>")
        fun italicTagEnd() = Token(TokenType.ItalicTagEnd, "</i>")
        fun boldAsta() = Token(TokenType.BoldAsta, "**")
        fun boldTagStart() = Token(TokenType.BoldTagStart, "<b>")
        fun boldTagEnd() = Token(TokenType.BoldTagEnd, "</b>")
        fun centerStart() = Token(TokenType.CenterStart, "<center>")
        fun centerEnd() = Token(TokenType.CenterEnd, "</center>")
        fun smallStart() = Token(TokenType.SmallStart, "<small>")
        fun smallEnd() = Token(TokenType.SmallEnd, "</small>")
        fun inlineCode(s: String) = Token(TokenType.InlineCode, s, "`$s`")
        fun functionStart(s: String) = Token(TokenType.FunctionStart, s, "$[$s ")
        fun functionEnd() = Token(TokenType.FunctionEnd, "]", "]")
    }
}

data class TokenParseResult(val success: Boolean, val holder: TokenHolder, val next: String)

data class TokenHolder(
    val tokenList: List<Token>
) {
    fun append(newResult: Token): TokenHolder {
        return TokenHolder(mutableListOf<Token>().also {
            it.addAll(this.tokenList)
            it.add(newResult)
        })
    }
}

typealias TokenParser = (String, TokenHolder) -> TokenParseResult