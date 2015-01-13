/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Nizlumina Studio (Malaysia)
 *
 * Unless specified, permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.nizlumina.minori.android.utility;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility scraper for some of the Hummingbird Pages.
 * As of current, it mainly access the Upcoming chart <a href="https://hummingbird.me/anime/upcoming/">here</a>
 */
public class HummingbirdScraper
{
    static final String identifierClass = "image";
    private static final String endpoint = "https://hummingbird.me/anime/upcoming/";

    public static String getEndpoint()
    {
        return endpoint;
    }

    /**
     * Try to scrape the data from the HTML body
     *
     * @param htmlBody The whole HTML document
     * @return A list of Hummingbird 'slugs' where it can then be fed to the official API.
     */
    public static List<String> scrapeUpcoming(String htmlBody)
    {
        Document document = Jsoup.parse(htmlBody);
        Elements identifierElements = document.getElementsByClass(identifierClass);
        Elements relativeLinks = identifierElements.select("a[href]");
        List<String> slugs = new ArrayList<>();
        for (Element relativeLink : relativeLinks)
        {
            String value = relativeLink.attr("href").substring(7); //it works in the first try!! Woohoo!!

            //Future logging
            //Log.v(HummingbirdScraper.class.getSimpleName(), value);
            slugs.add(value);
        }
        return slugs;
    }
}
