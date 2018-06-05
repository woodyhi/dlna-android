package cn.cj.dlna;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.fourthline.cling.model.meta.Device;

import java.util.List;

/**
 * Created by june on 2017/3/5.
 */

public class DeviceAdapter extends BaseAdapter {
    private Context context;
    private List<Device> list;

    public DeviceAdapter(Context context, List<Device> list){
        this.context = context;
        this.list = list;
    }

    public void add(Device d){
        int position = list.indexOf(d);
        if(position > 0){
            list.remove(position);
            list.add(position, d);
        }else {
            list.add(d);
        }
        notifyDataSetChanged();
    }

    public void remove(Device d){
        list.remove(d);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null){
            convertView = View.inflate(context, R.layout.item_device_browser, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        holder.name.setText(list.get(position).getDetails().getFriendlyName());
        return convertView;
    }

    private class ViewHolder {
        TextView name;

        ViewHolder(View view){
            name = (TextView) view.findViewById(R.id.device_name);
        }
    }

}
