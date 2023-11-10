package com.nuwarobotics.sample.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.codertainment.dpadview.DPadView;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;


public class FrameStreamingActivity extends AppCompatActivity {

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
    // private Button btnSet;//unnecesary
    private Button btnConnect;
    private String serverIP;
    Thread bitmapThread;
    private  static DPadView gamepad;
    //TODO the gamepad button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);
        // we assign the buttons to a variable name
        etIP = findViewById(R.id.ipText);
        etPort  = findViewById(R.id.portText);
        //btnSet = findViewById(R.id.setIp);//innecesario
        btnConnect = findViewById(R.id.btnConnect);
       // gamepad = findViewById(R.id.dpad);


        //btnConnect.setOnClickListener();
        initView();
    }

    @Override
    protected void onDestroy() {
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    private enum CONNECTION_STATE {
        DISCONNECTED, CONNECTING, CONNECTED
    }

    private void initView() {


        btnConnect.setOnClickListener((v) -> {
            // first we get ip
            new Thread(() -> {
               serverIP = etIP.getText().toString().trim();
               remotePortNumber = Integer.parseInt((etPort.getText().toString().trim()));

                Log.d("jesus", "retireved server ip");
               // serverIP = "192.168.85.130";
                // we connect to the socket
                try {
                    client = new Socket(InetAddress.getByName(serverIP), remotePortNumber);
                    Log.d("socket","client connected");
                }catch (IOException e) {
                   //e.printStackTrace();
                    Log.w("jesus",e.getMessage());
                }

                //we start the receive bitmap
               // receiveBitmap();
                receiveTest();
            }).start();

            //
        });



    }

    private void receiveBitmap() {
        while (client.isConnected()) {
            try {
                input = client.getInputStream();
                //TODO deermine buffer size i thinhk is height withd * 4
                //or *3
                int bytestoStore = WIDTH * HEIGHT * 4;
                byte[] buffer = new byte[bytestoStore]; //
                int bytesRead;
                // TODO How we decode the array into a bitmap
				/*
			byte[] buffer = new byte[bitmapSize];
            int bytesRead;
            ByteArrayOutputStream bitmapBuffer = new ByteArrayOutputStream();

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                bitmapBuffer.write(buffer, 0, bytesRead);
            }*/

                // i think it could be like this
                input.read(buffer);
                Bitmap bmp = BitmapFactory.decodeByteArray(buffer, 0, bytestoStore);
                mImageFrame.setImageBitmap(bmp);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //   });
private void receiveTest(){

while (null != client){
    if(client.isConnected()){
        try {
            ObjectInputStream ois = new ObjectInputStream(input);
            byte[] bytes = (byte[])ois.readObject();
            Bitmap  bmp = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
            runOnUiThread(()-> ((ImageView)findViewById(R.id.img_frame)).setImageBitmap(bmp));

        }catch (Exception e){
            Log.w("Jesus","parse bmp exception message" +e.getMessage());
        }

    }
}
}
    //TODO Send command
    private  void sendCommand(){





    }



    }



