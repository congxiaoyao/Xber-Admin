package com.congxiaoyao.xber_admin.dispatch;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.congxiaoyao.xber_admin.R;

/**
 * Created by guo on 2017/3/22.
 */

public class TransportFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transport_content, container, false);
        EditText transport_content = (EditText) view.findViewById(R.id.ed_transport_content);
        EditText transport_remark = (EditText) view.findViewById(R.id.ed_remark_content);
        Button btn_next = (Button) view.findViewById(R.id.btn_transport_next);
        Button btn_back = (Button) view.findViewById(R.id.btn_transport_back);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

}
