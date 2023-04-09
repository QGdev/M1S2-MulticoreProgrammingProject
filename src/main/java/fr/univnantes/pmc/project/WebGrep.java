package fr.univnantes.pmc.project;

import fr.univnantes.pmc.project.api.ParsedPage;
import fr.univnantes.pmc.project.impl.TL2Dictionary;
import fr.univnantes.pmc.project.threadpool.ThreadPool;
import fr.univnantes.pmc.project.tools.MichaelScottQueue;
import fr.univnantes.pmc.project.tools.Tools;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class WebGrep {

    // Used to store each urls that have been explored
    private final static TL2Dictionary explored_ = new TL2Dictionary();

    // Used to store each urls that have been explored and their parsed page
    private final static ConcurrentHashMap<String, ParsedPage> explored = new ConcurrentHashMap<>();

    // Used to linearize the printing of the results
    private final static MichaelScottQueue<String> printQueue = new MichaelScottQueue<>();

    // Used to parallelize the exploration of the pages with our custom thread pool
    private final static ThreadPool threadPool = new ThreadPool(Tools.numberThreads());

    private final static ParsedPage nullPage = new ParsedPage() {

        @Override
        public List<String> matches() {
            return null;
        }

        @Override
        public List<String> hrefs() {
            return null;
        }

        @Override
        public String address() {
            return null;
        }
    };

    /**
     * Explore a page and recursively explore other pages
     *
     * @param address the address of the page to explore
     */
    private static void explore(String address) {
        threadPool.submit(() -> {
            try {
                /*
                 * Check that the page was not already explored and adds it
                 */
                if (explored.putIfAbsent(address, nullPage) == null) {
                    // Parse the page to find matches and hypertext links
                    ParsedPage page = Tools.parsePage(address);
                    if (!page.matches().isEmpty()) {

                        explored.put(address, page);
                        explored_.add(address);
                        printQueue.enqueue(address);

                        // Recursively explore other pages
                        for (String href : page.hrefs())
                            explore(href);
                    }
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
                /* We could retry later... */
            }
        });

    }


    public static void main(String[] args) throws InterruptedException, IOException {
        // Initialize the program using the options given in argument
        if (args.length == 0)
            Tools.initialize("-cet --threads=1000 Nantes https://fr.wikipedia.org/wiki/Nantes");
        else
            Tools.initialize(args);

        // Get the starting URL given in argument
        for (String address : Tools.startingURL())
            explore(address);

        String url;
        while (true) {
            url = printQueue.dequeue();
            if (url != null)
                Tools.print(explored.get(url));
        }
    }
}