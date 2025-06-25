package io.github.doblon8.jzbar;

import static io.github.doblon8.jzbar.bindings.zbar.*;

public class Config {
    public static final int ADD_CHECK = ZBAR_CFG_ADD_CHECK();
    public static final int ASCII = ZBAR_CFG_ASCII();
    public static final int BINARY = ZBAR_CFG_BINARY();
    public static final int ENABLE = ZBAR_CFG_ENABLE();
    public static final int EMIT_CHECK = ZBAR_CFG_EMIT_CHECK();
    public static final int MAX_LEN = ZBAR_CFG_MAX_LEN();
    public static final int MIN_LEN = ZBAR_CFG_MIN_LEN();
    public static final int NUM = ZBAR_CFG_NUM();
    public static final int POSITION = ZBAR_CFG_POSITION();
    public static final int TEST_INVERTED = ZBAR_CFG_TEST_INVERTED();
    public static final int UNCERTAINTY = ZBAR_CFG_UNCERTAINTY();
    public static final int X_DENSITY = ZBAR_CFG_X_DENSITY();
    public static final int Y_DENSITY = ZBAR_CFG_Y_DENSITY();
}
