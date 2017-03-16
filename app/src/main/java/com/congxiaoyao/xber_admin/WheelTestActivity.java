package com.congxiaoyao.xber_admin;

import android.databinding.DataBindingUtil;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;

import com.congxiaoyao.xber_admin.databinding.ActivityWheelTestBinding;

import kankan.wheel.widget.adapters.ArrayWheelAdapter;

import static com.baidu.mapapi.BMapManager.getContext;

public class WheelTestActivity extends AppCompatActivity {

    ActivityWheelTestBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,
                R.layout.activity_wheel_test);
        ArrayWheelAdapter<String> viewAdapter = new ArrayWheelAdapter<>(getContext(),
                getYearsArray());
        viewAdapter.setTextSize(36);
        viewAdapter.setTextColor(ContextCompat.getColor(getContext(), R.color.colorDarkGray));
        viewAdapter.setEnableMultiTextColor(true);
        viewAdapter.setTextSelectedColor(ContextCompat.getColor(WheelTestActivity.this,
                R.color.colorBlack));
        binding.wheelYear.setViewAdapter(viewAdapter);
        binding.wheelYear.setDrawShadows(false);
        binding.wheelYear.setHint("年");
        binding.wheelYear.setCurrentItem(35);
        binding.wheelYear.setHintSizePx((int) spToPx(12));
        binding.wheelYear.setHintPaddingPx((int) dpToPx(8));
        binding.wheelYear.setWheelForeground(R.drawable.wheel_val_gray);

        viewAdapter = new ArrayWheelAdapter<>(getContext(), getMonthsArray());
        viewAdapter.setTextSize(36);
        viewAdapter.setEnableMultiTextColor(true);
        viewAdapter.setTextColor(ContextCompat.getColor(getContext(), R.color.colorDarkGray));
        viewAdapter.setTextSelectedColor(ContextCompat.getColor(WheelTestActivity.this,
                R.color.colorBlack));
        binding.wheelMonth.setViewAdapter(viewAdapter);
        binding.wheelMonth.setDrawShadows(false);
        binding.wheelMonth.setHint("月");
        binding.wheelMonth.setHintSizePx((int) spToPx(12));
        binding.wheelMonth.setHintPaddingPx((int) dpToPx(8));
        binding.wheelMonth.setWheelForeground(R.drawable.wheel_val_gray);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        int currentItem = binding.wheelYear.getCurrentItem();
        ArrayWheelAdapter adapter = (ArrayWheelAdapter) binding.wheelYear.getViewAdapter();
        CharSequence yyyy = adapter.getItemText(currentItem);
        adapter = (ArrayWheelAdapter) binding.wheelMonth.getViewAdapter();
        CharSequence MM = adapter.getItemText(binding.wheelMonth.getCurrentItem());
        Log.d(TAG.ME, "onBackPressed: " + yyyy + ":" + MM);
    }

    protected float spToPx(int sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                sp, getContext().getResources().getDisplayMetrics());
    }
    protected float dpToPx(int sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                sp, getContext().getResources().getDisplayMetrics());
    }

    protected static String[] years = null;

    public static String[] getYearsArray() {
        if (years == null) {
            String[] str = new String[60];
            for (int i = 0; i < str.length; i++) {
                str[i] = String.valueOf(i + 1960);
            }
            years = str;
        }
        return years;
    }

    public static void clearYears() {
        years = null;
    }


    protected static String[] highs = null;

    public static int getIndexByYear(int year) {
        if(year< 1960) return 0;
        if (year >= 2020) return 60;
        return year - 1960;
    }

    public static String[] getMonthsArray() {
        return new String[]{"01","02"
                ,"03"
                ,"04"
                ,"05"
                ,"06"
                ,"07"
                ,"08"
                ,"09"
                ,"10"
                ,"11"
                ,"12"};
    }
}
