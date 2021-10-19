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

@RestController
public class YtubeChannelSearch {

    /**
     * Define a global variable that identifies the name of a file that
     * contains the developer's API key.
     */
    private static final String PROPERTIES_FILENAME = "youtube.properties";

    private static final long NUMBER_OF_CHANNELS_RETURNED = 10;

    /**
     * Define a global instance of a Youtube object, which will be used
     * to make YouTube Data API requests.
     */
    private static YouTube youtube;

    /**
     * Initialize a YouTube object to search for videos on YouTube. Then
     * display the name and thumbnail image of each video in the result set.
     *
     * @param userSearch command line args.
     * @return Liste of channel
     */
    @RequestMapping({"/youtubeChannelSearch"})
    public static ArrayList<ArrayList<String>> ytubeSearch(@RequestParam("rechearch") String userSearch) {
        // Read the developer key from the properties file.
        Properties properties = new Properties();
        try {
            InputStream in = YtubeChannelSearch.class.getResourceAsStream("/" + PROPERTIES_FILENAME);
            properties.load(in);

        } catch (IOException e) {
            System.err.println("There was an error reading " + PROPERTIES_FILENAME + ": " + e.getCause()
                    + " : " + e.getMessage());
            System.exit(1);
        }

        try {
            // This object is used to make YouTube Data API requests. The last
            // argument is required, but since we don't need anything
            // initialized when the HttpRequest is initialized, we override
            // the interface and provide a no-op function.
            youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, new HttpRequestInitializer() {
                public void initialize(HttpRequest request) throws IOException {
                }
            }).setApplicationName("Dashboard").build();

            // Prompt the user to enter a query term.

            // Define the API request for retrieving search results.
            YouTube.Search.List search = youtube.search().list("id,snippet");

            // Set your developer key from the {{ Google Cloud Console }} for
            // non-authenticated requests. See:
            // {{ https://cloud.google.com/console }}
            String apiKey = properties.getProperty("youtube.apikey");
            search.setKey(apiKey);
            search.setQ(userSearch);

            // Restrict the search results to only include videos. See:
            // https://developers.google.com/youtube/v3/docs/search/list#type
            search.setType("channel");

            // To increase efficiency, only retrieve the fields that the
            // application uses.
            search.setFields("items(id/kind, id/channelId,snippet/title, " +
                    "snippet/description, " + "snippet/thumbnails/default/url)");
            search.setMaxResults(NUMBER_OF_CHANNELS_RETURNED);

            // Call the API and print results.
            SearchListResponse searchResponse = search.execute();
            List<SearchResult> searchResultList = searchResponse.getItems();
            if (searchResultList != null) {
                return channelSearch(searchResultList.iterator(), userSearch);
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
     * Prints out all results in the Iterator. For each result, print the
     * title, video ID, and thumbnail.
     * @param iteratorSearchResults Iterator of SearchResults to print
     * @param query Search query (String)
     **/
    private static ArrayList<ArrayList<String>> channelSearch(Iterator<SearchResult> iteratorSearchResults, String query) {
        ArrayList<ArrayList<String>> channels = new ArrayList<ArrayList<String>>();
        ArrayList<String> tmp = new ArrayList<String>();

        if (!iteratorSearchResults.hasNext()) {
            System.out.println(" There aren't any results for your query.");
        }

        while (iteratorSearchResults.hasNext()) {
            SearchResult singleChannel = iteratorSearchResults.next();
            ResourceId rId = singleChannel.getId();

            // Confirm that the result represents a video. Otherwise, the
            // item will not contain a video ID.
            if (rId.getKind().equals("youtube#channel")) {
                Thumbnail thumbnail = singleChannel.getSnippet().getThumbnails().getDefault();
                tmp.add("https://www.youtube.com/" + rId.getChannelId());
                tmp.add(singleChannel.getSnippet().getDescription());
                tmp.add(singleChannel.getSnippet().getChannelTitle());
                tmp.add(singleChannel.getSnippet().getChannelId());
                tmp.add(thumbnail.getUrl());
                channels.add(tmp);
            }
            tmp = new ArrayList<String>();
        }
        return channels;
    }
}