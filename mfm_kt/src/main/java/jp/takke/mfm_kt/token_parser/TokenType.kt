package jp.takke.mfm_kt.token_parser

// 字句解析結果の種別
enum class TokenType {
    // 字句解析内部でのみ使用
    Char,

    // Charを連結した任意の文字列
    String,

    // **
    Bold1,

    // *
    Italic1,

    // <i>...</i>
    ItalicTagStart, ItalicTagEnd,

    // `...`
    InlineCode,

    // >...
    QuoteLine1,

    // >>...
    QuoteLine2,

    // $[...]
    FunctionStart, FunctionEnd,

    // <center>...</center>
    CenterStart, CenterEnd,

    // <small>...</small>
    SmallStart, SmallEnd,
}