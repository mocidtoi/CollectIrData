package com.thanhnv.collectirdata;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.jingxun.jingxun.bean.DeviceItemBean;
import com.jingxun.jingxun.helper.ConfigureHelper;
import com.jingxun.jingxun.helper.DeviceProbeHelper;
import com.jingxun.jingxun.helper.RequestHelper;
import com.jingxun.jingxun.helper.SendCommandHelper;
import com.jingxun.jingxun.listener.ConfigureListener;
import com.jingxun.jingxun.listener.IProbeCallBack;
import com.jingxun.jingxun.listener.ResponseCallBack;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by thanhnv on 05/10/2016.
 */
public class DeviceBeanManager {
    private static final int STUDY_SUCCESS = 1, STUDY_FAILED = 2, STUDY_STOP = 3, STUDY_CANCEL = 4,
                PROBE_SUCCESS = 5, PROBE_FAILED = 6;

    private Context mContext;
    private DeviceItemBean myDeviceItemBean;

    private String phoneCode;

    private boolean isStopStudy = false;

    private boolean unikeyProbe = true;
    private SharedPreferences sharedPreferences;

    public DeviceBeanManager(Context mContext, SharedPreferences sharedPreferences) {
        this.mContext = mContext;
        phoneCode = getPhoneImei(mContext);
        this.sharedPreferences = sharedPreferences;
        updateNewDeviceBean();
    }

    public void updateNewDeviceBean(){
        String deviceId = sharedPreferences.getString(MainActivity.DID, null);
        String key = sharedPreferences.getString(MainActivity.KEY, null);
        String ip = sharedPreferences.getString(MainActivity.IP, null);
        String serverIp = sharedPreferences.getString(MainActivity.SERVER_IP, null);
        myDeviceItemBean = createDeviceItemBean(deviceId, key, ip, serverIp);
    }

    public void updateSharePreference(String deviceId, String key, String ip, String serverIp){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(MainActivity.DID, deviceId);
        editor.putString(MainActivity.KEY, key);
        editor.putString(MainActivity.IP, ip);
        editor.putString(MainActivity.SERVER_IP, serverIp);
        editor.commit();
    }

    public void configure(String ssid, String passWifi) {

        ConfigureHelper.getInstance().startConfigure(mContext, ssid, passWifi, new ConfigureListener() {
            @Override
            public void onSuccess(DeviceItemBean deviceItemBean) {
                if (onConfigureDeviceBean != null){
                    updateSharePreference(deviceItemBean.getDeviceId(), deviceItemBean.getKey(), null, null);
                    updateNewDeviceBean();
                    Log.d("DeviceBeanManager", "Configure success");
                    onConfigureDeviceBean.onSuccess(deviceItemBean.getDeviceId(), deviceItemBean.getKey());
                }
            }

            @Override
            public void onFailed(Exception e) {
                Log.d("DeviceBeanManager", "Configure failed");
                if (onConfigureDeviceBean != null){
                    onConfigureDeviceBean.onFailed();
                }
            }
        });
    }

    public void probeDevice(String deviceId, String key) {
        RequestHelper.getInstance().releaseSocket();

        Log.d("DeviceBeanManager", deviceId + " - " + key);

        LinkedList<DeviceItemBean> mList = new LinkedList<DeviceItemBean>();

        DeviceItemBean bean = new DeviceItemBean.DeivceItemBuilder()
                .deviceId(deviceId)
                .key(key)
                .build();
        mList.add(bean);

        DeviceProbeHelper.getInstance().startProbe(mContext, mList, new IProbeCallBack() {
            @Override
            public void onCallBack(List<DeviceItemBean> list) {
                Log.d("DeviceBeanManager", "probe ---->");

                if (list.get(0).isOnline()){
                    DeviceItemBean bean = list.get(0);

                    updateSharePreference(bean.getDeviceId(), bean.getKey(), bean.getIp(), bean.getServerIp());
                    updateNewDeviceBean();

                    Log.d("DeviceBeanManager", "success probe ");
                    Log.d("DeviceBeanManager", "server probe: " + bean.getServerIp());
                    Log.d("DeviceBeanManager", "ip probe: " + bean.getIp());

                    createSocket();
                    mHandler.sendEmptyMessage(PROBE_SUCCESS);
                } else {
                    Log.d("DeviceBeanManager", "failed probe ");
                    mHandler.sendEmptyMessage(PROBE_FAILED);
                }
            }
        });
    }

