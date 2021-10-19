package fr.dashboard.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@RestController
public class Reddit {

    @Value("${reddit.client.clientId}")
    private static String _ClientId;
    @Value("${reddit.client.clientSecret}")
    private static String _ClientSecret;
    @Value("${reddit.client.accessTokenUri}")
    private static String _authUrl;

    private String getAuthToken(){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(_ClientId, _ClientSecret);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.put("User-Agent",
                Collections.singletonList("tomcat:com.e4developer.e4reddit-test:v1.0 (by /u/bartoszjd)"));
        String body = "grant_type=client_credentials";
        HttpEntity<String> request
                = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
                _authUrl, request, String.class);
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = new HashMap<>();
        try {
            map.putAll(mapper
                    .readValue(Objects.requireNonNull(response.getBody()), new TypeReference<Map<String,Object>>(){}));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return String.valueOf(map.get("access_token"));
    }

    @RequestMapping({"/subreddit"})
    public ArrayList<String> mySubreddits(@RequestParam("subReddit") String subReddit, @Value("${reddit.client.clientId}") String ClientId,
                                                     @Value("${reddit.client.clientSecret}") String ClientSecret,
                                                     @Value("${reddit.client.accessTokenUri}") String authUrl) throws IOException {
        File filde = new File("user.config");
        if (!filde.exists())
            filde.createNewFile();
        filde = ResourceUtils.getFile("user.config");
        String sd = new String(Files.readAllBytes(filde.toPath()));
        String vsteam = sd.substring(0, sd.indexOf("vreddit="));
        vsteam = vsteam + "vreddit=";
        String fin = sd.substring(sd.indexOf("}m"), sd.indexOf("}x"));
        fin = fin + "}x";
        String str = vsteam + subReddit + fin;
        try (FileWriter fw = new FileWriter("user.config")) {
            fw.write(str);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Reddit._ClientId = ClientId;
        Reddit._ClientSecret = ClientSecret;
        Reddit._authUrl = authUrl;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        String authToken = getAuthToken();
        headers.setBearerAuth(authToken);
        headers.put("User-Agent",
                Collections.singletonList("tomcat:com.e4developer.e4reddit-test:v1.0 (by /u/bartoszjd)"));
        HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
        String url = "https://oauth.reddit.com/r/"+subReddit+"/hot";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        File user = new File("red.config");
        if (!user.exists())
            user.createNewFile();
        try (FileWriter file = new FileWriter("red.config")) {
            file.write(response.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        File file = new File("red.config");
        if (!file.exists())
            file.createNewFile();
        file = ResourceUtils.getFile("red.config");
        String content = new String(Files.readAllBytes(file.toPath()));
        Scanner scanner = new Scanner(content);

        ArrayList<String> splitString = new ArrayList<String>();
        while (scanner.hasNextLine()) {
            for (String s : scanner.nextLine().split(",")) {
                boolean isFound1 = s.endsWith(".jpg\"");
                boolean isFound2 = s.endsWith(".png\"");
                if (isFound1 || isFound2) {
                    s = s.replace("\"", "");
                    s = s.replace("url", "");
                    s = s.replace("thumbnail", "");
                    s = s.replace(": ", "");
                    splitString.add(s);
                }
            }
        }
        return splitString;
    }
}
