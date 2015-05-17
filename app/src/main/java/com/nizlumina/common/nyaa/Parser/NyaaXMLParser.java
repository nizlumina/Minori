package com.nizlumina.common.nyaa.Parser;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.nizlumina.common.nyaa.NyaaEntry;
import com.nizlumina.common.nyaa.NyaaFansubGroup;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

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
    public static List<NyaaFansubGroup> Group(@NonNull List<NyaaEntry> rawList)
    {
        HashMap<Pair<String, String>, NyaaFansubGroup> fMap = new HashMap<Pair<String, String>, NyaaFansubGroup>();

        for (NyaaEntry rawEntry : rawList)
        {
            if (rawEntry.getFansub() == null || rawEntry.getTitle() == null)
            {
//                Log.v(debugKey + "Group()", rawEntry.stringData());
                continue;
            }
            NyaaFansubGroup nyaaFansubGroup;

            Pair<String, String> pair = new Pair<String, String>(rawEntry.getFansub(), rawEntry.getTitle());
            if (!fMap.keySet().contains(pair))
            {
                nyaaFansubGroup = new NyaaFansubGroup(rawEntry.getFansub());

                nyaaFansubGroup.setId(rawEntry.getId());
                nyaaFansubGroup.setSeriesTitle(rawEntry.getTitle());
                nyaaFansubGroup.setLatestEpisode(rawEntry.getCurrentEpisode());
                nyaaFansubGroup.setTrustCategory(rawEntry.getTrustCategory());
                nyaaFansubGroup.getResolutions().add(rawEntry.getResolution());
                nyaaFansubGroup.getNyaaEntries().add(rawEntry);

                fMap.put(new Pair<String, String>(rawEntry.getFansub(), rawEntry.getTitle()), nyaaFansubGroup);
            }
            else
            {
                nyaaFansubGroup = fMap.get(new Pair<String, String>(rawEntry.getFansub(), rawEntry.getTitle()));

                nyaaFansubGroup.getNyaaEntries().add(rawEntry);

                if (rawEntry.getCurrentEpisode() > nyaaFansubGroup.getLatestEpisode())
                {
                    nyaaFansubGroup.setLatestEpisode(rawEntry.getCurrentEpisode());
                    nyaaFansubGroup.setId(rawEntry.getId());
                }


                if (nyaaFansubGroup.getTrustCategory().ordinal() < rawEntry.getTrustCategory().ordinal()) //Nyaa Rules. If the group already is A+ in another release, they will be considered A+ in another release.. or was it still the same now..?
                {
                    nyaaFansubGroup.setTrustCategory(rawEntry.getTrustCategory());
                }

                if (rawEntry.getResolution() != null && !nyaaFansubGroup.getResolutions().contains(rawEntry.getResolution()))
                {
                    nyaaFansubGroup.getResolutions().add(rawEntry.getResolution());
                }

                if (rawEntry.getQuality() != null && !nyaaFansubGroup.getQualities().contains(rawEntry.getQuality()))
                {
                    nyaaFansubGroup.getQualities().add(rawEntry.getQuality());
                }
            }
        }

        List<NyaaFansubGroup> finalGroups = new ArrayList<NyaaFansubGroup>(fMap.values());

        for (NyaaFansubGroup nyaaFansubGroup : finalGroups)
        {
            if (nyaaFansubGroup.getNyaaEntries().size() > 1)
            {
                Collections.sort(nyaaFansubGroup.getNyaaEntries(), new Comparator<NyaaEntry>()
                {
                    @Override
                    public int compare(NyaaEntry entry, NyaaEntry entry2)
                    {
                        return entry.getCurrentEpisode() - entry2.getCurrentEpisode(); //ascending since when downloaded it goes 1 -> 5 and torrent dl-der can add based on that order (oldest first)
                    }
                });
            }
        }
        return finalGroups;
    }

    //This will always return an empty list (not null) even if nothing is found
    public List<NyaaEntry> Parse(InputStream inputStream) //throws XmlPullParserException,IOException... or yer mom
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
                        else if (entry != null && !entry.isFailToParse())
                        {
                            if (name.equals(link))
                            {
                                entry.setTorrentLink(parsedtext);

                                //Also set id to nyaa torrentID (easier to find and download too)
                                entry.setId(idParser.parseID(parsedtext));
                            }
                            else if (name.equals(description)) //this actually get the strings inside CDATA
                            {
                                //entry.description = parsedtext;//set trust
                                descParser.parseTrust(entry, parsedtext);
                                //Log.i("PARSER", "description found: " + parsedtext);
                            }
                            else if (name.equals(pubDate))
                            {
                                entry.setPubDate(parsedtext);
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
                            if (entry.getRawTitle() != null && !entry.isFailToParse())
                                entries.add(entry);
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