package com.nizlumina.minori.core.Nyaa.Parser;

//import android.util.Log;

import com.nizlumina.minori.core.Nyaa.NyaaEntry;


/**
 * Handle parsing single line strings into an AnimeEntry. To use it, create a local instance and use parseTitle() to parse.
 * TODO: optimize char.eauals(comparisons)
 */
public class NyaaTitleParser
{
    static final String[] QUALITY_TYPE = new String[]{"BD", "BDRIP", "AAC", "SD", "Hi10P", "Hi10", "10bit", "8bit"};
    static final String[] RES_TYPE = new String[]{"720p", "720", "480", "480p", "1080", "1080p"};

    static final String[] EXTRAS = new String[]{"V0", "V1", "V2", "V3", "V4", "V5", "v0", "v1", "v2", "v3", "v4", "v5"};

    @SuppressWarnings("ConstantConditions")
    public NyaaEntry parseTitle(String mEntryString)
    {

        if (mEntryString == null) return null;

        String entryString = mEntryString.trim();

        if (entryString.length() == 0 || !(entryString.charAt(0) == '[' || entryString.charAt(0) == '('))
            return null;

        NyaaEntry nyaaEntry = new NyaaEntry();
        try
        {
            int index = 0, cursorIndex = 0, titleStartIndex = 0;

            boolean bracketStart = false, groupSet = false, titleStart = false, rawTitleSet = false;
            String[] tags = new String[20];

            int tagIndex = 0; // Just to count the tags included in the string.

            //Main parse
            int entryLength = entryString.length();

            char[] entry = entryString.toCharArray();

            for (Character c : entry)
            {
                //Get Group, first bracket
                if (!bracketStart)
                {
                    if (c == '[' || c == '(')
                    {
                        bracketStart = true;
                        cursorIndex = index + 1; //IMPORTANT: cursorIndex is referring to the first character AFTER the first bracket

                        if (titleStart) //ignore this block when reading through the first time
                        {
                            rawTitleSet = true;
                            String rawTitle = entryString.substring(titleStartIndex, index);
                            nyaaEntry.rawTitle = rawTitle.replace('_', ' ').trim();
                            titleStart = false;
                        }
                    }
                }
                else
                {
                    if (c == ']' || c == ')')
                    {
                        bracketStart = false;
                        if (!groupSet)
                        {
                            groupSet = true;
                            nyaaEntry.fansub = entryString.substring(cursorIndex, index);
                        }
                        if (rawTitleSet)
                        {
                            if (tagIndex < 20)
                            {
                                tags[tagIndex] = entryString.substring(cursorIndex, index);
                                tagIndex++;
                            }

                        }
                    }
                }

                if (!rawTitleSet)
                {
                    if (groupSet && !titleStart)
                    {
                        titleStart = true;
                        titleStartIndex = index + 1; //functionally, its the same as cursorIndex
                    }
                }

                index++;
            }

            //Get fileType
            int reverseIndex = entryLength - 1;
            for (int i = 0; i < reverseIndex; i++)
            {
                try
                {
                    if (entry[reverseIndex - i] == '.')
                    {
                        nyaaEntry.fileType = entryString.substring(entryLength - i, entryLength);
                        break;
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            //Get extras and sanitize rawTitle
            try
            {
                String rawTitle = nyaaEntry.rawTitle;
                if (rawTitle != null)
                    for (String extra : EXTRAS)
                    {
                        if (rawTitle.contains(extra))
                        {
                            nyaaEntry.extras = extra;
                            nyaaEntry.rawTitle = rawTitle.replace(extra, "").trim();
                        }
                    }
            }
            catch (Exception e)
            {
                e.printStackTrace();
//                Log.v("NyaaTitleParser - parseTitle()", "Entry string[" + entryString + "]" + nyaaEntry.stringData());
            }

            //Get currentEpisode and finalize Title
            if (rawTitleSet)
            {
                char[] titleChars = nyaaEntry.rawTitle.toCharArray();
                int titleLength = nyaaEntry.rawTitle.length();
                int numberStartIndex = -1;
                boolean foundNumber = false, sanitizing = false;
                for (int i = titleLength - 1, base = 0; i >= 0; i--)
                {
                    if (sanitizing)
                    {
                        if (titleChars[i] != ' ' && titleChars[i] != '_' && titleChars[i] != '-')
                        {
                            //Log.v("NyaaTitleParser", "Index = " + i + "Raw:"+ animeEntry.rawTitle);
                            nyaaEntry.title = nyaaEntry.rawTitle.substring(0, i + 1);//since substring(z, k) => k denote length inclusively from z.

                            if (nyaaEntry.title == null || nyaaEntry.title.length() == 0)
                                return null;
                            //Log.v("NyaaTitleParser", "Output:"+animeEntry.title);

                            nyaaEntry.episodeString = nyaaEntry.rawTitle.substring(numberStartIndex, titleLength);
                            break;
                        }
                        continue;
                    }

                    if (titleChars[i] == ' ' || titleChars[i] == '_')
                    {
                        if (foundNumber)
                        {
                            numberStartIndex = i + 1;
                            sanitizing = true;
                        }
                        continue;
                    }

                    if (Character.isDigit(titleChars[i]))
                    {
                        int digit = Character.getNumericValue(titleChars[i]);

                        if (digit >= 0)
                        {
                            if (!foundNumber)
                                foundNumber = true;
                            if (nyaaEntry.currentEpisode < 0)
                                nyaaEntry.currentEpisode = 0;

                            int compoundDigit = digit * intPow(10, base);
                            nyaaEntry.currentEpisode += compoundDigit;
                            base++;
                        }
                    }
                    else
                    {
                        nyaaEntry.failToParse = true;
                        break;
                    }


                }
            }

            //SetTags
            tagMatching(nyaaEntry, tags);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return nyaaEntry;
    }

    //Simple int exponential to parse episode in base 10
    private int intPow(int a, int b)
    {
        int result = 1;
        for (int i = 0; i < b; i++)
        {
            result *= a;
        }
        return result;

    }

    //Internal comparisons
    private Boolean compareType(String tag, String[] validationStrings)
    {
        for (String validationString : validationStrings)
        {
            if (tag.compareToIgnoreCase(validationString) == 0) //TODO: performance check
                return true;
        }
        return false;
    }

    //Applying tags
    private void tagMatching(NyaaEntry entry, String[] tags)
    {
        for (String tag : tags)
        {
            if (tag == null) continue;

            if (compareType(tag, QUALITY_TYPE))
            {
                entry.quality = tag;
            }
            else if (compareType(tag, RES_TYPE))
            {
                entry.resolutionString = tag;
                entry.resolution = NyaaEntry.Resolution.matchResolution(tag);
            }
            else if (tag.length() == 8 && !tag.contains("x"))
            {
                if (hashStringValidation(tag))
                    entry.hash = tag; //and yer mom
            }
        }

        if (entry.resolution == null) entry.resolution = NyaaEntry.Resolution.DEFAULT;
    }

    private boolean hashStringValidation(String input)
    {
        for (char c : input.toCharArray())
        {
            if (!(c >= '0' && c <= '9' || c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F'))
                return false;
        }
        return true;
    }
}