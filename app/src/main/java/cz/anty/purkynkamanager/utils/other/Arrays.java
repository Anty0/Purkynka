package cz.anty.purkynkamanager.utils.other;

/**
 * Created by anty on 15.6.15.
 *
 * @author anty
 */
public class Arrays {

    public static String[] add(String[] strings, String s) {
        String[] newStrings = new String[strings.length + 1];
        System.arraycopy(strings, 0, newStrings, 0, strings.length);
        newStrings[strings.length] = s;
        return newStrings;
    }

    public static String[] remove(String[] strings, String s) {
        int index = indexOf(strings, s);
        if (index == -1) return strings;
        String[] newStrings = new String[strings.length - 1];
        for (int i = 0; i < strings.length; i++) {
            if (i == index) continue;
            if (i > index) newStrings[i - 1] = strings[i];
            else newStrings[i] = strings[i];
        }
        return newStrings;
    }

    public static boolean contains(Object[] objects, Object o) {
        if (o == null) {
            for (Object obj : objects) {
                if (obj == null) return true;
            }
            return false;
        }

        for (Object obj : objects) {
            if (o.equals(obj)) return true;
        }
        return false;
    }

    public static int indexOf(Object[] objects, Object o) {
        if (o == null) {
            for (int i = 0; i < objects.length; i++) {
                if (objects[i] == null) return i;
            }
            return -1;
        }

        for (int i = 0; i < objects.length; i++) {
            if (o.equals(objects[i])) return i;
        }
        return -1;
    }
}
