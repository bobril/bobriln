package com.bobril.bobriln;

import android.graphics.Color;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class ColorUtils {
    static Map<String,Integer> namedColors = new HashMap<String,Integer>();

    static {
        namedColors.put("transparent",0x00000000);
        namedColors.put("aliceblue",0xfff0f8ff);
        namedColors.put("antiquewhite",0xfffaebd7);
        namedColors.put("aqua",0xff00ffff);
        namedColors.put("aquamarine",0xff7fffd4);
        namedColors.put("azure",0xfff0ffff);
        namedColors.put("beige",0xfff5f5dc);
        namedColors.put("bisque",0xffffe4c4);
        namedColors.put("black",0xff000000);
        namedColors.put("blanchedalmond",0xffffebcd);
        namedColors.put("blue",0xff0000ff);
        namedColors.put("blueviolet",0xff8a2be2);
        namedColors.put("brown",0xffa52a2a);
        namedColors.put("burlywood",0xffdeb887);
        namedColors.put("burntsienna",0xffea7e5d);
        namedColors.put("cadetblue",0xff5f9ea0);
        namedColors.put("chartreuse",0xff7fff00);
        namedColors.put("chocolate",0xffd2691e);
        namedColors.put("coral",0xffff7f50);
        namedColors.put("cornflowerblue",0xff6495ed);
        namedColors.put("cornsilk",0xfffff8dc);
        namedColors.put("crimson",0xffdc143c);
        namedColors.put("cyan",0xff00ffff);
        namedColors.put("darkblue",0xff00008b);
        namedColors.put("darkcyan",0xff008b8b);
        namedColors.put("darkgoldenrod",0xffb8860b);
        namedColors.put("darkgray",0xffa9a9a9);
        namedColors.put("darkgreen",0xff006400);
        namedColors.put("darkgrey",0xffa9a9a9);
        namedColors.put("darkkhaki",0xffbdb76b);
        namedColors.put("darkmagenta",0xff8b008b);
        namedColors.put("darkolivegreen",0xff556b2f);
        namedColors.put("darkorange",0xffff8c00);
        namedColors.put("darkorchid",0xff9932cc);
        namedColors.put("darkred",0xff8b0000);
        namedColors.put("darksalmon",0xffe9967a);
        namedColors.put("darkseagreen",0xff8fbc8f);
        namedColors.put("darkslateblue",0xff483d8b);
        namedColors.put("darkslategray",0xff2f4f4f);
        namedColors.put("darkslategrey",0xff2f4f4f);
        namedColors.put("darkturquoise",0xff00ced1);
        namedColors.put("darkviolet",0xff9400d3);
        namedColors.put("deeppink",0xffff1493);
        namedColors.put("deepskyblue",0xff00bfff);
        namedColors.put("dimgray",0xff696969);
        namedColors.put("dimgrey",0xff696969);
        namedColors.put("dodgerblue",0xff1e90ff);
        namedColors.put("firebrick",0xffb22222);
        namedColors.put("floralwhite",0xfffffaf0);
        namedColors.put("forestgreen",0xff228b22);
        namedColors.put("fuchsia",0xffff00ff);
        namedColors.put("gainsboro",0xffdcdcdc);
        namedColors.put("ghostwhite",0xfff8f8ff);
        namedColors.put("gold",0xffffd700);
        namedColors.put("goldenrod",0xffdaa520);
        namedColors.put("gray",0xff808080);
        namedColors.put("green",0xff008000);
        namedColors.put("greenyellow",0xffadff2f);
        namedColors.put("grey",0xff808080);
        namedColors.put("honeydew",0xfff0fff0);
        namedColors.put("hotpink",0xffff69b4);
        namedColors.put("indianred",0xffcd5c5c);
        namedColors.put("indigo",0xff4b0082);
        namedColors.put("ivory",0xfffffff0);
        namedColors.put("khaki",0xfff0e68c);
        namedColors.put("lavender",0xffe6e6fa);
        namedColors.put("lavenderblush",0xfffff0f5);
        namedColors.put("lawngreen",0xff7cfc00);
        namedColors.put("lemonchiffon",0xfffffacd);
        namedColors.put("lightblue",0xffadd8e6);
        namedColors.put("lightcoral",0xfff08080);
        namedColors.put("lightcyan",0xffe0ffff);
        namedColors.put("lightgoldenrodyellow",0xfffafad2);
        namedColors.put("lightgray",0xffd3d3d3);
        namedColors.put("lightgreen",0xff90ee90);
        namedColors.put("lightgrey",0xffd3d3d3);
        namedColors.put("lightpink",0xffffb6c1);
        namedColors.put("lightsalmon",0xffffa07a);
        namedColors.put("lightseagreen",0xff20b2aa);
        namedColors.put("lightskyblue",0xff87cefa);
        namedColors.put("lightslategray",0xff778899);
        namedColors.put("lightslategrey",0xff778899);
        namedColors.put("lightsteelblue",0xffb0c4de);
        namedColors.put("lightyellow",0xffffffe0);
        namedColors.put("lime",0xff00ff00);
        namedColors.put("limegreen",0xff32cd32);
        namedColors.put("linen",0xfffaf0e6);
        namedColors.put("magenta",0xffff00ff);
        namedColors.put("maroon",0xff800000);
        namedColors.put("mediumaquamarine",0xff66cdaa);
        namedColors.put("mediumblue",0xff0000cd);
        namedColors.put("mediumorchid",0xffba55d3);
        namedColors.put("mediumpurple",0xff9370db);
        namedColors.put("mediumseagreen",0xff3cb371);
        namedColors.put("mediumslateblue",0xff7b68ee);
        namedColors.put("mediumspringgreen",0xff00fa9a);
        namedColors.put("mediumturquoise",0xff48d1cc);
        namedColors.put("mediumvioletred",0xffc71585);
        namedColors.put("midnightblue",0xff191970);
        namedColors.put("mintcream",0xfff5fffa);
        namedColors.put("mistyrose",0xffffe4e1);
        namedColors.put("moccasin",0xffffe4b5);
        namedColors.put("navajowhite",0xffffdead);
        namedColors.put("navy",0xff000080);
        namedColors.put("oldlace",0xfffdf5e6);
        namedColors.put("olive",0xff808000);
        namedColors.put("olivedrab",0xff6b8e23);
        namedColors.put("orange",0xffffa500);
        namedColors.put("orangered",0xffff4500);
        namedColors.put("orchid",0xffda70d6);
        namedColors.put("palegoldenrod",0xffeee8aa);
        namedColors.put("palegreen",0xff98fb98);
        namedColors.put("paleturquoise",0xffafeeee);
        namedColors.put("palevioletred",0xffdb7093);
        namedColors.put("papayawhip",0xffffefd5);
        namedColors.put("peachpuff",0xffffdab9);
        namedColors.put("peru",0xffcd853f);
        namedColors.put("pink",0xffffc0cb);
        namedColors.put("plum",0xffdda0dd);
        namedColors.put("powderblue",0xffb0e0e6);
        namedColors.put("purple",0xff800080);
        namedColors.put("rebeccapurple",0xff663399);
        namedColors.put("red",0xffff0000);
        namedColors.put("rosybrown",0xffbc8f8f);
        namedColors.put("royalblue",0xff4169e1);
        namedColors.put("saddlebrown",0xff8b4513);
        namedColors.put("salmon",0xfffa8072);
        namedColors.put("sandybrown",0xfff4a460);
        namedColors.put("seagreen",0xff2e8b57);
        namedColors.put("seashell",0xfffff5ee);
        namedColors.put("sienna",0xffa0522d);
        namedColors.put("silver",0xffc0c0c0);
        namedColors.put("skyblue",0xff87ceeb);
        namedColors.put("slateblue",0xff6a5acd);
        namedColors.put("slategray",0xff708090);
        namedColors.put("slategrey",0xff708090);
        namedColors.put("snow",0xfffffafa);
        namedColors.put("springgreen",0xff00ff7f);
        namedColors.put("steelblue",0xff4682b4);
        namedColors.put("tan",0xffd2b48c);
        namedColors.put("teal",0xff008080);
        namedColors.put("thistle",0xffd8bfd8);
        namedColors.put("tomato",0xffff6347);
        namedColors.put("turquoise",0xff40e0d0);
        namedColors.put("violet",0xffee82ee);
        namedColors.put("wheat",0xfff5deb3);
        namedColors.put("white",0xffffffff);
        namedColors.put("whitesmoke",0xfff5f5f5);
        namedColors.put("yellow",0xffffff00);
        namedColors.put("yellowgreen",0xff9acd32);
    }

    static int toColor(Object value) {
        if (value==null) return Color.TRANSPARENT;
        if (value instanceof String) {
            String s = (String)value;
            // TODO support other formats of color
            if (s.length()==0) return Color.TRANSPARENT;
            if (s.charAt(0)=='#' && s.length()<=9) {
                int v = Integer.valueOf(s.substring(1),16);
                if (s.length()==4) {
                    return Color.rgb(17*(v>>8),17*((v>>4)&15),17*(v&15));
                }
                if (s.length()==7) {
                    return Color.rgb(v>>16,(v>>8)&255,v&255);
                }
            }
            Integer r = namedColors.get(s);
            if (r!=null) return r;
        }
        throw new RuntimeException("Cannot parse "+value+" as Color");
    }
}
