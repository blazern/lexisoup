package blazern.lexisoup.utils

fun String.onlyLetters(): String =
    replace(Regex("[^\\p{L}\\s]"), "")

fun String.noQuotationMarks(): String =
    trim().replace(Regex("^[\"'„‚«‹“‘]+|[\"'“”‘’»›]+$"), "")
