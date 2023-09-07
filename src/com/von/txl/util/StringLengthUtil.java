package com.von.txl.util;

import java.nio.charset.Charset;

public class StringLengthUtil {

    public static final String DEFAULT_CHARSET_NAME = "UTF-8";

    public static String left(String str, int size, char c) {
        if (null == str) {
            return null;
        }
        int strLength = calcStrLength(str);
        if (size <= 0 || size <= strLength) {
            return str;
        }
        return repeat(size - strLength, c).concat(str);
    }

    public static String right(String str, int size, char c) {
        if (null == str) {
            return null;
        }
        int strLength = calcStrLength(str);
        if (size <= 0 || size <= strLength) {
            return str;
        }
        return str.concat(repeat(size - strLength, c));
    }

    public static String center(String str, int size, char c) {
        if (null == str) {
            return null;
        }
        int strLength = calcStrLength(str);
        if (size <= 0 || size <= strLength) {
            return str;
        }
        str = left(str, strLength + (size - strLength) / 2, c);
        str = right(str, size, c);
        return str;
    }

    public static String left(String str, int size) {
        return left(str, size, ' ');
    }

    public static String right(String str, int size) {
        return right(str, size, ' ');
    }

    public static String center(String str, int size) {
        return center(str, size, ' ');
    }

    private static String repeat(int size, char c) {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < size; index++) {
            builder.append(c);
        }
        return builder.toString();
    }

    public static int calcStrLength(String str) {
        return calcStrLength(str, DEFAULT_CHARSET_NAME);
    }

    private static int calcStrLength(String str, String charset) {
        int len = 0;
        int index = 0;
        byte[] bytes = str.getBytes(Charset.forName(charset));
        while (bytes.length > 0) {
            short tmp = (short) (bytes[index] & 0xf0);
            if (tmp >= 0xb0) {
                if (tmp < 0xc0) {
                    index += 2;
                    len += 2;
                } else if (tmp == 0xc0 || tmp == 0xd0) {
                    index += 2;
                    len += 2;
                } else if (tmp == 0xe0) {
                    index += 3;
                    len += 2;
                } else if (tmp == 0xf0) {
                    short tmp0 = (short) (((short) bytes[index]) & 0xf0);
                    if (tmp0 == 0) {
                        index += 4;
                        len += 2;
                    } else if (tmp0 > 0 && tmp0 <= 11) {
                        index += 5;
                        len += 2;
                    } else if (tmp0 > 11) {
                        index += 6;
                        len += 2;
                    }
                }
            } else {
                index += 1;
                len += 1;
            }
            if (index > bytes.length - 1) {
                break;
            }
        }
        return len;
    }


    public static String processNull(String value) {
        return (null == value || value.isEmpty()) ? "" : value;
    }

    public static void main(String[] args) {
        System.out.println(StringLengthUtil.calcStrLength("中国"));
        System.out.println(StringLengthUtil.calcStrLength("CN"));
    }
}
