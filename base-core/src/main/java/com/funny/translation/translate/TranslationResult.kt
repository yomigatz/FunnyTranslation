package com.funny.translation.translate

import androidx.annotation.Keep

/**
 *
 * @property engineName String 翻译引擎
 * @property basicResult Translation 基本翻译结果，见[Translation]
 * @property sourceString String 源语言
 * @property detailText String? 详细翻译结果，Markdown形式
 * @property targetLanguage Language? 目标语言
 * @constructor
 */
@Keep
data class TranslationResult(
    var engineName: String = "",
    var basicResult: Translation = Translation(""),
    var sourceString: String = "",
    var detailText : String? = null,
    var targetLanguage: Language? = Language.AUTO
) {
    /**
     * 设置 basicResult 的 trans 为 text
     * @param text String
     */
    fun setBasicResult(text: String) {
        basicResult.trans = text
        //该方法调用次数正常
    }
}

/**
 * 翻译bean
 * @property trans String 基本的翻译内容
 * @property phoneticNotation String? 音标
 * @property partOfSpeech String? 词性
 * @constructor
 */
@Keep
data class Translation(
    var trans: String,
    var phoneticNotation: String? = null,//注音
    var partOfSpeech: String? = null //词性
)

//艹，就因为这个折腾了一天
val EmptyTranslation = Translation("")
