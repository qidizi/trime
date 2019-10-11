/*
 * Copyright (C) 2015-present, osfans
 * waxaca@163.com https://github.com/osfans
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.osfans.trime;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * {@link RecognitionListener 語音輸入}
 */
class Speech implements RecognitionListener {
    private SpeechRecognizer speech;
    private Intent recognizerIntent;
    private String TAG = "Speech";
    private Context context;

    Speech(Context context) {
        this.context = context;
        speech = SpeechRecognizer.createSpeechRecognizer(context);
        speech.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        //recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");
        //recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.getPackageName());
        //recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        //recognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "開始語音");
    }

    private void alert(String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public void start() {
        speech.startListening(recognizerIntent);
    }

    private void stop() {
        speech.stopListening();
    }

    private void destroy() {
        if (speech != null) {
            speech.destroy();
        }
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.i(TAG, "onBeginningOfSpeech");
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.i(TAG, "onBufferReceived: " + Arrays.toString(buffer));
    }

    @Override
    public void onEndOfSpeech() {
        Log.i(TAG, "onEndOfSpeech");
        alert("识别中...");
    }

    @Override
    public void onError(int errorCode) {
        speech.stopListening();
        speech.destroy();
        String errorMessage = getErrorText(errorCode);
        alert(errorMessage);
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
        Log.i(TAG, "onEvent");
    }

    @Override
    public void onPartialResults(Bundle arg0) {
        Log.i(TAG, "onPartialResults");
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
        Log.i(TAG, "onReadyForSpeech");
        alert("请开始说话：");
    }

    @Override
    public void onResults(Bundle results) {
        stop();
        destroy();
        Log.i(TAG, "onResults");
        Trime trime = Trime.getService();
        if (trime == null) return;
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        if (null == matches)
          return;

        String opencc_config = Config.get(context).getString("speech_opencc_config");
        StringBuilder text = new StringBuilder();
        for (String result : matches)
          text.append(Rime.openccConvert(result, opencc_config));
        trime.commitText(text.toString());
    }

    @Override
    public void onRmsChanged(float rms_dB) {
        Log.i(TAG, "onRmsChanged: " + rms_dB);
    }

    private static String getErrorText(int errorCode) {
        String error;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                error = "录制异常";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                error = "tts组件异常";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                error = "请授予录音权限";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                error = "tts网络异常";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                error = "tts网络超时";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                error = "无法识别";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                error = "tts正被使用，请稍候";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                error = "tts服务异常";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                error = "等待语音输入超时";
                break;
            default:
                error = "未知错误";
                break;
        }
        return error;
    }
}
