package fr.dashboard.server;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;


/**
 * Donne accès à la page about.json
 */
@RestController
public class About {

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
    /**
     * Lit le fichier about.json, et l'actualise l'IP ainsi que le temps actuel avant de le mapper
     * @return about.json
     * @throws Exception
     */
    @RequestMapping({"/about.json"})
    public static String about() throws Exception {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream is = classloader.getResourceAsStream("about.json");
        String to_find = "\"current_time\": ";
        String to_find_2 = "\"host\": ";
        String data = convertStreamToString(is);
        StringBuilder tmp = new StringBuilder(data);
        tmp.insert(data.indexOf(to_find) + to_find.length(), System.currentTimeMillis());
        try(final DatagramSocket socket = new DatagramSocket()){
            socket.connect(InetAddress.getByName("8.8.8.8"), 8080);
            String ip = socket.getLocalAddress().getHostAddress();
            tmp.insert(30, "\"" + ip + "\"");
        }
        return tmp.toString();
    }
}