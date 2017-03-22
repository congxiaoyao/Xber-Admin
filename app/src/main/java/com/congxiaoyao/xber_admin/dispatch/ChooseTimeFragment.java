package com.congxiaoyao.xber_admin.dispatch;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.codbking.calendar.CaledarAdapter;
import com.codbking.calendar.CalendarBean;
import com.codbking.calendar.CalendarDateView;
import com.codbking.calendar.CalendarView;
import com.congxiaoyao.location.utils.RoundList;
import com.congxiaoyao.xber_admin.R;
import com.congxiaoyao.xber_admin.TAG;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;

import static com.congxiaoyao.xber_admin.WheelTestActivity.getHoursArray;
import static com.congxiaoyao.xber_admin.WheelTestActivity.getMinuteArray;

/**
 * Created by guo on 2017/3/17.
 */

public class ChooseTimeFragment extends Fragment {

    private RoundList<DateView> list;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    private Date parse1 = null,parse2 = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        CoordinatorLayout view = (CoordinatorLayout) inflater.inflate(R.layout.fragment_date_start, container, false);
//        FragmentStartDateBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_date_start, container, false);
        ((DispatchTaskActivity) (getContext())).showWeekLine();
        list = new RoundList<>(3);
        final NestedScrollView scrollView = (NestedScrollView) view.findViewById(R.id.scrollView);
        final BottomSheetBehavior behavior = BottomSheetBehavior.from(scrollView);
        Button btn_choose_time = (Button) view.findViewById(R.id.btn_choose_time);
        final TextView tv_start_time = (TextView) view.findViewById(R.id.tv_start_time);
        final TextView tv_end_time = (TextView) view.findViewById(R.id.tv_end_time);

        final CalendarDateView calendarDateView = (CalendarDateView) view.findViewById(R.id.calendarDateView);
        calendarDateView.setAdapter(new CaledarAdapter() {
            @Override
            public View getView(View view, ViewGroup viewGroup, final CalendarBean calendarBean) {
                long l = System.currentTimeMillis();
                if (view == null) {
                    view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_xiaomi, null);
                }
                final TextView text = (TextView) view.findViewById(R.id.text);
                text.setText("" + calendarBean.day);

                boolean onPage = false;
                onPage = onPage || isCompare(calendarBean);
                if (list.size() == 2) {
                    for (DateView dateView : list) {
                        onPage = onPage || (dateView.getBean().equals(equals(calendarBean)));
                    }
                } else if (list.size() == 3) {
                    for (int i = 1; i < 3; i++) {
                        onPage = onPage || (list.get(i).getBean().equals(equals(calendarBean)));
                    }
                } else if (list.size() == 1) {
                    onPage = onPage || (list.get(0).getBean().equals(equals(calendarBean)));
                }
                if (!onPage) {
                    view.setBackgroundResource(R.drawable.item_not_select_xiaomi);
                } else {
                    view.setBackgroundResource(R.drawable.item_select_xiaomi);
                }
                //mothFlag 0是当月，-1是月前，1是月后
                if (calendarBean.mothFlag != 0) {
                    text.setTextColor(0xff9299a1);
                } else {
                    text.setTextColor(0xff444444);
                }
                final View finalView = view;
                view.post(new Runnable() {
                    @Override
                    public void run() {
                        finalView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                list.add(new DateView(calendarBean,v));
                                for (int i = 0; i < list.size(); i++) {
                                    list.get(i).getView().setBackgroundResource(R.drawable.item_select_xiaomi);
                                }
                                if (list.size() == 3) list.get(0).getView().setBackgroundColor(Color.TRANSPARENT);
                                if (list.get(0).equals(list.get(1))) {

                                }
                                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                            }
                        });
                    }
                });
                return view;
            }
        });

        calendarDateView.setOnItemClickListener(new CalendarView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int i, CalendarBean calendarBean) {
                ((DispatchTaskActivity)getContext()).setToolBarTitle(calendarBean.toString());
            }
        });
        final WheelView wheelHour = (WheelView) view.findViewById(R.id.wheel_hour);
        final WheelView wheelMinute = (WheelView) view.findViewById(R.id.wheel_minute);

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

        btn_choose_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (list.size()==0)return;
                String time = (wheelHour.getCurrentItem() + 1)
                        + ":"
                        + wheelMinute.getCurrentItem();
                if (list.size() == 1) {
                    DateView dateView = list.get(0);
                    dateView.setTime(time);
                    tv_start_time.setText(dateView.getBean().toString() + " " + time);
                    behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    return;
                } else if (list.size() == 2) {
                    list.get(1).setTime(time);
                    if (!isFirst(list.get(0), list.get(1))) {
                        tv_start_time.setText(list.get(0).getBean().toString() + " " + list.get(0).getTime());
                        tv_end_time.setText(list.get(1).getBean().toString() + " " + list.get(1).getTime());
                        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        return;
                    } else {
                        tv_start_time.setText(list.get(1).getBean().toString() + " " + list.get(1).getTime());
                        tv_end_time.setText(list.get(0).getBean().toString() + " " + list.get(0).getTime());
                        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        return;
                    }

                } else if (list.size() == 3) {
                    list.get(2).setTime(time);
                    if (!isFirst(list.get(1), list.get(2))) {
                        tv_start_time.setText(list.get(1).getBean().toString() + " " + list.get(1).getTime());
                        tv_end_time.setText(list.get(2).getBean().toString() + " " + time);
                        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        return;
                    } else {
                        tv_start_time.setText(list.get(2).getBean().toString() + " " + time);
                        tv_end_time.setText(list.get(1).getBean().toString() + " " + list.get(1).getTime());
                        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        return;
                    }
                }
            }
        });

        return view;
    }

    protected float spToPx(int sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                sp, getContext().getResources().getDisplayMetrics());
    }
    protected float dpToPx(int sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                sp, getContext().getResources().getDisplayMetrics());
    }

    public class DateView {
        private CalendarBean bean;
        private View view;
        private String time;

        public DateView(CalendarBean bean, View view) {
            this.bean = bean;
            this.view = view;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public CalendarBean getBean() {
            return bean;
        }

        public void setBean(CalendarBean bean) {
            this.bean = bean;
        }

        public View getView() {
            return view;
        }

        public void setView(View view) {
            this.view = view;
        }
    }

    public boolean isFirst(DateView view1, DateView view2) {
        try {
            parse1 = simpleDateFormat.parse(view1.getBean().toString() + " " + view1.getTime());
            parse2 = simpleDateFormat.parse(view2.getBean().toString() + " " + view2.getTime());
        } catch (ParseException e) {
            Log.d(TAG.ME, "isFirst: ", e);
        }
        return parse1.getTime() - parse2.getTime() > 0;
    }

    public boolean isCompare(CalendarBean calendarBean) {
        if (list.size()==0||list==null) return false;
        int size = list.size();
        if (size == 3) {
            for (int i =1;i<size;i++) {
                if (calendarBean.equals(list.get(i).getBean())) return true;
            }
            return false;
        }
        for (DateView dateView : list) {
            if (dateView.getBean().equals(calendarBean)) return true;
        }
        return false;
    }

}
