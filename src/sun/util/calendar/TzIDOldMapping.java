package sun.util.calendar;

import java.util.HashMap;
import java.util.Map;

class TzIDOldMapping
{
  static final Map<java.lang.String, java.lang.String> MAP = new HashMap();

  static
  {
    [Ljava.lang.String[] arrayOfString;1 = { { "ACT", "Australia/Darwin" }, { "AET", "Australia/Sydney" }, { "AGT", "America/Argentina/Buenos_Aires" }, { "ART", "Africa/Cairo" }, { "AST", "America/Anchorage" }, { "BET", "America/Sao_Paulo" }, { "BST", "Asia/Dhaka" }, { "CAT", "Africa/Harare" }, { "CNT", "America/St_Johns" }, { "CST", "America/Chicago" }, { "CTT", "Asia/Shanghai" }, { "EAT", "Africa/Addis_Ababa" }, { "ECT", "Europe/Paris" }, { "EST", "America/New_York" }, { "HST", "Pacific/Honolulu" }, { "IET", "America/Indianapolis" }, { "IST", "Asia/Calcutta" }, { "JST", "Asia/Tokyo" }, { "MIT", "Pacific/Apia" }, { "MST", "America/Denver" }, { "NET", "Asia/Yerevan" }, { "NST", "Pacific/Auckland" }, { "PLT", "Asia/Karachi" }, { "PNT", "America/Phoenix" }, { "PRT", "America/Puerto_Rico" }, { "PST", "America/Los_Angeles" }, { "SST", "Pacific/Guadalcanal" }, { "VST", "Asia/Saigon" } };
    [Ljava.lang.String[] arrayOfString;2 = arrayOfString;1;
    int i = arrayOfString;2.length;
    for (int j = 0; j < i; ++j)
    {
      [Ljava.lang.String localString; = arrayOfString;2[j];
      MAP.put(localString;[0], localString;[1]);
    }
  }
}