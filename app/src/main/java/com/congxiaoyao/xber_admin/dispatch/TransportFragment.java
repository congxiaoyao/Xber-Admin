package com.congxiaoyao.xber_admin.dispatch;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transport_content, container, false);
        final EditText transport_content = (EditText) view.findViewById(R.id.ed_transport_content);
        final EditText transport_remark = (EditText) view.findViewById(R.id.ed_remark_content);
        Button btn_next = (Button) view.findViewById(R.id.btn_transport_next);
        Button btn_back = (Button) view.findViewById(R.id.btn_transport_back);

        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DispatchTaskActivity context = (DispatchTaskActivity) getContext();
                String content = transport_content.getText().toString();
                String note = transport_remark.getText().toString();
                if (content==null) return;
                context.setContent(content);
                if (note == null) {
                    context.setNote("无");
                } else {
                    context.setNote(note);
                }
                context.jumpToNext(TransportFragment.this);
            }
        });
        return view;
    }

}
