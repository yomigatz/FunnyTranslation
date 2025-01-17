package com.funny.translation.translation

import android.util.Log
import com.funny.translation.bean.Consts
import com.funny.translation.network.OkHttpUtils
import com.funny.translation.trans.TranslationException
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.math.BigInteger
import java.net.URLEncoder
import java.security.MessageDigest
import java.util.*

class TranslationJinshanEasy(sourceString: String?, sourceLanguage: Short, targetLanguage: Short) :
    BasicTranslationTask(
        sourceString!!, sourceLanguage, targetLanguage
    ) {

    companion object{
        const val TAG = "TranslationJinshanEasy"
    }

    @Throws(TranslationException::class)
    override fun getBasicText(url: String): String {
        return try {
            val string = OkHttpUtils.get(url) //OkHttpUtil.getWithIP(url,"182.32.234.161",9999);
            Log.i(TAG, "获取到的string:$string")
            string
        } catch (e: IOException) {
            e.printStackTrace()
            throw TranslationException(Consts.ERROR_POST)
        }
    }

    //    @Override
    //    public TranslationResult getFormattedResult(String basicText) throws TranslationException {
    //        TranslationResult result = new TranslationResult(engineKind);
    //        try{
    //            JSONObject all = new JSONObject(basicText);
    //            JSONObject message = all.getJSONObject("message");
    //            JSONObject baseInfo = message.getJSONObject("baesInfo");
    //        }catch (JSONException e){
    //            e.printStackTrace();
    //            throw new TranslationException(Consts.ERROR_JSON);
    //        }
    //    }
    //版本 2
    @Throws(TranslationException::class)
    override fun getFormattedResult(basicText: String) {
        try {
            val all = JSONObject(basicText)
            if (all.getInt("status") == 1) {
                val baseInfo = all.getJSONObject("message").getJSONObject("baesInfo")
                if (baseInfo.has("symbols")) { //释义比较丰富的词汇
                    val symbols = baseInfo.getJSONArray("symbols")
                    val parts = symbols.getJSONObject(0).getJSONArray("parts")
                    val means = parts.getJSONObject(0).getJSONArray("means")
                    val sb = StringBuilder()
                    for (i in 0 until means.length()) {
                        sb.append(means.getString(i))
                        sb.append("\n")
                    }
                    sb.deleteCharAt(sb.length - 1)
                    result.setBasicResult(sb.toString())
                } else {
                    result.setBasicResult(baseInfo.getString("translate_result"))
                }
            } else if (all.getInt("status") == 10001) {
                result.setBasicResult("错误的访问！【" + all.getString("message") + "】")
            } else {
                throw TranslationException(Consts.ERROR_DATED_ENGINE)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            throw TranslationException(Consts.ERROR_JSON)
        }
    }

    //版本 1
    //    @Override
    //    TranslationResult getFormattedResult(String basicText) throws TranslationException {
    //        TranslationResult result = new TranslationResult(engineKind);
    //        try {
    //            JSONObject all = new JSONObject(basicText);
    //            if (all.getInt("status")==1){
    //                JSONObject content = all.getJSONObject("content");
    //                String trans = content.getString("out");
    //                result.setBasicResult(trans);
    //            }else if(all.getInt("status")==0){
    //                JSONObject content = all.getJSONObject("content");
    //                JSONArray detailArr = content.getJSONArray("word_mean");
    //                StringBuilder text = new StringBuilder();
    //                for (int i = 0; i < detailArr.length(); i++) {
    //                    text.append(detailArr.getString(i));
    //                    text.append("\n");
    //                }
    //                text.deleteCharAt(text.length()-1);
    //                result.setBasicResult(text.toString());
    //                //result.setPhoneticNotation();
    //            }
    //            else{
    //                throw new TranslationException(Consts.ERROR_DATED_ENGINE);
    //            }
    //        } catch (JSONException e) {
    //            e.printStackTrace();
    //            throw new TranslationException(Consts.ERROR_JSON);
    //        }
    //        return result;
    //    }
    //2020/12/26 可用,但太简单
    //http://dict-co.iciba.com/api/dictionary.php?w=go&key=0EAE08A016D6688F64AB3EBB2337BFB0
    //    9AA9FA4923AC16CED1583C26CF284C3F
    //2021.3.23 破解金山翻译
    //参见爬取金山翻译.py
    private val client = 6
    private val key = 1000006
    override fun madeURL(): String {
        val engineKind = engineName
        val from = Consts.LANGUAGES[sourceLanguage.toInt()][engineKind.toInt()]
        val to = Consts.LANGUAGES[targetLanguage.toInt()][engineKind.toInt()]
        var url = ""
        try {
            val word = URLEncoder.encode(sourceString, "utf-8").replace("\\+".toRegex(), "%20")
            val time = System.currentTimeMillis()
            // url = String.format("http://www.iciba.com/index.php?a=getWordMean&c=search&word=%s",URLEncoder.encode(sourceString,"utf-8"));
            //url = String.format("http://fy.iciba.com/ajax.php?a=fy&f=%s&t=%s&w=%s",from,to, URLEncoder.encode(sourceString,"utf-8"));
            url = "https://dict.iciba.com/dictionary/word/query/web?" +
                    "client=" + client +
                    "&key=" + key +
                    "&timestamp=" + time +
                    "&word=" + URLEncoder.encode(word, "utf-8") +
                    "&signature=" + getSignature(word, time)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return url
    }

    override val isOffline: Boolean
        get() = false

    private fun getSignature(word: String, time: Long): String? {
        //t : '610000061616418129580nice%20to%20meet%20you.'
        val t =
            String.format(Locale.CHINA, "%d%d%s%s", client, key, time, word)
        //param :"/dictionary/word/query/web610000061616418129580nice%20to%20meet%20you.7ece94d9f9c202b0d2ec557dg4r9bc"
        val param =
            "/dictionary/word/query/web" + t + "7ece94d9f9c202b0d2ec557dg4r9bc"
        return getMD5LowerCase(param)
    }

    /**
     * 对字符串md5加密
     *
     * @param str 传入要加密的字符串
     * @return MD5加密后的字符串(小写+字母)
     */
    private fun getMD5LowerCase(str: String): String? {
        return try {
            // 生成一个MD5加密计算摘要
            val md = MessageDigest.getInstance("MD5")
            // 计算md5函数
            md.update(str.toByteArray())
            // digest()最后确定返回md5 hash值，返回值为8为字符串。因为md5 hash值是16位的hex值，实际上就是8位的字符
            // BigInteger函数则将8位的字符串转换成16位hex值，用字符串来表示；得到字符串形式的hash值
            BigInteger(1, md.digest()).toString(16)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override val engineName: String
        get() = Consts.ENGINE_JINSHAN
}