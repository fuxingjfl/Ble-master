package zsx.hldkmj.fy.com.antilose;

import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;

import com.calypso.bluelib.bean.SearchResult;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

public class DeviceListAdapter extends BaseQuickAdapter<BleDevice, BaseViewHolder> {


    public DeviceListAdapter(@LayoutRes int layoutResId, @Nullable List<BleDevice> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, BleDevice item) {
        helper.setText(R.id.name, item.getName());
        helper.setText(R.id.mac, item.getAddress());
        helper.setText(R.id.rssi, String.format("Rssi: %d", item.getRssi()));
    }
}
