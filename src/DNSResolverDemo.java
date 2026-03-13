
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.*;

    class DNSEntry {
        String domain;
        String ipAddress;
        long timestamp;
        long expiryTime;

        public DNSEntry(String domain, String ipAddress, long ttlSeconds) {
            this.domain = domain;
            this.ipAddress = ipAddress;
            this.timestamp = System.currentTimeMillis();
            this.expiryTime = timestamp + ttlSeconds * 1000;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }

    class DNSCache {

        private final int capacity;

        // LRU Cache using LinkedHashMap
        private final LinkedHashMap<String, DNSEntry> cache;

        private long hits = 0;
        private long misses = 0;

        private long totalLookupTime = 0;
        private long totalRequests = 0;

        public DNSCache(int capacity) {
            this.capacity = capacity;

            this.cache = new LinkedHashMap<String, DNSEntry>(capacity, 0.75f, true) {
                protected boolean removeEldestEntry(Map.Entry<String, DNSEntry> eldest) {
                    return size() > DNSCache.this.capacity;
                }
            };

            startCleanupThread();
        }

        public synchronized String resolve(String domain) {

            long start = System.nanoTime();

            DNSEntry entry = cache.get(domain);

            if (entry != null && !entry.isExpired()) {
                hits++;
                recordTime(start);
                return entry.ipAddress;
            }

            if (entry != null && entry.isExpired()) {
                cache.remove(domain);
            }

            misses++;

            String ip = queryUpstreamDNS(domain);

            if (ip != null) {
                cache.put(domain, new DNSEntry(domain, ip, 300)); // default TTL
            }

            recordTime(start);

            return ip;
        }

        private void recordTime(long start) {
            long end = System.nanoTime();
            totalLookupTime += (end - start);
            totalRequests++;
        }

        private String queryUpstreamDNS(String domain) {
            try {
                InetAddress address = InetAddress.getByName(domain);
                return address.getHostAddress();
            } catch (Exception e) {
                System.out.println("DNS lookup failed: " + e.getMessage());
                return null;
            }
        }

        private void startCleanupThread() {

            ScheduledExecutorService scheduler =
                    Executors.newSingleThreadScheduledExecutor();

            scheduler.scheduleAtFixedRate(() -> {

                synchronized (this) {

                    Iterator<Map.Entry<String, DNSEntry>> it = cache.entrySet().iterator();

                    while (it.hasNext()) {
                        Map.Entry<String, DNSEntry> entry = it.next();

                        if (entry.getValue().isExpired()) {
                            it.remove();
                        }
                    }
                }

            }, 10, 10, TimeUnit.SECONDS);
        }

        public synchronized void getCacheStats() {

            double hitRate = (totalRequests == 0)
                    ? 0
                    : ((double) hits / totalRequests) * 100;

            double avgLookupMs = (totalRequests == 0)
                    ? 0
                    : (totalLookupTime / totalRequests) / 1_000_000.0;

            System.out.println("Cache Stats:");
            System.out.println("Hits: " + hits);
            System.out.println("Misses: " + misses);
            System.out.println("Hit Rate: " + String.format("%.2f", hitRate) + "%");
            System.out.println("Avg Lookup Time: " + String.format("%.3f", avgLookupMs) + " ms");
        }
    }

    public class DNSResolverDemo {

        public static void main(String[] args) throws Exception {

            DNSCache cache = new DNSCache(5);

            System.out.println("Resolving google.com...");
            String ip1 = cache.resolve("google.com");
            System.out.println("IP: " + ip1);

            Thread.sleep(200);

            System.out.println("\nResolving google.com again...");
            String ip2 = cache.resolve("google.com");
            System.out.println("IP: " + ip2);

            System.out.println("\nResolving openai.com...");
            System.out.println("IP: " + cache.resolve("openai.com"));

            System.out.println("\nResolving github.com...");
            System.out.println("IP: " + cache.resolve("github.com"));

            cache.getCacheStats();
        }
    }


