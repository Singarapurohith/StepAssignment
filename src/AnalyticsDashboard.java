
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

    class PageViewEvent {
        String url;
        String userId;
        String source;

        public PageViewEvent(String url, String userId, String source) {
            this.url = url;
            this.userId = userId;
            this.source = source;
        }
    }

    public class AnalyticsDashboard {

        // Track total page views per URL
        private Map<String, AtomicInteger> pageViews = new ConcurrentHashMap<>();

        // Track unique visitors per URL
        private Map<String, Set<String>> uniqueVisitors = new ConcurrentHashMap<>();

        // Track traffic source counts
        private Map<String, AtomicInteger> trafficSources = new ConcurrentHashMap<>();

        // Queue for incoming events
        private BlockingQueue<PageViewEvent> eventQueue = new LinkedBlockingQueue<>();

        // Number of top pages to track
        private final int TOP_N = 10;

        public AnalyticsDashboard() {
            // Start event processing thread
            Thread processor = new Thread(this::processEvents);
            processor.setDaemon(true);
            processor.start();

            // Start dashboard update thread every 5 seconds
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(this::printDashboard, 5, 5, TimeUnit.SECONDS);
        }

        // Simulate event arrival
        public void processEvent(PageViewEvent event) {
            eventQueue.offer(event);
        }

        // Process events from the queue
        private void processEvents() {
            while (true) {
                try {
                    PageViewEvent event = eventQueue.take(); // blocking
                    // Update page views
                    pageViews.computeIfAbsent(event.url, k -> new AtomicInteger(0)).incrementAndGet();

                    // Update unique visitors
                    uniqueVisitors.computeIfAbsent(event.url, k -> ConcurrentHashMap.newKeySet()).add(event.userId);

                    // Update traffic sources
                    trafficSources.computeIfAbsent(event.source.toLowerCase(), k -> new AtomicInteger(0)).incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        // Print the dashboard
        private void printDashboard() {
            System.out.println("\n--- Real-Time Dashboard ---");

            // Top N pages by total views
            PriorityQueue<Map.Entry<String, AtomicInteger>> topPages = new PriorityQueue<>(
                    Map.Entry.comparingByValue(Comparator.comparingInt(AtomicInteger::get))
            );

            for (Map.Entry<String, AtomicInteger> entry : pageViews.entrySet()) {
                topPages.offer(entry);
                if (topPages.size() > TOP_N) topPages.poll();
            }

            List<Map.Entry<String, AtomicInteger>> topList = new ArrayList<>();
            while (!topPages.isEmpty()) topList.add(topPages.poll());
            Collections.reverse(topList);

            System.out.println("Top Pages:");
            for (int i = 0; i < topList.size(); i++) {
                Map.Entry<String, AtomicInteger> entry = topList.get(i);
                String url = entry.getKey();
                int views = entry.getValue().get();
                int unique = uniqueVisitors.getOrDefault(url, Collections.emptySet()).size();
                System.out.printf("%d. %s - %d views (%d unique)\n", i + 1, url, views, unique);
            }

            // Traffic source percentages
            int totalVisits = trafficSources.values().stream().mapToInt(AtomicInteger::get).sum();
            System.out.println("\nTraffic Sources:");
            for (Map.Entry<String, AtomicInteger> entry : trafficSources.entrySet()) {
                double percent = (entry.getValue().get() * 100.0) / totalVisits;
                System.out.printf("%s: %.0f%%\n", capitalize(entry.getKey()), percent);
            }
        }

        private String capitalize(String s) {
            if (s.isEmpty()) return s;
            return s.substring(0, 1).toUpperCase() + s.substring(1);
        }

        // Example usage
        public static void main(String[] args) throws InterruptedException {
            AnalyticsDashboard dashboard = new AnalyticsDashboard();

            // Simulate incoming page view events
            dashboard.processEvent(new PageViewEvent("/article/breaking-news", "user_123", "google"));
            dashboard.processEvent(new PageViewEvent("/article/breaking-news", "user_456", "facebook"));
            dashboard.processEvent(new PageViewEvent("/sports/championship", "user_789", "direct"));
            dashboard.processEvent(new PageViewEvent("/article/breaking-news", "user_123", "google"));
            dashboard.processEvent(new PageViewEvent("/sports/championship", "user_101", "google"));

            // Keep the program running to see dashboard updates
            Thread.sleep(20000);
        }
    }