    public void startLearning() {
        isStopStudy = false;



        if (RequestHelper.getInstance().isLocalConnected()){
            Log.d("DeviceBeanManager", "Local");
            learningIrLocal();
            return;
        } else {
            if (myDeviceItemBean.getServerIp() != null) {
                learningIrRemote();
            } else {
                Toast.makeText(mContext, "Server has problem, click try again after some minute", Toast.LENGTH_SHORT).show();
            }
        }

    }





    // remote lerning ....
    private void learningIrRemote(){
        String param = SendCommandHelper.irStudy(myDeviceItemBean.getDeviceId(), phoneCode);

        Log.d("DeviceBeanManager", "param remote learning: " + param);
        Log.d("DeviceBeanManager", "server remote learning: " + myDeviceItemBean.getServerIp());

        if (myDeviceItemBean.getServerIp() == null){
            Toast.makeText(mContext, "Server has problem", Toast.LENGTH_SHORT).show();
            return;
        }



        RequestHelper.getInstance().requestRemoteData(myDeviceItemBean.getServerIp(), param, new ResponseCallBack() {
            @Override
            public void onSuccess(int i, JSONObject jsonObject) {
                Log.d("DeviceBeanManager", "flag remote : " + i);
                Log.d("DeviceBeanManager", "json remote : " + jsonObject.toString()+"\n----");

                remoteReceiveReslt(myDeviceItemBean.getServerIp(), myDeviceItemBean.getDeviceId(), phoneCode);
            }

            @Override
            public void onFailed(Exception e) {
                Log.d("DeviceBeanManager", "error remote : 1");
                mHandler.sendEmptyMessage(STUDY_FAILED);
            }
        });
    }

    private void remoteReceiveReslt(final String serverIp, final String deviceId, final String phoneCode) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                String param = SendCommandHelper.getIRStudyResult(deviceId, phoneCode);
                int count = 0;
                Log.d("DeviceBeanManager", "learning success 0");

