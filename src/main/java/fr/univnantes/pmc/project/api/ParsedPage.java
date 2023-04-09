package fr.univnantes.pmc.project.api;

import java.util.List;

/**
 * Represents the results of a parsed Web Page, including its matching expressions and hyperlinks.
 *
 * @author Matthieu Perrin
 */
public interface ParsedPage {
	/**
	 * Gets the URL of the Web page
	 *
	 * @return URL of the Web page
	 */
	String address();

	/**
	 * Gets the list of HTML elements (h1, h2, h3, h4, h5, p, dt and dd) containing a subexpression matching the regular expression
	 *
	 * @return list of HTML elements
	 */
	List<String> matches();

	/**
	 * Gets the list of URLs references on the page
	 *
	 * @return list of URLs
	 */
	List<String> hrefs();
}

