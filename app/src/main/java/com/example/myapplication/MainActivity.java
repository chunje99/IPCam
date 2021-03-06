package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.VideoView;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.INTERNET,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.BIND_ACCESSIBILITY_SERVICE
    };

    /**
     * Checks if the app has permission to write to device storage
     * <p>
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyPermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private SurfaceHolder.Callback surfaceListener = new SurfaceHolder.Callback() {

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // TODO Auto-generated method stub
            //camera.release();
            Log.e("surface", "Destroyed");
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            //// TODO Auto-generated method stub
            //camera = Camera.open();
            //try {
            //   camera.setPreviewDisplay(holder);
            //} catch (IOException e) {
            //    // TODO Auto-generated catch block
            //    e.printStackTrace();
            //}
            Log.e("surface", "Created");
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            //// TODO Auto-generated method stub
            //Camera.Parameters parameters = camera.getParameters();
            //parameters.setPreviewSize(width, height);
            //camera.startPreview();
            Log.e("surface", "Changed");
        }
    };

    private SurfaceView mPreview = null;
    private SurfaceHolder mHolder = null;
    String Path = "";
    Camera mCamera = null;
    Socket mSocket = null;
    DataOutputStream mOS = null;
    int mWidth = 0;
    int mHeight= 0;
    String TAG = "MyApp";
    private void AppInfo(){
        Log.i(TAG, "BOARD = " + Build.BOARD);
        Log.i(TAG, "BRAND = " + Build.BRAND);
        Log.i(TAG, "CPU_ABI = " + Build.CPU_ABI);
        Log.i(TAG, "DEVICE = " + Build.DEVICE);
        Log.i(TAG, "DISPLAY = " + Build.DISPLAY);
        Log.i(TAG, "FINGERPRINT = " + Build.FINGERPRINT);
        Log.i(TAG, "HOST = " + Build.HOST);
        Log.i(TAG, "ID = " + Build.ID);
        Log.i(TAG, "MANUFACTURER = " + Build.MANUFACTURER);
        Log.i(TAG, "MODEL = " + Build.MODEL);
        Log.i(TAG, "PRODUCT = " + Build.PRODUCT);
        Log.i(TAG, "TAGS = " + Build.TAGS);
        Log.i(TAG, "TYPE = " + Build.TYPE);
        Log.i(TAG, "USER = " + Build.USER);
        Log.i(TAG, "VERSION.RELEASE = " + Build.VERSION.RELEASE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // 화면을 portrait(세로) 화면으로 고정하고 싶은 경우
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        // 화면을 landscape(가로) 화면으로 고정하고 싶은 경우

        //setContentView(R.layout.main);
        // setContentView()가 호출되기 전에 setRequestedOrientation()이 호출되어야 함

        Log.e("Main", "onCreate");
        AppInfo();
        verifyPermissions(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if( mPreview == null) {
            mPreview = findViewById(R.id.surface);
            //mPreview.getHolder().setFixedSize(740, 360);
            mHolder = mPreview.getHolder();
            //mHolder.addCallback(this);
            mHolder.addCallback(surfaceListener);
        }

        String sd = Environment.getExternalStorageDirectory().getAbsolutePath();
        Path = sd + "/recvideo.mp4";


        Button fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCamera == null) {
                    Thread conThread = new Thread(conSocket);
                    conThread.start();
                    //Thread sendThread = new Thread(sendMsg);
                    //sendThread.start();

                    mPreview.setVisibility(View.VISIBLE);
                    //Thread conThread = new Thread(null, conSocket, "conSocket");
                    //conThread.start();
                    mCamera = Camera.open(0);
                    Camera.Parameters parameters = mCamera.getParameters();
                    Log.d("PreviewFormat :", String.valueOf(parameters.getPreviewFormat()));
                    Log.d("PreviewFrameRate:", String.valueOf(parameters.getPreviewFrameRate()));
                    Log.d("PreviewWidth:", String.valueOf(parameters.getPreviewSize().width));
                    Log.d("PreviewHeight:", String.valueOf(parameters.getPreviewSize().height));
                    parameters.setPreviewFormat(ImageFormat.YV12);
                    parameters.setPreviewFrameRate(30);
                    try {
                        mCamera.setPreviewDisplay(mHolder);
                        Display display = ((WindowManager)getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
                        mWidth = parameters.getPreviewSize().width;
                        mHeight = parameters.getPreviewSize().height;
                        if( !Build.PRODUCT.equals("sdk_gphone_x86")) {
                            mWidth = 640;
                            mHeight = 360;
                        }
                        int tmp = mWidth;
                        mCamera.setDisplayOrientation(0);
                        /*
                        if(display.getRotation() == Surface.ROTATION_0) {
                            //mWidth = mHeight;
                            //mHeight = tmp;
                            mCamera.setDisplayOrientation(90);
                            Log.d("Camera :", "0");
                        } else if(display.getRotation() == Surface.ROTATION_90) {
                            mCamera.setDisplayOrientation(180);
                            Log.d("Camera :", "90");
                        } else if(display.getRotation() == Surface.ROTATION_180) {
                            mCamera.setDisplayOrientation(0);
                            Log.d("Camera :", "180");
                        } else if(display.getRotation() == Surface.ROTATION_270) {
                            mCamera.setDisplayOrientation(0);
                            Log.d("Camera :", "90");
                        }
                        */
                        parameters.setPreviewSize(mWidth, mHeight);
                    } catch (IOException e) {
                        Log.e("Camera", e.getMessage());
                    }
                    mCamera.setParameters(parameters);
                    mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                        private long timestamp = 0;

                        public synchronized void onPreviewFrame(byte[] data, Camera camera) {
                            Log.v("CameraTest", "Time Gap = " + (System.currentTimeMillis() - timestamp) + " : size = " + String.valueOf(data.length));
                            timestamp = System.currentTimeMillis();
                            try {
                                //Thread sendThread = new Thread(sendMsg);
                                //encode(data);
                                Thread sendThread = new Thread(new sendMessage(mOS, data));
                                sendThread.start();
                                //camera.addCallbackBuffer(data);
                            } catch (Exception e) {
                                Log.e("CameraTest", "addCallbackBuffer error :" + e.getMessage());
                                return;
                            }
                            return;
                        }
                    });
                    //initCodec();
                    mCamera.startPreview();
                    //mCamera.unlock();
                } else {
                    mCamera.autoFocus (new Camera.AutoFocusCallback() {
                        public void onAutoFocus(boolean success, Camera camera) {
                            if(success){
                                Log.d("Camera", "autoFocus Success");
                            }
                            Log.d("Camera", "autoFocus");
                        }

                    });

                }
            }
        });
        Button fab2 = findViewById(R.id.fab2);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCamera != null) {
                    Log.i("Media", "stop camera");
                    mCamera.stopPreview();
                    mCamera.setPreviewCallback(null);
                    mCamera.release();
                    mCamera = null;
                }
                //Thread stopThread = new Thread(stopRecorde);
                //Log.i("Media", "stop Thread Created");
                //stopThread.start();
                Log.d("fab2", "stop");

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        Log.e("Main", "onDestroy in");
        super.onDestroy();
        /*
        if (mCamera != null) {
            Log.i("Media", "stop camera");
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
        */
        Log.e("Main", "onDestroy out");
    }

    public Runnable conSocket = new Runnable() {
        public void run() {
            Log.d("conSocket", "in");
            try {
                if(mSocket != null){
                    mSocket.close();
                    mSocket = null;
                }
                mSocket = new Socket("192.168.0.16", 8888);

                //mFileDescriptor = ParcelFileDescriptor.fromSocket(mSocket);
                mOS = new DataOutputStream(mSocket.getOutputStream());
                Log.d("start", "Connection Socket");
                //mSocket.close();
            } catch (IOException e) {
                Log.d("start", e.getMessage());
            }
            Log.d("conSocket", "out");
        }
    };

    class sendMessage implements Runnable {
        byte[] mData;
        DataOutputStream mDos;

        sendMessage(DataOutputStream dos, byte[] data) {
            mDos= dos;
            //mData = data;
            //mData = YV12toYUV420PackedSemiPlanar(data, mWidth, mHeight);
            mData = YV12toYUV420Planar(data, mWidth, mHeight);
            //mData = convertYUV420_NV21toRGB8888(data, mWidth, mHeight);
        }

        public void run() {
            Log.d("sendMsg", "in");
            //encode(mData);
            try {
                mDos.write(mData);
                //mDos.writeUTF("This is the first type of message.");
                mDos.flush();
                Log.d("start", "sendMsg  Socket");
            } catch (IOException e) {
                Log.d("start", e.getMessage());
            }
            Log.d("sendMsg", "out");
        }
    }

    public byte[] swapYV12toI420(byte[] yv12bytes, int width, int height) {
        byte[] i420bytes = new byte[yv12bytes.length];
        for (int i = 0; i < width*height; i++)
            i420bytes[i] = yv12bytes[i];
        for (int i = width*height; i < width*height + (width/2*height/2); i++)
            i420bytes[i] = yv12bytes[i + (width/2*height/2)];
        for (int i = width*height + (width/2*height/2); i < width*height + 2*(width/2*height/2); i++)
            i420bytes[i] = yv12bytes[i - (width/2*height/2)];
        return i420bytes;
    }
    public static byte[] YV12toYUV420PackedSemiPlanar(final byte[] input, final int width, final int height) {
        /*
         * COLOR_TI_FormatYUV420PackedSemiPlanar is NV12
         * We convert by putting the corresponding U and V bytes together (interleaved).
         */
        byte[] output = new byte[input.length];
        final int frameSize = width * height;
        final int qFrameSize = frameSize/4;

        System.arraycopy(input, 0, output, 0, frameSize); // Y

        for (int i = 0; i < qFrameSize; i++) {
            output[frameSize + i*2] = input[frameSize + i + qFrameSize]; // Cb (U)
            output[frameSize + i*2 + 1] = input[frameSize + i]; // Cr (V)
        }
        return output;
    }
    public static byte[] YV12toYUV420Planar(byte[] input, int width, int height) {
        /*
         * COLOR_FormatYUV420Planar is I420 which is like YV12, but with U and V reversed.
         * So we just have to reverse U and V.
         */
        byte[] output = new byte[input.length];
        final int frameSize = width * height;
        final int qFrameSize = frameSize/4;

        System.arraycopy(input, 0, output, 0, frameSize); // Y
        System.arraycopy(input, frameSize, output, frameSize + qFrameSize, qFrameSize); // Cr (V)
        System.arraycopy(input, frameSize + qFrameSize, output, frameSize, qFrameSize); // Cb (U)

        return output;
    }
}