package tcpServer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class tipServer {
    public static void main(String[] args) {
        try {
            ServerSocket server = new ServerSocket(7777);
	    HashMap<String, Object> hm = new HashMap<String, Object>();
	    while(true){
                System.out.println("waiting....connect");
		Socket sock = server.accept();
		TipThread tipThread = new TipThread(sock, hm);
		tipThread.start();
	    }
	}
    }
}
