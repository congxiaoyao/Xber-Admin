package com.congxiaoyao.xber_admin.driverslist;

import com.congxiaoyao.httplib.request.CarRequest;
import com.congxiaoyao.httplib.request.retrofit2.XberRetrofit;
import com.congxiaoyao.httplib.response.CarDetail;
import com.congxiaoyao.xber_admin.driverslist.module.DriverSection;
import com.congxiaoyao.xber_admin.mvpbase.presenter.ListLoadablePresenterImpl;
import com.congxiaoyao.xber_admin.utils.Token;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by guo on 2017/3/26.
 */

public class DriverListPresenterImpl extends ListLoadablePresenterImpl<DriverListContract.View>
        implements DriverListContract.Presenter {

    private Map<Character, Integer> indexMap = new HashMap<>();

    public DriverListPresenterImpl(DriverListContract.View view) {
        super(view);
    }

    @Override
    public Observable<? extends List> pullListData() {
        Observable<List<DriverSection>> observable = XberRetrofit.create(CarRequest.class)
                .getFreeCars(System.currentTimeMillis()+1000000000000L
                        ,System.currentTimeMillis()+    1000400000000L
                        ,Token.value)
                .map(new Func1<List<CarDetail>, List<DriverSection>>() {
                    @Override
                    public List<DriverSection> call(List<CarDetail> carDetails) {
                        List<DriverSection> drivers = new ArrayList<DriverSection>();
                        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
                        format.setCaseType(HanyuPinyinCaseType.UPPERCASE);
                        for (CarDetail carDetail : carDetails) {
                            DriverSection section = carDetail2DriverSection(carDetail, format);
                            if (section!=null) drivers.add(section);
                        }

                        Map<Character, List<DriverSection>> map = new HashMap<Character, List<DriverSection>>();
                        for (DriverSection driverSection : drivers) {
                            char c = driverSection.header.charAt(0);
                            List<DriverSection> list = map.get(c);
                            if (list == null) {
                                list = new ArrayList<DriverSection>();
                                map.put(c, list);
                            }
                            list.add(driverSection);
                        }
                        drivers.clear();
                        for (int i = 'A'; i <= 'Z'; i++) {
                            List<DriverSection> list = map.get((char)i);
                            if (list == null) continue;
                            drivers.add(new DriverSection(true, String.valueOf((char) i)));
                            drivers.addAll(list);
                        }
                        for (int i = 0; i < drivers.size(); i++) {
                            DriverSection section = drivers.get(i);
                            if (section.isHeader) {
                                indexMap.put(section.header.charAt(0), i);
                            }
                        }
                        int value = 0;
                        for (int i = 'A'; i <= 'Z'; i++) {
                            Integer index = indexMap.get((char) i);
                            if (index == null) {
                                indexMap.put((char) i, value);
                            }else {
                                value = index;
                            }
                        }
                        return drivers;
                    }
                });
        return observable;
    }

    public DriverSection carDetail2DriverSection(CarDetail carDetail,HanyuPinyinOutputFormat format) {
        String name = carDetail.getUserInfo().getName();
        DriverSection section = null;
        try {
            String[] names = PinyinHelper.toHanyuPinyinStringArray(name.charAt(0), format);
            section = new DriverSection(false, String.valueOf(names[0].charAt(0)));
            section.t = carDetail;
        } catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
            badHanyuPinyinOutputFormatCombination.printStackTrace();
        }
        return section;
    }

    @Override
    public int getIndexByChar(char x) {
        Integer integer = indexMap.get(x);
        return integer == null ? 0 : integer;
    }
}
