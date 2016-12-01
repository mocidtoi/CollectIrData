package com.thanhnv.collectirdata;

import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by thanhnv on 05/10/2016.
 */
public class DBManager{
    private static final String PATH_DB = Environment.getExternalStorageDirectory().getPath()+
                                File.separator + "Thanhnv" + File.separator + "Database";
    private static final String DB_NAME = "IR_data_db.sqlite";
    private static final String ASSETS_FILE_NAME = "IR_data_db";
    private static final String TAG = DBManager.class.getSimpleName();

    private SQLiteDatabase mSQLiteDB;


    // copy file db when start app
    private void copyDB(Context context){
        new File(PATH_DB).mkdirs();
        File dbFile = new File(PATH_DB + File.separator + DB_NAME);
        if(dbFile.exists()){
            return;
        }

        try {
            Log.d(TAG, dbFile.getPath());
            dbFile.createNewFile();

            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open(ASSETS_FILE_NAME);
            FileOutputStream outputStream = new FileOutputStream(dbFile);

            byte[] b = new byte[1024];
            int lenght = inputStream.read(b);
            while (lenght > 0){
                outputStream.write(b, 0, lenght);
                lenght = inputStream.read(b);
            }

            inputStream.close();
            outputStream.close();
            Log.d(TAG, "DB is copied");

        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    // contructor
    public DBManager(Context context){
        copyDB(context);
    }
    // open DB if you want to use db
    private void openDB(){
        if ( mSQLiteDB == null || !mSQLiteDB.isOpen()) {
            mSQLiteDB = SQLiteDatabase.openDatabase(PATH_DB + File.separator + DB_NAME, null, SQLiteDatabase.OPEN_READWRITE);
        }
    }
    // close DB when you have finished to work with db
    public void closeDB(){
        if ( mSQLiteDB != null && mSQLiteDB.isOpen() ) {
            mSQLiteDB.close();
            mSQLiteDB = null;
        }
    }
    // insert data
    public boolean insertData(String deviceType, String model, String name, String irData, String label, String icon){
        openDB();
        ContentValues content = new ContentValues();

        content.put("device_type", deviceType);
        content.put("model", model);
        content.put("name", name);
        content.put("ir_data", irData);
        content.put("label", label);
        content.put("icon", icon);

        int result = (int)mSQLiteDB.insert("ir_data", null, content);
        closeDB();
        return result > -1;
    }
    // delete data
    public boolean deleteData(int id){
        openDB();
        boolean result;
        if (mSQLiteDB.delete("ir_data", "id" + "=" + id, null) > 0){
            result = true;
        } else {
            result = false;
        }

        return result;
    }



    // get info data
    public List<ItemIrData> getDataList(){
        openDB();
        List<ItemIrData> itemIrDataList = new ArrayList<ItemIrData>();

        String sql = "select * from ir_data;";

        Cursor cursor = mSQLiteDB.rawQuery(sql, null);

        if (cursor!=null){
            cursor.moveToFirst();

            int idIndex = cursor.getColumnIndex("id");
            int deviceTypeIndex = cursor.getColumnIndex("device_type");
            int modelIndex = cursor.getColumnIndex("model");
            int nameIndex = cursor.getColumnIndex("name");
            int irDataIndex = cursor.getColumnIndex("ir_data");
            int labelIndex = cursor.getColumnIndex("label");
            int iconIndex = cursor.getColumnIndex("icon");

            while (cursor.isAfterLast() == false){
                String id = cursor.getString(idIndex);
                String deviceType = cursor.getString(deviceTypeIndex);
                String model = cursor.getString(modelIndex);
                String name = cursor.getString(nameIndex);
                String irData = cursor.getString(irDataIndex);
                String label = cursor.getString(labelIndex);
                String icon = cursor.getString(iconIndex);

                ItemIrData itemIrData = new ItemIrData(Integer.parseInt(id), deviceType, model, name, irData,
                        label, icon);

                itemIrDataList.add(itemIrData);
                cursor.moveToNext();
            }
        }
        cursor.close();
        closeDB();
        return itemIrDataList;
    }

    public List<ItemIrData> sortByDeviceType() {
        openDB();

        openDB();
        List<ItemIrData> itemIrDataList = new ArrayList<ItemIrData>();

        String sql = "select * from ir_data order by device_type asc;";

        Cursor cursor = mSQLiteDB.rawQuery(sql, null);

        if (cursor!=null){
            cursor.moveToFirst();

            int idIndex = cursor.getColumnIndex("id");
            int deviceTypeIndex = cursor.getColumnIndex("device_type");
            int modelIndex = cursor.getColumnIndex("model");
            int nameIndex = cursor.getColumnIndex("name");
            int irDataIndex = cursor.getColumnIndex("ir_data");
            int labelIndex = cursor.getColumnIndex("label");
            int iconIndex = cursor.getColumnIndex("icon");

            while (cursor.isAfterLast() == false){
                String id = cursor.getString(idIndex);
                String deviceType = cursor.getString(deviceTypeIndex);
                String model = cursor.getString(modelIndex);
                String name = cursor.getString(nameIndex);
                String irData = cursor.getString(irDataIndex);
                String label = cursor.getString(labelIndex);
                String icon = cursor.getString(iconIndex);

                ItemIrData itemIrData = new ItemIrData(Integer.parseInt(id), deviceType, model, name, irData,
                        label, icon);

                itemIrDataList.add(itemIrData);
                cursor.moveToNext();
            }
        }
        cursor.close();
        closeDB();
        return itemIrDataList;
    }

    public List<ItemIrData> sortByName() {
        openDB();

        openDB();
        List<ItemIrData> itemIrDataList = new ArrayList<ItemIrData>();

        String sql = "select * from ir_data order by name asc;";

        Cursor cursor = mSQLiteDB.rawQuery(sql, null);

        if (cursor!=null){
            cursor.moveToFirst();

            int idIndex = cursor.getColumnIndex("id");
            int deviceTypeIndex = cursor.getColumnIndex("device_type");
            int modelIndex = cursor.getColumnIndex("model");
            int nameIndex = cursor.getColumnIndex("name");
            int irDataIndex = cursor.getColumnIndex("ir_data");
            int labelIndex = cursor.getColumnIndex("label");
            int iconIndex = cursor.getColumnIndex("icon");

            while (cursor.isAfterLast() == false){
                String id = cursor.getString(idIndex);
                String deviceType = cursor.getString(deviceTypeIndex);
                String model = cursor.getString(modelIndex);
                String name = cursor.getString(nameIndex);
                String irData = cursor.getString(irDataIndex);
                String label = cursor.getString(labelIndex);
                String icon = cursor.getString(iconIndex);

                ItemIrData itemIrData = new ItemIrData(Integer.parseInt(id), deviceType, model, name, irData,
                        label, icon);

                itemIrDataList.add(itemIrData);
                cursor.moveToNext();
            }
        }
        cursor.close();
        closeDB();
        return itemIrDataList;
    }

    public List<ItemIrData> sortByModel() {
        openDB();
        List<ItemIrData> itemIrDataList = new ArrayList<ItemIrData>();

        String sql = "select * from ir_data order by model asc;";

        Cursor cursor = mSQLiteDB.rawQuery(sql, null);

        if (cursor!=null){
            cursor.moveToFirst();
            int idIndex = cursor.getColumnIndex("id");
            int deviceTypeIndex = cursor.getColumnIndex("device_type");
            int modelIndex = cursor.getColumnIndex("model");
            int nameIndex = cursor.getColumnIndex("name");
            int irDataIndex = cursor.getColumnIndex("ir_data");
            int labelIndex = cursor.getColumnIndex("label");
            int iconIndex = cursor.getColumnIndex("icon");

            while (cursor.isAfterLast() == false){
                String id = cursor.getString(idIndex);
                String deviceType = cursor.getString(deviceTypeIndex);
                String model = cursor.getString(modelIndex);
                String name = cursor.getString(nameIndex);
                String irData = cursor.getString(irDataIndex);
                String label = cursor.getString(labelIndex);
                String icon = cursor.getString(iconIndex);

                ItemIrData itemIrData = new ItemIrData(Integer.parseInt(id), deviceType, model, name, irData,
                        label, icon);

                itemIrDataList.add(itemIrData);
                cursor.moveToNext();
            }
        }
        cursor.close();
        closeDB();
        return itemIrDataList;
    }

    public int getNewId() {
        openDB();
        int newId = -1;

        String sql = "select id from ir_data order by id desc limit 1;";
        Cursor cursor = mSQLiteDB.rawQuery(sql, null);

        if (cursor != null){
            cursor.moveToFirst();
            int idIndex = cursor.getColumnIndex("id");
            String id = cursor.getString(idIndex);
            newId = Integer.parseInt(id);
            cursor.moveToNext();
        }
        cursor.close();

        closeDB();
        return newId;
    }
}