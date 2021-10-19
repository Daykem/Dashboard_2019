package fr.dashboard.server.Twitter;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import twitter4j.*;

/**
 * Search users with the specified query.
 *
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public final class TwitUserSearch {
    /**
     * Usage: java twitter4j.examples.user.SearchUsers [query]
     *
     * @param search message
     */

    @RequestMapping({"/twitterUserSearch"})
    public static void userSearch(@RequestParam("rechearch") String search) {
        try {
            Twitter twitter = new TwitterFactory().getInstance();
            int page = 1;
            ResponseList<User> users;
            do {
                users = twitter.searchUsers(search, page);
                for (User user : users) {
                    if (user.getStatus() != null) {
                        System.out.println("@" + user.getScreenName() + " - " + user.getStatus().getText());
                    } else {
                        // the user is protected
                        System.out.println("@" + user.getScreenName());
                    }
                }
                page++;
            } while (users.size() != 0 && page < 50);
            System.out.println("done.");
            System.exit(0);
        } catch (TwitterException te) {
            te.printStackTrace();
            System.out.println("Failed to search users: " + te.getMessage());
            System.exit(-1);
        }
    }
}