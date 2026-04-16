package recommender.Api;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import recommender.Model.CategoryRegistry;
import recommender.Model.Event;
import recommender.Model.MyVector;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Loader Data from YouTube API
 * Converts API answers to Event objects
 */
public class YouTubeDataLoader {

    private final YouTube youtube;
    public final CategoryRegistry categoryRegistry;

    public YouTubeDataLoader(YouTube youtube, CategoryRegistry categoryRegistry) {
        this.youtube = youtube;
        this.categoryRegistry = categoryRegistry;
    }

    /**
     * Getting the history of views from YouTube
     * @param maxEvents is max count of events (0 = all)
     * @return list of Event for user
     */

    public List<Event> fetchLikedVideos(int maxEvents) throws IOException {
        List<Event> events = new ArrayList<>();
        String pageToken = null;

        System.out.println("Loading liked videos from YouTube...");
        System.out.println("=====================================");

        do {
            // Make the API request to get MetaData
            YouTube.PlaylistItems.List request = youtube.playlistItems()
                    .list(Arrays.asList("snippet", "contentDetails"));

            request.setPlaylistId("LL");  // LL = Liked Videos playlist
            request.setMaxResults(50L);
            request.setPageToken(pageToken);

            PlaylistItemListResponse response = request.execute();  // Block the code to get information

            System.out.println("📄 Page loaded: " + response.getItems().size() + " items");

            for (PlaylistItem item : response.getItems()) {
                // Getting Info
                String videoId = item.getContentDetails().getVideoId();
                String title = item.getSnippet().getTitle();
                String publishedAt = item.getSnippet().getPublishedAt().toString();

                System.out.println("\nLiked video: " + title);
                System.out.println("   Date liked: " + publishedAt);

                // Getting MetaData Info
                Video video = getVideoDetails(videoId);
                if (video != null) {
                    String categoryId = video.getSnippet().getCategoryId();
                    String categoryName = categoryRegistry.getCategoryName(categoryId);
                    int watchTime = (int) parseDuration(video.getContentDetails().getDuration());

                    System.out.println("   Category ID: " + categoryId);
                    System.out.println("   Category name: " + categoryName);
                    System.out.println("   Video duration: " + watchTime + " min");


                    // Create an Event
                    if (categoryName != null && watchTime > 0) {
                        LocalDate date = LocalDate.parse(publishedAt, DateTimeFormatter.ISO_DATE_TIME);
                        Event event = new Event(date, categoryId, watchTime);
                        events.add(event);
                        System.out.println("   ✅ Added to history! Index: " + categoryId);
                    } else {
                        System.out.println("   ⚠️ Skipped - category not in our list");
                    }
                }

                // Breaking if it is the end
                if (maxEvents > 0 && events.size() >= maxEvents) {
                    break;
                }
            }

            pageToken = response.getNextPageToken();

        } while (pageToken != null && (maxEvents == 0 || events.size() < maxEvents));

        System.out.println("\n=====================================");
        System.out.println("✅ Total loaded: " + events.size() + " liked videos");
        return events;
    }

    /**
     * Getting information about video by its ID
     */
    private Video getVideoDetails(String videoId) throws IOException {
        VideoListResponse response = youtube.videos()
                .list(Arrays.asList("snippet", "contentDetails"))
                .setId(Collections.singletonList(videoId))
                .execute();

        if (response.getItems() != null && !response.getItems().isEmpty()) {
            return response.getItems().get(0);
        }
        return null;
    }


    /**
     * Parse duration from ISO format to minutes
     */
    private double parseDuration(String duration) {
        if (duration == null) return 5.0;

        duration = duration.replace("PT", "");
        double minutes = 0;

        if (duration.contains("H")) {
            minutes += Double.parseDouble(duration.split("H")[0]) * 60;
            duration = duration.substring(duration.indexOf("H") + 1);
        }
        if (duration.contains("M")) {
            minutes += Double.parseDouble(duration.split("M")[0]);
            duration = duration.substring(duration.indexOf("M") + 1);
        }
        if (duration.contains("S")) {
            minutes += Double.parseDouble(duration.split("S")[0]) / 60.0;
        }

        return Math.max(minutes, 1.0);
    }

    public void recommendVideo(List<Map.Entry<String, Double>> top) throws IOException {
        int i = 0;

        while(i < top.size() && top.get(i).getValue() != 0){
            long countOfRecommended = (int)(top.get(i).getValue()*10);


            YouTube.Search.List request = youtube.search()
                    .list(List.of("snippet"));
            request.setQ(top.get(i).getKey());
            request.setType(List.of("video"));
            request.setMaxResults(countOfRecommended);
            request.setOrder("relevance");
            request.setRegionCode("US");

            SearchListResponse response = request.execute();

            for(SearchResult result : response.getItems()){
                System.out.println("  " + result.getSnippet().getTitle());
                System.out.println("  https://youtube.com/watch?v=" + result.getId().getVideoId());
                System.out.println();
            }
            i++;
        }
    }

}