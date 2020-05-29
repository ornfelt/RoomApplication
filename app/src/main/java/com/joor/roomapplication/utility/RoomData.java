package com.joor.roomapplication.utility;

import android.content.res.Resources;

import androidx.core.os.ConfigurationCompat;

/**
 * This class contains image links and information for available group rooms at Campus Gotland
 * @author Jonas Ornfelt & Daniel Arnesson
 */

public class RoomData {

    private String imageLinkAnget = "https://www.campusgotland.uu.se/digitalAssets/732/c_732880-l_3-k_a-nget_400.jpg";
    private String imageLinkBacksippan = "https://www.campusgotland.uu.se/digitalAssets/732/c_732884-l_3-k_backsippan_717x400.jpg";
    private String imageLinkC11 = "https://www.campusgotland.uu.se/digitalAssets/706/c_706752-l_3-k_c11_717.jpg";
    private String imageLinkC13 = "https://www.campusgotland.uu.se/digitalAssets/706/c_706750-l_3-k_c13_717.jpg";
    private String imageLinkC15 = "https://www.campusgotland.uu.se/digitalAssets/706/c_706748-l_3-k_c15_717.jpg";
    private String imageLinkFlundran = "https://www.campusgotland.uu.se/digitalAssets/732/c_732876-l_3-k_flundran_400.jpg";
    private String imageLinkHeden = "https://www.campusgotland.uu.se/digitalAssets/732/c_732888-l_3-k_heden_400.jpg";
    private String imageLinkMyren = "https://www.campusgotland.uu.se/digitalAssets/733/c_733210-l_3-k_myren_400.jpg";
    private String imageLinkRauken = "https://www.campusgotland.uu.se/digitalAssets/732/c_732878-l_3-k_rauken_717x400.jpg";

    //these values are hard coded, but could be retrieved dynamically via an API-request
    private int roomSizeAnget = 10;
    private int roomSizeBacksippan = 4;
    private int roomSizeC11 = 5;
    private int roomSizeC13 = 5;
    private int roomSizeC15 = 4;
    private int roomSizeFlundran = 8;
    private int roomSizeHeden = 2;
    private int roomSizeMyren = 3;
    private int roomSizeRauken = 8;

    public String getLinkByRoomName(String name) {
        if (name.toLowerCase().equals("änget")) {
            return imageLinkAnget;
        } else if (name.toLowerCase().equals("backsippan")) {
            return imageLinkBacksippan;
        } else if (name.toLowerCase().equals("c11")) {
            return imageLinkC11;
        } else if (name.toLowerCase().equals("c13")) {
            return imageLinkC13;
        } else if (name.toLowerCase().equals("c15")) {
            return imageLinkC15;
        } else if (name.toLowerCase().equals("flundran")) {
            return imageLinkFlundran;
        } else if (name.toLowerCase().equals("heden")) {
            return imageLinkHeden;
        } else if (name.toLowerCase().equals("myren")) {
            return imageLinkMyren;
        } else if (name.toLowerCase().equals("rauken")) {
            return imageLinkRauken;
        }
        return name;
    }

    public String getInfoByRoomName(String name) {
        //this string is used for all rooms and contains the same information as the info at https://www.campusgotland.uu.se/
        String swedishTemplate = " är ett av rummen du även kan boka som student för t.ex. grupparbete.";
        String englishTemplate = " can be booked by students for group work.";

        String language = ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0).toString();

if (language.equals("sv_SE")){
        if (name.toLowerCase().equals("änget")) {
            return name + " har " + roomSizeAnget + " platser. " + name + swedishTemplate;
        } else if (name.toLowerCase().equals("backsippan")) {
            return name + " har " + roomSizeBacksippan + " platser. " + name + swedishTemplate;
        } else if (name.toLowerCase().equals("c11")) {
            return name + " har " + roomSizeC11 + " platser. " + name + swedishTemplate;
        } else if (name.toLowerCase().equals("c13")) {
            return name + " har " + roomSizeC13 + " platser. " + name + swedishTemplate;
        } else if (name.toLowerCase().equals("c15")) {
            return name + " har " + roomSizeC15 + " platser. " + name + swedishTemplate;
        } else if (name.toLowerCase().equals("flundran")) {
            return name + " har " + roomSizeFlundran + " platser. " + name + swedishTemplate;
        } else if (name.toLowerCase().equals("heden")) {
            return name + " har " + roomSizeHeden + " platser. " + name + swedishTemplate;
        } else if (name.toLowerCase().equals("myren")) {
            return name + " har " + roomSizeMyren + " platser. " + name + swedishTemplate;
        } else if (name.toLowerCase().equals("rauken")) {
            return name + " har " + roomSizeRauken + " platser. " + name + swedishTemplate;
        }
        return name;
    }
else

if (name.toLowerCase().equals("änget")) {
    return name + " has " + roomSizeAnget + " seats. " + name + englishTemplate;
} else if (name.toLowerCase().equals("backsippan")) {
    return name + " has " + roomSizeBacksippan + " seats. " + name + englishTemplate;
} else if (name.toLowerCase().equals("c11")) {
    return name + " has " + roomSizeC11 + " seats. " + name + englishTemplate;
} else if (name.toLowerCase().equals("c13")) {
    return name + " has " + roomSizeC13 + " seats. " + name + englishTemplate;
} else if (name.toLowerCase().equals("c15")) {
    return name + " has " + roomSizeC15 + " seats. " + name + englishTemplate;
} else if (name.toLowerCase().equals("flundran")) {
    return name + " has " + roomSizeFlundran + " seats. " + name + englishTemplate;
} else if (name.toLowerCase().equals("heden")) {
    return name + " has " + roomSizeHeden + " seats. " + name + englishTemplate;
} else if (name.toLowerCase().equals("myren")) {
    return name + " has " + roomSizeMyren + " seats " + name + englishTemplate;
} else if (name.toLowerCase().equals("rauken")) {
    return name + " has " + roomSizeRauken + " seats. " + name + englishTemplate;
}
        return name;
    }

}
