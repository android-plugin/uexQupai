package org.zywx.wbpalmstar.plugin.uexqupai;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.text.TextUtils;
import android.util.Log;

import com.duanqu.qupai.android.app.QupaiDraftManager;
import com.duanqu.qupai.android.app.QupaiServiceImpl;
import com.duanqu.qupai.editor.EditorResult;
import com.duanqu.qupai.engine.session.MovieExportOptions;
import com.duanqu.qupai.engine.session.VideoSessionCreateInfo;
import com.duanqu.qupai.recorder.EditorCreateInfo;
import com.duanqu.qupai.upload.AuthService;
import com.duanqu.qupai.upload.QupaiAuthListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;

public class EUExQupai extends EUExBase {
    private static final String TAG = "EUExQupai";
    private float density;
    private final EditorCreateInfo createInfo = new EditorCreateInfo();
    private VideoSessionCreateInfo.Builder builder;
    private static int QUPAI_RECORD_REQUEST = 1;
    private static String CALLBACK_INIT= "uexQupai.cbInit";
    private static String CALLBACK_RECORD = "uexQupai.cbRecord";
    private String initCallbackId;
    private String recordCallbackId;

    private Context context;

    public EUExQupai(Context context, EBrowserView view) {
        super(context, view);
        this.context = context;
        density = context.getResources().getDisplayMetrics().density;
    }

    public void init(String params[]) {
        if (params == null &&  params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        if (params.length == 2) {
            initCallbackId = params[1];
        }
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(params[0]);
            String appKey = jsonObject.optString("appKey", "");
            String appSecret = jsonObject.optString("appSecret", "");
            String space = jsonObject.optString("space", "");
            if (TextUtils.isEmpty(appKey) || TextUtils.isEmpty(appSecret) || TextUtils.isEmpty(space)) {
                Log.i(TAG, "appKey, appSecret or space is empty");
                return;
            }
            initAuth(context, appKey, appSecret, space);
        } catch (Exception e) {
            Log.i(TAG, e.getMessage());
        }
    }

