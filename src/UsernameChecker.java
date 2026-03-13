import java.util.*;
public class UsernameChecker {

        // HashMap to store username -> userId
        private HashMap<String, Integer> usernameMap;

        // HashMap to store username attempt frequency
        private HashMap<String, Integer> attemptFrequency;

        public UsernameChecker() {
            usernameMap = new HashMap<>();
            attemptFrequency = new HashMap<>();
        }

        // Method to add existing users
        public void addUser(String username, int userId) {
            usernameMap.put(username, userId);
        }

        // Check username availability
        public boolean checkAvailability(String username) {

            // Track attempt frequency
            attemptFrequency.put(username,
                    attemptFrequency.getOrDefault(username, 0) + 1);

            // O(1) lookup
            return !usernameMap.containsKey(username);
        }

        // Suggest alternative usernames
        public List<String> suggestAlternatives(String username) {

            List<String> suggestions = new ArrayList<>();

            for (int i = 1; i <= 5; i++) {
                String newUsername = username + i;

                if (!usernameMap.containsKey(newUsername)) {
                    suggestions.add(newUsername);
                }
            }

            return suggestions;
        }

        // Show popularity of attempted usernames
        public void showAttemptFrequency() {
            System.out.println("\nUsername Attempt Frequency:");
            for (String name : attemptFrequency.keySet()) {
                System.out.println(name + " -> " + attemptFrequency.get(name));
            }
        }

        public static void main(String[] args) {

            UsernameChecker checker = new UsernameChecker();

            // Pre-existing users (simulate 10M users)
            checker.addUser("john_doe", 101);
            checker.addUser("alex123", 102);
            checker.addUser("emma_watson", 103);

            Scanner sc = new Scanner(System.in);

            System.out.print("Enter username to check: ");
            String username = sc.nextLine();

            boolean available = checker.checkAvailability(username);

            if (available) {
                System.out.println(username + " is available!");
            } else {
                System.out.println(username + " is already taken.");

                List<String> suggestions = checker.suggestAlternatives(username);

                System.out.println("Suggested Usernames:");
                for (String s : suggestions) {
                    System.out.println(s);
                }
            }

            checker.showAttemptFrequency();
        }
    }




