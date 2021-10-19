package fr.dashboard.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.CompositeFilter;

import javax.servlet.Filter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.sql.*;
import java.util.*;

@SpringBootApplication
@RestController
@EnableOAuth2Client
@EnableAuthorizationServer
@Order(200)
public class DashboardServer extends WebSecurityConfigurerAdapter {

    @Qualifier("oauth2ClientContext")
    @Autowired
    OAuth2ClientContext oauth2ClientContext;
    private TestRestTemplate weatherprevRestTemplate = new TestRestTemplate();

    @RequestMapping({"/weatherprev"})
    public ResponseEntity<Map> weatherprev(@RequestParam("location") String principal) throws IOException {
        String location = principal;
        if (principal != null && !principal.equals("")) {
            location = principal;
        } else {
            location = "Nice";
        } //
        final String REST_SERVICE_URI = "http://api.openweathermap.org/data/2.5/forecast?q=" + location + "&appid=";
        ResponseEntity<Map> entity = this.weatherprevRestTemplate.getForEntity(REST_SERVICE_URI, Map.class);
        return entity;
    }

    private TestRestTemplate testRestTemplate = new TestRestTemplate();

    @RequestMapping({"/weather"})
    public ResponseEntity<Map> weather(@RequestParam("location") String principal) throws IOException {
        if (principal != null && !principal.equals("")) {
            File file = null;
            file = ResourceUtils.getFile("user.config");
            String content = new String(Files.readAllBytes(file.toPath()));
            String newStr = content.substring(0, content.indexOf(" weather="));
            String search = content.substring(0, content.indexOf("{"));
            String lastStr = content.substring(content.indexOf(", dweather="), content.indexOf("}x"));
            String str = newStr + " weather=" + principal + lastStr + "}x";
            File fff = new File("user.config");
            if (!fff.exists())
                fff.createNewFile();
            try (FileWriter f = new FileWriter("cine.config")) {
                f.write(str);
            } catch (IOException e) {
                e.printStackTrace();
            }
            StringBuilder sb = new StringBuilder();
            File fl = new File("users.config");
            if (!fl.exists())
                fl.createNewFile();
            try {
                fl = ResourceUtils.getFile("users.config");
                String c = new String(Files.readAllBytes(fl.toPath()));
                Scanner s = new Scanner(c);
                while (s.hasNextLine()) {
                    String line = s.nextLine();
                    if (!line.contains(search)) {
                        sb.append(line);
                        sb.append("\n");
                    }
                }
                s.close();
                sb.append(str);
                File users = new File("users.config");
                if (!users.exists())
                    users.createNewFile();
                try (FileWriter myfile = new FileWriter("users.config")) {
                    myfile.write(sb.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            final String REST_SERVICE_URI = "http://api.openweathermap.org/data/2.5/weather?q=" + principal + ",fr&appid=";
            ResponseEntity<Map> entity = this.testRestTemplate.getForEntity(REST_SERVICE_URI, Map.class);
            return entity;
        }
        return null;
    }

    private TestRestTemplate cine2Template = new TestRestTemplate();

    @RequestMapping({"/cici"})
    public ResponseEntity<Map> cici(@RequestParam("location") String principal) throws IOException {
        final String REST_CINE_URI = "https://api.themoviedb.org/3/search/movie?api_key=&language=fr&query=" + principal + "&page=1&include_adult=false\n";
        return this.cine2Template.getForEntity(REST_CINE_URI, Map.class);
    }

    private TestRestTemplate cineTemplate = new TestRestTemplate();

    @RequestMapping({"/cinema"})
    public ArrayList<ArrayList<String>> cinema(@RequestParam("location") String principal) throws IOException {
        final String REST_CINE_URI = "https://api.themoviedb.org/3/movie/now_playing?api_key=&language=" + principal + "page=1";
        ResponseEntity<Map> res = this.cineTemplate.getForEntity(REST_CINE_URI, Map.class);
        File user = new File("cine.config");
        if (!user.exists())
            user.createNewFile();
        try (FileWriter file = new FileWriter("cine.config")) {
            file.write(res.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        File file = new File("cine.config");
        if (!file.exists())
            file.createNewFile();
        file = ResourceUtils.getFile("cine.config");
        String content = new String(Files.readAllBytes(file.toPath()));
        Scanner scanner = new Scanner(content);

        ArrayList<ArrayList<String>> arr = new ArrayList<ArrayList<String>>();
        while (scanner.hasNextLine()) {
            ArrayList<String> splitString = new ArrayList<String>();
            for (String s : scanner.nextLine().split(",")) {
                boolean isFound3 = s.contains("poster_path=");
                if (isFound3) {
                    s = s.replace(" ", "");
                    s = s.replace("poster_path=", "");
                    splitString.add(s);
                }
                boolean isFound = s.contains(" title=");
                if (isFound) {
                    s = s.replace(" ", "");
                    s = s.replace("title=", "");
                    splitString.add(s);
                }
                boolean isFound2 = s.contains("release_date=");
                if (isFound2) {
                    s = s.replace("}", "");
                    s = s.replace(" ", "");
                    s = s.replace("release_date=", "");
                    splitString.add(s);
                    arr.add(splitString);
                    splitString = new ArrayList<String>();
                }
            }
        }
        return arr;
    }

    private TestRestTemplate steamRestTemplate = new TestRestTemplate();

    @RequestMapping({"/getsteam"})
    public ResponseEntity<Map> steam(@RequestParam("appid") String principal) throws IOException {
        final String REST_STEAM_URI = "https://api.steampowered.com/ISteamUserStats/GetNumberOfCurrentPlayers/v1/?format=json&appid=" + principal;
        File file = new File("user.config");
        if (!file.exists())
            file.createNewFile();
        file = ResourceUtils.getFile("user.config");
        String s = new String(Files.readAllBytes(file.toPath()));
        String vsteam = s.substring(0, s.indexOf("vsteam="));
        vsteam = vsteam + "vsteam=";
        String fin = s.substring(s.indexOf("}s2"), s.indexOf("}x"));
        fin = fin + "}x";
        String str = vsteam + principal + fin;
        try (FileWriter fw = new FileWriter("user.config")) {
            fw.write(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this.steamRestTemplate.getForEntity(REST_STEAM_URI, Map.class);
    }

    @RequestMapping({"/changing"})
    public ResponseEntity<Map> changing(@RequestParam("c") String principal) throws IOException {
        System.out.println(principal);
        final String REST_STEAM_URI = "https://api.steampowered.com/ISteamNews/GetNewsForApp/v2/?appid=" + principal + "&count=5";
        File file = new File("user.config");
        if (!file.exists())
            file.createNewFile();
        file = ResourceUtils.getFile("user.config");
        String s = new String(Files.readAllBytes(file.toPath()));
        String vsteam = s.substring(0, s.indexOf("v2steam="));
        vsteam = vsteam + "v2steam=";
        String fin = s.substring(s.indexOf("}y "), s.indexOf("}x"));
        fin = fin + "}x";
        String str = vsteam + principal + fin;
        try (FileWriter fw = new FileWriter("user.config")) {
            fw.write(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this.steamRestTemplate.getForEntity(REST_STEAM_URI, Map.class);
    }

    @RequestMapping({"/gettab"})
    public ArrayList<String> gettab() throws IOException {
        File file = new File("user.config");
        if (!file.exists())
            file.createNewFile();
        file = ResourceUtils.getFile("user.config");
        String s = new String(Files.readAllBytes(file.toPath()));
        String vsteam = s.substring(s.indexOf("vsteam="), s.indexOf("}s2 "));
        vsteam = vsteam.replace("vsteam=", "");
        String v2steam = s.substring(s.indexOf("v2steam="), s.indexOf("}y "));
        v2steam = v2steam.replace("v2steam=", "");
        String vytb = s.substring(s.indexOf("vytb="), s.indexOf("}r "));
        vytb = vytb.replace("vytb=", "");
        String vreddit = s.substring(s.indexOf("vreddit="), s.indexOf("}m "));
        vreddit = vreddit.replace("vreddit=", "");
        String vmeme = s.substring(s.indexOf("vmeme="), s.indexOf("}gm "));
        vmeme = vmeme.replace("vmeme=", "");
        String vgiphy = s.substring(s.indexOf("vgiphy="), s.indexOf("}d "));
        vgiphy = vgiphy.replace("vgiphy=", "");
        String vmusic = s.substring(s.indexOf("vmusic="), s.indexOf("}t "));
        vmusic = vmusic.replace("vmusic=", "");
        String vtwitter = s.substring(s.indexOf("vtwitter="), s.indexOf("}gi "));
        vtwitter = vtwitter.replace("vtwitter=", "");
        String vgithub = s.substring(s.indexOf("vgithub="), s.indexOf("}x"));
        vgithub = vgithub.replace("vgithub=", "");
        ArrayList<String> res = new ArrayList<>();
        res.add(vsteam);
        res.add(v2steam);
        res.add(vytb);
        res.add(vreddit);
        res.add(vmeme);
        res.add(vgiphy);
        res.add(vmusic);
        res.add(vtwitter);
        res.add(vgithub);
        System.out.println("TEST");
        System.out.println(res);
        return res;
    }

    @RequestMapping({"/userparse"})
    public String[] userparse() throws IOException {
        File file = new File("user.config");
        if (!file.exists())
            file.createNewFile();
        file = ResourceUtils.getFile("user.config");
        String s = new String(Files.readAllBytes(file.toPath()));
        String str = s.substring(s.indexOf(" dweather="));
        str = str.replace(" dweather=", "");
        str = str.replace(" dcine=", "");
        str = str.replace(" dytb=", "");
        str = str.replace(" dmap=", "");
        str = str.replace(" dred=", "");
        str = str.replace(" dsteam=", "");
        str = str.replace(" dmeme=", "");
        str = str.replace(" dlip=", "");
        str = str.replace(" ddeez=", "");
        str = str.replace(" dtwit=", "");
        str = str.replace(" dgith=", "");
        str = str.replace("}", "");
        String[] res = str.split(",");
        return res;
    }

    @RequestMapping({"/getuser"})
    public String getuser() throws IOException {
        File file = new File("user.config");
        if (!file.exists())
            file.createNewFile();
        file = ResourceUtils.getFile("user.config");
        String s = new String(Files.readAllBytes(file.toPath()));
        String str = s.substring(s.indexOf(" weather="), s.indexOf(", dweather="));
        String sr = str.replace(" weather=", "");
        String st = sr.replace(", dweather=", "");
        return st;
    }

    public static String getEncodedPassword(String key) {
        byte[] uniqueKey = key.getBytes();
        byte[] hash = null;
        try {
            hash = MessageDigest.getInstance("MD5").digest(uniqueKey);
        } catch (NoSuchAlgorithmException e) {
            throw new Error("no MD5 support in this VM");
        }
        StringBuffer hashString = new StringBuffer();
        for (int i = 0; i < hash.length; ++i) {
            String hex = Integer.toHexString(hash[i]);
            if (hex.length() == 1) {
                hashString.append('0');
                hashString.append(hex.charAt(hex.length() - 1));
            } else {
                hashString.append(hex.substring(hex.length() - 2));
            }
        }
        return hashString.toString();
    }

    private TestRestTemplate ipdRestTemplate = new TestRestTemplate();

    @RequestMapping({"/getip"})
    public ResponseEntity<String> getip(@RequestParam("ip") String principal) {
        final String REST_STEAM_URI = ("https://api.ipfinder.io/v1/" + principal + "?token=free");
        return this.ipdRestTemplate.getForEntity(REST_STEAM_URI, String.class);
    }

    private TestRestTemplate gidRestTemplate = new TestRestTemplate();

    @RequestMapping({"/getgif"})
    public ResponseEntity<String> getgif(@RequestParam("gif") String principal) throws IOException {
        final String REST_STEAM_URI = ("https://api.giphy.com/v1/gifs/search?api_key=&q=" + principal + "&limit=25&offset=0&rating=G&lang=en");
        File file = new File("user.config");
        if (!file.exists())
            file.createNewFile();
        file = ResourceUtils.getFile("user.config");
        String s = new String(Files.readAllBytes(file.toPath()));
        String vsteam = s.substring(0, s.indexOf("vgiphy="));
        vsteam = vsteam + "vgiphy=";
        String fin = s.substring(s.indexOf("}d"), s.indexOf("}x"));
        fin = fin + "}x";
        String str = vsteam + principal + fin;
        try (FileWriter fw = new FileWriter("user.config")) {
            fw.write(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this.gidRestTemplate.getForEntity(REST_STEAM_URI, String.class);
    }

    private TestRestTemplate randdRestTemplate = new TestRestTemplate();

    @RequestMapping({"/gettrand"})
    public ResponseEntity<String> gettrand(@RequestParam("catt") String principal) throws IOException {
        final String REST_STEAM_URI = ("https://api.thecatapi.com/v1/images/search?category_ids=" + principal);
        File file = new File("user.config");
        if (!file.exists())
            file.createNewFile();
        file = ResourceUtils.getFile("user.config");
        String s = new String(Files.readAllBytes(file.toPath()));
        String vsteam = s.substring(0, s.indexOf("vmeme="));
        vsteam = vsteam + "vmeme=";
        String fin = s.substring(s.indexOf("}gm"), s.indexOf("}x"));
        fin = fin + "}x";
        String str = vsteam + principal + fin;
        try (FileWriter fw = new FileWriter("user.config")) {
            fw.write(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this.randdRestTemplate.getForEntity(REST_STEAM_URI, String.class);
    }

    private TestRestTemplate musicRestTemplate = new TestRestTemplate();

    @RequestMapping({"/getmusic"})
    public ResponseEntity<Map> music(@RequestParam("artist") String principal) throws IOException {
        final String REST_SERVICE_URI = "https://api.deezer.com/search?q=" + principal;
        File file = new File("user.config");
        if (!file.exists())
            file.createNewFile();
        file = ResourceUtils.getFile("user.config");
        String s = new String(Files.readAllBytes(file.toPath()));
        String vsteam = s.substring(0, s.indexOf("vmusic="));
        vsteam = vsteam + "vmusic=";
        String fin = s.substring(s.indexOf("}t"), s.indexOf("}x"));
        fin = fin + "}x";
        String str = vsteam + principal + fin;
        try (FileWriter fw = new FileWriter("user.config")) {
            fw.write(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this.musicRestTemplate.getForEntity(REST_SERVICE_URI, Map.class);
    }

    @RequestMapping({"/modifsubcri"})
    public boolean modifsubcri(@RequestParam("weather") Boolean weather, @RequestParam("cine") Boolean cine, @RequestParam("ytb") Boolean ytb, @RequestParam("map") Boolean mymap, @RequestParam("red") Boolean reddit, @RequestParam("steam") Boolean steam, @RequestParam("meme") Boolean meme, @RequestParam("lip") Boolean lip, @RequestParam("deezer") Boolean deezer, @RequestParam("twitter") Boolean twitter, @RequestParam("gith") Boolean gith) throws IOException {
        File file = null;
        file = ResourceUtils.getFile("user.config");
        String content = new String(Files.readAllBytes(file.toPath()));
        String g = content.substring(0, content.indexOf(", dweather="));
        String fin = content.substring(content.indexOf("}s1 "), content.indexOf("}x"));
        String str = g + ", dweather=" + weather.toString() + ", " + "dcine=" + cine.toString() + ", " + "dytb=" + ytb.toString() + ", " + "dmap=" + mymap.toString() + ", " + "dred=" + reddit.toString() + ", " + "dsteam=" + steam.toString() + ", " + "dmeme=" + meme.toString() + ", " + "dlip=" + lip.toString() + ", " + "ddeez=" + deezer.toString() + ", " + "dtwit=" + twitter.toString() + ", " + "dgith=" + gith.toString() + fin + "}x";
        System.out.println("GGG");
        System.out.println(str);
        StringBuilder sb = new StringBuilder();
        File fl = new File("users.config");
        if (!fl.exists())
            fl.createNewFile();
        fl = ResourceUtils.getFile("users.config");
        String c = new String(Files.readAllBytes(fl.toPath()));
        Scanner s = new Scanner(c);
        while (s.hasNextLine()) {
            String line = s.nextLine();
            if (!line.equals(content)) {
                sb.append(line);
                sb.append("\n");
            }
        }
        s.close();
        sb.append(str);
        File users = new File("users.config");
        if (!users.exists())
            users.createNewFile();
        try (FileWriter myfile = new FileWriter("users.config")) {
            myfile.write(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        File user = new File("user.config");
        if (!user.exists())
            user.createNewFile();
        try (FileWriter fw = new FileWriter("user.config")) {
            fw.write(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private int getboule(Boolean key) {
        if (key)
            return 1;
        return 0;
    }

    private TestRestTemplate twitchgameRestTemplate = new TestRestTemplate();

    @RequestMapping({"/streamerstwitch"})
    public ResponseEntity<String> streamerstwitch(@RequestParam("rechearch") String nb) {
        final String REST_SERVICE_URI = "https://api.twitch.tv/helix/streams/?first=" + nb;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Client-ID", "");
        HttpEntity<ResponseEntity> requestEntity = new HttpEntity<>(null, headers);
        ResponseEntity<String> result = twitchgameRestTemplate.exchange(REST_SERVICE_URI, HttpMethod.GET, requestEntity, String.class);
        return result;
    }

    private TestRestTemplate gametwitchRestTemplate = new TestRestTemplate();

    @RequestMapping({"/gametwitch"})
    public ResponseEntity<String> gametwitch(@RequestParam("rechearch") String nb) {
        final String REST_SERVICE_URI = "https://api.twitch.tv/helix/games/top?first=" + nb;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Client-ID", "");
        HttpEntity<ResponseEntity> requestEntity = new HttpEntity<>(null, headers);
        ResponseEntity<String> result = gametwitchRestTemplate.exchange(REST_SERVICE_URI, HttpMethod.GET, requestEntity, String.class);
        return result;
    }

    @RequestMapping({"/subcrib"})
    public boolean subcrib(@RequestParam("weather") Boolean weather, @RequestParam("cine") Boolean cine, @RequestParam("ytb") Boolean ytb, @RequestParam("map") Boolean mymap, @RequestParam("red") Boolean reddit, @RequestParam("steam") Boolean steam, @RequestParam("meme") Boolean meme, @RequestParam("lip") Boolean lip, @RequestParam("deezer") Boolean deezer, @RequestParam("twitter") Boolean twitter, @RequestParam("gith") Boolean gith) throws IOException, ClassNotFoundException, SQLException {
        File fh = new File("user.config");
        if (!fh.exists())
            fh.createNewFile();
        File file = new File("user.config");
        file = ResourceUtils.getFile("user.config");
        String content = new String(Files.readAllBytes(file.toPath()));
        String aemail = content.substring(content.indexOf("email="), content.indexOf(" , password"));
        String remail = aemail.replace(" email=", "");
        String apass = content.substring(content.indexOf(" password="), content.indexOf(" , weather"));
        String repass = apass.replace(" password=", "");
        if (System.getenv("MYSQL_HOST") != null && System.getenv("MYSQL_HOST") != null) {
//            Connection con = DriverManager.getConnection("jdbc:mysql://" + System.getenv("MYSQL_HOST") + ":" + System.getenv("MYSQL_PORT") + "/dashboard", "root", System.getenv("MYSQL_ROOT_PASSWORD"));
        }
/*        String userName, p, url;
        Connection con;
        Statement st;

        userName = "root";
        p = "Carrera4";
        url = "jdbc:mysql://localhost:3306/dashboard";
        String query = "SELECT * FROM dashboard.dashboard"; //Ca arrive a le lire !!!!!!! FIOUUUUUUUUUUUUUUU
        String result = "UPDATE `dashboard`.`dashboard` t SET t.`dweather` = " + getboule(weather) + ", t.`dcine` = " + getboule(cine) + ", t.`dytb` = " + getboule(ytb) + ", t.`dmap` = " + getboule(mymap) + ", t.`dred` = " + getboule(reddit) + ", t.`dsteam` = " + getboule(steam) + ", t.`dmeme` = " + getboule(meme) + ", t.`dlip` = " + getboule(lip) + ", t.`ddeez` = " + getboule(deezer) + ", t.`dtwit` = " + getboule(twitter) + ", t.`dgith` = " + getboule(gith) + " WHERE t.`email` LIKE '" + remail + "' ESCAPE '#' AND t.`password` LIKE '" + repass + "' ESCAPE '#' AND t.`weather` LIKE 'Nice' ESCAPE '#' AND t.`dweather` = 1 AND t.`dcine` = 1 AND t.`dytb` = 1 AND t.`dmap` = 1 AND t.`dred` = 1 AND t.`dsteam` = 1 AND t.`dmeme` = 1 AND t.`dlip` = 1 AND t.`ddeez` = 1 AND t.`dtwit` = 1 AND t.`dgith` = 1";
        Class.forName("com.mysql.cj.jdbc.Driver");
        con = DriverManager.getConnection(url, userName, p);
        st = con.createStatement();
        System.out.println("Connection is successful");
        int r = st.executeUpdate(result);
        ResultSet rs = st.executeQuery(query);

        while (rs.next()) {
            String firstName = rs.getString("email");
            String lastName = rs.getString("password");
        }
*/
        content = content.replace("}", "");
        if (!content.contains("dcine=")) {
            String sf = content + ", dweather=" + weather.toString() + ", " + "dcine=" + cine.toString() + ", " + "dytb=" + ytb.toString() + ", " + "dmap=" + mymap.toString() + ", " + "dred=" + reddit.toString() + ", " + "dsteam=" + steam.toString() + ", " + "dmeme=" + meme.toString() + ", " + "dlip=" + lip.toString() + ", " + "ddeez=" + deezer.toString() + ", " + "dtwit=" + twitter.toString() + ", " + "dgith=" + gith.toString() + "}";
            String str = sf + "s1 vsteam=730}s2 v2steam=730}y vytb=Squeezie}r vreddit=cat}m vmeme=3}gm vgiphy=cat}d vmusic=eminem}t vtwitter=TAKOVistawk}gi vgithub=mrlizzard}x";
            StringBuilder sb = new StringBuilder();
            File fl = new File("users.config");
            if (!fl.exists())
                fl.createNewFile();
            try {
                fl = ResourceUtils.getFile("users.config");
                String c = new String(Files.readAllBytes(fl.toPath()));
                Scanner s = new Scanner(c);
                while (s.hasNextLine()) {
                    String line = s.nextLine();
                    if (!line.equals(content)) {
                        sb.append(line);
                        sb.append("\n");
                    }
                }
                s.close();
                sb.append(str);
                File users = new File("users.config");
                if (!users.exists())
                    users.createNewFile();
                try (FileWriter myfile = new FileWriter("users.config")) {
                    myfile.write(sb.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                File user = new File("user.config");
                if (!user.exists())
                    user.createNewFile();
                try (FileWriter fw = new FileWriter("user.config")) {
                    fw.write(str);
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        } else {
            return true;
        }
    }

    @RequestMapping({"/user", "/me"})
    public Map<String, String> user(Principal principal) {
        Map<String, String> map = new LinkedHashMap<>();
        if (principal != null) {
            map.put("name", principal.getName());
        }
        return map;
    }

    @RequestMapping({"/connection", "/me"})
    public Boolean connection(@RequestParam("email") String email, @RequestParam("password") String password) {
        File file = new File("users.config");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        try {
            String content = new String(Files.readAllBytes(file.toPath()));
            Scanner scanner = new Scanner(content);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                boolean isFound = line.contains(email);
                boolean isFound2 = line.contains(getEncodedPassword(password));
                if (isFound && isFound2) {
                    File user = new File("user.config");
                    if (!user.exists())
                        user.createNewFile();
                    try (FileWriter f = new FileWriter("user.config")) {
                        f.write(line);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return true;
                }
            }
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @RequestMapping({"/crea", "/me"})
    public String creation(@RequestParam("email") String email, @RequestParam("password") String pp) {
/*        String a = getEncodedPassword(pp);
        String userName,p,url,driver;
        Connection con;
        Statement st;
        userName = "root";
        p = "Carrera4";
        url = "jdbc:mysql://localhost:3306/dashboard";

        userName=System.getenv("USERNAME");
        p=System.getenv("PASSWORD");
        url=System.getenv("URL");
        driver=System.getenv("DRIVER");*/
        try {
            String a = getEncodedPassword(pp);
            String userName, p, url;
            Connection con;
            Statement st;

            String obj = email + "{" +
                    "email=" + email + " " +
                    ", password=" + a + " " +
                    ", weather=" + "Nice" + " " +
                    "}";
            File user = new File("user.config");
            if (!user.exists()) {
                try {
                    user.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try (FileWriter file = new FileWriter("user.config")) {
                file.write(obj);
            } catch (IOException e) {
                System.out.println("FAIL");
                e.printStackTrace();
            }
            if (System.getenv("MYSQL_HOST") != null && System.getenv("MYSQL_PORT") != null) {
                con = DriverManager.getConnection("jdbc:mysql://" + System.getenv("MYSQL_HOST") + ":" + System.getenv("MYSQL_PORT") + "/dashboard", "root", "");

                String query = "SELECT * FROM dashboard.dashboard"; //Ca arrive a le lire !!!!!!! FIOUUUUUUUUUUUUUUU
                String tmp = "INSERT INTO `dashboard`.`dashboard` (`email`, `password`, `weather`) VALUES ('" + email + "', '" + a + "', 'Nice')";
                st = con.createStatement();
                System.out.println("Connection is successful");
                int r = st.executeUpdate(tmp);
                ResultSet rs = st.executeQuery(query);

                while (rs.next()) {
                    String firstName = rs.getString("email");
                    String lastName = rs.getString("password");
                    String weather = rs.getString("weather");
                }
            }
        } catch (Exception e) {
            System.out.println("FAIL");
            e.printStackTrace();
        }
        return "OK";
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http.antMatcher("/**").authorizeRequests().antMatchers("/**", "/login**", "/webjars/**", "/error**").permitAll().anyRequest()
                .authenticated().and().exceptionHandling()
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/")).and().logout()
                .logoutSuccessUrl("/").permitAll().and().csrf()
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()).and()
                .addFilterBefore(ssoFilter(), BasicAuthenticationFilter.class);
        // @formatter:on
    }

    @Configuration
    @EnableResourceServer
    protected static class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {
        @Override
        public void configure(HttpSecurity http) throws Exception {
            // @formatter:off
            http.antMatcher("/me").authorizeRequests().anyRequest().authenticated();
            // @formatter:on
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(DashboardServer.class, args);
    }

    @Bean
    public FilterRegistrationBean<OAuth2ClientContextFilter> oauth2ClientFilterRegistration(OAuth2ClientContextFilter filter) {
        FilterRegistrationBean<OAuth2ClientContextFilter> registration = new FilterRegistrationBean<OAuth2ClientContextFilter>();
        registration.setFilter(filter);
        registration.setOrder(-100);
        return registration;
    }

    @Bean
    @ConfigurationProperties("github")
    public ClientResources github() {
        String obj = "tom.rouvier@epitech.eu{email=tom.rouvier@epitech.eu , password= , weather=Nice , dweather=true, dcine=true, dytb=true, dmap=true, dred=true, dsteam=true, dmeme=true, dlip=true, ddeez=true, dtwit=true, dgith=true}s1 vsteam=730}s2 v2steam=730}y vytb=Squeezie}r vreddit=cat}m vmeme=3}gm vgiphy=cat}d vmusic=eminem}t vtwitter=mrlizzard}gi vgithub=mrlizzard}x";
        File user = new File("user.config");
        if (!user.exists()) {
            try {
                user.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (FileWriter file = new FileWriter("user.config")) {
            file.write(obj);
        } catch (IOException e) {
            System.out.println("FAIL");
            e.printStackTrace();
        }
        try (FileWriter file = new FileWriter("users.config")) {
            file.write(obj);
        } catch (IOException e) {
            System.out.println("FAIL");
            e.printStackTrace();
        }
        return new ClientResources();
    }

    @Bean
    @ConfigurationProperties("facebook")
    public ClientResources facebook() {
        String obj = "tom.rouvier@epitech.eu{email=tom.rouvier@epitech.eu , password= , weather=Nice , dweather=true, dcine=true, dytb=true, dmap=true, dred=true, dsteam=true, dmeme=true, dlip=true, ddeez=true, dtwit=true, dgith=true}s1 vsteam=730}s2 v2steam=730}y vytb=Squeezie}r vreddit=cat}m vmeme=3}gm vgiphy=cat}d vmusic=eminem}t vtwitter=mrlizzard}gi vgithub=mrlizzard}x";
        File user = new File("user.config");
        if (!user.exists()) {
            try {
                user.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (FileWriter file = new FileWriter("user.config")) {
            file.write(obj);
        } catch (IOException e) {
            System.out.println("FAIL");
            e.printStackTrace();
        }
        try (FileWriter file = new FileWriter("users.config")) {
            file.write(obj);
        } catch (IOException e) {
            System.out.println("FAIL");
            e.printStackTrace();
        }
        return new ClientResources();
    }

    @Bean
    @ConfigurationProperties("twitter")
    public ClientResources twitter() {
        String obj = "tom.rouvier@epitech.eu{email=tom.rouvier@epitech.eu , password= , weather=Nice , dweather=true, dcine=true, dytb=true, dmap=true, dred=true, dsteam=true, dmeme=true, dlip=true, ddeez=true, dtwit=true, dgith=true}s1 vsteam=730}s2 v2steam=730}y vytb=Squeezie}r vreddit=cat}m vmeme=3}gm vgiphy=cat}d vmusic=eminem}t vtwitter=mrlizzard}gi vgithub=mrlizzard}x";
        File user = new File("user.config");
        if (!user.exists()) {
            try {
                user.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (FileWriter file = new FileWriter("user.config")) {
            file.write(obj);
        } catch (IOException e) {
            System.out.println("FAIL");
            e.printStackTrace();
        }
        try (FileWriter file = new FileWriter("users.config")) {
            file.write(obj);
        } catch (IOException e) {
            System.out.println("FAIL");
            e.printStackTrace();
        }
        return new ClientResources();
    }

    @Bean
    @ConfigurationProperties("linkedin")
    public ClientResources linkedin() {
        String obj = "tom.rouvier@epitech.eu{email=tom.rouvier@epitech.eu , password= , weather=Nice , dweather=true, dcine=true, dytb=true, dmap=true, dred=true, dsteam=true, dmeme=true, dlip=true, ddeez=true, dtwit=true, dgith=true}s1 vsteam=730}s2 v2steam=730}y vytb=Squeezie}r vreddit=cat}m vmeme=3}gm vgiphy=cat}d vmusic=eminem}t vtwitter=mrlizzard}gi vgithub=mrlizzard}x";
        File user = new File("user.config");
        if (!user.exists()) {
            try {
                user.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (FileWriter file = new FileWriter("user.config")) {
            file.write(obj);
        } catch (IOException e) {
            System.out.println("FAIL");
            e.printStackTrace();
        }
        try (FileWriter file = new FileWriter("users.config")) {
            file.write(obj);
        } catch (IOException e) {
            System.out.println("FAIL");
            e.printStackTrace();
        }
        return new ClientResources();
    }

    @Bean
    @ConfigurationProperties("google")
    public ClientResources google() {
        String obj = "tom.rouvier@epitech.eu{email=tom.rouvier@epitech.eu , password= , weather=Nice , dweather=true, dcine=true, dytb=true, dmap=true, dred=true, dsteam=true, dmeme=true, dlip=true, ddeez=true, dtwit=true, dgith=true}s1 vsteam=730}s2 v2steam=730}y vytb=Squeezie}r vreddit=cat}m vmeme=3}gm vgiphy=cat}d vmusic=eminem}t vtwitter=mrlizzard}gi vgithub=mrlizzard}x";
        File user = new File("user.config");
        if (!user.exists()) {
            try {
                user.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (FileWriter file = new FileWriter("user.config")) {
            file.write(obj);
        } catch (IOException e) {
            System.out.println("FAIL");
            e.printStackTrace();
        }
        try (FileWriter file = new FileWriter("users.config")) {
            file.write(obj);
        } catch (IOException e) {
            System.out.println("FAIL");
            e.printStackTrace();
        }
        return new ClientResources();
    }

    private Filter ssoFilter() {
        CompositeFilter filter = new CompositeFilter();
        List<Filter> filters = new ArrayList<>();
        filters.add(ssoFilter(facebook(), "/login/facebook"));
        filters.add(ssoFilter(github(), "/login/github"));
        filters.add(ssoFilter(twitter(), "/login/twitter"));
        filters.add(ssoFilter(linkedin(), "/login/linkedin"));
        filters.add(ssoFilter(google(), "/login/google"));
        filter.setFilters(filters);
        return filter;
    }

    private Filter ssoFilter(ClientResources client, String path) {
        OAuth2ClientAuthenticationProcessingFilter filter = new OAuth2ClientAuthenticationProcessingFilter(path);
        OAuth2RestTemplate template = new OAuth2RestTemplate(client.getClient(), oauth2ClientContext);
        filter.setRestTemplate(template);
        UserInfoTokenServices tokenServices = new UserInfoTokenServices(
                client.getResource().getUserInfoUri(), client.getClient().getClientId());
        filter.setTokenServices(tokenServices);
        return filter;
    }
}

class ClientResources {

    @NestedConfigurationProperty
    private AuthorizationCodeResourceDetails client = new AuthorizationCodeResourceDetails();

    @NestedConfigurationProperty
    private ResourceServerProperties resource = new ResourceServerProperties();

    public AuthorizationCodeResourceDetails getClient() {
        return client;
    }

    public ResourceServerProperties getResource() {
        return resource;
    }
}
