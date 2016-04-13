package org.zywx.wbpalmstar.plugin.uexqupai;


public class Contant {

    /**
     * 默认最大时长
     */
    public static int DEFAULT_DURATION_MAX_LIMIT = 8;

    public static int DEFAULT_DURATION_LIMIT_MIN = 2;

    /**
     * 默认CRF参数
     */
    public static int DEFAULT_VIDEO_RATE_CRF = 6;

    /**
     * VideoPreset
     */
    public static String DEFAULT_VIDEO_Preset = "faster";
    /**
     * VideoLevel
     */
    public static int DEFAULT_VIDEO_LEVEL = 30;

    /**
     * VideoTune
     */
    public static String DEFAULT_VIDEO_TUNE = "zerolatency";
    /**
     * movflags_KEY
     */
    public static String DEFAULT_VIDEO_MOV_FLAGS_KEY = "movflags";


    public static String DEFAULT_VIDEO_MOV_FLAGS_VALUE = "+faststart";

    public static int DEFAULT_BEAUTY_SKIN_RATE = 80;

    public static String VIDEOPATH;

    public static String THUMBNAILPATH = VIDEOPATH + ".png";

    public static String accessToken;//accessToken 通过调用授权接口得到
    public static String space = "fred"; //存储目录 建议使用uid cid之类的信息
}
