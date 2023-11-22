package com.nuwarobotics.sample.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
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

public class FrameStreamingActivity extends AppCompatActivity implements View.OnClickListener {

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

    private Button btnConnect;
    private String serverIP;
    private AtomicBoolean streamingFlag = new AtomicBoolean(true);


    //TODO the gamepad but
    // ton

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);
        // we assign the buttons to a variable name
        etIP = findViewById(R.id.ipText);
        etPort = findViewById(R.id.portText);
        //btnSet = findViewById(R.id.setIp);//innecesario
        btnConnect = findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(this);
        mImageFrame = findViewById(R.id.img_frame);
        findViewById(R.id.upButton).setOnClickListener(this);
        findViewById(R.id.downButton).setOnClickListener(this);
        findViewById(R.id.leftButton).setOnClickListener(this);
        findViewById(R.id.rightButton).setOnClickListener(this);
        findViewById(R.id.onButton).setOnClickListener(this);
        findViewById(R.id.offButton).setOnClickListener(this);

    }

    @Override
    protected void onDestroy() {
        try {
            if (null != client && !client.isClosed())
                client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) { //TODO: convert into an on Touch instead of an on click ?
        switch (v.getId()) {
            case R.id.downButton:
                sendCommand("moving","backward");
                break;
            case R.id.upButton:
                sendCommand("moving","frontward");
                break;
            case R.id.leftButton:
                sendCommand("moving","turnLeft");
                break;
            case R.id.rightButton:
                sendCommand("moving","turnRight");
                break;
            case R.id.onButton:
              if(!streamingFlag.get()){
                  streamingFlag.set(true);
                  sendCommand("streaming","start");
                  turnOnStreaming();
              }

                break;
            case R.id.offButton:
                streamingFlag.set(false);
                sendCommand("streaming","stop");
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



                }).start();
                break;
        }
    }

    private void turnOnStreaming() {// Should we use thread instead of a void or a flag inside the while

        new Thread(() -> {


            if (null != client) {
                while (!client.isClosed()&& streamingFlag.get()) {
                    if (client.isConnected()) {
                        Log.i("socket", "socket connected");
                        try {
                            ObjectInputStream ois = new ObjectInputStream(client.getInputStream());
                            byte[] bytes = (byte[]) ois.readObject();

                            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//                        Bitmap bmp = BitmapFactory.decodeStream(client.getInputStream());
                            Log.i("bmp", "bitmap decoded");
                            runOnUiThread(() -> mImageFrame.setImageBitmap(bmp));
                        } catch (Exception e) {
                            Log.w("jesus", "parse bmp exception message = " + e.getMessage());
                        }
                    }
                }
            }
        }).start();
    }

    private enum CONNECTION_STATE {
        DISCONNECTED, CONNECTING, CONNECTED
    }

    private void sendCommand(String property,String action) {//Should receive the parameters instead to cleaner?
   new Thread(()-> {
      if(client.isConnected() && client != null){
          try {
              JSONObject cmd = new JSONObject();
              cmd.put("property",property);
              cmd.put("action",action);
              ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream());
              oos.writeObject(cmd);
              oos.flush();
          } catch (JSONException | IOException e) {
              e.printStackTrace();
          }
      }

   }).start();

    }
}