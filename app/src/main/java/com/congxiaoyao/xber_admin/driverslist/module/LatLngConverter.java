package com.congxiaoyao.xber_admin.driverslist.module;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by congxiaoyao on 2017/4/2.
 */
public class LatLngConverter<T> {

    private LatLngMapper<T> mapper;

    private LatLngConverter() {

    }

    public static <T> LatLngConverter<T> init(LatLngMapper<T> mapper) {
        LatLngConverter<T> converter = new LatLngConverter<>();
        converter.mapper = mapper;
        return converter;
    }

    public byte[] toByteArray(List<T> list) {
        if (list == null || list.size() == 0) return new byte[0];
        byte[] result = new byte[list.size() * 2 * 8];
        int resultP = 0, dataP = 0;
        while (dataP < list.size()) {
            T data = list.get(dataP++);
            resultP = doubleToBytes(mapper.getLng(data), result,
                    doubleToBytes(mapper.getLat(data), result, resultP));
        }
        return result;
    }

    public List<T> fromByteArray(byte[] array) {
        if (array.length % 16 != 0) throw new RuntimeException("数组不合法");
        int len = array.length / 16;
        List<T> result = new ArrayList<T>(len);
        for (int i = 0, j = 0; i < len; i++, j += 16) {
            T t = mapper.toObject(bytesToDouble(array, j),
                    bytesToDouble(array, j + 8));
            result.add(t);
        }
        return result;
    }

    /**
     * double转化为byte数组并存放运算结果到dest数组
     * @param d
     * @param dest
     * @param start 从dest中的那个位置开始
     * @return 下一个可插入数据的index
     */
    private static int doubleToBytes(double d, byte[] dest, int start) {
        long v = Double.doubleToLongBits(d);
        dest[start] = (byte) (v >>> 56);
        dest[start + 1] = (byte) (v >>> 48);
        dest[start + 2] = (byte) (v >>> 40);
        dest[start + 3] = (byte) (v >>> 32);
        dest[start + 4] = (byte) (v >>> 24);
        dest[start + 5] = (byte) (v >>> 16);
        dest[start + 6] = (byte) (v >>> 8);
        dest[start + 7] = (byte) v;
        return start + 8;
    }

    private static double bytesToDouble(byte[] readBuffer, int startIndex) {
        return Double.longBitsToDouble((((long) readBuffer[startIndex] << 56) |
                ((long) (readBuffer[startIndex + 1] & 0xff) << 48) |
                ((long) (readBuffer[startIndex + 2] & 0xff) << 40) |
                ((long) (readBuffer[startIndex + 3] & 0xff) << 32) |
                ((long) (readBuffer[startIndex + 4] & 0xff) << 24) |
                ((readBuffer[startIndex + 5] & 0xff) << 16) |
                ((readBuffer[startIndex + 6] & 0xff) <<  8) |
                ((readBuffer[startIndex + 7] & 0xff))));
    }
}
