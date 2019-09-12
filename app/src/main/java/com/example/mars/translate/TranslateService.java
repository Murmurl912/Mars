package com.example.mars.translate;

public abstract class TranslateService {

    public interface OnTranslateReturn {
        void onTranslateReturn(int status, String result);
    }

    public abstract void requestTranslation(String source, OnTranslateReturn callback);

}
