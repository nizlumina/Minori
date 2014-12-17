package com.nizlumina.minori.core.Nyaa.Parser;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.nizlumina.minori.core.Nyaa.NyaaEntry;
import com.nizlumina.minori.core.Nyaa.NyaaFansubGroup;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

//import android.util.Log;

/**
 * Base parser for all Nyaa RSS stream. Parse() will take the resulting network inputStream and spits out a list of NyaaEntry
 * <p>
 * The Parse method parse each xml object thru a series of internal parsers.
 * The title where most of the meat is is actualy parsed from in an order from right to left AND THEN from left to right.
 * </p>
 */
public class NyaaXMLParser
{
    final String debugKey = "NyaaXMLParser.";

    /**
     * Optional grouping. You can remove this if you don't need it.
     * Note that the method read the resulting list from Parse() and create a list of FansubGroup(s) that also holds reference to the original list items.
     *
     * @param rawList
     * @return
     */
    public static ArrayList<NyaaFansubGroup> Group(@NonNull ArrayList<NyaaEntry> rawList)
    {
        HashMap<Pair<String, String>, NyaaFansubGroup> fMap = new HashMap<Pair<String, String>, NyaaFansubGroup>();

        for (NyaaEntry rawEntry : rawList)
        {
            if (rawEntry.fansub == null || rawEntry.title == null)
            {
//                Log.v(debugKey + "Group()", rawEntry.stringData());
                continue;
            }
            NyaaFansubGroup nyaaFansubGroup;

            Pair<String, String> pair = new Pair<String, String>(rawEntry.fansub, rawEntry.title);
            if (!fMap.keySet().contains(pair))
            {
                nyaaFansubGroup = new NyaaFansubGroup(rawEntry.fansub);

                nyaaFansubGroup.id = rawEntry.id;
                nyaaFansubGroup.seriesTitle = rawEntry.title;
                nyaaFansubGroup.latestEpisode = rawEntry.currentEpisode;
                nyaaFansubGroup.trustCategory = rawEntry.trustCategory;
                nyaaFansubGroup.resolutions.add(rawEntry.resolution);
                nyaaFansubGroup.animeEntries.add(rawEntry);

                fMap.put(new Pair<String, String>(rawEntry.fansub, rawEntry.title), nyaaFansubGroup);
            }
            else
            {
                nyaaFansubGroup = fMap.get(new Pair<String, String>(rawEntry.fansub, rawEntry.title));

                nyaaFansubGroup.animeEntries.add(rawEntry);

                if (rawEntry.currentEpisode > nyaaFansubGroup.latestEpisode)
                {
                    nyaaFansubGroup.latestEpisode = rawEntry.currentEpisode;
                    nyaaFansubGroup.id = rawEntry.id;
                }


                if (nyaaFansubGroup.trustCategory.ordinal() < rawEntry.trustCategory.ordinal()) //Nyaa Rules. If the group already is A+ in another release, they will be considered A+ in another release.. or was it still the same now..?
                {
                    nyaaFansubGroup.trustCategory = rawEntry.trustCategory;
                }

                if (rawEntry.resolution != null && !nyaaFansubGroup.resolutions.contains(rawEntry.resolution))
                {
                    nyaaFansubGroup.resolutions.add(rawEntry.resolution);
                }

                if (rawEntry.quality != null && !nyaaFansubGroup.qualities.contains(rawEntry.quality))
                {
                    nyaaFansubGroup.qualities.add(rawEntry.quality);
                }
            }
        }

        ArrayList<NyaaFansubGroup> finalGroups = new ArrayList<NyaaFansubGroup>(fMap.values());

        for (NyaaFansubGroup nyaaFansubGroup : finalGroups)
        {
            if (nyaaFansubGroup.animeEntries.size() > 1)
            {
                Collections.sort(nyaaFansubGroup.animeEntries, new Comparator<NyaaEntry>()
                {
                    @Override
                    public int compare(NyaaEntry entry, NyaaEntry entry2)
                    {
                        return entry.currentEpisode - entry2.currentEpisode; //ascending since when downloaded it goes 1 -> 5 and torrent dl-der can add based on that order (oldest first)
                    }
                });
            }
        }
        return finalGroups;
    }

    //This will always return an empty list (not null) even if nothing is found
    public ArrayList<NyaaEntry> Parse(InputStream inputStream) //throws XmlPullParserException,IOException... or yer mom
    {
        ArrayList<NyaaEntry> entries = new ArrayList<NyaaEntry>();

        try
        {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();

            xpp.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            xpp.setInput(inputStream, null);
            xpp.nextTag();
            return parseFeed(xpp, entries);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /*  Todo: [SEMI-DONE]. Nullified all the non-group entries. A-OK. Sample shitty file title: Hanamonogatari PV3 [Trollo]. Might put parseTitle() to return null on this shit, and then simply encapsulate with if(entry!= null).
        Still, might need to revise to [Generic Tag] file name - episode [extras][extras]
     */
    private ArrayList<NyaaEntry> parseFeed(XmlPullParser xpp, ArrayList<NyaaEntry> entries) throws XmlPullParserException, IOException
    {
        int eventType = xpp.getEventType();
        //int index = 0, tagCount = 0;
        boolean insideItem = false;

        //cache
        String name, parsedtext;
        String item = "item", description = "description", title = "title", link = "link", pubDate = "pubDate";

        //init entry and titleParser
        NyaaEntry entry = new NyaaEntry();
        NyaaTitleParser titleParser = new NyaaTitleParser();
        NyaaIDParser idParser = new NyaaIDParser();
        NyaaDescriptionParser descParser = new NyaaDescriptionParser();

        while (eventType != XmlPullParser.END_DOCUMENT)
        {
            switch (eventType)
            {
                case XmlPullParser.START_DOCUMENT:
                    break;
                case XmlPullParser.START_TAG:
                    //tagCount++;
                    name = xpp.getName();
                    if (name.equals(item) && !insideItem)
                    {
                        //index++; //For debugging
                        insideItem = true;
                    }
                    //Process first level childs
                    else if (insideItem)
                    {
                        xpp.next();
                        parsedtext = xpp.getText();
                        if (name.equals(title))
                        {
                            //Log.i("PARSER", "Title found: " + parsedtext);
                            entry = titleParser.parseTitle(parsedtext);
                        }
                        else if (entry != null && !entry.failToParse)
                        {
                            if (name.equals(link))
                            {
                                entry.torrentLink = parsedtext;

                                //Also set id to nyaa torrentID (easier to find and download too)
                                entry.id = idParser.parseID(parsedtext);
                            }
                            else if (name.equals(description)) //this actually get the strings inside CDATA
                            {
                                //entry.description = parsedtext;//set trust
                                descParser.parseTrust(entry, parsedtext);
                                //Log.i("PARSER", "description found: " + parsedtext);
                            }
                            else if (name.equals(pubDate))
                            {
                                entry.pubDate = parsedtext;
                            }
                        }

                    }
                    break;
                case XmlPullParser.END_TAG:
                    //tagCount++;
                    name = xpp.getName();
                    if (name.equals(item))
                    {
                        if (entry != null)
                        {
                            if (entry.rawTitle != null && !entry.failToParse) entries.add(entry);
                        }
                        insideItem = false;
                        //Log.i("Parser", "End item " + index);
                    }
                    break;
            }

            eventType = xpp.next();
        }
        //Log.i("COUNT", "TagCount: " + tagCount);

        //entriesDebug(entries);

        return entries;
    }

}