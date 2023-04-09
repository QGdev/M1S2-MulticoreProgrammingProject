package fr.univnantes.pmc.project.tools;

import fr.univnantes.pmc.project.api.ParsedPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains the tools necessary to -- parse the inputs the program -- parse web
 * page, searching for a regular expression and hyperlinks -- print the outputs
 * of the program Tools.initialize("--help") outputs the list of possible
 * options
 *
 * @author Matthieu Perrin
 */
public final class Tools {

    private static Pattern matchPattern = null;
    private static int nbThreads = 1;
    private static final List<String> startingURL = new LinkedList<String>();
    private static boolean count = false; // -c --count
    private static boolean emphasize = false; // -e --emphasize
    private static boolean noFilename = false; // -f --no-filename
    private static boolean filesWithMatches = false; // -l --files-with-matches
    private static boolean onlyMatching = false; // -o --only-matching
    private static boolean quiet = false; // -q --quiet
    private static boolean initialTab = false; // -t --initial-tab
    private static boolean offline = false; // -O --offline

    /**
     * Gets the regular expression for which matching expressions will be searched
     * on the Web
     *
     * @return the regular expression
     * @see Pattern
     */
    public static String getRegularExpression() {
        return matchPattern.pattern();
    }

    /**
     * Sets the regular expression for which matching expressions will be searched
     * on the Web
     *
     * @param regExp the regular expression
     * @see Pattern
     */
    public static void setRegularExpression(String regExp) {
        matchPattern = Pattern.compile(regExp, Pattern.DOTALL);
    }

    /**
     * Gets the number of threads on which the program must be parallelized
     *
     * @return the number of threads on which the program must be parallelized
     */
    public static int numberThreads() {
        return nbThreads;
    }

    /**
     * Gets the list of URLs from which the expression must be recursively searched
     * on the Web These URLs are typically found in the options used to initialize
     * the program
     *
     * @return The list of URL seeds
     */
    public static List<String> startingURL() {
        return startingURL;
    }

    /**
     * Sets the options of the class according to the arguments
     * Tools.initialize("--help") outputs the list of possible options
     *
     * @param args An array of arguments, as obtained in the main() method
     */
    public static void initialize(String[] args) {
        for (String input : args) {
            if (input.charAt(0) == '-') {
                if ((input.charAt(1) != '-' && input.contains("c")) || input.equals("--count"))
                    count = true;
                if ((input.charAt(1) != '-' && input.contains("e")) || input.equals("--emphasize"))
                    emphasize = true;
                if ((input.charAt(1) != '-' && input.contains("f")) || input.equals("--no-filename"))
                    noFilename = true;
                if ((input.charAt(1) != '-' && input.contains("l")) || input.equals("--files-with-matches"))
                    filesWithMatches = true;
                if ((input.charAt(1) != '-' && input.contains("o")) || input.equals("--only-matching"))
                    onlyMatching = true;
                if ((input.charAt(1) != '-' && input.contains("q")) || input.equals("--quiet"))
                    quiet = true;
                if ((input.charAt(1) != '-' && input.contains("t")) || input.equals("--initial-tab"))
                    initialTab = true;
                if ((input.charAt(1) != '-' && input.contains("O")) || input.equals("--offline"))
                    offline = true;
                if ((input.charAt(1) != '-' && input.contains("h")) || input.equals("--help")) {
                    System.out.println("Use : java WebGrep [OPTION]... PATTERN [URL]...");
                    System.out.println("Search for PATTERN recursively on the Web, starting from the given URL.");
                    System.out.println(
                            "Example: java WebGrep -ceht --threads=1000 Nantes https://fr.wikipedia.org/wiki/Nantes");
                    System.out.println();
                    System.out.println("Options:");
                    System.out.println("\t-c, --count\t\t\tPrint a count of matching html block for each found url.");
                    System.out.println("\t-e, --emphasize\t\t\tPrint the matching expression in colors.");
                    System.out.println("\t-f, --no-filename\t\tSuppress the prefixing of url on output.");
                    System.out.println("\t-h, --help\t\t\tOutput a usage message.");
                    System.out.println(
                            "\t-l, --files-with-matches\tSuppress normal output; instead print the url of each page that contains the pattern.");
                    System.out.println(
                            "\t-o, --only-matching\t\tPrint only the matched (non-empty) parts of a matching line, with each such part on a separate output line.");
                    System.out.println("\t-q, --quiet\t\t\tQuiet; do not write anything to standard output.");
                    System.out.println(
                            "\t-t, --initial-tab\t\tMake sure that the first character of actual line content lies on a tab stop, so that the alignment of tabs looks normal.");
                    System.out.println(
                            "\t-O, --offline\t\tOpen the local copy of the web page. Useful if a firewall blocks your internet access.");
                    System.out.println("\t    --threads=n\t\t\tParallelizes the search amongst n threads");
                }
                if (input.contains("--threads=")) {
                    nbThreads = Integer.parseInt(input.substring(10));
                }
            } else if (matchPattern == null) {
                setRegularExpression(input);
            } else {
                startingURL.add(input);
            }
        }
    }

