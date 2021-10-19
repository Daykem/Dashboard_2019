package fr.dashboard.server.Youtube;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Thumbnail;
import fr.dashboard.server.Auth;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * Retourne la liste des vidéos correspondant à la recherche de l'utilisateur
 */
@RestController
public class YtubeVideoSearch {

    /**
     * Définit une variable globale qui identifie le nom du fichier qui contient
     * la clé API du developpeur
     */
    private static final String PROPERTIES_FILENAME = "youtube.properties";

    private static final long NUMBER_OF_VIDEOS_RETURNED = 10;

    /**
     * Définit une instance gloable d'un object Youtube, qui sera utilisée pour
     * faire des requêtes à l'Api Youtube Data
     */
    private static YouTube youtube;

    /**
     * Prépare les ressources nécéssaires à l'appel de l'API Youtube pour les recherches de vidéos
     * @param userSearch Nom de la vidéo recherchée
     * @return Liste des videos correspondant à la recherche
     */
    @RequestMapping({"/youtubeVideoSearch"})
    public static ArrayList<ArrayList<String>> ytubeSearch(@RequestParam("rechearch") String userSearch) {
        // Lire la clé développeur the ficher properties
        Properties properties = new Properties();
        try {
            InputStream in = YtubeVideoSearch.class.getResourceAsStream("/" + PROPERTIES_FILENAME);
            properties.load(in);

        } catch (IOException e) {
            System.err.println("There was an error reading " + PROPERTIES_FILENAME + ": " + e.getCause()
                    + " : " + e.getMessage());
            System.exit(1);
        }

        try {
            youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, new HttpRequestInitializer() {
                public void initialize(HttpRequest request) throws IOException {
                }
            }).setApplicationName("Dashboard").build();
            // Définit la requête API pour retrouver les resultats de la recherche
            YouTube.Search.List search = youtube.search().list("id,snippet");

            String apiKey = properties.getProperty("youtube.apikey");
            search.setKey(apiKey);
            search.setQ(userSearch);

            // Restreint les résulats de la recherche pour seulement inclure les vidéos
            search.setType("video");

            // Pour améliorer l'efficacité, pour retrouver les domaines que l'application utilise
            search.setFields("items(id/kind,id/videoId,snippet/title, " + "snippet/channelTitle,snippet/channelId, " +
                    "snippet/description, " + "snippet/thumbnails/default/url)");
            search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);

            // Appelle l'API et retourne les résultats
            SearchListResponse searchResponse = search.execute();
            List<SearchResult> searchResultList = searchResponse.getItems();
            if (searchResultList != null) {
                return videoSearch(searchResultList.iterator(), userSearch);
            }
        } catch (GoogleJsonResponseException e) {
            System.err.println("There was a service error: " + e.getDetails().getCode() + " : "
                    + e.getDetails().getMessage());
        } catch (IOException e) {
            System.err.println("There was an IO error: " + e.getCause() + " : " + e.getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    /**
     * Retourne les videos Youtube recherchées
     * @param iteratorSearchResults Résultats de la recherche
     * @return Informatins utiles résulatant de la recherche
     */
    private static ArrayList<ArrayList<String>> videoSearch(Iterator<SearchResult> iteratorSearchResults, String query) {
        ArrayList<ArrayList<String>> videos = new ArrayList<ArrayList<String>>();
        ArrayList<String> tmp = new ArrayList<String>();
        if (!iteratorSearchResults.hasNext()) {
            System.out.println(" There aren't any results for your query.");
        }

        while (iteratorSearchResults.hasNext()) {
            SearchResult singleVideo = iteratorSearchResults.next();
            ResourceId rId = singleVideo.getId();

            // Confirme que les résulats sont bien des vidéos
            if (rId.getKind().equals("youtube#video")) {
                Thumbnail thumbnail = singleVideo.getSnippet().getThumbnails().getDefault();
                tmp.add("https://www.youtube.com/watch?v=" + rId.getVideoId());
                tmp.add(singleVideo.getSnippet().getTitle());
                tmp.add(singleVideo.getSnippet().getDescription());
                tmp.add(singleVideo.getSnippet().getChannelTitle());
                tmp.add(singleVideo.getSnippet().getChannelId());
                tmp.add(thumbnail.getUrl());
                videos.add(tmp);
            }
            tmp = new ArrayList<String>();
        }
        return videos;
    }
}