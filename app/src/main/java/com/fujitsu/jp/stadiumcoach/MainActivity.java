package com.fujitsu.jp.stadiumcoach;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;
import android.view.WindowManager.LayoutParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainActivity extends ActionBarActivity implements TextToSpeech.OnInitListener {

    // = 0 の部分は、適当な値に変更してください（とりあえず試すには問題ないですが）
    private static final int REQUEST_CODE = 0;

    private TextToSpeech tts;
    private Context context;

    private ProgressDialog progressBar;

    private ActionHandler act;

    private MyAsyncTask task;
    private String res = null;


    //プロジェクトリスト
    private CharSequence[] items;
    private  ArrayList<String> list = new ArrayList<String>();
    private ArrayList<String> pjId = new ArrayList<String>();
    AlertDialog dialog;


    private ListItemManager sListItemManager;   // 会話表示View管理クラスオブジェクト
    private ScrollView sScrollView;             // 会話表示のスクロールビューオブジェクト
    private LinearLayout mBaseLayout;           // 会話表示のベースレイアウトオブジェクト

    private  WebView wv;

    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {
            //String ready = "準備OKです";
            //tts.speak(ready, TextToSpeech.QUEUE_FLUSH, null);
            //Toast.makeText(this, ready, Toast.LENGTH_SHORT).show();

            //ロードするプロジェクト一覧の表示
            loadProjectList();

        } else {

        }
    }

    private void loadProjectList(){
        // サブスレッドで実行するタスクを作成
        task = new MyAsyncTask() {
            @Override
            protected String doInBackground(String... params) {
                String resultsString = params[0];
                try {
                    // Twitter フォロー実行
                    SendHttpRequest http = new SendHttpRequest();
                    String json_org = http.getProjectList();

                    res = json_org;//インスタンス変数にＪＳＯＮ(命令セット)をセット

                    return json_org;
                } catch (Exception e) {
                    e.printStackTrace();
                    //Toast.makeText(this.getActivity(), "Network Busy!", Toast.LENGTH_SHORT).show();

                }
                return null;
            }

            @Override
            protected void onPostExecute(String json_org) {
                try {
                    JSONArray jsons = new JSONArray(json_org);

                    for (int i = 0; i < jsons.length(); i++) {
                        // 情報を取得
                        JSONObject event = jsons.getJSONObject(i);

                        // pjname
                        String pjname = event.getString("pjname");
                        String id = event.getString("id");

                        list.add(pjname);
                        pjId.add(id);
                    }

                    //リストの表示
                    items = list.toArray(new CharSequence[list.size()]);
                    dialog = new AlertDialog.Builder(MainActivity.this)
                            .setTitle("コーチを選んでね")
                            .setSingleChoiceItems(
                                    items,
                                    0, // Initial
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which){
                                            Toast.makeText(MainActivity.this, list.get(which)+"を起動します", Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                            //ロボナイゼーヨンイニシャライズ
                                            //Robotプログラムスタート
                                            initRobot( pjId.get(which) );
                                        }
                                    })
                            .setPositiveButton("Close", null)
                            .show();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Network Busy!", Toast.LENGTH_SHORT).show();
                    return;
                }



                //ぐるぐる
                progressBar.dismiss();//消去
            }
        };

        task.execute("");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //Contextの取得
        context = getApplicationContext();

        //ボタンの押した動作
        ImageButton button = (ImageButton) findViewById(R.id.talk);

        //テストの押した動作
        Button send = (Button) findViewById(R.id.send);

        //TTSの初期化
        tts = new TextToSpeech(context, this);

        //トグルボタン（顔認識のON/OFF）
        //ToggleButton tglbtn = (ToggleButton) findViewById(R.id.toggleButton);

        // 会話表示エリア
        mBaseLayout = (LinearLayout) findViewById(R.id.conersation_base);
        sScrollView = (ScrollView) findViewById(R.id.scrollView);
        //sCommandTv = (TextView) findViewById(R.id.textView1);
        // 会話表示View管理クラス
        sListItemManager = new ListItemManager(this, mBaseLayout);


        //ぐるぐる
        progressBar = new ProgressDialog(this);
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setMessage("処理を実行中しています");
        progressBar.setCancelable(true);
        progressBar.show();


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    // インテント作成
                    Intent intent = new Intent(
                            RecognizerIntent.ACTION_RECOGNIZE_SPEECH); // ACTION_WEB_SEARCH
                    intent.putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    intent.putExtra(
                            RecognizerIntent.EXTRA_PROMPT,
                            "Let's say!"); // お好きな文字に変更できます

                    intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);// 取得する結果の数

                    // インテント発行
                    startActivityForResult(intent, REQUEST_CODE);
                } catch (ActivityNotFoundException e) {
                    // このインテントに応答できるアクティビティがインストールされていない場合
                    Toast.makeText(MainActivity.this,
                            "ActivityNotFoundException", Toast.LENGTH_LONG).show();
                }
            }
        });

        //ボタン用
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//テストの押した動作
                EditText txt = (EditText) findViewById(R.id.editText);
                SpannableStringBuilder sb = (SpannableStringBuilder)txt.getText();
                String str = sb.toString();

                if(str.length() == 0){
                    return;
                }
                else {
                    txt.setText("");
                    executeRobot(str);
                }
            }
        });

    }


    // アクティビティ終了時に呼び出される
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 自分が投げたインテントであれば応答する
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            String resultsString = "";

            // 結果文字列リスト
            ArrayList<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);

            /*for (int i = 0; i< results.size(); i++) {
                // ここでは、文字列が複数あった場合に結合しています
                resultsString += results.get(i);
            }*/
            resultsString = results.get(0);

            // トーストを使って結果を表示
            Toast.makeText(this, resultsString, Toast.LENGTH_LONG).show();


            //会話から実行
            executeRobot( resultsString );


            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    private void initRobot ( String projectName ){
        // サブスレッドで実行するタスクを作成
        task = new MyAsyncTask() {
            @Override
            protected String doInBackground(String... params) {
                String resultsString = params[0];
                try {
                    // Twitter フォロー実行
                    SendHttpRequest http = new SendHttpRequest();
                    String json_org = http.sendRequestToGarako( resultsString );

                    res = json_org;//インスタンス変数にＪＳＯＮ(命令セット)をセット

                    this.setParam(resultsString);

                    progressBar.dismiss();//消去

                    return json_org;
                } catch (Exception e) {
                    e.printStackTrace();
                    //Toast.makeText(this.getActivity(), "Network Busy!", Toast.LENGTH_SHORT).show();

                }
                return null;
            }

            @Override
            protected void onPostExecute(String json_org) {
                String ready = "コーチの準備ができました";
                tts.speak(ready, TextToSpeech.QUEUE_FLUSH, null);
                Toast.makeText(this.getActivity(), ready, Toast.LENGTH_SHORT).show();

            }
        };

        try {

            //アクションハンドラの生成
            act = new ActionHandler(this);

            task.setActivity(this);
            task.setTts(this.tts);
            act.setContext(context);
            act.setsListItemManager(sListItemManager);
            act.setsScrollView(sScrollView);

            //非同期処理開始
            task.execute( projectName );
        }catch( Exception e){
            e.printStackTrace();
        }

    }


    /**
     * executeRobot
     */
    private void executeRobot( String resultsString ){

        act.startDialogue(resultsString);

        //表示
        progressBar.show();

        // サブスレッドで実行するタスクを作成
        task = new MyAsyncTask() {
            @Override
            protected String doInBackground(String... params) {
                String resultsString = params[0];
                try {

                    if(res == null) {

                        // Twitter フォロー実行
                        SendHttpRequest http = new SendHttpRequest();
                        String json_org = http.sendRequestToGarako(resultsString);
                        res = json_org;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    //Toast.makeText(this.getActivity(), "Network Busy!", Toast.LENGTH_SHORT).show();

                }
                progressBar.dismiss();//消去
                this.setParam(resultsString);
                return res;
            }

            @Override
            protected void onPostExecute(String json_org) {

                // トーストを使って結果を表示
                //Toast.makeText(this.getActivity(), json_org, Toast.LENGTH_SHORT).show();

                //WebView webView = (WebView) findViewById(R.id.webView);
                //webView.loadUrl(url);
                //webView.loadData(data, "text/html", null);
                //webView.loadDataWithBaseURL(null, json_org, "text/html", "UTF-8", null);

                String resultsString = this.getParam();


                act.setTts(this.getTts());
                //act.setContext(context);

                //----------------------------------
                //-- JSONの振り分け処理
                //----------------------------------

                act.analyzeJson(resultsString, json_org);

            }
        };

        task.setActivity(this);
        task.setTts( this.tts);
        //アクションハンドラの生成
        //act = new ActionHandler( this );
        act.setContext(context);



        task.execute( resultsString );
     }


//--------------------------------------------------------------------------------
//--- Camera -----------------------------------------------------------------------------
//--------------------------------------------------------------------------------

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        //コールバック関数をセット
        //SurfaceHolder holder = mView.getHolder();
       // holder.addCallback(surfaceHolderCallback);
       // holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    /** カメラのコールバックです。 */
 /*   private SurfaceHolder.Callback surfaceHolderCallback = new SurfaceHolder.Callback() {

        @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        @Override
        public void surfaceCreated(SurfaceHolder holder) {

            try {
                // 生成されたとき
                //mCamera = Camera.open(1);

                // リスナをセット  // 顔検出の開始
                //mCamera.setFaceDetectionListener(faceDetectionListener);

                //mCamera.stopFaceDetection();

                // プレビューをセットする
                //mCamera.setPreviewDisplay(holder);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
            // 変更されたとき
            Camera.Parameters parameters = mCamera.getParameters();
            List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
            Camera.Size previewSize = previewSizes.get(0);
            //parameters.setPreviewSize(previewSize.width, previewSize.height);
            parameters.setPreviewSize(640, 480);
            // width, heightを変更する
            mCamera.setParameters(parameters);
            mCamera.startPreview();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // 破棄されたとき
            mCamera.release();
            mCamera = null;
        }

    };
*/

//--------------------------------------------------------------------------------
//--------------------------------------------------------------------------------
//--------------------------------------------------------------------------------
@Override
protected void onResume() {
    super.onResume();

    if (mBaseLayout.getChildCount() == 0) {
        // 初回表示のみ
        // 初回表示が遅れる対応
        return;
    }

    // 画面下までスクロール
    act.scrollToBottom();
}


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tts.shutdown();
        // 音声認識サービスの終了
        sListItemManager = null;
        //mCamera.release();
    }

    @Override
    public void onUserLeaveHint(){
        //ホームボタンが押された時や、他のアプリが起動した時に呼ばれる
        //戻るボタンが押された場合には呼ばれない
        //Toast.makeText(getApplicationContext(), "Pause!" , Toast.LENGTH_SHORT).show();
       // this.finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode){
            case KeyEvent.KEYCODE_BACK:
                //戻るボタンが押された時の処理。
                Toast.makeText(this, "Good bye!" , Toast.LENGTH_SHORT).show();
                finish();
                return true;
        }
        return false;
    }
}
