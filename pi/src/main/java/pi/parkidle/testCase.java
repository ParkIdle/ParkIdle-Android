package pi.parkidle;

import android.location.Location;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by misui on 20/03/2018.
 */

public class testCase {
    private static int index;
    private static List<String> test;
    private static List<Integer> accuracy;
    private static List<Location> locations;
    private Location loc;

    public testCase(int mode) {
        test = new ArrayList<String>();
        accuracy = new ArrayList<Integer>();
        locations = new ArrayList<Location>();
        index = 0;

        switch (mode) {
            case 0:
                Log.w("Test0","Sono partito bro");
                //deve restituire 1 sei partito 0 sei arrivato
                test.add("ON FOOT");
                test.add("STILL");
                test.add("ON FOOT");
                test.add("IN VEHICLE");
                test.add("IN VEHICLE");

                accuracy.add(80);
                accuracy.add(50);
                accuracy.add(90);
                accuracy.add(80);
                accuracy.add(100);

                loc = new Location("Fake Location number 0");
                loc.setLatitude(41.91485540000001);
                loc.setLongitude(12.4117458);
                locations.add(loc);

                loc = new Location("Fake Location number 1");
                loc.setLatitude(41.91485540000001);
                loc.setLongitude(12.4117458);
                locations.add(loc);

                loc = new Location("Fake Location number 2");
                loc.setLatitude(41.91485540000001);
                loc.setLongitude(12.4117458);
                locations.add(loc);

                loc = new Location("Fake Location number 3");
                loc.setLatitude(41.89379036975438);
                loc.setLongitude(12.492442428766594);
                locations.add(loc);

                loc = new Location("Fake Location number 4");
                loc.setLatitude(41.89408187170548);
                loc.setLongitude(12.493151873051033);
                locations.add(loc);
                Log.w("Test0","ho finito bro");



            case 1:
                Log.w("Test1","Sono partito bro");
                //deve restituire 0 sei partito 0 sei arrivato
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");


                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);


                loc = new Location("Fake Location number 0");
                loc.setLatitude(41.9660932);
                loc.setLongitude(12.4555325);
                locations.add(loc);

                loc = new Location("Fake Location number 1");
                loc.setLatitude(41.9660929);
                loc.setLongitude(12.4555283);
                locations.add(loc);

                loc = new Location("Fake Location number 2");
                loc.setLatitude(41.9660934);
                loc.setLongitude(12.4555297);
                locations.add(loc);

                loc = new Location("Fake Location number 3");
                loc.setLatitude(41.9661012);
                loc.setLongitude(12.4555674);
                locations.add(loc);
                Log.w("Test1","Sono arrivato bro");
            case 2:
                Log.w("Test2","Sono partito bro");
                //deve restituire 0 sei partito 0 sei arrivato
                test.add("STILL");
                test.add("ON FOOT");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");

                accuracy.add(100);
                accuracy.add(100);
                accuracy.add(35);
                accuracy.add(99);
                accuracy.add(91);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);

                loc = new Location("Fake Location number 0");
                loc.setLatitude(41.9660434914191);
                loc.setLongitude(12.455212140784383);
                locations.add(loc);

                loc = new Location("Fake Location number 1");
                loc.setLatitude(41.965909095713506);
                loc.setLongitude(12.455684886164642);
                locations.add(loc);

                loc = new Location("Fake Location number 2");
                loc.setLatitude(41.96627517305908);
                loc.setLongitude(12.455529195393382);
                locations.add(loc);

                loc = new Location("Fake Location number 3");
                loc.setLatitude(41.9661054);
                loc.setLongitude(12.455734);
                locations.add(loc);

                loc = new Location("Fake Location number 4");
                loc.setLatitude(41.9660955);
                loc.setLongitude(12.4555193);
                locations.add(loc);

                loc = new Location("Fake Location number 5");
                loc.setLatitude(41.9660955);
                loc.setLongitude(12.4555193);
                locations.add(loc);

                loc = new Location("Fake Location number 6");
                loc.setLatitude(41.9660955);
                loc.setLongitude(12.4555193);
                locations.add(loc);

                loc = new Location("Fake Location number 7");
                loc.setLatitude(41.9660955);
                loc.setLongitude(12.4555193);
                locations.add(loc);

                loc = new Location("Fake Location number 8");
                loc.setLatitude(41.9660955);
                loc.setLongitude(12.4555193);
                locations.add(loc);

                loc = new Location("Fake Location number 9");
                loc.setLatitude(41.9660955);
                loc.setLongitude(12.4555193);
                locations.add(loc);

