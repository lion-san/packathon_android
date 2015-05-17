/*
 * 2015 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package com.fujitsu.jp.stadiumcoach;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
* メイン画面、会話のテキスト表示を管理するクラス
*/
public class ListItemManager {
    /** 発話の種別　ユーザの発話 ({@value}) */
    public static final int LIST_TYPE_USER                      = 0;
    /** 発話の種別　システムの発話 ({@value}) */
    public static final int LIST_TYPE_SYSTEM                    = 1;

    /** 表示の種別　初回メッセージの表示 ({@value}) */
    private static final int ITEM_TYPE_FIRST_MSG                = 1;
    /** 表示の種別　音声認識中の表示 ({@value}) */
    private static final int ITEM_TYPE_SPEECH_PROGRESS          = 2;
    /** 表示の種別　音声認識結果の表示 ({@value}) */
    private static final int ITEM_TYPE_SPEECH_RESULT            = 3;
    /** 表示の種別　音声認識エラーの表示 ({@value}) */
    private static final int ITEM_TYPE_SPEECH_ERROR             = 4;
    /** 表示の種別　意図解釈中の表示 ({@value}) */
    private static final int ITEM_TYPE_INTERPRETATION_ROGRESS   = 5;
    /** 表示の種別　意図解釈結果の表示 ({@value}) */
    private static final int ITEM_TYPE_INTERPRETATION_RESULT    = 6;
    /** 表示の種別　システムーメッセージの表示 ({@value}) */
    private static final int ITEM_TYPE_SYSTEM_MSG               = 9;

    /** コンテキスト */
    private final Context mContext;
    /** 発話テキスト表示レイアウト */
    private final LinearLayout mBaseLayout;
    /** LayoutInflaterオブジェクト */
    private final LayoutInflater mInflater;

    // 発話があった場合に削除するため保持しておくView
    private View mOneTimeView = null;

    private WebView wv;

    public ListItemManager(Context context, LinearLayout baseLayout) {
        mContext = context;
        mBaseLayout = baseLayout;
        mInflater = ((Activity) context).getLayoutInflater();
    }

    /**
     * 初回メッセージを表示する。
     */
    public void setFirstMessage() {
        String message = mContext.getString(R.string.txt_first_message);
        View view = getLatestView();
        if (view == null) {
            setTextItem(message, LIST_TYPE_SYSTEM, ITEM_TYPE_FIRST_MSG, false, false, false);
        }
        //既に表示がされている場合は、初回メッセージを表示しない
    }

    /**
     * 音声認識中テキストを表示する。
     */
    public void setSpeechProgress() {
        String progress = mContext.getString(R.string.txt_speech_progress);
        //音声認識中テキストを更新する。
        //更新対象の種別：音声認識中 or 音声認識エラー
        boolean updata = updateTextItems(progress, false,
                ITEM_TYPE_SPEECH_PROGRESS,
                ITEM_TYPE_SPEECH_PROGRESS,
                ITEM_TYPE_SPEECH_ERROR);

        //更新対象がなかった場合は新規に追加する
        if (!updata) {
            setTextItem(progress, LIST_TYPE_SYSTEM, ITEM_TYPE_SPEECH_PROGRESS, false, false, false);
        }
    }

    /**
     * 音声認識結果を表示する。
     * @param text 音声認識結果
     */
    public void setSpeechResult(String text) {
        removeTextItem(ITEM_TYPE_SPEECH_PROGRESS);
        setTextItem(text, LIST_TYPE_USER, ITEM_TYPE_SPEECH_RESULT, false, true, false);
    }

    /**
     * 音声入力がない場合のエラーテキストを表示する。
     */
    public void setSpeechTimeOutError() {
        setSpeechRecgError(R.string.txt_error_speech_timout);
    }

    /**
     * 音声認識中テキストを削除する。
     */
    public void removeSpeechProgress() {
        removeTextItem(ITEM_TYPE_SPEECH_PROGRESS);
    }

