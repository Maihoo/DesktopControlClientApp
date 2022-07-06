package com.example.desktopcontrol;

import android.app.ExpandableListActivity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteBuffer;

public class ConnectionManager implements Runnable {

    String resp;
    boolean connected, running;
    boolean open = false;
    private PrintWriter out;
    private InputStream is;
    private BufferedReader bufferedReader;
    Thread thread;
    Bitmap placeholder;

    public String ip = "31.16.121.18";
    public int port1 = 8787;
    public int port2 = 8686;

    public synchronized void start() {
        open = false;
        if(running) {
            open = true;
            return;
        }
        running = true;
        thread = new Thread(this);
        thread.start();
        open = true;
    }

    @Override
    public void run() {

        try {
            Socket s = null;
            String resp = "";

            while(!connected) {
                Log.d("IMPORTANT","not blocked");
                try {
                    Log.d("connecting...", "trying to create socket");
                    s = new Socket(ip, port1);
                    Log.d("connecting...", "socket created");

                    out = new PrintWriter(s.getOutputStream(), true);
                    is = s.getInputStream();
                    bufferedReader = new BufferedReader(new InputStreamReader(is));
                    Log.d("connecting...", "is, br and os created");

                    out.println("check");
                    out.flush();
                    Log.d("connecting...", "check sent");

                    resp = bufferedReader.readLine();
                    Log.d("connecting...", "response read: " + resp);
                    //info.setText("response read");

                    if (resp.equalsIgnoreCase("granted")) {
                        connected = true;

                        Log.d("connecting...", "successfully connected");

                    }
                } catch(Exception e ) {Log.d("error","couldn't connect to a server.." + e.toString());}
            }
        }
        catch(Exception e){ Log.d("error","connection not possible2: " + e.toString());}
    }

    public void screenshot(){

        open = false;
        placeholder = null;

        new Thread() {
            @Override
            public void run() {
                if(connected) {
                    try{
                        out.println("screenshot" + "\r\n");
                        out.flush();
                        Log.d("screenshot", "sent screenshot");

                        /*byte[] sizeAr = new byte[4];
                        is.read(sizeAr);
                        int size = ByteBuffer.wrap(sizeAr).asIntBuffer().get(); */

                        resp = bufferedReader.readLine();
                        Log.d("screenshot", "response: " + resp);

                        resp = bufferedReader.readLine();
                        Log.d("screenshot", "size: " + resp);
                        int size = Integer.valueOf(resp);

                        Log.d("screenshot", "size: " + size);

                        byte[] imageAr = new byte[size];
                        byte[] result = new byte[size];
                        int nread = 0;
                        int total = 0;

                        Log.d("screenshot", "before loop");

                        Socket s2 = new Socket(ip, port2);
                        Log.d("screenshot...", "socket 2 created");

                        InputStream is2 = s2.getInputStream();


                        try { thread.sleep(500); } catch (Exception e) {  Log.d("screenshot", "Error: " + size); e.printStackTrace(); }

                        while(total < size) {

                            placeholder = null;
                            //Log.d("screenshot", "loop");
                            nread = is2.read(imageAr);


                            for(int i = 0; i < nread; i++){
                                if(total + i + 100 >= size) break;

                                result[total + i] = imageAr[i];
                            }
                            total += nread;


                        }
                        Log.d("screenshot", "after loop");

                        Bitmap decodedBitmap = null;
                        decodedBitmap = BitmapFactory.decodeByteArray(result, 0, result.length);

                        Log.d("screenshot PROOF", "size:" + size + " nread: " + nread + " total: " + total);

                        placeholder = decodedBitmap;
                        open = true;

                    } catch (Exception e) { Log.d("screenshot ERROR", "screenshot failed: " + e.toString()); }
                }
            }
        }.start();
    }

    public void move(final int x1, final int y1, final int x2, final int y2, final int moveScreen, final int screen){
        open = false;
        new Thread() {
            @Override
            public void run() {
                if(connected) {
                    try{
                        out.println("move" + "\r\n");
                        out.flush();

                        out.println(x1 + ":" + y1 + ":" + x2 + ":" +  y2 + ":" + moveScreen + ":" + screen + "\r\n");
                        out.flush();

                        open = true;
                    } catch (Exception e) { Log.d("move ERROR", "click failed: " + e.toString()); }
                }
            }
        }.start();
    }

