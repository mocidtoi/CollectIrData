package com.thanhnv.collectirdata;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

/**
 * Created by thanhnv on 05/10/2016.
 */

public class MyAdapterIr extends RecyclerView.Adapter<MyAdapterIr.MyHolder> {
    private List<ItemIrData> dataSet;
    private Context mContext;

    public MyAdapterIr(Context context, List<ItemIrData> dataSet) {
        this.dataSet = dataSet;
        mContext = context;
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_ir_data, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(MyHolder holder, final int position) {
        ItemIrData item = dataSet.get(position);

        holder.mySelf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("ADAPTER", "click");
                myOnClickListenter.onClickLister(position);
            }
        });

        holder.mySelf.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                myOnClickListenter.onLongClickListener(position);
                return true;
            }
        });

        holder.txtDeviceType.setText(item.getDeviceType());
        holder.txtModel.setText(item.getModel());
        holder.txtName.setText(item.getName());
        holder.txtIrData.setText(item.getIrData());

        holder.btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testListener.onTestListener(position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder {
        public View mySelf;
        public TextView txtDeviceType, txtModel, txtName, txtIrData;
        public Button btnTest;

        public MyHolder(View itemView) {
            super(itemView);
            mySelf = itemView;
            txtDeviceType = (TextView) itemView.findViewById(R.id.txt_device_type);
            txtModel = (TextView) itemView.findViewById(R.id.txt_model);
            txtName = (TextView) itemView.findViewById(R.id.txt_name);
            txtIrData = (TextView) itemView.findViewById(R.id.txt_ir_data);

            btnTest = (Button) itemView.findViewById(R.id.btn_test);
        }
    }

    public interface TestListener{
        void onTestListener(int position);
    }

    private TestListener testListener;
    public void setTestListener(TestListener testListener){
        this.testListener = testListener;
    }

    public interface MyOnClickListenter{
        void onClickLister(int position);
        void onLongClickListener(int position);
    }

    private MyOnClickListenter myOnClickListenter;

    public void setMyOnClickListenter(MyOnClickListenter myOnClickListenter){
        this.myOnClickListenter = myOnClickListenter;
    }
}
