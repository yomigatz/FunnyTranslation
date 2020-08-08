package com.funny.translation.translation;

import com.funny.translation.bean.Consts;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;

public class TranslationGoogleNormal extends BasicTranslationTask {
    public TranslationGoogleNormal(TranslationHelper helper, String sourceString, short sourceLanguage, short targetLanguage, short engineKind) {
        super(helper, sourceString, sourceLanguage, targetLanguage, engineKind);
    }

    @Override
    String getBasicText(String url) throws TranslationException {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            String from= Consts.LANGUAGES[sourceLanguage][engineKind];
            String to=Consts.LANGUAGES[targetLanguage][engineKind];
            URL realUrl = new URL(String.format("https://translate.google.cn/translate_a/single?client=webapp&sl=%s&tl=%s&hl=%s&dt=at&dt=bd&dt=ex&dt=ld&dt=md&dt=qca&dt=rw&dt=rm&dt=ss&dt=t&source=btn&ssel=5&tsel=5&kc=0&tk=%s&q=%s",from,to,to,FunnyGoogleApi.tk(sourceString,"439500.3343569631"),android.net.Uri.encode(sourceString)));
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");

            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            //1.获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            //2.中文有乱码的需要将PrintWriter改为如下
            //out=new OutputStreamWriter(conn.getOutputStream(),"UTF-8")
            // 发送请求参数
            String param="";
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
            System.out.println("result:"+result);
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！"+e);
            e.printStackTrace();
            throw new TranslationException(Consts.ERROR_POST);
        }
        //使用finally块来关闭输出流、输入流
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
                throw new TranslationException(Consts.ERROR_IO);
            }
        }
        //result=formatResult(result);
        //System.out.println(result);
        //System.out.println("post推送结果："+result);
        return result;
    }

    @Override
    TranslationResult getFormattedResult(String basicText) throws TranslationException {
        TranslationResult result=new TranslationResult(Consts.ENGINE_GOOGLE);
        try
        {
            JSONArray all=new JSONArray(basicText);
            StringBuilder sb = new StringBuilder();
            int i = 0;
            JSONArray jsonArray_0 = all.getJSONArray(0);
            while(!jsonArray_0.getJSONArray(i).isNull(0)){
                String string = jsonArray_0.getJSONArray(i).getString(0);
                if(!string.equals("null")) {
                    sb.append(string);
                    i++;
                }
            }
            String basicResult=sb.toString();
            result.setBasicResult(basicResult);
            //System.out.println(basicResult);

            if (!all.isNull(5)) {
                JSONArray detailArr = all.getJSONArray(5);
                int length = detailArr.length();
                String[][] detailTexts = new String[length][];
                for (int j = 0; j < length; j++) {
                    JSONArray eachDetail = detailArr.getJSONArray(j); //5 j
                    if (eachDetail instanceof JSONArray) {
                        String text = eachDetail.getString(0);
                        if (eachDetail.isNull(2)) {
                            break;
                        }
                        JSONArray explaination = eachDetail.getJSONArray(2); //5 j 2
                        detailTexts[j] = new String[explaination.length() + 1];//多一个，第一个放文字
                        detailTexts[j][0] = text;
                        for (int k = 0; k < explaination.length(); k++) {
                            detailTexts[j][k + 1] = explaination.getJSONArray(k).getString(0);// 5 j 2 k 0
                        }
                        showArray(detailTexts);
                    }
                }
            }
            //System.out.println(all);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            throw new TranslationException(Consts.ERROR_JSON);
        }
        return result;
    }

    @Override
    String madeURL() {
        return null;
    }

    @Override
    boolean isOffline() {
        return false;
    }

    public static void showArray(String[][] arr){
        for(String[] arr1:arr){
            for(String str:arr1){
                System.out.print(str);
                System.out.print(" ");
            }
            System.out.println("");
        }
    }
}
