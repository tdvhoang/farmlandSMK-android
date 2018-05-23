package hdv.ble.tdx.ui.main_old;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import hdv.ble.tdx.R;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by Ann on 1/29/16.
 */
public class LeDeviceListAdapter extends BaseAdapter {

    private final Context context;
    private final List<ExtendedBluetoothDevice> items;
    private final LayoutInflater inflate;

    public LeDeviceListAdapter(Context context) {
        this.context = context;
        this.items = new ArrayList<>();
        inflate = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
    }

    public void addDevice(ExtendedBluetoothDevice device){
        items.add(device);
    }

    public List<ExtendedBluetoothDevice> getItems(){
        return items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public ExtendedBluetoothDevice getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    Vh vh;
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (convertView == null) {
            view = inflate.inflate( R.layout.row_device, parent,false);
            vh = new Vh(view);
            view.setTag(vh);
        } else {
            vh = (Vh) view.getTag();
        }
        vh.tvNameDeviceRow.setText(items.get(position).device.getName() +"\n" + items.get(position).device.getAddress());

        final int rssiPercent = (int) (100.0f * (127.0f + items.get(position).rssi) / (127.0f + 20.0f));
        vh.rssi.setImageLevel(rssiPercent);


        return view;
    }

    public void clear() {
        items.clear();
    }

    static class Vh {
        @Bind(R.id.tvNameDeviceRow)
        TextView tvNameDeviceRow;
        @Bind(R.id.rssiDeviceRow)
        ImageView rssi;



        public Vh(View view) {
            ButterKnife.bind(this, view);
        }
    }


}



