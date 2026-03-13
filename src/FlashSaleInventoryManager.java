import java.util.*;
public class FlashSaleInventoryManager {




        // productId -> stockCount
        private HashMap<String, Integer> inventory;

        // productId -> waiting list (FIFO)
        private HashMap<String, LinkedHashMap<Integer, Integer>> waitingList;

        public FlashSaleInventoryManager() {
            inventory = new HashMap<>();
            waitingList = new HashMap<>();
        }

        // Add product with stock
        public void addProduct(String productId, int stock) {
            inventory.put(productId, stock);
            waitingList.put(productId, new LinkedHashMap<>());
        }

        // Check stock availability
        public int checkStock(String productId) {
            return inventory.getOrDefault(productId, 0);
        }

        // Purchase item (Thread-safe)
        public synchronized void purchaseItem(String productId, int userId) {

            int stock = inventory.getOrDefault(productId, 0);

            if (stock > 0) {

                stock--;
                inventory.put(productId, stock);

                System.out.println("User " + userId +
                        " purchased " + productId +
                        " successfully. Remaining stock: " + stock);

            } else {

                LinkedHashMap<Integer, Integer> queue = waitingList.get(productId);

                int position = queue.size() + 1;
                queue.put(userId, position);

                System.out.println("User " + userId +
                        " added to waiting list. Position #" + position);
            }
        }

        // Display waiting list
        public void showWaitingList(String productId) {

            LinkedHashMap<Integer, Integer> queue = waitingList.get(productId);

            System.out.println("\nWaiting List for " + productId);

            for (Map.Entry<Integer, Integer> entry : queue.entrySet()) {
                System.out.println("UserID: " + entry.getKey() +
                        " Position: " + entry.getValue());
            }
        }

        public static void main(String[] args) {

            FlashSaleInventoryManager manager = new FlashSaleInventoryManager();

            // Add product with stock
            manager.addProduct("IPHONE15_256GB", 5);

            System.out.println("Stock Available: "
                    + manager.checkStock("IPHONE15_256GB"));

            // Simulating multiple purchase requests
            manager.purchaseItem("IPHONE15_256GB", 101);
            manager.purchaseItem("IPHONE15_256GB", 102);
            manager.purchaseItem("IPHONE15_256GB", 103);
            manager.purchaseItem("IPHONE15_256GB", 104);
            manager.purchaseItem("IPHONE15_256GB", 105);

            // Stock finished → waiting list starts
            manager.purchaseItem("IPHONE15_256GB", 106);
            manager.purchaseItem("IPHONE15_256GB", 107);

            manager.showWaitingList("IPHONE15_256GB");
        }
    }

