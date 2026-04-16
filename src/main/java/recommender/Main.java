package recommender;

import com.google.api.services.youtube.YouTube;
import recommender.Api.YouTubeAuth;
import recommender.Api.YouTubeDataLoader;
import recommender.Model.CategoryRegistry;
import recommender.Model.Event;
import recommender.Model.User;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        try {
            System.out.println("🎬 YouTube Recommendation System");
            System.out.println("================================");
            System.out.println("📌 Using Liked Videos as interest signals\n");

            // 1. Load categories
            YouTube youtube = YouTubeAuth.authenticate();
            var categoryRegistry = new CategoryRegistry();
            var dataLoader = new YouTubeDataLoader(youtube, categoryRegistry);
            System.out.println("📂 Loading categories...");
            System.out.println("✅ Loaded " + dataLoader.categoryRegistry.getDimension() + " categories");

            // 2. Authorize YouTube
            System.out.println("🔐 Authorizing YouTube...");
            System.out.println("✅ Authorization successful!\n");

            // 3. Load liked videos
            YouTubeDataLoader ytLoader = new YouTubeDataLoader(youtube, new CategoryRegistry());
            List<Event> likedVideos = ytLoader.fetchLikedVideos(100);  // Загружаем до 100 лайкнутых видео

            if (likedVideos.isEmpty()) {
                System.out.println("\n❌ No liked videos found!");
                System.out.println("   Please like some videos on YouTube first, then try again.");
                return;
            }

            System.out.println("\n####################################################################\n");


            // 4. Create user and add liked videos as history
            User user = new User("YouTube User", ytLoader.categoryRegistry);
            for (Event event : likedVideos) {
                user.addEvent(event);
            }

            System.out.println("\n📊 User profile created with " + user.getHistory().size() + " liked videos");

            // 5. Show simple statistics
            System.out.println("\n📊 Simple statistics (total watch time per category):");
            user.showVector();

            // 6. Calculate with decay
            System.out.println("\n📊 Calculating user vector with time decay...");
            user.calculateWithDecayAndDynamics(0.95);

            System.out.println("\n####################################################################\n");

            // 7. Get Top categories
            System.out.println("🎯 TOP CATEGORIES:");
            System.out.println("======================");
            List<Map.Entry<String, Double>> top = user.getTopCategories(5);
            for (var entry : top) {
                System.out.printf("  %s: %.3f%n", entry.getKey(), entry.getValue());
            }

            // 8. Get Recommendations
            ytLoader.recommendVideo(top);

        } catch (IOException e) {
            System.out.println("\n❌ IO Error: " + e.getMessage());
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            System.out.println("\n❌ Security Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}