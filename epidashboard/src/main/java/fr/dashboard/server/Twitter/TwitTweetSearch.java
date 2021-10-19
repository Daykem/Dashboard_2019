package fr.dashboard.server.Twitter;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class TwitTweetSearch {

    public static Twitter getTwitterinstance() {
        /**
         * if not using properties file, we can set access token by following way
         */
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey("")
                .setOAuthConsumerSecret("")
                .setOAuthAccessToken("")
                .setOAuthAccessTokenSecret("");
        TwitterFactory tf = new TwitterFactory(cb.build());
        Twitter twitter = tf.getSingleton();

/*
        Twitter twitter = TwitterFactory.getSingleton();
*/
        return twitter;

    }
    @RequestMapping({"/twitterTweetCreation"})
    public static String createTweet(@RequestParam("rechearch") String tweet) throws TwitterException {
        Twitter twitter = getTwitterinstance();
        System.out.println(tweet);
        Status status = twitter.updateStatus(tweet);
        return status.getText();
    }

    @RequestMapping({"/twitterTimeline"})
    public static List<String> getTimeLine() throws TwitterException {
        Twitter twitter = getTwitterinstance();
        List<Status> statuses = twitter.getHomeTimeline();
        return statuses.stream().map(
                item -> item.getText()).collect(
                Collectors.toList());
    }

    @RequestMapping({"/twitterSendMessage"})
    public static String sendDirectMessage(@RequestParam("name") String recipientName, @RequestParam("rechearch") String msg) throws TwitterException {
        Twitter twitter = getTwitterinstance();
        DirectMessage message = twitter.sendDirectMessage(recipientName, msg);
        return message.getText();
    }

    @RequestMapping({"/twitterTweetSearch"})
    public static List<String> searchtweets(@RequestParam("rechearch") String search) throws TwitterException {
        Twitter twitter = getTwitterinstance();
        Query query = new Query(search);
        QueryResult result = twitter.search(query);
        List<Status> statuses = result.getTweets();
        return statuses.stream().map(
                item -> item.getText()).collect(
                Collectors.toList());
    }

/*
    @RequestMapping({"/twitter"})
    public static List<String> searchtweets() throws TwitterException {
        Twitter twitter = getTwitterinstance();
        Query query = new Query("source:twitter4j baeldung");
        QueryResult result = twitter.search(query);
        List<Status> statuses = result.getTweets();
        System.out.println(statuses.stream().map(
                item -> item.getText()).collect(
                Collectors.toList()));
        return statuses.stream().map(
                item -> item.getText()).collect(
                Collectors.toList());
    }
*/

    public static void streamFeed() {

        StatusListener listener = new StatusListener(){

            @Override
            public void onException(Exception e) {
                e.printStackTrace();
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice arg) {
                System.out.println("Got a status deletion notice id:" + arg.getStatusId());
            }

            @Override
            public void onScrubGeo(long userId, long upToStatusId) {
                System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
            }

            @Override
            public void onStallWarning(StallWarning warning) {
                System.out.println("Got stall warning:" + warning);
            }

            @Override
            public void onStatus(Status status) {
                System.out.println(status.getUser().getName() + " : " + status.getText());
            }

            @Override
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
            }
        };

        TwitterStream twitterStream = new TwitterStreamFactory().getInstance();

        twitterStream.addListener(listener);

        twitterStream.sample();

    }

}
