
import java.util.*;

class PlagiarismDetector {

    // n-gram size (5-gram recommended)
    private static final int N = 5;

    // ngram -> set of document IDs
    private Map<String, Set<String>> ngramIndex = new HashMap<>();

    // document -> list of its ngrams
    private Map<String, List<String>> documentNgrams = new HashMap<>();


    // Generate n-grams from text
    private List<String> generateNgrams(String text) {
        List<String> ngrams = new ArrayList<>();

        String[] words = text.toLowerCase().split("\\s+");

        for (int i = 0; i <= words.length - N; i++) {
            StringBuilder gram = new StringBuilder();

            for (int j = 0; j < N; j++) {
                gram.append(words[i + j]).append(" ");
            }

            ngrams.add(gram.toString().trim());
        }

        return ngrams;
    }


    // Add document to database
    public void addDocument(String docId, String text) {

        List<String> ngrams = generateNgrams(text);
        documentNgrams.put(docId, ngrams);

        for (String gram : ngrams) {

            ngramIndex.putIfAbsent(gram, new HashSet<>());
            ngramIndex.get(gram).add(docId);

        }
    }


    // Analyze document for plagiarism
    public void analyzeDocument(String docId) {

        List<String> ngrams = documentNgrams.get(docId);

        if (ngrams == null) {
            System.out.println("Document not found.");
            return;
        }

        Map<String, Integer> matchCount = new HashMap<>();


        for (String gram : ngrams) {

            Set<String> docs = ngramIndex.get(gram);

            if (docs != null) {

                for (String otherDoc : docs) {

                    if (!otherDoc.equals(docId)) {
                        matchCount.put(otherDoc,
                                matchCount.getOrDefault(otherDoc, 0) + 1);
                    }

                }

            }
        }


        System.out.println("Extracted " + ngrams.size() + " n-grams");

        for (String otherDoc : matchCount.keySet()) {

            int matches = matchCount.get(otherDoc);

            double similarity =
                    (matches * 100.0) / ngrams.size();

            System.out.println("Found " + matches +
                    " matching n-grams with \"" +
                    otherDoc + "\"");

            System.out.printf("Similarity: %.2f%%", similarity);

            if (similarity > 50) {
                System.out.println(" (PLAGIARISM DETECTED)");
            } else if (similarity > 10) {
                System.out.println(" (suspicious)");
            } else {
                System.out.println();
            }

            System.out.println();
        }
    }


    // Main method for testing
    public static void main(String[] args) {

        PlagiarismDetector detector = new PlagiarismDetector();

        String essay1 =
                "machine learning is a method of data analysis " +
                        "that automates analytical model building";

        String essay2 =
                "machine learning is a method of data analysis " +
                        "that automates analytical models";

        String essay3 =
                "the quick brown fox jumps over the lazy dog";

        detector.addDocument("essay_089.txt", essay1);
        detector.addDocument("essay_092.txt", essay2);
        detector.addDocument("essay_123.txt", essay3);

        detector.analyzeDocument("essay_092.txt");
    }
}