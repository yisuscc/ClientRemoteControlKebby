package com.nuwarobotics.sample.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

import ContainerSocket.DataType;
import ContainerSocket.SocketByteContainer;


public class FrameStreamingActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    /*
     * Available resolutions for NB2 are:
     * width=1920 height=1080
     * width=1600 height=1200
     * width=1440 height=1080
     * width=1280 height=960
     * width=1280 height=768
     * width=1280 height=720
     * width=1024 height=768
     * width=800 height=600
     * width=800 height=480
     * width=720 height=480
     * width=640 height=480
     * width=640 height=360
     * width=480 height=360
     * width=480 height=320
     * width=352 height=288
     * width=320 height=240
     * width=176 height=144
     * width=160 height=120
     */

    private ImageView mImageFrame;

    final int WIDTH = 1280;
    final int HEIGHT = 768;
    int remotePortNumber = 49169;
    private Socket client;
    private String ipServer;
    private EditText etIP;
    private EditText etPort;
    private InputStream input;
    private OutputStream output;
    private Handler mHandlerUP = new Handler();
    private Handler mHandlerDown = new Handler();
    private Handler mHandlerLeft = new Handler();
    private Handler mHandlerRight = new Handler();
    private Handler mHandler;
    private FaceInfoView2 mFaceInfo;
    private Button btnConnect;
    private String serverIP;
    private AtomicBoolean streamingFlag = new AtomicBoolean(true);
    private long timeDelay = 100;

    private SocketByteContainer sbc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);
        etIP = findViewById(R.id.ipText);
        etPort = findViewById(R.id.portText);
        btnConnect = findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(this);
        mImageFrame = findViewById(R.id.img_frame);
        mFaceInfo = findViewById(R.id.faceInfo);
        findViewById(R.id.btnDisconnect).setOnClickListener(this);
        findViewById(R.id.leftButton).setOnTouchListener(this);
        findViewById(R.id.rightButton).setOnTouchListener(this);
        findViewById(R.id.upButton).setOnTouchListener(this);
        findViewById(R.id.downButton).setOnTouchListener(this);
        findViewById(R.id.onButton).setOnClickListener(this);
        findViewById(R.id.offButton).setOnClickListener(this);
        showDisconnectedButtons();


    }

    @Override
    protected void onDestroy() {
        try {
            if (null != client && !client.isClosed())
                if (client.isConnected()) {
                    streamingFlag.set(false);
                    sendCommand("general", "disconnect");
                    client.close();
                }

        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    private long delay = 500;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.downButton:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        if (mHandlerDown != null) {
                            mHandlerDown.removeCallbacks(back);
                            mHandlerDown = null;
                        }
                        break;
                    case MotionEvent.ACTION_DOWN:
                        mHandlerDown = new Handler();
                        mHandlerDown.postDelayed(back, timeDelay);
                        break;
                }
                break;
            case R.id.upButton:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        if (mHandlerUP != null) {
                            mHandlerUP.removeCallbacks(front);
                            mHandlerUP = null;
                        }
                        break;
                    case MotionEvent.ACTION_DOWN:
                        mHandlerUP = new Handler();
                        mHandlerUP.postDelayed(front, timeDelay);
                        break;
                }

                break;
            case R.id.leftButton:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        if (mHandlerLeft != null) {
                            mHandlerLeft.removeCallbacks(left);
                            mHandlerLeft = null;
                        }
                        break;
                    case MotionEvent.ACTION_DOWN:
                        mHandlerLeft = new Handler();
                        mHandlerLeft.postDelayed(left, timeDelay);
                        break;
                }
                break;
            case R.id.rightButton:
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        if (mHandlerRight != null) {
                            mHandlerRight.removeCallbacks(right);
                            mHandlerRight = null;
                        }
                        break;
                    case MotionEvent.ACTION_DOWN:
                        mHandlerRight = new Handler();
                        mHandlerRight.postDelayed(right, timeDelay);
                        break;
                }
                break;


        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.onButton:
                if (!streamingFlag.get()) {
                    streamingFlag.set(true);
                    sendCommand("streaming", "start");
                    turnOnStreaming();

                }
                break;
            case R.id.offButton:
                streamingFlag.set(false);
                sendCommand("streaming", "stop");
                break;
            case R.id.btnConnect:
                new Thread(() -> {
                    serverIP = etIP.getText().toString().trim();
                    remotePortNumber = Integer.parseInt((etPort.getText().toString().trim()));

                    Log.d("jesus", "retireved server ip");
                    // serverIP = "192.168.85.130";
                    // we connect to the socket
                    // Avoid duplication connections
                    try {
                        if (client != null && !client.isClosed()) {
                            client.close();
                        }
                    } catch (IOException e) {
                        //e.printStackTrace();
                        Log.w("jesus", "e.getMessage() = " + e.getMessage());
                    }

                    try {
                        client = new Socket(InetAddress.getByName(serverIP), remotePortNumber);
                        Log.d("socket", "client " + client + " try to connect " + serverIP + ":" + remotePortNumber);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    runOnUiThread(() -> showConnectedButtons());


                }).start();
                turnOnStreaming();

                break;
            case R.id.btnDisconnect:
                new Thread(() -> {
                    streamingFlag.set(false);
                    sendCommand("general", "disconnect");
                    runOnUiThread(() -> showDisconnectedButtons());
                }).start();
                break;

        }
    }

    private void showDisconnectedButtons() {
        (findViewById(R.id.ipText)).setVisibility(View.VISIBLE);
        (findViewById(R.id.portText)).setVisibility(View.VISIBLE);
        (findViewById(R.id.btnConnect)).setVisibility(View.VISIBLE);
        (findViewById(R.id.upButton)).setVisibility(View.INVISIBLE);
        (findViewById(R.id.downButton)).setVisibility(View.INVISIBLE);
        (findViewById(R.id.leftButton)).setVisibility(View.INVISIBLE);
        (findViewById(R.id.rightButton)).setVisibility(View.INVISIBLE);
        (findViewById(R.id.btnDisconnect)).setVisibility(View.INVISIBLE);
        (findViewById(R.id.offButton)).setVisibility(View.INVISIBLE);
        (findViewById(R.id.onButton)).setVisibility(View.INVISIBLE);

    }

    private void showConnectedButtons() {
        (findViewById(R.id.ipText)).setVisibility(View.INVISIBLE);
        (findViewById(R.id.portText)).setVisibility(View.INVISIBLE);
        (findViewById(R.id.btnConnect)).setVisibility(View.INVISIBLE);
        (findViewById(R.id.upButton)).setVisibility(View.VISIBLE);
        (findViewById(R.id.downButton)).setVisibility(View.VISIBLE);
        (findViewById(R.id.leftButton)).setVisibility(View.VISIBLE);
        (findViewById(R.id.rightButton)).setVisibility(View.VISIBLE);
        (findViewById(R.id.btnDisconnect)).setVisibility(View.VISIBLE);
        (findViewById(R.id.offButton)).setVisibility(View.VISIBLE);
        (findViewById(R.id.onButton)).setVisibility(View.VISIBLE);

    }

    private void turnOnStreaming() {

        new Thread(() -> {


            if (null != client) {
                while (!client.isClosed() && streamingFlag.get()) {
                    if (client.isConnected()) {
                        Log.i("socket", "socket connected");

                        if(streamingFlag.get()){
                            try {
                                Log.d("decode","prooced to invoke the received data");
                                receivedata2();

                            } catch (IOException | JSONException | ClassNotFoundException e) {
                                Log.d("decode", "counld decodethe information "+ e.getMessage());
                               ;
                            }
                        }



                    }
                }
            }
        }).start();
    }

    private final Runnable left = new Runnable() {
        @Override
        public void run() {
            sendCommand("moving", "turnLeft");
            mHandlerLeft.postDelayed(this, timeDelay);
        }
    };


    private final Runnable right = new Runnable() {
        @Override
        public void run() {
            sendCommand("moving", "turnRight");
            mHandlerRight.postDelayed(this, timeDelay);
        }
    };

    private final Runnable front = new Runnable() {
        @Override
        public void run() {
            sendCommand("moving", "frontward");
            mHandlerUP.postDelayed(this, timeDelay);
        }
    };

    private final Runnable back = new Runnable() {
        @Override
        public void run() {
            sendCommand("moving", "backward");
            mHandlerDown.postDelayed(this, timeDelay);
        }
    };

    private void receivedata2() throws IOException, ClassNotFoundException, JSONException {
        ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
        SocketByteContainer sbc = (SocketByteContainer)ois.readObject();
        if(sbc != null){
            switch (sbc.getDataType()){
                case BITMAP:
                    Log.d("decode","received a bitmap");
                    byte[] bytes = (byte[]) sbc.getDataArray();
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    runOnUiThread(() -> mImageFrame.setImageBitmap(bmp));
                    break;
                case JSON:
                    byte[] b = (byte[]) sbc.getDataArray();
                    String str = new String(b);
                    Log.d("decode","received a json"+str);
                    JSONObject dataInfo = new JSONObject(str);
                    mFaceInfo.setData(dataInfo);

                    break;
            }
        }
    }

   /* private void receiveData() throws IOException, JSONException {
        DataInputStream  dis = new DataInputStream(client.getInputStream());
        // leemos el primer entero que nos da la información del tamaño de bytes
        int dataSize = dis.readInt();
        byte[] dataWithHeader = new byte[dataSize];
        dis.read(dataWithHeader);
        byte[] data  = new byte[dataSize-1];
        Byte  header = 0;
        for(int i= 0; i<dataSize;i++){
            if(i==0){
               header = dataWithHeader[i];
            }
            else{//TODO: changing the ofset  i think we can remove this part
                data[i-1] = dataWithHeader[i];
            }
        }
        switch (header.intValue()){
            case 255: // is a bitmap
                Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                Log.i("decoded", "bitmap decoded");
                runOnUiThread(() -> mImageFrame.setImageBitmap(bmp));
                break;
            case 245: // is a string or a json
                // first we convert it to string
                String string=  new String(data);
                Log.i("decoded","JSON Decoded"+ string);
                JSONObject cmd = string != null ? new JSONObject(string) : null;
                // TODO: what we do wiith the json?11111111111111111111111
                break;

        }





        //

    }*/

    private void sendCommand(String property, String action) {
        new Thread(() -> {
            Log.d("jesus", "" + property + action);
            if (client != null && client.isConnected()) {

                try {
                    JSONObject cmd = new JSONObject();
                    cmd.put("property", property);
                    cmd.put("action", action);
                    SocketByteContainer sbc = new SocketByteContainer(DataType.JSON,cmd.toString().getBytes());
                    ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
                    oos.writeObject(sbc);
                    oos.flush();
                } catch (JSONException | IOException e) {

                    Log.d("jesus", "" + e.getMessage());
                }
            }

        }).start();

    }
}