package com.congxiaoyao.xber_admin.dispatch;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.codbking.calendar.CaledarAdapter;
import com.codbking.calendar.CalendarBean;
import com.codbking.calendar.CalendarDateView;
import com.codbking.calendar.CalendarView;
import com.congxiaoyao.location.utils.RoundList;
import com.congxiaoyao.xber_admin.R;
import com.congxiaoyao.xber_admin.TAG;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;

/**
 * Created by guo on 2017/3/17.
 */

public class ChooseTimeFragment extends Fragment {

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    private int todayPage;

    private TextView tvStartTime;
    private TextView tvEndTime;
    private TextView[] textViews;

    private BottomSheetBehavior<NestedScrollView> behavior;
    private NestedScrollView scrollView;
    private WheelView wheelHour;
    private WheelView wheelMinute;
    private TextView title;

    private DateTime tempDateTime = new DateTime();

    private RoundListC<DateTime> dateTimes = new RoundListC<>(2);
    private RoundList<View> views = new RoundList<>(2);
    private CalendarDateView calendarDateView;
    private int thisYear;
    private int thisMonth;
    private int thisDay;
    private String dateTitle;

    @Nullable
    public View onCreateView1(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FrameLayout frameLayout = new FrameLayout(container.getContext());
        frameLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        View loadingBar = inflater.inflate(R.layout.view_progress_bar, frameLayout, false);
        loadingBar.setVisibility(View.VISIBLE);
        frameLayout.addView(loadingBar);
        return frameLayout;
    }

    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                              Bundle savedInstanceState) {
        final CoordinatorLayout view = (CoordinatorLayout) inflater.inflate(R.layout.fragment_date_start,
                container, false);
        getDispatchTaskActivity().showWeekLine();
        getDispatchTaskActivity().showToolbarButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jumpToToday();
            }
        });
        scrollView = (NestedScrollView) view.findViewById(R.id.scrollView);
        scrollView.setTag(null);
        behavior = BottomSheetBehavior.from(scrollView);
        behavior.setBottomSheetCallback(new MyBottomSheetCallback());
        Button chooseTimeButton = (Button) view.findViewById(R.id.btn_choose_time);
        title = (TextView) view.findViewById(R.id.tv_title);
        chooseTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTimeSelected(wheelHour.getCurrentItem() + 1, wheelMinute.getCurrentItem());
                scrollView.setTag(true);
                behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        tvStartTime = (TextView) view.findViewById(R.id.tv_start_time);
        tvEndTime = (TextView) view.findViewById(R.id.tv_end_time);
        textViews = new TextView[]{tvStartTime, tvEndTime};

        calendarDateView = (CalendarDateView) view
                .findViewById(R.id.calendarDateView);
        calendarDateView.setAdapter(new MyCalendarAdapter());
        calendarDateView.addOnPageChangeListener(new MyPageListener());
        calendarDateView.setOnItemClickListener(new CalendarView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int i, CalendarBean bean) {
                String title = bean.year + "年" + bean.moth + "月";
                dateTitle = title;
                getDispatchTaskActivity().setToolBarTitle(title);
            }
        });
        final Calendar calendar = Calendar.getInstance();
        thisYear = calendar.get(Calendar.YEAR);
        thisMonth = calendar.get(Calendar.MONTH) + 1;
        thisDay = calendar.get(Calendar.DAY_OF_MONTH);
        if (dateTitle == null) {
            dateTitle = thisYear + "年" + thisMonth + "月";
        }
        ((DispatchTaskActivity) getContext()).setToolBarTitle(dateTitle);
        wheelHour = (WheelView) view.findViewById(R.id.wheel_hour);
        wheelMinute = (WheelView) view.findViewById(R.id.wheel_minute);

        ArrayWheelAdapter<String> viewAdapter = new ArrayWheelAdapter<>(getContext(),
                getHoursArray());
        viewAdapter.setTextSize(20);
        viewAdapter.setTextPaddingTop((int) dpToPx(2));
        viewAdapter.setTextPaddingBottom((int) dpToPx(2));
        viewAdapter.setTextColor(ContextCompat.getColor(getContext(), R.color.colorDarkGray));
        viewAdapter.setEnableMultiTextColor(true);
        viewAdapter.setTextSelectedColor(ContextCompat.getColor(getContext(),
                R.color.colorPrimaryDark));
        wheelHour.setViewAdapter(viewAdapter);
        wheelHour.setDrawShadows(false);
        wheelHour.setHint("点");
        wheelHour.setCurrentItem(35);
        wheelHour.setHintSizePx((int) spToPx(12));
        wheelHour.setHintPaddingPx((int) dpToPx(10));
        wheelHour.setWheelForeground(R.drawable.wheel_val_gray);
        View.OnTouchListener touchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                scrollView.stopNestedScroll();
                return false;
            }
        };
        wheelHour.setOnTouchListener(touchListener);

        viewAdapter = new ArrayWheelAdapter<>(getContext(), getMinuteArray());
        viewAdapter.setTextColor(ContextCompat.getColor(getContext(), R.color.colorDarkGray));
        viewAdapter.setEnableMultiTextColor(true);
        viewAdapter.setTextSelectedColor(ContextCompat.getColor(getContext(),
                R.color.colorPrimaryDark));
        viewAdapter.setTextSize(20);
        viewAdapter.setTextPaddingTop((int) dpToPx(2));
        viewAdapter.setTextPaddingBottom((int) dpToPx(2));
        wheelMinute.setViewAdapter(viewAdapter);
        wheelMinute.setDrawShadows(false);
        wheelMinute.setHint("分");
        wheelMinute.setCurrentItem(35);
        wheelMinute.setHintSizePx((int) spToPx(12));
        wheelMinute.setHintPaddingPx((int) dpToPx(10));
        wheelMinute.setWheelForeground(R.drawable.wheel_val_gray);
        wheelMinute.setOnTouchListener(touchListener);

        wheelMinute.post(new Runnable() {
            @Override
            public void run() {
                todayPage = calendarDateView.getCurrentItem();
            }
        });

        view.findViewById(R.id.btn_choose_time_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (views.size() != 2) {
                    Toast.makeText(getContext(), "请继续你的表演", Toast.LENGTH_SHORT).show();
                    return;
                }
                long start = dateTimes.getFirst().toTime();
                long end = dateTimes.getLast().toTime();
                if (start < end) {
                    long temp = start;
                    start = end;
                    end = temp;
                }
                //TODO jump to next fragment
                Log.d(TAG.ME, "onClick: start" + start);
                Log.d(TAG.ME, "onClick: end" + end);
                DispatchTaskActivity context = (DispatchTaskActivity) getContext();
                context.setStartTime(start);
                context.setEndTime(end);
                context.setToday(dateTitle);
                context.jumpToNext(ChooseTimeFragment.this);
            }
        });
        return view;
    }

    private void jumpToToday() {
        int currentItem = calendarDateView.getCurrentItem();
        while (currentItem != todayPage) {
            if (currentItem > todayPage) currentItem--;
            if (currentItem < todayPage) currentItem++;
            calendarDateView.setCurrentItem(currentItem, true);
        }
    }

    private DispatchTaskActivity getDispatchTaskActivity() {
        return (DispatchTaskActivity) (getContext());
    }

    public class MyBottomSheetCallback extends BottomSheetBehavior.BottomSheetCallback {

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            Log.d(TAG.ME, "onStateChanged: "+newState);
            if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                Object tag = bottomSheet.getTag();
                if (tag != null) {
                    bottomSheet.setTag(null);
                } else {
                    onTimeCancel();
                }
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {

        }
    }

    public class MyCalendarAdapter implements CaledarAdapter {

        @Override
        public View getView(View view, ViewGroup viewGroup, final CalendarBean calendarBean) {
            long l = System.currentTimeMillis();
            if (view == null) {
                view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.item_xiaomi, null);
            }
            final TextView text = (TextView) view.findViewById(R.id.text);
            text.setText(String.valueOf(calendarBean.day));

            //mothFlag 0是当月，-1是月前，1是月后
            if (calendarBean.mothFlag != 0) {
                text.setTextColor(0xff9299a1);
            } else {
                text.setTextColor(0xff444444);
            }
            clearViews(view, calendarBean);
            final View finalView = view;
            final View.OnClickListener l1 = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (behavior.getState() == BottomSheetBehavior.STATE_SETTLING) return;
                    if (behavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        return;
                    }
                    title.setText(dateTimes.addTimes % 2 == 0 ?
                            R.string.please_select_start_time : R.string.please_select_end_time);
                    int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                    int minute = Calendar.getInstance().get(Calendar.MINUTE);
                    wheelHour.setCurrentItem(hour - 1);
                    wheelMinute.setCurrentItem(minute);
                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    onDateSelected(finalView, calendarBean);
                }
            };
            view.post(new Runnable() {
                @Override
                public void run() {
                    finalView.setOnClickListener(l1);
                }
            });
            return view;
        }

        private void clearViews(View view, CalendarBean calendarBean) {
            boolean shouldSelect = false;
            for (int i = 0; i < dateTimes.size(); i++) {
                DateTime dateTime = dateTimes.get(i);
                shouldSelect = shouldSelect | dateTime.bean.equals(calendarBean);
            }
            if (shouldSelect) {
                view.setBackgroundResource(R.drawable.item_select_xiaomi);
            } else if (isToday(calendarBean)) {
                view.setBackgroundResource(R.drawable.item_today_xiaomi);
            } else {
                view.setBackgroundColor(Color.TRANSPARENT);
            }
        }

    }

    private boolean isToday(CalendarBean bean) {
        return (bean.year == thisYear &&
                bean.moth == thisMonth &&
                bean.day == thisDay);
    }

    private void onDateSelected(View view, CalendarBean bean) {
        Log.d(TAG.ME, "onDateSelected: " + calendarDateView.getCurrentItem());
        tempDateTime.bean = bean;
        if (views.size() == 2) {
            for (int i = 0; i < 2; i++) {
                views.get(i).setBackgroundColor(Color.TRANSPARENT);
            }
            views.removeAll();
            textViews[0].setText(R.string.please_select_start_time);
            textViews[1].setText(R.string.please_select_end_time);
        }
        views.add(view);
        for (int i = 0; i < 2; i++) {
            views.get(i).setBackgroundResource(R.drawable.item_select_xiaomi);
        }
    }

    private void onTimeSelected(int hour, int minute) {
        DateTime dateTime = new DateTime();
        dateTime.bean = tempDateTime.bean;
        dateTime.hour = hour;
        dateTime.minute = minute;
        dateTimes.add(dateTime);
        for (int i = 0; i < dateTimes.size(); i++) {
            textViews[i].setText(dateTimes.get(i).toNiceString());
        }
        changeTextIfNeed();
    }

    private void onTimeCancel() {
        if (views.size() == 0) return;
        if (isToday(tempDateTime.bean)) {
            views.getLast().setBackgroundResource(R.drawable.item_today_xiaomi);
        }else {
            views.getLast().setBackgroundColor(Color.TRANSPARENT);
        }
        if (views.size() == 1) {
            dateTimes.removeAll();
            views.removeAll();
        } else if (views.size() == 2) {
            View first = views.getFirst();
            DateTime dt = dateTimes.getFirst();
            views.removeAll();
            dateTimes.removeAll();
            views.add(first);
            dateTimes.add(dt);
        }
    }

    private void changeTextIfNeed() {
        if (dateTimes.size() < 2) return;
        DateTime dateTime0 = dateTimes.get(0);
        DateTime dateTime1 = dateTimes.get(1);
        long t1 = dateTime0.toTime();
        long t2 = dateTime1.toTime();
        if (t1 > t2) {
            CharSequence text = textViews[0].getText();
            textViews[0].setText(textViews[1].getText());
            textViews[1].setText(text);
        }
    }

    protected float spToPx(int sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                sp, getContext().getResources().getDisplayMetrics());
    }

    protected float dpToPx(int sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                sp, getContext().getResources().getDisplayMetrics());
    }

    private static String[] getMinuteArray() {
        String[] minute = new String[60];
        for (int i = 0; i < 10; i++) {
            minute[i] = "0" + i;
        }
        for (int i = 10; i <= 59; i++) {
            minute[i] = i + "";
        }
        return minute;
    }

    private static String[] getHoursArray() {
        String[] hours = new String[24];
        for (int i = 1; i < 10; i++) {
            hours[i - 1] = "0" + i;
        }
        for (int i = 10; i <= 24; i++) {
            hours[i - 1] = i + "";
        }
        return hours;
    }

    public class DateTime {
        CalendarBean bean;
        int hour = -1;
        int minute = -1;

        @Override
        public String toString() {
            return bean + " " + hour + ":" + minute;
        }

        public String toNiceString() {
            StringBuilder builder = new StringBuilder();
            builder.append(bean.year).append("-")
                    .append(bean.moth).append("-")
                    .append(bean.day).append("  ")
                    .append(hour).append(":")
                    .append(minute);
            return builder.toString();
        }

        long toTime() {
            try {
                return simpleDateFormat.parse(toString()).getTime();
            } catch (ParseException e) {
                Log.d(TAG.ME, "toTime: ", e);
            }
            return -1;
        }
    }

    private class RoundListC<T> extends RoundList<T>{

        int addTimes = 0;

        public RoundListC(int limitSize) {
            super(limitSize);
        }

        @Override
        public void add(T t) {
            super.add(t);
            addTimes++;
        }
    }

    private class MyPageListener extends ViewPager.SimpleOnPageChangeListener {
        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == 1 && behavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        }
    }
}
