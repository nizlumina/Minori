package com.nizlumina.minori.core.Nyaa.Parser;


import com.nizlumina.minori.core.Nyaa.NyaaEntry;

/**
 * Description parser. May handle extra method for checking seeds number and size yadda yadda in the future
 */
public class NyaaDescriptionParser
{
    public void parseTrust(NyaaEntry inputEntry, String input)
    {
        if (input.length() > 0)
        {
            try
            {
                if (input.contains(NyaaEntry.Static.TRUSTED))
                {
                    if (input.contains(NyaaEntry.Static.A))
                    {
                        inputEntry.trustCategory = NyaaEntry.Trust.APLUS;
                    }

                    else
                    {
                        inputEntry.trustCategory = NyaaEntry.Trust.TRUSTED;
                    }
                }
                else if (input.contains(NyaaEntry.Static.REMAKES))
                {
                    inputEntry.trustCategory = NyaaEntry.Trust.REMAKES;
                }
                else
                {
                    inputEntry.trustCategory = NyaaEntry.Trust.ALL;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }


}
