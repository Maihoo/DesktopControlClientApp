package com.example.desktopcontrol;

import androidx.appcompat.app.AppCompatActivity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;


public class MainActivity extends AppCompatActivity {

    Long ts1, ts2;

    ImageView lamp;
    ImageView background;
    ImageView crosshair;
    TextView xAxis;
    TextView yAxis;
    TextView info;
    int screen = 2, moveScreen = 2;
    int x, y;
    int crosshairX = 500, crosshairY = 500, moveX = 500, moveY = 500;
    int diffX = 0;
    int diffY = 0;
    Button connectBTN, screenshotBTN, clickBTN, doubleClickBTN, screenOneBTN, screenTwoBTN, screenThreeBTN, enterTextBTN, moveBTN, arrowleftBTN, arrowrightBTN;
    EditText enterText;
    boolean connected, blockClicker, moveSelected;
    boolean diffCanChange = true;
    Thread thread;
    ConnectionManager connectionManager = new ConnectionManager();
    UDPListenerService udp;

    private VelocityTracker mVelocityTracker = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        getWindow().getDecorView().setSystemUiVisibility(flags);

        final View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    decorView.setSystemUiVisibility(flags);
                }
            }
        });

        setContentView(R.layout.activity_main);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        crosshair = findViewById(R.id.crosshair);

        background = findViewById(R.id.background);
        background.setImageResource(R.drawable.backgroundsmall);
        background.setScaleX(1);
        background.setScaleY(1);
        enterText = findViewById(R.id.enterText);
        xAxis = findViewById(R.id.xAxis);
        yAxis = findViewById(R.id.yAxis);
        info = findViewById(R.id.info);

        screenshotBTN = findViewById(R.id.screenshotBTN);
        screenshotBTN.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                takeScreenshot();
            }
        });

        connectBTN =  findViewById(R.id.connectBTN);
        connectBTN.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                connect();
            }
        });

        moveBTN = findViewById(R.id.moveBTN);
        moveBTN.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                move();
            }
        });

        arrowleftBTN = findViewById(R.id.arrowleftBTN);
        arrowleftBTN.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(screen != 1) {
                    connectionManager.arrowleft(screen);
                    checkConnection();
                    try { thread.sleep(500); } catch (Exception e) { e.printStackTrace(); }
                    if(screen == 2) {screen = 1; connectionManager.screenChangeOne();}
                    if(screen == 3) {screen = 2; connectionManager.screenChangeTwo();}
                    checkConnection();
                    try { thread.sleep(500); } catch (Exception e) { e.printStackTrace(); }
                    takeScreenshot();
                }
            }
        });

        arrowrightBTN = findViewById(R.id.arrowrightBTN);
        arrowrightBTN.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(screen != 3) {
                    connectionManager.arrowright(screen);
                    checkConnection();
                    try { thread.sleep(500); } catch (Exception e) { e.printStackTrace(); }
                    if (screen == 2) { screen = 3; connectionManager.screenChangeThree(); }
                    if (screen == 1) { screen = 2; connectionManager.screenChangeTwo(); }
                    checkConnection();
                    try { thread.sleep(500); } catch (Exception e) { e.printStackTrace(); }
                    takeScreenshot();
                }
            }
        });

        clickBTN = findViewById(R.id.clickBTN);
        clickBTN.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(screen == 1) {
                    int temp = 1920 - (1920-crosshairX);
                    crosshairX = 1080-crosshairY;
                    crosshairY = temp;
                }
                connectionManager.click(crosshairX, crosshairY);
                checkConnection();
                try { thread.sleep(500); } catch (Exception e) { e.printStackTrace(); }
                takeScreenshot();
            }
        });

        doubleClickBTN = findViewById(R.id.doubleClickBTN);
        doubleClickBTN.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(screen == 1) {
                    int temp = 1920 - (1920-crosshairX);
                    crosshairX = 1080-crosshairY;
                    crosshairY = temp;
                }
                connectionManager.doubleClick(crosshairX, crosshairY);
                checkConnection();
                try { thread.sleep(500); } catch (Exception e) { e.printStackTrace(); }
                takeScreenshot();
            }
        });

        enterTextBTN = findViewById(R.id.enterTextBTN);
        enterTextBTN.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                connectionManager.sendText((enterText.getText()).toString());
                checkConnection();
                try { thread.sleep(500); } catch (Exception e) { e.printStackTrace(); }
                takeScreenshot();
            }
        });

        screenOneBTN = findViewById(R.id.screenOneBTN);
        screenOneBTN.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                connectionManager.screenChangeOne();
                checkConnection();
                screen = 1;
                try { thread.sleep(500); } catch (Exception e) { e.printStackTrace(); }
                takeScreenshot();
            }
        });

        screenTwoBTN = findViewById(R.id.screenTwoBTN);
        screenTwoBTN.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                connectionManager.screenChangeTwo();
                checkConnection();
                screen = 2;
                try { thread.sleep(500); } catch (Exception e) { e.printStackTrace(); }
                takeScreenshot();
            }
        });

        screenThreeBTN = findViewById(R.id.screenThreeBTN);
        screenThreeBTN.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                connectionManager.screenChangeThree();
                checkConnection();
                screen = 3;
                try { thread.sleep(500); } catch (Exception e) { e.printStackTrace(); }
                takeScreenshot();
            }
        });

        try {
            connect();
            connect();
            checkConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.e("UDP", "test1");
        udp = new UDPListenerService();
        Log.e("UDP", udp.startListenForUDPBroadcast());
        Log.e("UDP", "test2");
    }

    public void takeScreenshot2(){
        while(udp.placeholder == null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        background.setImageBitmap(udp.placeholder);

    }

    public void takeScreenshot(){
        while (!connectionManager.open){
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        double timestamp1 = System.currentTimeMillis();

        connectionManager.screenshot();
        while(!connectionManager.open){
            //Log.d("check", "DELAY");
            double timestamp2 = System.currentTimeMillis();
            if(timestamp2 - timestamp1 > 4000) {
                Log.d("MainActivity", "" + (timestamp2-timestamp1));
                connectionManager.clear();
                break;
            }
        }

        if(screen == 1) {
            background.setRotation(-90);
            background.setScaleX((float) 1.8);
            background.setScaleY((float) 1.8);
        } else {
            background.setRotation(0);
            background.setScaleX(1);
            background.setScaleY(1);
        }

        background.setImageBitmap(connectionManager.placeholder);
        Log.d("check", "DONE WITH SCREENSHOT METHOD");

        //retakeScreenshot();
    }

    public void connect() {
        Log.d("check", "connect");
        connectionManager.start();
        while (!connectionManager.connected){
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        checkConnection();
    }

    void checkConnection() {
        Log.d("check", "checkConnection");
        if(connectionManager.connected) {
            connected = true;
            connectBTN.setBackgroundTintList(this.getBaseContext().getResources().getColorStateList(R.color.green));
            Log.d("connecting...", "connection successful");
            info.setText("connection successful");
        } else {
            connected = false;
            connectBTN.setBackgroundTintList(this.getBaseContext().getResources().getColorStateList(R.color.grey));
            Log.d("connecting...", "not connected");
            info.setText("no connection");
        }
    }

    void move(){
        if(screen == 1) {
            int temp = 1920 - (1920-crosshairX);
            crosshairX = 1080-crosshairY;
            crosshairY = temp;
        }
        if(moveSelected){
            connectionManager.move(moveX, moveY, crosshairX, crosshairY, moveScreen, screen);
            moveBTN.setBackgroundTintList(this.getBaseContext().getResources().getColorStateList(R.color.grey));
            moveSelected = false;
            checkConnection();
            try { thread.sleep(500); } catch (Exception e) { e.printStackTrace(); }
            takeScreenshot();
        }
        else {
            moveSelected = true;
            moveX = crosshairX;
            moveY = crosshairY;
            moveScreen = screen;
            moveBTN.setBackgroundTintList(this.getBaseContext().getResources().getColorStateList(R.color.green));
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        //float width = background.getWidth();

        int[] viewCoords = new int[2];
        background.getLocationOnScreen(viewCoords);

        int touchX = (int) event.getX();
        //touchX = (int) ((touchX * (width/1920))    +    ((1920 - 1920*(width/1920) )));
        int touchY = (int) event.getY();

        int imageX = (int) ((float) touchX*((float) 1920/(float) 1920)) - viewCoords[0];
        int imageY = (int) ((float) touchY*((float) 1080/(float) 1080)) - viewCoords[1];

        if(diffCanChange){
            diffX =  imageX - (int) crosshair.getX();
            diffY =  imageY - (int) crosshair.getY();
            diffCanChange = false;
        }

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                ts1 = System.currentTimeMillis()/1000;

                if(!blockClicker) {
                    if (mVelocityTracker == null) {
                        // Retrieve a new VelocityTracker object to watch the
                        // velocity of a motion.
                        mVelocityTracker = VelocityTracker.obtain();
                    } else {
                        // Reset the velocity tracker back to its initial state.
                        mVelocityTracker.clear();
                    }

                    if (connected) {
                        x = imageX;
                        y = imageY;
                        //wantsToClick = true;
                    }

                    // Add a user's movement to the tracker.
                    mVelocityTracker.addMovement(event);
                }
                else {
                    blockClicker = false;
                }
                break;
            case MotionEvent.ACTION_MOVE:

                xAxis.setText(" " + (imageX-diffX));
                yAxis.setText(" " + (imageY-diffY));
                
                crosshairX = imageX-diffX;
                crosshairY = imageY-diffY;

                if(crosshairX > 1920) crosshairX = 1920;
                if(crosshairY > 1080) crosshairY = 1080;
                if(crosshairX < 0   ) crosshairX = 0;
                if(crosshairY < 0   ) crosshairY = 0;

                crosshair.setX(crosshairX-50);
                crosshair.setY(crosshairY-50);

                break;
            case MotionEvent.ACTION_UP:
                ts2 = System.currentTimeMillis()/1000;

                if(ts2-ts1 < 0.2 && connectionManager.connected) {
                    //wantsToClick = true;
                    //connectionManager.click(crosshairX, crosshairY);
                }

                if (connected) {
                    diffCanChange = true;
                    //wantsToClick = true;
                    //connectionManager.click(crosshairX, crosshairY);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                mVelocityTracker.recycle();
                break;
        }
        return true;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }
}


