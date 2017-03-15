package com.congxiaoyao.xber_admin.login;

import android.databinding.DataBindingUtil;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.congxiaoyao.Admin;
import com.congxiaoyao.xber_admin.R;
import com.congxiaoyao.xber_admin.databinding.FragmentLoginBinding;
import com.congxiaoyao.xber_admin.mvpbase.view.LoadableViewImpl;

/**
 * Created by congxiaoyao on 2017/3/15.
 */

public class LoginFragment extends LoadableViewImpl<LoginContract.Presenter>
        implements LoginContract.View ,View.OnClickListener{

    private FragmentLoginBinding binding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false);
        binding.btnLogin.getBackground().setColorFilter(ContextCompat.getColor(getContext(),
                R.color.colorPrimary), PorterDuff.Mode.SRC);
        binding.btnLogin.setOnClickListener(this);
        progressBar = (ContentLoadingProgressBar) binding.getRoot()
                .findViewById(R.id.content_progress_bar);
        Admin admin = presenter.getAdmin();
        if (admin != null) {
            String username = admin.getUserName();
            String password = admin.getPassword();
            if (username != null) binding.etUsername.setText(username);
            if (password != null) binding.etPassword.setText(password);
        }
        return binding.getRoot();
    }

    @Override
    public void showLoginError() {
        Toast.makeText(getContext(), "登陆失败", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showLoginSuccess() {
        Toast.makeText(getContext(), "登陆成功", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        String password = binding.etPassword.getText().toString().trim();
        String userName = binding.etUsername.getText().toString().trim();
        presenter.login(userName, password);
    }
}
