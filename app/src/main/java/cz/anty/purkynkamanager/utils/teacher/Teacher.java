package cz.anty.purkynkamanager.utils.teacher;

import android.content.Context;

import java.util.Locale;

import cz.anty.purkynkamanager.utils.list.listView.MultilineItem;

/**
 * Created by anty on 22.6.15.
 *
 * @author anty
 */
public class Teacher implements MultilineItem {

    private static final String tab00c0 = "AAAAAAACEEEEIIII" +
            "DNOOOOO\u00d7\u00d8UUUUYI\u00df" +
            "aaaaaaaceeeeiiii" +
            "\u00f0nooooo\u00f7\u00f8uuuuy\u00fey" +
            "AaAaAaCcCcCcCcDd" +
            "DdEeEeEeEeEeGgGg" +
            "GgGgHhHhIiIiIiIi" +
            "IiJjJjKkkLlLlLlL" +
            "lLlNnNnNnnNnOoOo" +
            "OoOoRrRrRrSsSsSs" +
            "SsTtTtTtUuUuUuUu" +
            "UuUuWwYyYZzZzZzF";
    private final String name, shortcut, phoneNumber, email;

    Teacher(String name, String shortcut, String phoneNumber, String email) {
        this.name = name;
        this.shortcut = removeDiacritic(shortcut.toLowerCase());
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    Teacher(String name, String phoneNumber, String email) {
        this.name = name;
        String[] nameS = this.name.split(" ");
        this.shortcut = removeDiacritic((nameS[nameS.length - 1].substring(0, 2)
                + nameS[nameS.length - 2].substring(0, 2)).toLowerCase());
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    private static String removeDiacritic(String source) {
        char[] result = new char[source.length()];
        char one;
        for (int i = 0; i < source.length(); i++) {
            one = source.charAt(i);
            if (one >= '\u00c0' && one <= '\u017f') {
                one = tab00c0.charAt((int) one - '\u00c0');
            }
            result[i] = one;
        }
        return new String(result);
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        String[] name = this.name.split(" ");
        return name[name.length - 1];
    }

    public String getShortcut() {
        return shortcut;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return getName() + " " + getShortcut().toUpperCase(Locale.getDefault());
    }

    @Override
    public CharSequence getTitle(Context context, int position) {
        return toString();
    }

    @Override
    public CharSequence getText(Context context, int position) {
        return getPhoneNumber() + " " + getEmail();
    }
}