    public void config(String params[]) {
        String jsonStr = "{}";
        if (params != null &&  params.length > 0) {
            jsonStr = params[0];
        }
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(jsonStr);
            double minDuration = jsonObject.optDouble("minDuration", Contant.DEFAULT_DURATION_LIMIT_MIN);
            double maxDuration = jsonObject.optDouble("maxDuration", Contant.DEFAULT_DURATION_MAX_LIMIT);
            int rate = jsonObject.optInt("rate", 2000000);//默认200w
            int width = jsonObject.optInt("width", 640);
            int height = jsonObject.optInt("height", 480);
            boolean cameraFrontOn = jsonObject.optBoolean("cameraFrontOn", true);
            boolean openBeautySkin = jsonObject.optBoolean("openBeautySkin", true);
            int beautySkinRate = jsonObject.optInt("beautySkinRate", Contant.DEFAULT_BEAUTY_SKIN_RATE);//美颜比例

            MovieExportOptions movie_options = new MovieExportOptions.Builder()
                    .setVideoProfile("high")
                    .setVideoBitrate(rate)
                    .setVideoPreset(Contant.DEFAULT_VIDEO_Preset).setVideoRateCRF(Contant.DEFAULT_VIDEO_RATE_CRF)
                    .setOutputVideoLevel(Contant.DEFAULT_VIDEO_LEVEL)
                    .setOutputVideoTune(Contant.DEFAULT_VIDEO_TUNE)
                    .configureMuxer(Contant.DEFAULT_VIDEO_MOV_FLAGS_KEY, Contant.DEFAULT_VIDEO_MOV_FLAGS_VALUE)
                    .build();

            builder = new VideoSessionCreateInfo.Builder();
            builder.setOutputDurationMin(minDuration)
                    .setOutputDurationLimit((float) maxDuration)
                    .setMovieExportOptions(movie_options)
                    .setCameraFacing(cameraFrontOn ? Camera.CameraInfo.CAMERA_FACING_FRONT :
                            Camera.CameraInfo.CAMERA_FACING_BACK)
                    .setVideoSize(width, height)
                    .setCaptureHeight(118 * density) //固定值
                    .setFlashLightOn(true)
                    .setTimelineTimeIndicator(true);
            if (openBeautySkin) {
                builder.setBeautySkinViewOn(true);
                builder.setBeautySkinOn(true);
                if (beautySkinRate > 100 || beautySkinRate < 1) {
                    beautySkinRate = Contant.DEFAULT_BEAUTY_SKIN_RATE;
                    builder.setBeautyProgress(beautySkinRate);
                }
            }
            createInfo.setSessionCreateInfo(builder.build());
            createInfo.setNextIntent(null);
            createInfo.setOutputThumbnailSize(width, height);//输出图片宽高
            String videoPath = FileUtils.newOutgoingFilePath(context);
            createInfo.setOutputVideoPath(videoPath);//输出视频路径
            createInfo.setOutputThumbnailPath(videoPath + ".png");//输出图片路径
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
        }
    }

    public void record(String params[]) {
        if (params != null && params.length > 0) {
            recordCallbackId = params[0];
        }
        JSONObject jsonObject = new JSONObject();
        if (builder == null) {
            try {
                jsonObject.put("status", 1);
                jsonObject.put("error", "please call config method first");
                callback(CALLBACK_RECORD, jsonObject);
                return;
            } catch (JSONException e) {
                Log.i(TAG, e.getMessage());
            }
        }
        registerActivityResult();
        QupaiServiceImpl qupaiService= new QupaiServiceImpl.Builder()
                .setEditorCreateInfo(createInfo).build();
        qupaiService.showRecordPage((Activity) context, QUPAI_RECORD_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            EditorResult result = new EditorResult(data);
            if (result != null) {
                //得到视频path，和缩略图path的数组，返回十张缩略图,和视频时长
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("videoPath", result.getPath());
                    jsonObject.put("thumbnail", result.getPath() + ".png");
                    callback(CALLBACK_RECORD, jsonObject);
                } catch (JSONException e) {
                    Log.i(TAG, e.getMessage());
                }
                //删除草稿
                QupaiDraftManager draftManager = new QupaiDraftManager();
                draftManager.deleteDraft(data);
            }
        }
    }

    private void callback(String methodName, JSONObject result) {
        if(CALLBACK_INIT.equals(methodName)) {
            if (!TextUtils.isEmpty(initCallbackId)) {
                callbackToJs(Integer.parseInt(initCallbackId), false, result);
            } else {
                callBackPluginJs(methodName, result.toString());
            }
            return;
        }
        if (CALLBACK_RECORD.equals(methodName)) {
            if(!TextUtils.isEmpty(recordCallbackId)) {
                callbackToJs(Integer.parseInt(recordCallbackId), false, result);
            } else {
                callBackPluginJs(methodName, result.toString());
            }
        }
    }
    private void callBackPluginJs(String methodName, String jsonData){
        String js = SCRIPT_HEADER + "if(" + methodName + "){"
                + methodName + "('" + jsonData + "');}";
        onCallback(js);
    }

    private  void initAuth(Context context ,String appKey,String appsecret,String space){
        AuthService service = AuthService.getInstance();
        service.setQupaiAuthListener(new QupaiAuthListener() {
            @Override
            public void onAuthError(int errorCode, String message) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("status", 1);
                    jsonObject.put("code", errorCode);
                    callback(CALLBACK_INIT, jsonObject);
                } catch (JSONException e) {
                    Log.i(TAG, e.getMessage());
                }
            }

            @Override
            public void onAuthComplte(int responseCode, String responseMessage) {
                Contant.accessToken = responseMessage;
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("status", 0);
                    callback(CALLBACK_INIT, jsonObject);
                } catch (JSONException e) {
                    Log.i(TAG, e.getMessage());
                }
                Log.i(TAG, "auth complete");
            }
        });
        service.startAuth(context,appKey, appsecret, space);
    }

    @Override
    protected boolean clean() {
        return true;
    }
}
