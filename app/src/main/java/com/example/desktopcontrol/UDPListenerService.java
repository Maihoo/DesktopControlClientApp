package com.example.desktopcontrol;

import java.io.ByteArrayInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;


/*
 * Linux command to send UDP:
 * #socat - UDP-DATAGRAM:192.168.1.255:11111,broadcast,sp=11111
 */
public class UDPListenerService extends Service {
    static String UDP_BROADCAST = "UDPBroadcast";
    boolean f = true;
    Bitmap placeholder;

    //Boolean shouldListenForUDPBroadcast = false;
    DatagramSocket socket;

    public UDPListenerService() {    }

    private void listenAndWaitAndThrowIntent(InetAddress broadcastIP, Integer port) throws Exception {
        byte[] recvBuf = new byte[15000];
        if (socket == null || socket.isClosed()) {
            socket = new DatagramSocket(port);
            socket.setBroadcast(true);
        }
        //socket.setSoTimeout(1000);
        DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
        Log.e("UDP", "Waiting for UDP broadcast");

        String senderIP = "";
        String message = "";

        Bitmap decodedBitmap = null;
        socket.receive(packet);
        Log.e("UDP", "Waiting for UDP broadcast2");
        int size = 0;
        String recieved = new String(packet.getData(), 0, 16);
        Log.e("UDP", "size: " + recieved);
        byte[] result = new byte[size];
        byte[] imageAr = new byte[size];

        int nread;
        int total = 0;
        while(total < size) {

            placeholder = null;
            //Log.d("screenshot", "loop");

            socket.receive(packet);
            nread = packet.getLength();
            imageAr = packet.getData();

            for(int i = 0; i < nread; i++){
                if(total + i + 100 >= size) break;

                result[total + i] = imageAr[i];
            }
            total += nread;
        }
/*
        while(f) {
            Log.e("UDP", "why r u geh?");
            socket.receive(packet);
            Log.e("UDP", "bro?");
            senderIP = packet.getAddress().getHostAddress();
            message = new String(packet.getData()).trim();

            Log.e("UDP", "Got UDB broadcast from " + senderIP + ", message: " + message);



        }*/
        decodedBitmap = BitmapFactory.decodeByteArray(result, 0, packet.getData().length);
        placeholder = decodedBitmap;


        broadcastIntent(senderIP, message);
        socket.close();
    }

    private void broadcastIntent(String senderIP, String message) {
        Intent intent = new Intent(UDPListenerService.UDP_BROADCAST);
        intent.putExtra("sender", senderIP);
        intent.putExtra("message", message);
        sendBroadcast(intent);
    }

    Thread UDPBroadcastThread;

    public String startListenForUDPBroadcast() {
        UDPBroadcastThread = new Thread(new Runnable() {
            public void run() {
                try {
                    InetAddress broadcastIP = InetAddress.getByName("192.168.178.65"); //172.16.238.42 //192.168.1.255
                    Integer port = 11111;
                    while (shouldRestartSocketListen) {
                        listenAndWaitAndThrowIntent(broadcastIP, port);
                    }
                    //if (!shouldListenForUDPBroadcast) throw new ThreadDeath();
                } catch (Exception e) {
                    Log.i("UDP", "no longer listening for UDP broadcasts cause of error: " + e.getMessage());
                }
            }
        });
        UDPBroadcastThread.start();
        return "ah";
    }

    private Boolean shouldRestartSocketListen=true;

    void stopListen() {
        shouldRestartSocketListen = false;
        socket.close();
    }

    @Override
    public void onCreate() {

    };

    @Override
    public void onDestroy() {
        stopListen();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        shouldRestartSocketListen = true;
        startListenForUDPBroadcast();
        Log.i("UDP", "Service started");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
