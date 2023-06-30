package jp.takke.mfm_kt.token_parser

// 字句解析結果の種別
enum class TokenType {
    // 字句解析内部でのみ使用
    Char,

    // Charを連結した任意の文字列
    String,

    // >...
    QuoteLine1,

    // >>...
    QuoteLine2,

    // <center>...</center>
    CenterStart, CenterEnd,

    // ***
    Big,

    // **
    BoldAsta,

    // <b>...</b>
    BoldTagStart, BoldTagEnd,

    // __
    BoldUnder,

    // <s>...</s>
    StrikeTagStart, StrikeTagEnd,

    // <small>...</small>
    SmallStart, SmallEnd,

    // <i>...</i>
    ItalicTagStart, ItalicTagEnd,

    // *
    ItalicAsta,

    // _
    ItalicUnder,

    // $[...]
    FunctionStart, FunctionEnd,

    // `...`
    InlineCode,

}