                loc = new Location("Fake Location number 10");
                loc.setLatitude(41.9660955);
                loc.setLongitude(12.4555193);
                locations.add(loc);

                loc = new Location("Fake Location number 11");
                loc.setLatitude(41.9660955);
                loc.setLongitude(12.4555193);
                locations.add(loc);
                Log.w("Test2","Sono arrivato bro");
            case 3:
                Log.w("Test3","Sono partito bro");
                //deve restituire 0 sei partito 0 sei arrivato
                test.add("STILL");
                test.add("ON FOOT");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");
                test.add("STILL");

                accuracy.add(100);
                accuracy.add(100);
                accuracy.add(35);
                accuracy.add(99);
                accuracy.add(91);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(61);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(99);
                accuracy.add(100);

                loc = new Location("Fake location number 0");
                loc.setLatitude(41.9660434914191);
                loc.setLongitude(12.455212140784383);
                locations.add(loc);

                loc = new Location("Fake location number 1");
                loc.setLatitude(41.965909095713506);
                loc.setLongitude(12.455684886164642);
                locations.add(loc);

                loc = new Location("Fake location number 2");
                loc.setLatitude(41.96627517305908);
                loc.setLongitude(12.455529195393382);
                locations.add(loc);

                loc = new Location("Fake location number 3");
                loc.setLatitude(41.9661054);
                loc.setLongitude(12.455734);
                locations.add(loc);

                loc = new Location("Fake location number 4");
                loc.setLatitude(41.9660955);
                loc.setLongitude(12.4555193);
                locations.add(loc);

                loc = new Location("Fake location number 5");
                loc.setLatitude(41.9660955);
                loc.setLongitude(12.4555193);
                locations.add(loc);

                loc = new Location("Fake location number 6");
                loc.setLatitude(41.9660955);
                loc.setLongitude(12.4555193);
                locations.add(loc);

                loc = new Location("Fake location number 7");
                loc.setLatitude(41.9660955);
                loc.setLongitude(12.4555193);
                locations.add(loc);

                loc = new Location("Fake location number 8");
                loc.setLatitude(41.9660955);
                loc.setLongitude(12.4555193);
                locations.add(loc);

                loc = new Location("Fake location number 9");
                loc.setLatitude(41.9660955);
                loc.setLongitude(12.4555193);
                locations.add(loc);

                loc = new Location("Fake location number 10");
                loc.setLatitude(41.9660955);
                loc.setLongitude(12.4555193);
                locations.add(loc);

                loc = new Location("Fake location number 11");
                loc.setLatitude(41.9660955);
                loc.setLongitude(12.4555193);
                locations.add(loc);

                loc = new Location("Fake location number 12");
                loc.setLatitude(41.9660794);
                loc.setLongitude(12.4554996);
                locations.add(loc);

                loc = new Location("Fake location number 13");
                loc.setLatitude(41.9660794);
                loc.setLongitude(12.4554996);
                locations.add(loc);

                loc = new Location("Fake location number 14");
                loc.setLatitude(41.9660794);
                loc.setLongitude(12.4554996);
                locations.add(loc);

                loc = new Location("Fake location number 15");
                loc.setLatitude(41.9660794);
                loc.setLongitude(12.4554996);
                locations.add(loc);

                loc = new Location("Fake location number 16");
                loc.setLatitude(41.9660794);
                loc.setLongitude(12.4554996);
                locations.add(loc);

                loc = new Location("Fake location number 17");
                loc.setLatitude(41.9660794);
                loc.setLongitude(12.4554996);
                locations.add(loc);

                loc = new Location("Fake location number 18");
                loc.setLatitude(41.9660794);
                loc.setLongitude(12.4554996);
                locations.add(loc);

                loc = new Location("Fake location number 19");
                loc.setLatitude(41.9660794);
                loc.setLongitude(12.4554996);
                locations.add(loc);

