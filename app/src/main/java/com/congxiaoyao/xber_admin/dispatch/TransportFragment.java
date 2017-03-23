package com.congxiaoyao.xber_admin.dispatch;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
        Button btn_next = (Button) view.findViewById(R.id.btn_next);
        transport_content.setFocusable(true);
        transport_content.setFocusableInTouchMode(true);
        transport_content.requestFocus();
        InputMethodManager imm = (InputMethodManager) getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(transport_content, 0);

        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftInput(getContext());
                DispatchTaskActivity context = (DispatchTaskActivity) getContext();
                String content = transport_content.getText().toString();
                String note = transport_remark.getText().toString();
                context.setContent(content);
                context.setNote(note);
                context.jumpToNext(TransportFragment.this);
            }
        });

        view.findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftInput(getContext());
                getFragmentManager().popBackStack();
                ((DispatchTaskActivity) getContext()).notifyToolBar(TransportFragment.this);
            }
        });
        return view;
    }
    protected void hideSoftInput(Context context) {
        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        View currentFocus = ((Activity) context).getCurrentFocus();
        if (currentFocus != null) {
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