                while (count < 20 && !isStopStudy) {
                    RequestHelper.getInstance().requestRemoteData(serverIp, param, new ResponseCallBack() {
                        @Override
                        public void onSuccess(int flag, JSONObject data) {
                            onGet_Study_result(data);
                            Log.d("DeviceBeanManager", "learning success 1");
                        }

                        @Override
                        public void onFailed(Exception e) {
                            Log.d("DeviceBeanManager", "learning onfailed 1");
                            mHandler.sendEmptyMessage(STUDY_FAILED);
                        }
                    });

                    try {
                        Thread.sleep(3000);
                        count ++;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }


    // local lerning Ir data...
    private void learningIrLocal(){
        String param = SendCommandHelper.irStudy(myDeviceItemBean.getDeviceId(), phoneCode);

        Log.d("DeviceBeanManager", "param remote: " + param);

        RequestHelper.getInstance().requestLocalData(myDeviceItemBean.getIp(), param, new ResponseCallBack() {
            @Override
            public void onSuccess(int i, JSONObject jsonObject) {
                Log.d("DeviceBeanManager", "flag local : " + i);
                Log.d("DeviceBeanManager", "json local : " + jsonObject.toString()+"\n----");

                localReceiveReslt(myDeviceItemBean.getIp(), myDeviceItemBean.getDeviceId(), phoneCode);
            }

            @Override
            public void onFailed(Exception e) {
                mHandler.sendEmptyMessage(STUDY_FAILED);
            }
        });
    }

    private void localReceiveReslt(final String ip, final String deviceId, final String phoneCode) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String param = SendCommandHelper.getIRStudyResult(deviceId, phoneCode);
                int count = 0;

                while (count < 20 && !isStopStudy) {

                    RequestHelper.getInstance().requestLocalData(ip, param, new ResponseCallBack() {
                        @Override
                        public void onSuccess(int flag, JSONObject data) {
                            onGet_Study_result(data);
                            Log.d("DeviceBeanManager", "learning success 1");
                        }

                        @Override
                        public void onFailed(Exception e) {
                            Log.d("DeviceBeanManager", "learning onfailed 1");
                            mHandler.sendEmptyMessage(STUDY_FAILED);
                        }
                    });

                    try {
                        Thread.sleep(3000);
                        count ++;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    // cancel lerning ....
    public void cancelLearning() {
        if (RequestHelper.getInstance().isLocalConnected()){

            String param = SendCommandHelper.cancelIRStudy(myDeviceItemBean.getDeviceId(), phoneCode);
            Log.d("DeviceBeanManager", "param cancel local");
            DeviceItemBean bean = new DeviceItemBean.DeivceItemBuilder()
                    .deviceId(myDeviceItemBean.getDeviceId())
                    .ip(myDeviceItemBean.getIp())
                    .serverIp(myDeviceItemBean.getServerIp())
                    .build();

            RequestHelper.getInstance().requestData(bean, param, null);
            mHandler.sendEmptyMessage(STUDY_CANCEL);

        } else {

            Log.d("DeviceBeanManager", "param cancel remote");
            String param = SendCommandHelper.cancelIRStudy(myDeviceItemBean.getDeviceId(), phoneCode);
            RequestHelper.getInstance().requestRemoteData(myDeviceItemBean.getServerIp(), param, null);
            mHandler.sendEmptyMessage(STUDY_CANCEL);

        }
    }

    // send command to device bean
    public void testWithIrData(String irData){
        if (irData == null || irData.equals("")){
            return;
        }

        String param = SendCommandHelper.transitIRCode(myDeviceItemBean.getDeviceId(), irData);


        Log.d("DeviceBeanManager", "param send ir data : " + param);

        DeviceItemBean bean = new DeviceItemBean.DeivceItemBuilder()
                .deviceId(myDeviceItemBean.getDeviceId())
                .ip(myDeviceItemBean.getIp())
                .serverIp(myDeviceItemBean.getServerIp())
                .build();

        RequestHelper.getInstance().requestData(bean, param, new ResponseCallBack() {
            @Override
            public void onSuccess(int i, JSONObject jsonObject) {
                Log.d("DeviceBeanManager", "json send success : " + jsonObject.toString());
                if (onTestIrData!= null){
                    onTestIrData.onSuccess();
                }
            }

            @Override
            public void onFailed(Exception e) {
                Log.d("DeviceBeanManager", "send onFailed: " + e.toString());
                if (onTestIrData!= null){
                    onTestIrData.onFailed();
                }
            }
        });

        Log.d("DeviceBeanManager", "send ir data \n----");
    }






    private void onGet_Study_result(JSONObject data){

        if(data==null){
            Log.d("DeviceBeanManger", "on get study : null");
            return;
        }
        Log.d("DeviceBeanManger", "on get study : " + data.toString());
        try {
            int wifi_cmd = data.getInt("wifi_cmd");
            switch (wifi_cmd) {
                case 6:
                    isStopStudy=true;
                    Log.d("DeviceBeanManger", "on get study success : ");
                    RequestHelper.getInstance().unRegisterListener();
                    if ( onStartLearning!= null) {
                        onStartLearning.onSuccess(data.getString("ir_data"));
                    }
                    break;
                case 7:
                    studyState(data.getInt("Res"));
                    break;
                default:
                    break;
            }
        } catch (JSONException e) {
            Log.d("DeviceBeanManger", "error" + e.toString());
            mHandler.sendEmptyMessage(STUDY_FAILED);
        }
    }

    private void studyState(int res){
        Log.d("DeviceBeanManager", "aaaaaa res " + res);
        switch (res) {
            case 0:
                isStopStudy=false;
                break;
            case 1:
                mHandler.sendEmptyMessage(STUDY_STOP);
                break;
            case 2:
                mHandler.sendEmptyMessage(STUDY_STOP);
                break;
            case 3:
                mHandler.sendEmptyMessage(STUDY_FAILED);
                break;
            default:
                break;
        }
    }





















    private DeviceItemBean createDeviceItemBean(String deviceId, String key, String ip, String serverIp){
        return new DeviceItemBean.DeivceItemBuilder()
                .deviceId(deviceId)
                .key(key)
                .ip(ip)
                .serverIp(serverIp)
                .build();
    }

    public static void stop() {
        DeviceProbeHelper.getInstance().stopProbe();
        RequestHelper.getInstance().unRegisterListener();
    }

    public void setProbeUnikey(boolean probeUnikey) {
        this.unikeyProbe = probeUnikey;
    }

    public void createSocket() {
        String ip = sharedPreferences.getString(MainActivity.IP, null);
        if (ip != null) {
            RequestHelper.getInstance().createSocket(ip);
        }
    }

    public void releaseSocket() {
        Log.d("DeviceBeanManager", "Release socket");
        RequestHelper.getInstance().releaseSocket();
    }


    public interface OnConfigureDeviceBean{
        void onSuccess(String deviceId, String key);
        void onFailed();
    }
    private OnConfigureDeviceBean onConfigureDeviceBean;

    public void setOnConfigureDeviceBean(OnConfigureDeviceBean onConfigureDeviceBean){
        this.onConfigureDeviceBean = onConfigureDeviceBean;
    }

    public interface OnProbeListener{
        void onSuccess(boolean unikey);
        void onFailed();
    }
    private OnProbeListener onProbeListener;

    public void setOnProbeListener(OnProbeListener onProbeListener){
        this.onProbeListener = onProbeListener;
    }

    public interface OnStartLearning{
        void onSuccess(String irData);
        void onFailed();
        void onCancel();
    }
    private OnStartLearning onStartLearning;

    public void setOnStartLearning(OnStartLearning onStartLearning){
        this.onStartLearning = onStartLearning;
    }

    public interface OnTestIrData{
        void onSuccess();
        void onFailed();
    }
    private OnTestIrData onTestIrData;

    public void setOnTestIrData(OnTestIrData onTestIrData){
        this.onTestIrData = onTestIrData;
    }




    public static String getPhoneImei(Context context){
        return ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            isStopStudy=true;
            RequestHelper.getInstance().unRegisterListener();
            switch (msg.what) {
                case STUDY_FAILED:
                    // study ...
                    if (onStartLearning != null) {
                        onStartLearning.onFailed();
                    }
                    break;
                case STUDY_CANCEL:
                    if (onStartLearning != null) {
                        onStartLearning.onCancel();
                    }
                    break;
                case STUDY_STOP:
                    if (onStartLearning != null) {
                        onStartLearning.onFailed();
                    }
                    break;

                // probe
                case PROBE_SUCCESS:
                    DeviceProbeHelper.getInstance().stopProbe();
                    if (onProbeListener != null) {
                        onProbeListener.onSuccess(unikeyProbe);
                    }
                    break;
                case PROBE_FAILED:
                    DeviceProbeHelper.getInstance().stopProbe();
                    if (onProbeListener != null) {
                        onProbeListener.onFailed();
                    }
                    break;
                default:
                    break;
            }
        }
    };
}
