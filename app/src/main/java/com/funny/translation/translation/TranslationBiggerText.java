package com.funny.translation.translation;


import androidx.preference.PreferenceManager;

import com.funny.translation.FunnyApplication;
import com.funny.translation.utils.FunnyBiggerText;

public class TranslationBiggerText extends  BasicTranslationTask {
    public TranslationBiggerText(TranslationHelper helper, String sourceString, short sourceLanguage, short targetLanguage, short engineKind) {
        super(helper, sourceString, sourceLanguage, targetLanguage, engineKind);
    }

    @Override
    public String getBasicText(String url) throws TranslationException {
        return sourceString;
    }

    @Override
    public TranslationResult getFormattedResult(String basicText) throws TranslationException {
        int performance = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(FunnyApplication.getFunnyContext()).getString("preference_bigger_text_performance","1"));
        FunnyBiggerText.fillChar = PreferenceManager.getDefaultSharedPreferences(FunnyApplication.getFunnyContext()).getString("preference_bigger_text_fill_char","");
        String str = "";
        switch (performance){
            case 0:
                str = FunnyBiggerText.drawWideString(FunnyApplication.getFunnyContext(),basicText);
                break;
            case 1:
                str = FunnyBiggerText.drawMiddleString(FunnyApplication.getFunnyContext(),basicText);
                break;
            case 2:
                str = FunnyBiggerText.drawNarrowString(FunnyApplication.getFunnyContext(),basicText);
                break;
        }
        return new TranslationResult(engineKind,str,null);
    }

    @Override
    public String madeURL() {
        return null;
    }

    @Override
    public boolean isOffline() {
        return true;
    }
}