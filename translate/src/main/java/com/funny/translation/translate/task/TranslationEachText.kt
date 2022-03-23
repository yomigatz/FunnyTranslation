package com.funny.translation.translate.task

import com.funny.translation.trans.Language
import com.funny.translation.trans.TranslationEngine
import com.funny.translation.trans.TranslationException
import com.funny.translation.translate.bean.Consts
import com.funny.translation.translate.engine.TranslationEngines
import com.funny.translation.translate.utils.FunnyEachText
import com.funny.translation.translate.utils.StringUtil
import org.json.JSONException
import java.io.IOException

class TranslationEachText() :
    BasicTranslationTask(), TranslationEngine by TranslationEngines.EachText{

    @Throws(TranslationException::class)
    override fun getBasicText(url: String): String {
        return sourceString
    }

    @Throws(TranslationException::class)
    override fun getFormattedResult(basicText: String) {
        val chinese = StringUtil.extraChinese(basicText)
        try {
            val words = FunnyEachText.words
            val stringBuilder = StringBuilder()
            for (element in chinese) {
                val each = element.toString()
                if (words.has(each)) {
                    stringBuilder.append(words.getString(each))
                }
                stringBuilder.append(" ")
            }
            result.setBasicResult(stringBuilder.toString())
        } catch (e: IOException) {
            e.printStackTrace()
            throw TranslationException(Consts.ERROR_IO)
        } catch (e: JSONException) {
            e.printStackTrace()
            throw TranslationException(Consts.ERROR_JSON)
        }
    }

    override fun madeURL(): String {
        return ""
    }

    override val isOffline: Boolean
        get() = true
}