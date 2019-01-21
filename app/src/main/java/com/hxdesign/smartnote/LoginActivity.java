package com.hxdesign.smartnote;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.biometrics.BiometricPrompt;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.hxdesign.smartnote.Fingerprint.FingerprintCore;
import com.hxdesign.smartnote.Fingerprint.FingerprintUtil;
import com.hxdesign.smartnote.dialog.KeyIn;
import com.hxdesign.smartnote.help.SmartNoteHelp;

import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class LoginActivity extends AppCompatActivity implements OnClickListener{
    private SharedPreferences mShared;
    private ArrayList<Long> keys;
    private TextView tv_top;
    private TextView tv_center;
    private TextView tv_bottom;
    private int count = 0;
    private int keyLength = 7;
    private ArrayList<Long> keyBeat;
    private ArrayList<Long> timePoint;
    private ArrayList<Long> timeBetween;
    private int inputType = 0;//0输入密码，1修改密码验证，2输入新密码，3输入新密码验证
    private String password = "";

    private FingerprintCore mFingerprintCore;
    //消息ID
    private static final int KEYIN_REQUESTCODE = 1;//添加
    private static final int KEYIN_RESULTCODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        tv_top = (TextView) findViewById(R.id.tv_top);
        tv_bottom = (TextView) findViewById(R.id.tv_bottom);
        tv_center = (TextView) findViewById(R.id.tv_center);

        initDate();
        readSharedPreferences();

        tv_center.setOnClickListener(this);
        tv_bottom.setOnClickListener(this);
        findViewById(R.id.tv_key_in).setOnClickListener(this);
        findViewById(R.id.tv_back).setOnClickListener(this);
        findViewById(R.id.tv_help).setOnClickListener(this);

        //设置初始密码
        initPassword();

        //初始化指纹设备
        initFingerprintCore();


        //startFingerprintCore();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                startFingerprintCore();
            }
        }, 500);//0.5秒后执行TimeTask的run方法
    }

    private void initFingerprintCore() {
        mFingerprintCore = new FingerprintCore(this);
        mFingerprintCore.setFingerprintManager(mResultListener);

    }

    private void startFingerprintCore(){
        //开始识别
        if (mFingerprintCore.isSupport()) {
            if (!mFingerprintCore.isHasEnrolledFingerprints()) {
                FingerprintUtil.openFingerPrintSettingPage(this);
                return;
            }
            if (mFingerprintCore.isAuthenticating()) {
            } else {
                mFingerprintCore.startAuthenticate();
            }
        } else {
        }
    }

    private FingerprintCore.IFingerprintResultListener mResultListener = new FingerprintCore.IFingerprintResultListener() {
        @Override
        public void onAuthenticateSuccess() {
            Toast.makeText(LoginActivity.this, "验证成功", Toast.LENGTH_SHORT).show();
            login();
            mFingerprintCore.onDestroy();
            finish();
        }

        @Override
        public void onAuthenticateFailed(int helpId) {

        }

        @Override
        public void onAuthenticateError(int errMsgId) {

        }

        @Override
        public void onStartAuthenticateResult(boolean isSuccess) {

        }
    };

    private void initDate() {
        keys = new ArrayList<Long>();
        timePoint = new ArrayList<Long>();
        timeBetween = new ArrayList<Long>();
        keyBeat = new ArrayList<Long>();
        long defalultKeys[] = {677,274,477,496,480,471};
        for(int i = 0; i < 6; i++){
            keyBeat.add(defalultKeys[i]);
        }
        count = 0;
        inputType = 0;
    }

    private void login(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void inputKey(String flag){
        Intent intent = new Intent(this, KeyIn.class);
        if(flag.equals("true")){
            intent.putExtra("newUser", "true");
        }else{
            intent.putExtra("newUser", "false");
        }
        startActivityForResult(intent, KEYIN_RESULTCODE);
    }

    public void openHelp(){
        Intent intent = new Intent(this, SmartNoteHelp.class);
        startActivity(intent);
    }

    public void checkKey(String str){
        if(password.length() == 0 && str.length() >= 0 && inputType == 0){
            //设置初始密码
            SharedPreferences.Editor editor = mShared.edit();
            editor.putString("passwords", str);
            editor.commit();
            Toast.makeText(LoginActivity.this, "密码设置成功，请牢记", Toast.LENGTH_SHORT).show();
            password = str;
            openHelp();
        }else if(inputType == 1){
            if(password.equals(str)){
                inputType = 2;
                timePoint.clear();
                timeBetween.clear();
                tv_center.setText("请输入新的节拍");
                tv_bottom.setText("确认");
                keyLength = 9999;
                count = 0;
            }else {
                inputType = 0;
                tv_center.setText("密码不正确，点击重来");
                timePoint.clear();
                timeBetween.clear();
                count = 0;
            }
        }else{
            if (password.equals(str))
                login();
            finish();
        }
    }

    public void initPassword(){
        if(password.length() == 0){
            inputKey("true");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case KEYIN_REQUESTCODE:
                if (resultCode == KEYIN_RESULTCODE) {
                    if (data != null) {
                        String inputkey = (String) data.getSerializableExtra("keyin");
                        checkKey(inputkey);
                    }
                } else{
                    finish();
                }
                break;
        }
    }

    private void readSharedPreferences() {
        mShared = getSharedPreferences("beat", MODE_PRIVATE);
        Map<String, Object> mapParam = (Map<String, Object>) mShared.getAll();
        int count = 0;
        for (Map.Entry<String, Object> item_map : mapParam.entrySet()) {
            String key = item_map.getKey();
            Object value = item_map.getValue();

            if(value instanceof Long && key.length() >= 7 && key.substring(0,7).equals("keyBeat")) {
                //这里需要次序
                for (Map.Entry<String, Object> item_map1 : mapParam.entrySet()) {
                    String key1 = item_map1.getKey();
                    Object value1 = item_map1.getValue();
                    if(value1 instanceof Long && key1.equals("keyBeat"+String.valueOf(count))){
                        count++;
                        keys.add(mShared.getLong(key1, 0l));
                    }
                }

            }

            if(value instanceof String && key.equals("passwords")){
                password = mShared.getString(key, "");
            }
        }
        if (keys.size()<=0) {
            //初始化一个默认密码
            for(int i = 0; i < keyBeat.size(); i++){
                Long num = keyBeat.get(i);
                keys.add(num);
            }
            keyLength = 7;
            tv_top.setText("唱好“世上只有妈妈好“体验节拍解锁功能");
        }else{
            //keyLength = keys.size()+1;
            keyLength = mShared.getInt("length",0);
            String txt = String.format("唱出你的歌(共%d拍)", keyLength);
            tv_top.setText(txt);
        }
    }

    private void writeSharedPreferences(){
        SharedPreferences.Editor editor = mShared.edit();
        for(int i = 0; i < keyBeat.size(); i++){
            editor.putLong("keyBeat" + String.valueOf(i), keyBeat.get(i));
        }
        editor.putInt("length", keyLength);
        editor.commit();
        tv_center.setText("修改成功");
    }

    public boolean checkAndEnter (int len){
        for(int i = 0; i < len; i++){
            if(timeBetween.get(i)/1.3 < keys.get(i) && timeBetween.get(i)/0.7 > keys.get(i)){
                continue;
            }else{
                return false;
            }
        }
        return true;
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.tv_top:
                break;
            case R.id.tv_center:
                inputBeat();
                checkBeat();
                break;
            case R.id.tv_bottom:
                setBeat();
                break;
            case R.id.tv_key_in:
                inputKey("false");
                break;
            case R.id.tv_help:
                openHelp();
                break;
            case R.id.tv_back:
                finish();
                break;
        }
    }

    public void inputBeat(){
        long timeValue = System.currentTimeMillis();
        timePoint.add(timeValue);
        count++;
        if(count > 1){
            Long between = timePoint.get(count - 1) - timePoint.get(count - 2);
            timeBetween.add(between);
            //打印显示
            String disText = String.format("%s\n%s%s", tv_center.getText().toString(), "时长间隔(ms):", String.valueOf(between));
            tv_center.setText(disText);
        }
    }
    public void checkBeat(){
        switch (inputType){
            case 0: //输入密码
                if(count == keyLength){
                    Boolean flag = checkAndEnter(keyLength - 1);
                    if(flag){
                        login();
                    }else {
                        timePoint.clear();
                        timeBetween.clear();
                        tv_center.setText("密码不正确，请屏住呼气再试一次");
                        count = 0;
                    }
                }
                break;
            case 1: //修改密码
                if(count == keyLength){
                    Boolean flag = checkAndEnter(keyLength - 1);

                }
                break;
            case 2: //输入新密码
                if(keyLength == count){
                    Boolean flag = checkAndEnter(keyLength - 1);
                    if(flag){
                        Toast.makeText(LoginActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                        writeSharedPreferences();
                        tv_center.setText("点击中央解锁");
                        tv_bottom.setText("修改密钥");
                        inputType = 0;
                        count = 0;
                    }else {
                        tv_center.setText("密码不正确，请再次输入");
                        timePoint.clear();
                        timeBetween.clear();
                        count = 0;
                    }
                }
                break;
        }
    }
    public void setBeat (){
        switch (inputType){
            case 0:
                inputType = 1;
                timePoint.clear();
                timeBetween.clear();
                count = 0;
                inputKey("false");
                break;
            case 1:
                break;
            case 2:
                keyLength = timePoint.size();
                keyBeat.clear();
                keys.clear();
                if(keyLength == 0) {
                    keyLength = 9999;
                    break;
                }
                for(int i = 0; i < keyLength - 1; i++){
                    keyBeat.add(timeBetween.get(i));
                    keys.add(timeBetween.get(i));
                }
                inputType = 2;
                tv_center.setText("请再次输入新节拍");
                timePoint.clear();
                timeBetween.clear();
                count = 0;
                break;
        }
    }
}