    public void click(final int crosshairX, final int crosshairY){

        open = false;
        new Thread() {
            @Override
            public void run() {
                if(connected) {
                    try{
                        out.println("click" + "\r\n");
                        out.flush();

                        out.println(crosshairX + ":" + crosshairY + "\r\n");
                        out.flush();

                        open = true;
                    } catch (Exception e) { Log.d("click ERROR", "click failed: " + e.toString()); }
                }
            }
        }.start();
    }

    public void doubleClick(final int crosshairX, final int crosshairY){
        open = false;
        new Thread() {
            @Override
            public void run() {
                if(connected) {
                    try{
                        out.println("doubleClick");
                        out.flush();

                        out.println(crosshairX + ":" + crosshairY);
                        out.flush();

                        open = true;
                    } catch (Exception e) { Log.d("doubleClick ERROR", "double click failed: " + e.toString()); }
                }
            }
        }.start();
    }

    public void sendText(final String textEntered){
        open = false;
        new Thread() {
            @Override
            public void run() {
                if(connected) {
                    try{

                        out.println("enterText" + "\r\n");
                        out.flush();

                        resp = "";

                        while(resp.length() < 2) resp = bufferedReader.readLine();
                        Log.d("text","textline read: " + resp);

                        out.println(textEntered);
                        Log.d("text","textEntered: " + textEntered);
                        out.flush();

                        out.println("done");
                        Log.d("text", "done printed");
                        out.flush();

                        resp = "";
                        while(resp.length() < 1) resp = bufferedReader.readLine();
                        Log.d("text", "response: " + resp);

                        open = true;
                    } catch (Exception e) { }
                }
            }
        }.start();
    }

    public void screenChangeOne(){
        open = false;
        new Thread() {
            @Override
            public void run() {
                if (connected) {
                    out.println("screenChangeOne" + "\r\n");
                    out.flush();
                    do{
                        try {
                            resp = bufferedReader.readLine();
                        } catch (IOException e) { e.printStackTrace(); }
                    } while(!resp.equalsIgnoreCase("success"));

                    Log.d("IMPORTANT", "resp: " + resp);

                    open = true;
                }
            }
        }.start();
    }

    public void screenChangeTwo(){
        open = false;
        new Thread() {
            @Override
            public void run() {
                if (connected) {
                    out.println("screenChangeTwo" + "\r\n");
                    out.flush();
                    do{
                        try {
                            resp = bufferedReader.readLine();
                        } catch (IOException e) { e.printStackTrace(); }
                    } while(!resp.equalsIgnoreCase("success"));

                    Log.d("IMPORTANT", "resp: " + resp);

                    open = true;
                }
            }
        }.start();
    }

    public void screenChangeThree(){
        open = false;
        new Thread() {
            @Override
            public void run() {
                if (connected) {
                    out.println("screenChangeThree" + "\r\n");
                    out.flush();
                    do{
                        try {
                            resp = bufferedReader.readLine();
                        } catch (IOException e) { e.printStackTrace(); }
                    } while(!resp.equalsIgnoreCase("success"));

                    Log.d("IMPORTANT", "resp: " + resp);

                    open = true;
                }
            }
        }.start();
    }

    public void arrowleft(int scrn){
        open = false;
        new Thread() {
            @Override
            public void run() {
                if (connected) {
                    out.println("arrowleft" + "\r\n");
                    out.flush();
                    do{
                        try {
                            resp = bufferedReader.readLine();
                        } catch (IOException e) { e.printStackTrace(); }
                    } while(!resp.equalsIgnoreCase("success"));

                    Log.d("IMPORTANT", "resp: " + resp);

                    open = true;
                }
            }
        }.start();
    }

    public void arrowright(int scrn){
        open = false;
        new Thread() {
            @Override
            public void run() {
                if (connected) {
                    out.println("arrowright" + "\r\n");
                    out.flush();
                    do{
                        try {
                            resp = bufferedReader.readLine();
                        } catch (IOException e) { e.printStackTrace(); }
                    } while(!resp.equalsIgnoreCase("success"));

                    Log.d("IMPORTANT", "resp: " + resp);

                    open = true;
                }
            }
        }.start();
    }

    public void clear(){
        open = false;
        new Thread() {
            @Override
            public void run() {
                if(connected) {
                    try{
                        out.println("clear" + "\r\n");
                        out.flush();

                        open = true;

                    } catch (Exception e) { }
                }
            }
        }.start();
    }
}
