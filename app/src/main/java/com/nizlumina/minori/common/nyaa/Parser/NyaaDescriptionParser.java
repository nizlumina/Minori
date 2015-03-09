package com.nizlumina.minori.common.nyaa.Parser;


import com.nizlumina.minori.common.nyaa.NyaaEntry;

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
                        inputEntry.setTrustCategory(NyaaEntry.Trust.APLUS);
                    }

                    else
                    {
                        inputEntry.setTrustCategory(NyaaEntry.Trust.TRUSTED);
                    }
                }
                else if (input.contains(NyaaEntry.Static.REMAKES))
                {
                    inputEntry.setTrustCategory(NyaaEntry.Trust.REMAKES);
                }
                else
                {
                    inputEntry.setTrustCategory(NyaaEntry.Trust.ALL);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }


}
