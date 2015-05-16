package com.fujitsu.jp.stadiumcoach;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.speech.RecognizerIntent;
import android.widget.Toast;

import java.util.List;

/**
 * 音声認識用のヘルパークラス
 */
public class SpeechRecognitionHelper {

    /**
     * 認識プロセスを実行し、認識アクティビティーの有無を確認します。
     * アクティビティーがない場合は、Google Play* に誘導して Google* 音声検索を
     * インストールします。
     * アクティビティーがある場合は、インテントを送信して実行します。
     *
     * @param callingActivity - 認識プロセスを開始するアクティビティー
     */
    public static void run(Activity callingActivity) {
        // 認識アクティビティーの有無を確認します。
        if (isSpeechRecognitionActivityPresented(callingActivity) == true) {
            // ある場合は認識プロセスを実行します。
            //startRecognition(callingActivity);
        } else {
            // ない場合は Google* 音声検索のインストール通知を表示します。
            Toast.makeText(callingActivity, "In order to activate speech recognition you must install \"Google Voice Search\"", Toast.LENGTH_LONG).show();
            // インストール処理を開始します。
            installGoogleVoiceSearch(callingActivity);
        }
    }

    /**
     * 音声認識アクティビティーの有無を確認します。
     * @param callerActivity - 確認処理を呼び出したアクティビティー
     * @戻り値 - アクティビティーがある場合は true、ない場合は false
     */
    private static boolean isSpeechRecognitionActivityPresented(Activity callerActivity) {
        try {
            // パッケージ・マネージャーのインスタンスを取得します。
            PackageManager pm = callerActivity.getPackageManager();
            // 音声認識インテントを処理可能なアクティビティーのリスト
            List activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);

            if (activities.size() != 0) {
                // リストが空でない場合、音声認識が可能です。
                return true;
            }
        } catch (Exception e) {

        }

        return false; // 音声を認識できるアクティビティーがありません。
    }


    /**
     * 音声認識要求のインテントを送信します。
     * @param callerActivity - 要求を送信したアクティビティー
     */
    private static void startRecognitionActivity(Activity callerActivity) {

        // RecognizerIntent.ACTION_RECOGNIZE_SPEECH アクションで
        // インテントを作成します。
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        // 追加のパラメーターを渡します。
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Select an application");    // ユーザーへのヒント
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        // 検索クエリーを高速に処理できるように
        // 最適化された認識モデルを設定します。
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        // 取得する結果の数
        // 最も関連性の高い最初の結果のみを選択します。

        // アクティビティーを開始し、結果を待機します。
        //ownerActivity.startActivityForResult(intent, SystemData.VOICE_RECOGNITION_REQUEST_CODE);
    }


    /**
     * Google* 音声検索をインストールするかどうか確認します。
     * ユーザーが同意した場合は、Google Play* に移動します。
     */
    private static void installGoogleVoiceSearch(final Activity ownerActivity) {

        // 音声検索をインストールするかどうか、ユーザーに
        // 確認するためのダイアログを作成します。
        Dialog dialog = new AlertDialog.Builder(ownerActivity)
                .setMessage("For recognition it's necessary to install \"Google Voice Search\"")    // ダイアログメッセージ
                .setTitle("Install Voice Search from Google Play?")
                        // ダイアログヘッダー
                .setPositiveButton("Install", new DialogInterface.OnClickListener() {    // 確認ボタン

                    // インストール・ボタンのクリックハンドラー
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            // Google Play* でアプリケーション・ページを
                            // 開くためのインテントを作成します。
                            // 音声検索パッケージ名:
                            // com.google.android.voicesearch
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.voicesearch"));
                            // アプリケーション履歴 (アクティビティーのコールスタック)
                            // に残らないようにフラグを設定します。
                            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);    // インテントを送信します。
                            ownerActivity.startActivity(intent);
                        } catch (Exception ex) {
                            // 問題が発生した場合は何もしません。
                        }
                    }})

                .setNegativeButton("Cancel", null)    // キャンセルボタン
                .create();

        dialog.show();    // ダイアログを表示します。
    }



}// End of class