                loc = new Location("Fake location number 20");
                loc.setLatitude(41.9660794);
                loc.setLongitude(12.4554996);
                locations.add(loc);
                Log.w("Test3","Sono arrivato bro");
            case 4:
                Log.w("Test4","Sono partito bro");
                //deve restituire 1 sei partito 0 sei arrivato
                test.add("STILL");
                test.add("STILL");
                test.add("ON FOOT");
                test.add("STILL");
                test.add("IN VEHICLE");
                test.add("IN VEHICLE");
                test.add("IN VEHICLE");
                test.add("IN VEHICLE");
                test.add("IN VEHICLE");
                test.add("IN VEHICLE");
                test.add("IN VEHICLE");
                test.add("IN VEHICLE");
                test.add("IN VEHICLE");
                test.add("IN VEHICLE");
                test.add("IN VEHICLE");
                test.add("IN VEHICLE");
                test.add("IN VEHICLE");
                test.add("IN VEHICLE");
                test.add("IN VEHICLE");
                test.add("IN VEHICLE");
                test.add("IN VEHICLE");
                test.add("IN VEHICLE");
                test.add("IN VEHICLE");
                test.add("IN VEHICLE");
                test.add("IN VEHICLE");
                test.add("IN VEHICLE");
                test.add("IN VEHICLE");
                test.add("IN VEHICLE");
                test.add("IN VEHICLE");
                test.add("IN VEHICLE");
                test.add("IN VEHICLE");
                test.add("IN VEHICLE");
                test.add("IN VEHICLE");
                test.add("IN VEHICLE");
                test.add("IN VEHICLE");
                test.add("IN VEHICLE");
                test.add("IN VEHICLE");
                test.add("IN VEHICLE");
                test.add("STILL");

                accuracy.add(100);
                accuracy.add(56);
                accuracy.add(99);
                accuracy.add(70);
                accuracy.add(44);
                accuracy.add(32);
                accuracy.add(84);
                accuracy.add(90);
                accuracy.add(92);
                accuracy.add(78);
                accuracy.add(49);
                accuracy.add(48);
                accuracy.add(98);
                accuracy.add(82);
                accuracy.add(70);
                accuracy.add(83);
                accuracy.add(44);
                accuracy.add(43);
                accuracy.add(83);
                accuracy.add(50);
                accuracy.add(31);
                accuracy.add(77);
                accuracy.add(52);
                accuracy.add(89);
                accuracy.add(91);
                accuracy.add(88);
                accuracy.add(81);
                accuracy.add(83);
                accuracy.add(94);
                accuracy.add(96);
                accuracy.add(54);
                accuracy.add(93);
                accuracy.add(96);
                accuracy.add(89);
                accuracy.add(89);
                accuracy.add(64);
                accuracy.add(86);
                accuracy.add(43);
                accuracy.add(34);
                accuracy.add(40);


                loc = new Location("Fake location number 1");
                loc.setLatitude(41.9660808);
                loc.setLongitude(12.4555098);
                locations.add(loc);

                loc = new Location("Fake location number 2");
                loc.setLatitude(41.9660808);
                loc.setLongitude(12.4555098);
                locations.add(loc);

                loc = new Location("Fake location number 3");
                loc.setLatitude(41.96631944486096);
                loc.setLongitude(12.45551437049076);
                locations.add(loc);

                loc = new Location("Fake location number 4");
                loc.setLatitude(41.96631944486096);
                loc.setLongitude(12.45551437049076);
                locations.add(loc);

                loc = new Location("Fake location number 5");
                loc.setLatitude(41.96440694546501);
                loc.setLongitude(12.457364264134602);
                locations.add(loc);

                loc = new Location("Fake location number 6");
                loc.setLatitude(41.965574936165574);
                loc.setLongitude(12.459945731632944);
                locations.add(loc);

                loc = new Location("Fake location number 7");
                loc.setLatitude(41.967597396541535);
                loc.setLongitude(12.461287844500516);
                locations.add(loc);

                loc = new Location("Fake location number 8");
                loc.setLatitude(41.97049440518721);
                loc.setLongitude(12.461844908643732);
                locations.add(loc);

                loc = new Location("Fake location number 9");
                loc.setLatitude(41.973115400759596);
                loc.setLongitude(12.460026182716561);
                locations.add(loc);



                loc = new Location("Fake location number 10");
                loc.setLatitude(41.9746909051618);
                loc.setLongitude(12.461072995222693);
                locations.add(loc);



                loc = new Location("Fake location number 11");
                loc.setLatitude(41.97503920961754);
                loc.setLongitude(12.463381734771092);
                locations.add(loc);



                loc = new Location("Fake location number 12");
                loc.setLatitude(41.97634761463633);
                loc.setLongitude(12.463059398236478);
                locations.add(loc);



                loc = new Location("Fake location number 13");
                loc.setLatitude(41.98137484749118);
                loc.setLongitude(12.471554517389851);
                locations.add(loc);



                loc = new Location("Fake location number 14");
                loc.setLatitude(41.98105856646042);
                loc.setLongitude(12.474442104091287);
                locations.add(loc);



