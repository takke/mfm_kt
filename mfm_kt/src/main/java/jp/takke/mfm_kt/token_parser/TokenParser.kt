package jp.takke.mfm_kt.token_parser

data class TokenResult(
    // 識別結果
    val type: TokenType,
    // 抽出した文字列
    val extractedValue: String,
    // マッチした全体の文字列(これを連結することで元の文字列が復元できること)
    val wholeText: String = extractedValue,
) {
    companion object {
        // テスト用
        fun string(string: String) = TokenResult(TokenType.String, string)
        fun italic1() = TokenResult(TokenType.Italic1, "*")
        fun italicTagStart() = TokenResult(TokenType.ItalicTagStart, "<i>")
        fun italicTagEnd() = TokenResult(TokenType.ItalicTagEnd, "</i>")
        fun bold1() = TokenResult(TokenType.Bold1, "**")
        fun centerStart() = TokenResult(TokenType.CenterStart, "<center>")
        fun centerEnd() = TokenResult(TokenType.CenterEnd, "</center>")
        fun smallStart() = TokenResult(TokenType.SmallStart, "<small>")
        fun smallEnd() = TokenResult(TokenType.SmallEnd, "</small>")
        fun inlineCode(s: String) = TokenResult(TokenType.InlineCode, s, "`$s`")
        fun functionStart(s: String) = TokenResult(TokenType.FunctionStart, s, "$[$s ")
        fun functionEnd() = TokenResult(TokenType.FunctionEnd, "]", "]")
    }
}

data class TokenParseResult(val success: Boolean, val holder: TokenResultHolder, val next: String)

data class TokenResultHolder(
    val tokenList: List<TokenResult>
) {
    fun append(newResult: TokenResult): TokenResultHolder {
        return TokenResultHolder(mutableListOf<TokenResult>().also {
            it.addAll(this.tokenList)
            it.add(newResult)
        })
    }
}

typealias TokenParser = (String, TokenResultHolder) -> TokenParseResult