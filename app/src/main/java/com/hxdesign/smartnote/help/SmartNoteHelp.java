package com.hxdesign.smartnote.help;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.hxdesign.smartnote.R;

public class SmartNoteHelp extends AppCompatActivity implements OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        TextView txt_help = (TextView) findViewById(R.id.tv_help);
        txt_help.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_help:  //复制链接
                ClipboardManager myClipboard = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                String text = "https://pan.baidu.com/s/1TUg-zpNou2SHu4kiDu4PFw";
                ClipData myClip = ClipData.newPlainText("text", text);
                myClipboard.setPrimaryClip(myClip);
                Toast.makeText(getApplicationContext(), "链接已复制到剪切板",Toast.LENGTH_LONG).show();
                finish();
                break;
        }
    }
}
