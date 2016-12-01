package com.thanhnv.collectirdata;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.jingxun.jingxun.helper.ConfigureHelper;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String SHA_BEAN_INFO = "SHA_BEAN_INFO", DID = "DID", KEY = "KEY",
                                IP = "IP", SERVER_IP = "SERVER_IP";
    public static final String DEVICE_TYPE = "DEVICE_TYPE", MODEL = "MODEL", NAME = "NAME",
                                LABEL = "LABEL", ICON = "ICON";

    private static final int CF_SUCCESS = 1, CF_FAILED = 2, STUDY_SUCCESS = 3;

    private RecyclerView rvIrData;
    private MyAdapterIr adapter;
    private List<ItemIrData> itemIrDataList;

    private DBManager dbManager;

    private Dialog dialog;

    private boolean isSortByDeviceType = false, isSortByModel = false, isSortByName = false;

    private Button btnDeviceType, btnModel, btnName;

    private boolean avaiable = false;

    private EditText edtDeviceType, edtModel, edtName, edtIrData, edtLabel, edtIcon;
    private TableRow trLearning, trSave;

    private boolean isClickConfigure = false;

    // dialog add
    private TextView tvInfo;
    // dialog add


    // dialog configure
    private TextView tvResult;
    private EditText edtSSID, edtPassWifi;
    private boolean configureDone = false;
    private boolean isSuccessfullyConfigure = false;
    // dialog configure

    private MyWifiManager myWifiManager;
    private DeviceBeanManager deviceBeanManager;

    SharedPreferences sharedPreferences;

    String irData = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences(SHA_BEAN_INFO, MODE_PRIVATE);

        dbManager = new DBManager(this);

        deviceBeanManager = new DeviceBeanManager(this, sharedPreferences);

        myWifiManager = new MyWifiManager(this);

        setInterfaceForBeanManager();

        initViews();
    }

    private void setInterfaceForBeanManager() {

        // configure call back....
        deviceBeanManager.setOnConfigureDeviceBean(new DeviceBeanManager.OnConfigureDeviceBean() {
            @Override
            public void onSuccess(String deviceId, String key) {
                Message message = new Message();
                message.what = CF_SUCCESS;
                message.setTarget(handler);
                message.sendToTarget();
                saveSharepreferences(deviceId, key, null, null);
                deviceBeanManager.probeDevice(deviceId, key);
            }

            @Override
            public void onFailed() {
                Message message = new Message();
                message.what = CF_FAILED;
                message.setTarget(handler);
                message.sendToTarget();
            }
        });

        // probe call back ....
        deviceBeanManager.setOnProbeListener(new DeviceBeanManager.OnProbeListener() {
            @Override
            public void onSuccess(boolean unikey) {
                if (unikey){
                    showDialogAddData();
                    Toast.makeText(MainActivity.this, "Probe success !!!!", Toast.LENGTH_SHORT).show();
                    avaiable = true;
                    deviceBeanManager.setProbeUnikey(false);
                }
            }

            @Override
            public void onFailed() {
                Toast.makeText(MainActivity.this, "Probe error !!!!", Toast.LENGTH_SHORT).show();
                gotoConfigure();
            }
        });

        // on learning ....
        deviceBeanManager.setOnStartLearning(new DeviceBeanManager.OnStartLearning() {
            @Override
            public void onSuccess(String irData) {
                Log.d(TAG, "ir data main : " + irData);
                MainActivity.this.irData = irData;
                handler.sendEmptyMessage(STUDY_SUCCESS);
            }

            @Override
            public void onFailed() {
                Toast.makeText(MainActivity.this, "Learning false !!!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {
                Toast.makeText(MainActivity.this, "Cancel learning !!!", Toast.LENGTH_SHORT).show();
            }
        });

        // on test ir data ...
        deviceBeanManager.setOnTestIrData(new DeviceBeanManager.OnTestIrData() {
            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, "Send ir data success", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed() {
                Toast.makeText(MainActivity.this, "Send ir data failed!!!", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void initViews() {
        // main screen
        itemIrDataList = dbManager.getDataList();
        rvIrData = (RecyclerView)findViewById(R.id.rv_database_ir);
        adapter = new MyAdapterIr(this, itemIrDataList);
        adapter.setMyOnClickListenter(new MyAdapterIr.MyOnClickListenter() {
            @Override
            public void onClickLister(int position) {
                showDialogInfoData(position);
            }

            @Override
            public void onLongClickListener(int position) {
                showDialogDelete(position);
            }
        });

        adapter.setTestListener(new MyAdapterIr.TestListener() {
            @Override
            public void onTestListener(int position) {
                Toast.makeText(MainActivity.this, "Test ir data", Toast.LENGTH_SHORT).show();
                deviceBeanManager.testWithIrData(itemIrDataList.get(position).getIrData());
            }
        });

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        rvIrData.setLayoutManager(layoutManager);
        rvIrData.setAdapter(adapter);


        btnDeviceType = ((Button)findViewById(R.id.btn_device_type));
        btnDeviceType.setOnClickListener(this);
        btnModel =((Button)findViewById(R.id.btn_model));
        btnModel.setOnClickListener(this);
        btnName = ((Button)findViewById(R.id.btn_name));
        btnName.setOnClickListener(this);

        ((Button)findViewById(R.id.btn_add)).setOnClickListener(this);
        ((Button)findViewById(R.id.btn_delete_share)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Delete group");
                alertDialog.setMessage("Do you want to delete data in sharepreferences");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear();
                        editor.commit();
                        Toast.makeText(MainActivity.this, "Delete all sharepreference", Toast.LENGTH_SHORT).show();
                        avaiable = false;
                    }
                });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                alertDialog.show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_device_type:
                sortByDeviceType();
                break;
            case R.id.btn_model:
                sortByModel();
                break;
            case R.id.btn_name:
                sortByName();
                break;
            case R.id.btn_add:
                addProcessing();
                break;
            default:
                break;
        }
    }

    private void addProcessing(){
        Toast.makeText(this, "Add new data .....", Toast.LENGTH_SHORT).show();
        if (!avaiable) {
            prepareDeviceBean();
        } else {
            showDialogAddData();
        }
    }

    private void prepareDeviceBean() {
        String deviceId = sharedPreferences.getString(DID, null);
        String key = sharedPreferences.getString(KEY, null);

        if (deviceId == null || key == null){
            gotoConfigure();
        }else {
            deviceBeanManager.probeDevice(deviceId, key);
        }

    }

    private void gotoConfigure() {
        if (dialog != null){
            dialog.dismiss();
        }

        dialog = new Dialog(this, R.style.DialogTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_configure);
        dialog.setCanceledOnTouchOutside(false);


        tvResult = (TextView) dialog.findViewById(R.id.tv_result);

        edtSSID = (EditText) dialog.findViewById(R.id.edt_ssid);
        edtPassWifi = (EditText) dialog.findViewById(R.id.edt_pass_wifi);

        edtSSID.setText(myWifiManager.getSSID());
        edtPassWifi.setHint("Enter password wifi");

        isClickConfigure = false;

        ((Button)dialog.findViewById(R.id.btn_configure)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isClickConfigure) {
                    String ssid = edtSSID.getText().toString();
                    String passWifi = edtPassWifi.getText().toString();
                    if (ssid.equals("") || passWifi.equals("")) {
                        Toast.makeText(MainActivity.this, "Please, enter info about wifi", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Log.d(TAG, "ssid " + ssid);
                    Log.d(TAG, "pass " + passWifi);

                    // configure
                    deviceBeanManager.configure(ssid, passWifi);
                    new MainActivity.MyAsyncTask().execute();
                    isClickConfigure = true;
                } else {
                    Toast.makeText(MainActivity.this, "Wait for configuring ....", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }



    private void showDialogAddData() {
        if (dialog != null){
            dialog.dismiss();
        }
        dialog = new Dialog(this, R.style.DialogTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_data);
        dialog.setCanceledOnTouchOutside(false);

        edtDeviceType = (EditText) dialog.findViewById(R.id.edt_device_type);
        edtModel = (EditText) dialog.findViewById(R.id.edt_model);
        edtName = (EditText) dialog.findViewById(R.id.edt_name);
        edtIrData = (EditText) dialog.findViewById(R.id.edt_ir_data);
        edtLabel = (EditText) dialog.findViewById(R.id.edt_label);
        edtIcon = (EditText) dialog.findViewById(R.id.edt_icon);

        trLearning = (TableRow) dialog.findViewById(R.id.tr_learning);
        trSave = (TableRow) dialog.findViewById(R.id.tr_save);

        tvInfo = (TextView) dialog.findViewById(R.id.tv_info);

        String deviceType = sharedPreferences.getString(DEVICE_TYPE, "");
        String model = sharedPreferences.getString(MODEL, "");
        String name = sharedPreferences.getString(NAME, "");
        String label = sharedPreferences.getString(LABEL, "");
        String icon = sharedPreferences.getString(ICON, "");

        edtDeviceType.setText(deviceType+"");
        edtModel.setText(model+"");
        edtName.setText(name+"");
        edtLabel.setText(label+"");
        edtIcon.setText(icon+"");

        ((Button) dialog.findViewById(R.id.btn_learning)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceBeanManager.startLearning();
            }
        });

        ((Button) dialog.findViewById(R.id.btn_cancel_learning)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceBeanManager.cancelLearning();
            }
        });




        ((Button) dialog.findViewById(R.id.btn_save)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Save data !!!", Toast.LENGTH_SHORT).show();
                String deviceType = edtDeviceType.getText().toString();
                String model = edtModel.getText().toString();
                String name = edtName.getText().toString();
                String irData = edtIrData.getText().toString();


                String label = edtLabel.getText().toString();
                String icon = edtIcon.getText().toString();

                if (irData.equals("") || deviceType.equals("")
                        || model.equals("") || name.equals("")){
                    Toast.makeText(MainActivity.this, "Empty info !!!!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (dbManager.insertData(deviceType, model, name, irData, label, icon)){
                    tvInfo.setText("Insert successfully");
                    edtIrData.setText("");
                    edtIrData.setEnabled(true);

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(DEVICE_TYPE, deviceType);
                    editor.putString(MODEL, model);
                    editor.putString(NAME, name);
                    editor.putString(LABEL, label);
                    editor.putString(ICON, icon);
                    editor.commit();


                } else {
                    tvInfo.setText("Insert failly \n Try againt. If has yet success, please report to me to fix.");
                }
                trLearning.setVisibility(View.VISIBLE);
                trSave.setVisibility(View.GONE);

            }
        });

        ((Button) dialog.findViewById(R.id.btn_cancel_save)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtIrData.setText("");
                edtIrData.setEnabled(true);

                trLearning.setVisibility(View.VISIBLE);
                trSave.setVisibility(View.GONE);
            }
        });



        ((ImageView)dialog.findViewById(R.id.img_delete_device_type)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtDeviceType.setText("");
            }
        });
        ((ImageView)dialog.findViewById(R.id.img_delete_model)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtModel.setText("");
            }
        });

        ((ImageView)dialog.findViewById(R.id.img_delete_name)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtName.setText("");
            }
        });

        ((ImageView)dialog.findViewById(R.id.img_delete_ir_data)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtIrData.isEnabled()) {
                    edtIrData.setText("");
                }
            }
        });

        ((ImageView)dialog.findViewById(R.id.img_delete_label)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtLabel.setText("");
            }
        });

        ((ImageView)dialog.findViewById(R.id.img_delete_icon)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edtIcon.setText("");
            }
        });



        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
                updateListViewInfoData();
                deviceBeanManager.releaseSocket();
            }
        });

        deviceBeanManager.createSocket();

        dialog.show();
    }

    private void showDialogInfoData(int position){
        if (dialog != null){
            dialog.dismiss();
        }

        ItemIrData irData = itemIrDataList.get(position);

        dialog = new Dialog(this, R.style.DialogTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_info_irdata);
        dialog.setCanceledOnTouchOutside(false);


        String deviceType = irData.getDeviceType();
        String model = irData.getModel();
        String name = irData.getName();
        String irdata = irData.getIrData();
        String label = irData.getLabel();
        String icon = irData.getIcon();

        if (label == null){
            label = "null";
        }

        if (icon == null){
            icon = "null";
        }

        ((TextView)dialog.findViewById(R.id.tv_device_type)).setText(deviceType);
        ((TextView)dialog.findViewById(R.id.tv_model)).setText(model);
        ((TextView)dialog.findViewById(R.id.tv_name)).setText(name);
        ((TextView)dialog.findViewById(R.id.tv_ir_data)).setText(irdata);
        ((TextView)dialog.findViewById(R.id.tv_label)).setText(label);
        ((TextView)dialog.findViewById(R.id.tv_icon)).setText(icon);


        ((Button)dialog.findViewById(R.id.btn_back_dialog_info)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }




    private void updateListViewInfoData() {
        if (isSortByDeviceType){
            itemIrDataList.clear();
            List<ItemIrData> temple = dbManager.sortByDeviceType();
            itemIrDataList.addAll(temple);
            adapter.notifyDataSetChanged();
        } else if (isSortByModel){
            itemIrDataList.clear();
            List<ItemIrData> temple = dbManager.sortByModel();
            itemIrDataList.addAll(temple);
            adapter.notifyDataSetChanged();
        } else if (isSortByName){
            itemIrDataList.clear();
            List<ItemIrData> temple = dbManager.sortByName();
            itemIrDataList.addAll(temple);
            adapter.notifyDataSetChanged();
        }else {
            itemIrDataList.clear();
            List<ItemIrData> temple = dbManager.getDataList();
            itemIrDataList.addAll(temple);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        deviceBeanManager.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        avaiable = false;

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(IP, null);
        editor.putString(SERVER_IP, null);
        editor.commit();
    }

    private class MyAsyncTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onPreExecute() {
            tvResult.setText("120s");
        }

        @Override
        protected Void doInBackground(Void... params) {
            int i = 120;
            while (!configureDone && i > 0){
                try {
                    Thread.sleep(1000);
                    i--;
                    publishProgress(i);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            tvResult.setText(values[0] + "s");
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (!configureDone){
                tvResult.setText("Time out...");
            } else {
                if (!isSuccessfullyConfigure){
                    tvResult.setText("Configure failed, please try again!!!");
                } else {
                    tvResult.setText("Configure Successfully!!!");
                }
            }
        }
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case CF_SUCCESS:
                    configureDone = true;
                    isSuccessfullyConfigure = true;
                    ConfigureHelper.getInstance().stopConfigure();
                    break;
                case CF_FAILED:
                    configureDone = true;
                    ConfigureHelper.getInstance().stopConfigure();
                    break;
                case STUDY_SUCCESS:
                    edtIrData.setText(irData);
                    edtIrData.setEnabled(false);
                    trLearning.setVisibility(View.GONE);
                    trSave.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }
    };

    private void saveSharepreferences(String deviceId, String key, String ip, String serverIp) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DID, deviceId);
        editor.putString(KEY, key);
        editor.putString(IP, ip);
        editor.putString(SERVER_IP, serverIp);
        editor.commit();
    }

    private void sortByDeviceType(){
        if (!isSortByDeviceType){
            btnDeviceType.setTextColor(Color.BLUE);
            btnModel.setTextColor(Color.BLACK);
            btnName.setTextColor(Color.BLACK);

            itemIrDataList.clear();
            List<ItemIrData> temple = dbManager.sortByDeviceType();
            itemIrDataList.addAll(temple);
            adapter.notifyDataSetChanged();
        } else {
            btnDeviceType.setTextColor(Color.BLACK);
            itemIrDataList.clear();
            List<ItemIrData> temple = dbManager.getDataList();
            itemIrDataList.addAll(temple);
            adapter.notifyDataSetChanged();
        }
        isSortByDeviceType = !isSortByDeviceType;
        isSortByModel = false;
        isSortByName = false;
    }

    private void sortByModel(){
        if (!isSortByModel){
            btnModel.setTextColor(Color.BLUE);
            btnDeviceType.setTextColor(Color.BLACK);
            btnName.setTextColor(Color.BLACK);

            itemIrDataList.clear();
            List<ItemIrData> temple = dbManager.sortByModel();
            itemIrDataList.addAll(temple);
            adapter.notifyDataSetChanged();
        } else {
            btnModel.setTextColor(Color.BLACK);
            itemIrDataList.clear();
            List<ItemIrData> temple = dbManager.getDataList();
            itemIrDataList.addAll(temple);
            adapter.notifyDataSetChanged();
        }
        isSortByModel = !isSortByModel;
        isSortByDeviceType = false;
        isSortByName = false;
    }

    private void sortByName(){
        if (!isSortByName){
            btnName.setTextColor(Color.BLUE);
            btnModel.setTextColor(Color.BLACK);
            btnDeviceType.setTextColor(Color.BLACK);

            itemIrDataList.clear();
            List<ItemIrData> temple = dbManager.sortByName();
            itemIrDataList.addAll(temple);
            adapter.notifyDataSetChanged();
        } else {
            btnName.setTextColor(Color.BLACK);
            itemIrDataList.clear();
            List<ItemIrData> temple = dbManager.getDataList();
            itemIrDataList.addAll(temple);
            adapter.notifyDataSetChanged();
        }
        isSortByName = !isSortByName;
        isSortByModel = false;
        isSortByDeviceType = false;
    }

    private void showDialogDelete(final int position){
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Delete group");
        alertDialog.setMessage("Do you want to delete data.");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dbManager.deleteData(itemIrDataList.get(position).getId())) {
                    Toast.makeText(MainActivity.this, "Delete item " + itemIrDataList.get(position).getName(), Toast.LENGTH_SHORT).show();
                    itemIrDataList.remove(position);
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MainActivity.this, "Has problem with db, please report to me to fix...", Toast.LENGTH_SHORT).show();
                }
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.show();
    }
}
