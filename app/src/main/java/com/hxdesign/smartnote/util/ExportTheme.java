package com.hxdesign.smartnote.util;

import android.os.Environment;
import android.widget.Toast;

import com.hxdesign.smartnote.bean.TableItem;
import com.hxdesign.smartnote.database.UserDBHelper;

import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class ExportTheme {
    private UserDBHelper mHelper;

    public static Boolean exportToExcel(ArrayList<TableItem> themes, UserDBHelper Helper){

        for(int i = 0; i < themes.size(); i++){
            ArrayList<String> titleDesc = new ArrayList<String>();
            ArrayList<String> titledatas = new ArrayList<String>();
            ArrayList<String> datas = new ArrayList<String>();
            titleDesc = JsonDataTtransfer.anaylse(themes.get(i).desc);
            String tableName = themes.get(i).name;
            String fileName = tableName + ".xls";
            for(int n = 0; n < titleDesc.size(); n = n+2){
                titledatas.add(titleDesc.get(n));
            }

            String TabRealName = "tab" + Helper.queryByname(tableName, "tabInfo").dbNum;
            datas = Helper.queryTabByDesc("1=1", TabRealName, titledatas.size());
            //操作excel的对象
            WritableWorkbook wwb = null;
            //创建文件路径
            String path = Environment.getExternalStorageDirectory()+"/hxdesign";
            try {
                //根据当前的文件路径创建统计的文件并且实例化出一个操作excel的对象
                wwb = Workbook.createWorkbook(new File(path+"/"+fileName));
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (wwb != null ){
                //创建底部的选项卡  传参是选项卡的名称  和  选型卡的索引
                WritableSheet writableSheet = wwb.createSheet(fileName,0);
                //创建excel的表头的信息
                //写一个时间
                Label labelC0 = new Label(0,0,"时间");
                try {
                    writableSheet.addCell(labelC0);
                } catch (WriteException e) {
                    e.printStackTrace();
                }
                for(int index = 0; index < titledatas.size(); index++){
                    //横向的在单元格中填写数据
                    Label labelC = new Label(index+1,0,titledatas.get(index));
                    try {
                        writableSheet.addCell(labelC);
                    } catch (WriteException e) {
                        e.printStackTrace();
                    }
                }
                //从实体中遍历数据并将数据写入excel文件中
                ArrayList<String> li;
                //_id,time,item0,item1,item2.....
                for ( int j = 0, count = 0; j < datas.size() ; j+=titledatas.size()+2, count++ ){
                    //将数据源列表中的数据整合成 一个个的字符串列表

                    li = new ArrayList<>();
                    li.add(datas.get(j+1));
                    for(int len = 0; len < titledatas.size(); len++){
                        li.add(datas.get(j+2+len));
                    }
                    int k = 0;
                    for (String l:li){
                        //将单个的字符串列表横向的填入到excel表中
                        Label labelC = new Label(k,count+1,l);
                        k++;
                        try {
                            writableSheet.addCell(labelC);
                        } catch (WriteException e) {
                            e.printStackTrace();
                        }
                    }
                    li = null;
                }
            }
            //将文件从内存写入到文件当中
            try {
                wwb.write();
                wwb.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (WriteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
