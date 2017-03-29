package com.congxiaoyao.xber_admin.spotmanage;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.congxiaoyao.xber_admin.R;

/**
 * Created by guo on 2017/3/24.
 */

public class SpotRecyclerViewAdapter extends RecyclerView.Adapter<SpotRecyclerViewAdapter.SpotViewHolder> {



    public SpotRecyclerViewAdapter(Context context) {

    }

    @Override
    public SpotViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return null;
    }

    @Override
    public void onBindViewHolder(SpotViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

   public class SpotViewHolder extends RecyclerView.ViewHolder {

        TextView tv_spot_name;
        TextView tv_spot_delete;
        TextView tv_spot_revise;

        public SpotViewHolder(View itemView) {
            super(itemView);
            tv_spot_name = (TextView) itemView.findViewById(R.id.tv_spot_name);
            tv_spot_delete = (TextView) itemView.findViewById(R.id.tv_spot_delete);
            tv_spot_revise = (TextView) itemView.findViewById(R.id.tv_spot_revise);
        }
    }
}