    /**
     * Sets the options of the class according to the arguments
     * Tools.initialize("--help") outputs the list of possible options
     *
     * @param args A string containing a list of arguments separated by spaces
     */
    public static void initialize(String args) {
        initialize(args.split(" "));
    }

    /**
     * Opens the Web page represented by the URL, and researches expressions
     * matching the pattern, as well as hyperlinks contained in the page.
     *
     * @param address the URL of the Web page to parse
     * @return a representation of the Web page, including its matching expressions
     * and hyperlinks
     * @throws IOException if the specified URL cannot be reached
     */
    public static ParsedPage parsePage(final String address) throws IOException {
        Document doc;
        if (offline) {
            File input = new File(address);
            doc = Jsoup.parse(input, "UTF-8", address);
        } else {
            doc = Jsoup.connect(address).get();
        }
        // Search for the regular expression in the page
        final List<String> matches = new LinkedList<String>();
        for (Element e : doc.select("h1,h2,h3,h4,h5,p,dt,dd")) {
            if (matchPattern.matcher(e.text()).find())
                matches.add(e.text());
        }
        // Search for the hypertext links in the page
        final List<String> hrefs = new LinkedList<String>();
        for (Element e : doc.select("a[href]")) {
            hrefs.add(e.attr("abs:href").split("#")[0]);
        }
        return new ParsedPage() {
            public String address() {
                return address;
            }

            public List<String> matches() {
                return matches;
            }

            public List<String> hrefs() {
                return hrefs;
            }
        };
    }

    /**
     * Outputs the matches found in the given Web page, according to the options
     * passed at initialization
     *
     * @param p Parsed page obtained as the result of Tools.parsePage(address);
     */
    public static void print(ParsedPage p) {
        if (!quiet && !p.matches().isEmpty()) {
            // Print the header with address and count
            if (!noFilename && count)
                System.out.print(p.address() + ": " + p.matches().size() + "\n");
            else if (!noFilename)
                System.out.print(p.address() + "\n");
            else if (count)
                System.out.print(p.matches().size() + "\n");
            // Print the list of matches
            if (!filesWithMatches) {
                for (String s : p.matches()) {
                    Matcher m = matchPattern.matcher(s);
                    while (m.find()) {
                        if (initialTab)
                            System.out.print("\t");
                        if (!onlyMatching)
                            System.out.print(s.substring(0, m.start(0)));
                        if (emphasize)
                            System.out.print("\033[0;31m");
                        System.out.print(m.group(0));
                        if (emphasize)
                            System.out.print("\u001B[0m");
                        if (!onlyMatching)
                            System.out.print(s.substring(m.end(0)));
                    }
                    System.out.println();
                }
            }
        }
    }
}
