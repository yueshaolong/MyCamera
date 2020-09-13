package com.example.android.camera2basic.camera1;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.amap.api.services.core.PoiItem;
import com.example.android.camera2basic.R;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ListHolder;
import com.orhanobut.dialogplus.OnItemClickListener;

import java.util.List;

public class DialogUtil {

    /**
     * 列表展示
     * @param context
     * @param listener
     * @param title
     * @param checkType
     * @param checked
     */
    public static void showListDialog(Context context, OnItemClickListener listener, String title,
                                      List<ICheckType> checkType, ICheckType checked){
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_header, null, false);
        ((TextView)view.findViewById(R.id.title)).setText(title);
        DialogPlus dialogPlus = DialogPlus.newDialog(context)
                .setContentHolder(new ListHolder())
                .setHeader(view)
                .setAdapter(new ListDialogAdapter(context, checkType, checked))
                .setOnItemClickListener(listener)
                .setCancelable(true)
                .setGravity(Gravity.BOTTOM)
                .create();
        dialogPlus.show();
        dialogPlus.getHeaderView().findViewById(R.id.cancel).setOnClickListener(view1 -> {
            if(dialogPlus.isShowing()){
                dialogPlus.dismiss();
            }
        });
    }

    public static class ListDialogAdapter extends BaseAdapter {
        private List<ICheckType> checkType;
        private Context context;
        private ICheckType checked;

        public ListDialogAdapter(Context context, List<ICheckType> checkType, ICheckType checked) {
            this.checkType = checkType;
            this.context = context;
            this.checked = checked;
        }

        @Override
        public int getCount() {
            return checkType.size();
        }

        @Override
        public Object getItem(int position) {
            return checkType.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolderList viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.poi_item, null, false);
                viewHolder = new ViewHolderList(convertView);
                convertView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolderList) convertView.getTag();
            }
            PoiItem poiItem = (PoiItem) checkType.get(position);
            viewHolder.poi_name.setText(poiItem.getTitle());
            viewHolder.poi_position.setText(poiItem.getDistance()+"米·"+poiItem.getSnippet());
            if(checked != null && checked.equals(checkType.get(position))){
//                viewHolder.iv_choice.setVisibility(View.VISIBLE);
            }else {
//                viewHolder.iv_choice.setVisibility(View.INVISIBLE);
            }
            return convertView;
        }
    }

    public static class ViewHolderList{
        TextView poi_name;
        TextView poi_position;
        public ViewHolderList(View view){
            poi_name = view.findViewById(R.id.poi_name);
            poi_position = view.findViewById(R.id.poi_position);
        }
    }

}
