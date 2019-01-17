
package com.hxdesign.smartnote;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import com.hxdesign.smartnote.adapter.TableAdapter;
import com.hxdesign.smartnote.bean.TableItem;
import com.hxdesign.smartnote.database.UserDBHelper;
import com.hxdesign.smartnote.TableDetailActivity;
import com.hxdesign.smartnote.dialog.AddTheme;
import com.hxdesign.smartnote.dialog.DeleteTheme;
import com.hxdesign.smartnote.util.ExportTheme;
import com.hxdesign.smartnote.widget.RecyclerExtras.OnItemClickListener;

import java.io.File;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements OnClickListener,
        OnItemClickListener{

    private Button btn_add;
    private Button btn_del;
    private Button btn_exp;
    private ListView lv_table;
    private GridView gv_table;
    private ArrayList<TableItem> tableItemList;
    private Drawable drawable;
    private int dividerHeight = 2;
    private UserDBHelper mHelper;
    private String mTableName = "";
    private String mDesc = "";

    private TableAdapter mTabAdapter;
    //消息ID
    private static final int ADDTHEME_REQUESTCODE = 1;//添加
    private static final int ADDTHEME_RESULTCODE = 1;
    private static final int DELETHEME_REQUESTCODE = 2;//删除
    private static final int DELETHEME_RESULTSTCODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //lv_table = (ListView) findViewById(R.id.lv_table);
        gv_table = (GridView) findViewById(R.id.gv_table);
        btn_add = (Button) findViewById(R.id.btn_addTheme);
        btn_del = (Button) findViewById(R.id.btn_delete);
        btn_exp = (Button) findViewById(R.id.btn_export);
        drawable = getResources().getDrawable(R.drawable.line_h);
        btn_add.setOnClickListener(this);
        btn_del.setOnClickListener(this);
        btn_exp.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_addTheme:  //
                addTheme();
                break;
            case R.id.btn_delete:
                deleteTheme();
                break;
            case R.id.btn_export:
                exportTheme();
                break;
        }
    }

    @Override
    public void onItemClick(View view, int position) {
//        String desc = String.format("您点击了第%d项，标题是%s", position + 1,
//                tableItemList.get(position).name);
        //Toast.makeText(this, desc, Toast.LENGTH_SHORT).show();
        TableItem aTable = tableItemList.get(position);
        Intent intent = new Intent(this, TableDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("name", aTable.name);
        bundle.putString("desc", aTable.desc);
        intent.putExtras(bundle);
        startActivity(intent);
    }
    //刷新
    public void refreshTab(){
        readSQLite();
    }

    //创建新表
    public void createNewTab(){
        if(mTableName.length() > 0 && mDesc.length() > 0){
            //mHelper.dynamicCreateTable(mTableName, mDesc);
        }
    }

    public void addTheme(){
        Intent intent = new Intent(this, AddTheme.class);
        startActivityForResult(intent, ADDTHEME_RESULTCODE);
    }

    public void deleteTheme(){
        Intent intent = new Intent(this, DeleteTheme.class);
        startActivityForResult(intent, DELETHEME_RESULTSTCODE);
    }

    public void exportTheme(){
        //申请SD卡读写权限
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{android
                .Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
    }

    //得到读写SD卡权限回调
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //创建文件夹
                    String path = Environment.getExternalStorageDirectory()+"/hxdesign";
                    File file = new File(path);
                    if(!file.exists()){
                        Boolean flag = file.mkdirs();
                    }
                    ExportTheme.exportToExcel(tableItemList, mHelper);
                    Toast.makeText(this, "完成导出，根目录->hxdesign查看", Toast.LENGTH_LONG).show();
                    break;
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode){
            case ADDTHEME_REQUESTCODE:
                if(resultCode == ADDTHEME_RESULTCODE){//添加
                    if(data != null){
                        String strTheme = (String) data.getSerializableExtra("theme");
                        String strDesc = (String) data.getSerializableExtra("desc");
                        if(strTheme.length() == 0) return;
                        //writeTabToSQLite((String) data.getSerializableExtra("theme"));
                        writeTabToSQLite(strTheme, strDesc);
                        refreshTab();
                        createNewTab();
                    }else{
                        showToast("空信息添加");
                    }
                }
                break;
            case DELETHEME_REQUESTCODE:
                if(resultCode == DELETHEME_RESULTSTCODE){
                    if(data != null){
                        deleteTabFromSQLite((String) data.getSerializableExtra("theme"));
                        refreshTab();
                    }else{
                        showToast("没有主题删除");
                    }
                }
                break;
        }
    }

    private void putIntoTabAdapter(ArrayList<TableItem> list){
        tableItemList = list;
        //TableAdapter adapter = new TableAdapter(this, R.layout.item_table, tableItemList, Color.WHITE);
        mTabAdapter = new TableAdapter(this, R.layout.item_table, tableItemList, Color.WHITE);
        mTabAdapter.setOnItemClickListener(this);

//        lv_table.setAdapter(mTabAdapter);
//        lv_table.setDivider(drawable);
//        lv_table.setDividerHeight(dividerHeight);
//        lv_table.setPadding(0, 0, 0, 0);
//        lv_table.setBackgroundColor(Color.TRANSPARENT);
//        lv_table.setPadding(0, dividerHeight, 0, dividerHeight);

        gv_table.setAdapter(mTabAdapter);
        //lv_table.setPadding(2, 2, 2, 2);
    }

    //写tab名字到数据库
    private void writeTabToSQLite(String tabname, String desc) {
        //查重
        if(mHelper.queryByname(tabname, "tabInfo") == null){
            TableItem tab = new TableItem(tabname, desc);
            mHelper.insert(tab, "tabInfo");
            //新建桌子 tab+id;
            mTableName = "tab" + mHelper.queryByname(tabname, "tabInfo").dbNum;
            mDesc = desc;
            //需要升级数据库来建立一个新表
            //mHelper.setNewTableInfo(mTableName, desc);
            mHelper.dynamicCreateTable(mTableName, desc);
        }else{
            showToast("已经存在的主题");
        }
    }

    //删除一个tab
    private void deleteTabFromSQLite(String str){
        String condition = "name = " + "\"" + str + "\"";
        int count = mHelper.delete(condition, "tabInfo");
        if(count < 1){
            showToast("找不到要删除的主题");
        }
    }

    //删除所有
    private  void deleteAllTab(){
        mHelper.deleteAll("tabInfo");
    }


    private void readSQLite() {
        if (mHelper == null) {
            showToast("数据库连接为空");
            return;
        }
        ArrayList<TableItem> userArray = mHelper.query_tab("1=1", "tabInfo");
//        ArrayList<String> tabIndexList = new ArrayList<String>();
//        ArrayList<String> tabList = new ArrayList<String>();
//        ArrayList<String> tabdescList = new ArrayList<String>();
//        for (int i=0; i<userArray.size(); i++) {
//            TableItem info = userArray.get(i);
//            //把tables放到容器里
//            tabIndexList.add(info.num);
//            tabList.add(info.name);
//            tabdescList.add(info.desc);
//        }
        putIntoTabAdapter(userArray);
        if (userArray==null || userArray.size()<=0) {
            showToast("数据库查询到的记录为空");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mHelper = UserDBHelper.getInstance(this, 2);
        mHelper.openReadLink();
        readSQLite();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mHelper.closeLink();
    }

    private void showToast(String desc) {
        Toast.makeText(this, desc, Toast.LENGTH_SHORT).show();
    }

}

