package com.nizlumina.minori.core.Nyaa.Parser;

/**
 * Similar to parsing fileType in NyaaXMLParser
 */
public class NyaaIDParser
{
    /**
     * Example string: "yooo5hooo5=420691"
     * '=' is at index = 10
     * Length = 17,
     * hence reverseIndex = 16 (0 to 16)
     * <p/>
     * First loop:
     * i = 0, reverseIndex - i -> 16 - 0 = 16 (charAt 16: 1)
     * <p/>
     * Second loop:
     * i = 1, reverseIndex - i = 15 (charAt 15: 9)
     * <p/>
     * Third loop
     * i = 2, reverseIndex - i = 14 (charAt 14: 6)
     * <p/>
     * so when,
     * <p/>
     * i = 6, reverseIndex - i -> 16 - 6 = 10 (charAt 10 : =)
     * <p/>
     * so substring( (entryLength - i -> 17 - 6 = 11) , 17) = 420691
     */
    public int parseID(String inputString)
    {
        int entryLength = inputString.length();
        int reverseIndex = entryLength - 1;
        for (int i = 0; i < reverseIndex; i++)
        {
            try
            {
                if (inputString.charAt(reverseIndex - i) == '=')
                {
                    return Integer.valueOf(inputString.substring(entryLength - i, entryLength));
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return -1;
    }
}