    /**
     * 意図解釈中テキストを表示する。
     */
    public void setInterpretationProgress(String message) {
        //String message = mContext.getString(R.string.txt_interpretation_progress);
        setTextItem(message, LIST_TYPE_SYSTEM, ITEM_TYPE_INTERPRETATION_ROGRESS, false, false, false);
    }

    /**
     * 意図解釈中テキストを表示する。
     */
    public void setMedia(String message) {
        //String message = mContext.getString(R.string.txt_interpretation_progress);
        setTextItem(message, LIST_TYPE_SYSTEM, ITEM_TYPE_INTERPRETATION_ROGRESS, false, true, true);
    }

    /**
     * 意図解釈結果を表示する
     * @param text 意図解釈結果
     * @param isDialog 対話中かどうか
     */
    public void setInterpretationResultParam(String text, boolean isDialog) {
        boolean updata = updateTextItems(text, isDialog,
                ITEM_TYPE_INTERPRETATION_RESULT,
                ITEM_TYPE_INTERPRETATION_ROGRESS);

        if (!updata) {
            setTextItem(text, LIST_TYPE_SYSTEM, ITEM_TYPE_INTERPRETATION_RESULT, isDialog, true, false);
        }
    }

    /**
     * 表示テキストを全て削除する。
     */
    public void clearAll() {
        mBaseLayout.removeAllViews();
    }

    /**
     * 表示テキストビューの生成、設定を行う
     * @param text 表示するテキスト
     * @param listType 発話の種別
     * @param itemType 表示の種別
     * @param isDialog 対話中かどうか（システムの発話のみ）
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void setTextItem(String text, int listType, int itemType, boolean isDialog, boolean reply, boolean media) {
        View item;

        if(media){
            item = mInflater.inflate(R.layout.item_text3, mBaseLayout, false);
            wv = (WebView)item.findViewById(R.id.webViewMedia);

            // カスタムWebViewを設定する
            wv.setWebViewClient(new CustomWebView(text));

            //textをセット
            wv.loadUrl("file:///android_asset/movie.html");

           // wv.getSettings().setJavaScriptEnabled(true);
            WebSettings settings = wv.getSettings();
            settings.setJavaScriptEnabled(true);
            //wv.evaluateJavascript("javascript:load_movie(" + text + ")", null);
            //String script = "javascript:load_movie('%s');";
            //wv.loadUrl( String.format( script, text ) );

            RelativeLayout.LayoutParams params =
                    (RelativeLayout.LayoutParams) wv.getLayoutParams();

            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 1);
            wv.setBackgroundResource(R.drawable.bg_active);
            mBaseLayout.addView(item);
            return;
        }
        else if(!reply) {
            item = mInflater.inflate(R.layout.item_text, mBaseLayout, false);

        }

        else{
            item = mInflater.inflate(R.layout.item_text2, mBaseLayout, false);
            wv = (WebView)item.findViewById(R.id.webView);
            wv.loadUrl("file:///android_asset/action.html");
        }

        item.setTag(itemType);
        TextView textView = (TextView) item.findViewById(R.id.speech_text);
        RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) textView.getLayoutParams();

        if (listType == LIST_TYPE_USER) {
            //ユーザの発話の場合
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 1);
            textView.setBackgroundResource(R.drawable.bg_user);
        } else if (isDialog) {
            //システムの発話（対話中）の場合
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 1);
            textView.setBackgroundResource(R.drawable.bg_active);
        }
         else if (isDialog)
        {
            //システムの発話（対話中）の場合
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 1);
            textView.setBackgroundResource(R.drawable.bg_active);
        }

        else {
            //システムの発話の場合
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 1);
            textView.setBackgroundResource(R.drawable.bg_system);
        }

        textView.setText(text);
        mBaseLayout.addView(item);
    }

    /**
     * 最新の表示テキストの更新を行う。
     * 最新の表示テキストが更新対象の表示種別でない場合は、更新を行わない。
     * @param text 更新するテキスト
     * @param newItemType 更新後の表示種別
     * @param isDialog 対話かどうか
     * @param oldItemType 更新対象の表示種別(可変)
     * @return true:更新完了 false:未更新
     */
    private boolean updateTextItems(
            String text, boolean isDialog, int newItemType, Integer... oldItemType) {
        //最新の表示テキストを取得
        View view = getLatestView();
        if (view == null) {
            return false;
        }

        if (view.getTag() == null) {
            return false;
        }

        //取得したテキストが更新対象の種別であるかの判定
        int itemType = Integer.parseInt(view.getTag().toString());
        boolean machType = false;
        for (int type : oldItemType) {
            if (itemType == type) {
                machType = true;
                break;
            }
        }

        if (!machType) {
            //更新対象の種別でない場合は更新しない
            return false;
        }

        //テキストの更新
        view.setTag(newItemType);
        TextView textView = (TextView) view.findViewById(R.id.speech_text);
        textView.setText(text);
        if (isDialog) {
            textView.setBackgroundResource(R.drawable.bg_active);
        } else {
            textView.setBackgroundResource(R.drawable.bg_system);
        }
        return true;
    }

