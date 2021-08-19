package com.funny.translation.trans

import androidx.annotation.Keep

@Keep
data class TranslationResult(
    var engineKind: Short,
    var basicResult: Translation = EmptyTranslation,
    var sourceString: String = "",
    val details: ArrayList<Translation>? = null
) {
    fun setBasicResult(text: String) {
        basicResult.trans = text
    }
}

@Keep
data class Translation(
    var trans: String,
    var phoneticNotation: String? = null,//注音
    var partOfSpeech: String? = null //词性
)

val EmptyTranslation = Translation("")