                loc = new Location("Fake location number 15");
                loc.setLatitude(41.98067726616036);
                loc.setLongitude(12.476200853344002);
                locations.add(loc);



                loc = new Location("Fake location number 16");
                loc.setLatitude(41.98014354043498);
                loc.setLongitude(12.480820592396748);
                locations.add(loc);



                loc = new Location("Fake location number 18");
                loc.setLatitude(41.98059444066205);
                loc.setLongitude(12.484879414908217);
                locations.add(loc);



                loc = new Location("Fake location number 19");
                loc.setLatitude(41.97991699501278);
                loc.setLongitude(12.486625129887054);
                locations.add(loc);



                loc = new Location("Fake location number 20");
                loc.setLatitude(41.97673425868042);
                loc.setLongitude(12.489443187438969);
                locations.add(loc);

                loc = new Location("Fake location number 21");
                loc.setLatitude(41.97623787171018);
                loc.setLongitude(12.492814681370204);
                locations.add(loc);

                loc = new Location("Fake location number 22");
                loc.setLatitude(41.976093582376166);
                loc.setLongitude(12.493368943681471);
                locations.add(loc);

                loc = new Location("Fake location number 23");
                loc.setLatitude(41.97934376303893);
                loc.setLongitude(12.493662203568974);
                locations.add(loc);

                loc = new Location("Fake location number 24");
                loc.setLatitude(41.98096316856624);
                loc.setLongitude(12.493325609930848);
                locations.add(loc);

                loc = new Location("Fake location number 25");
                loc.setLatitude(41.984114561445985);
                loc.setLongitude(12.493295447697141);
                locations.add(loc);

                loc = new Location("Fake location number 26");
                loc.setLatitude(41.98580186029188);
                loc.setLongitude(12.494182266655542);
                locations.add(loc);

                loc = new Location("Fake location number 27");
                loc.setLatitude(41.98760893856313);
                loc.setLongitude(12.497533346592842);
                locations.add(loc);

                loc = new Location("Fake location number 28");
                loc.setLatitude(41.99054022998917);
                loc.setLongitude(12.502183838676572);
                locations.add(loc);

                loc = new Location("Fake location number 29");
                loc.setLatitude(41.99054752643827);
                loc.setLongitude(12.508666814739268);
                locations.add(loc);

                loc = new Location("Fake location number 30");
                loc.setLatitude(41.989027026854615);
                loc.setLongitude(12.51234024031384);
                locations.add(loc);

                loc = new Location("Fake location number 31");
                loc.setLatitude(41.98584927561137);
                loc.setLongitude(12.519249587574084);
                locations.add(loc);

                loc = new Location("Fake location number 32");
                loc.setLatitude(41.98445902236781);
                loc.setLongitude(12.522345262785446);
                locations.add(loc);

                loc = new Location("Fake location number 33");
                loc.setLatitude(41.98188050935916);
                loc.setLongitude(12.527348172459387);
                locations.add(loc);

                loc = new Location("Fake location number 34");
                loc.setLatitude(41.97965910676764);
                loc.setLongitude(12.532693710562214);
                locations.add(loc);

                loc = new Location("Fake location number 35");
                loc.setLatitude(41.97681302479044);
                loc.setLongitude(12.537911089832646);
                locations.add(loc);

                loc = new Location("Fake location number 36");
                loc.setLatitude(41.96681502962176);
                loc.setLongitude(12.539343668126575);
                locations.add(loc);

                loc = new Location("Fake location number 37");
                loc.setLatitude(41.963609056740935);
                loc.setLongitude(12.535621576235487);
                locations.add(loc);

                loc = new Location("Fake location number 38");
                loc.setLatitude(41.96200169786676);
                loc.setLongitude(12.538668777103458);
                locations.add(loc);

                loc = new Location("Fake location number 39");
                loc.setLatitude(41.9602091920752);
                loc.setLongitude(12.539056036050088);
                locations.add(loc);

                loc = new Location("Fake location number 40");
                loc.setLatitude(41.960250461931395);
                loc.setLongitude(12.539051677145318);
                locations.add(loc);
                Log.w("Test4","Sono arrivato" +
                        " bro");

        }

    }




    public static String getTest(int index){
        return test.get(index);
    }

    public static int getAccuracy(int index){
        return accuracy.get(index);
    }

    public static Location getLocation(int index){
        return locations.get(index);
    }

    public static void incrementIndex(){
        index++;
        if (index > test.size()-1){
            index = 0;
        }
    }

    public static void setIndex(int x){
        index = x;
    }

    public static int getIndex(){
        return index;
    }

}