    /**
     * 最新の表示テキストの削除を行う。
     * 最新の表示テキストが削除対象の表示種別でない場合は、削除を行わない。
     * @param removeItemType 削除対象の表示種別
     */
    public void removeTextItem(int removeItemType) {
        View view = getLatestView();
        if (view == null) {
            return;
        }

        if (view.getTag() == null) {
            // タグがnullの場合何もしない
            return;
        }

        int itemType = Integer.parseInt(view.getTag().toString());
        if (itemType == removeItemType) {
            mBaseLayout.removeView(view);
        }
    }

    /**
     * 音声認識エラーテキストの表示を行う。
     * @param stringId エラーテキストのリソースID
     */
    private void setSpeechRecgError(int stringId) {
        //エラーテキストを更新する
        //更新対象の種別：音声認識中 or 音声認識エラー
        String progress = mContext.getString(stringId);
        boolean updata = updateTextItems(progress, false,
                ITEM_TYPE_SPEECH_ERROR,
                ITEM_TYPE_SPEECH_PROGRESS,
                ITEM_TYPE_SPEECH_ERROR);

        //更新されなかった場合は新規に表示
        if (!updata) {
            setTextItem(progress, LIST_TYPE_SYSTEM, ITEM_TYPE_SPEECH_ERROR, false, false, false);
        }
    }

    /**
     * 最新の表示テキストを取得する。
     * @return 最新の表示テキスト
     */
    private View getLatestView() {
        int childCount = mBaseLayout.getChildCount();
        if (childCount == 0) {
            return null;
        }
        View view = mBaseLayout.getChildAt(childCount - 1);
        return view;
    }

    /**
     * 削除すべきビューを削除
     */
    public void removeViews() {
        if (mOneTimeView != null && mBaseLayout.indexOfChild(mOneTimeView) >= 0) {
            // ベースレイアウトに削除すべきビューを含む場合削除
            mBaseLayout.removeView(mOneTimeView);
            mOneTimeView = null;
        }
    }

    /**
     * システムのメッセージを表示する。
     */
    public void setSystemMessage(String text) {
        setTextItem(text, LIST_TYPE_SYSTEM, ITEM_TYPE_SYSTEM_MSG, false, false, false);
    }

    /**
     * WebViewClientクラスを継承したカスタムWebView（内部クラス）
     *
     */
    private class CustomWebView extends WebViewClient {

        private String text;

        CustomWebView(String text){
            this.text = text;
        }

        //ページの読み込み完了
        @Override
        public void onPageFinished(WebView view, String url) {

            WebSettings settings = wv.getSettings();
            settings.setJavaScriptEnabled(true);

            // HTML内に埋め込まれている「callJavaScript()」関数を呼び出す
            //wv.loadUrl("javascript:callJavaScript()");
            //wv.evaluateJavascript("javascript:load_movie(" + text + ")", null);
            //wv.getSettings().setJavaScriptEnabled(true);
            //String script = "javascript:load_movie('%s');";
            String script = "javascript:test('%s');";
            wv.loadUrl( String.format( script, text ) );
        }
    }
}
