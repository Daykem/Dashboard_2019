package fr.dashboard.server;

import facebook4j.*;
import facebook4j.conf.Configuration;
import facebook4j.conf.ConfigurationBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Fbook {

    @RequestMapping({"/facebookFeed"})
    public static JSONArray fbFeed(@RequestParam("rechearch") String search) throws FacebookException, JSONException {
        // Make the configuration builder
        ConfigurationBuilder confBuilder = new ConfigurationBuilder();
        confBuilder.setDebugEnabled(true);

        // Set application id, secret key and access token
        confBuilder.setOAuthAppId("");
        confBuilder.setOAuthAppSecret("");
        confBuilder.setOAuthAccessToken("");

        // Set permission
        confBuilder.setOAuthPermissions("email,publish_stream, id, name, first_name, last_name, generic");
        confBuilder.setUseSSL(true);
        confBuilder.setJSONStoreEnabled(true);

        // Create configuration object
        Configuration configuration = confBuilder.build();

        // Create facebook instance
        FacebookFactory ff = new FacebookFactory(configuration);
        Facebook facebook = ff.getInstance();
        String results = getFacebookPosts(facebook, search);
        return stringToJson(results);
    }
    public static String getFacebookPosts(Facebook facebook, String search) throws FacebookException {
        // Get posts for a particular search
        ResponseList<Post> results =  facebook.getPosts(search);
        return results.toString();

    }
    public static JSONArray stringToJson(String data) throws JSONException {
        // Create JSON object
        JSONObject jsonObject = new JSONObject(data);
        JSONArray message = (JSONArray) jsonObject.getJSONArray("message");
        System.out.println("Message : "+ message);
        return message;
    }

}
