package com.nuwarobotics.sample.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    final int portNumber = 49169;
    private Socket client;
    private String ipServer;
    private EditText etIP;
	private InputStream input;
	private OutputStream output;
   // private Button btnSet;//unnecesary
    private Button btnConnect;
    private String serverIP;
    //TODO the gamepad button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);
        // we assign the buttons to a variable name
		etIP = findViewById(R.id.ipText);
		//btnSet = findViewById(R.id.setIp);//innecesario
		btnConnect = findViewById(R.id.btnConnect);
		mImageFrame = findViewById(R.id.img_frame);

		//btnConnect.setOnClickListener();
        initView();
    }

    @Override
    protected void onDestroy() {
		try {
			client.close();
		}catch (IOException e){
			e.printStackTrace();
		}
        super.onDestroy();
    }


    private void initView() {
    	btnConnect.setOnClickListener((v)->{
    		// first we get ip
			serverIP = etIP.getText().toString().trim();
			// we connect to the socket
			try {
				client = new Socket(serverIP,portNumber);
			}catch (IOException e){
				e.printStackTrace();
			}

			//we start the receive bitmap
			receiveBitmap();
			//
		});
    }
	private void receiveBitmap(){
		while(client.isConnected()){
			try {
				input = client.getInputStream();
				//TODO deermine buffer size i thinhk is height withd * 4
				//or *3
				int bytestoStore = WIDTH * HEIGHT* 4;
				byte []  buffer = new byte[bytestoStore]; //
				int bytesRead;
				// TODO How we decode the array into a bitmap
				/*
			byte[] buffer = new byte[bitmapSize];
            int bytesRead;
            ByteArrayOutputStream bitmapBuffer = new ByteArrayOutputStream();

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                bitmapBuffer.write(buffer, 0, bytesRead);
            }
				 */
			// i think it could be like this
				input.read(buffer);
				Bitmap bmp = BitmapFactory.decodeByteArray(buffer,0,bytestoStore);
				mImageFrame.setImageBitmap(bmp);



			}catch (IOException e){
				e.printStackTrace();
			}
		}
	}


}