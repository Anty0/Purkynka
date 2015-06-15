package cz.anty.utils.timetable;

/**
 * Created by anty on 13.6.15.
 *
 * @author anty
 */
public class Lesson {

    private static final String PARSE_CHARS = ":;|L|;:";
    private final String name, shortName, classString, teacher;

    public Lesson(String name, String shortName, String classString, String teacher) {
        this.name = name == null ? "" : name;
        this.shortName = shortName == null ? "" : shortName;
        this.classString = classString == null ? "" : classString;
        this.teacher = teacher == null ? "" : teacher;
    }

    public static Lesson parse(String toParse) {
        if ("".equals(toParse) || "null".equals(toParse)) return null;
        Builder builder = new Builder();
        String[] toParseData = toParse.split(PARSE_CHARS);

        for (int i = 0; i < toParseData.length; i++) {
            String toParseInfo = toParseData[i];

            switch (i) {
                case 0:
                    builder.setName(toParseInfo);
                    break;
                case 1:
                    builder.setShortName(toParseInfo);
                    break;
                case 2:
                    builder.setClassString(toParseInfo);
                    break;
                case 3:
                    builder.setTeacher(toParseInfo);
                    break;
            }
        }

        return builder.get();
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public String getClassString() {
        return classString;
    }

    public String getTeacher() {
        return teacher;
    }

    @Override
    public String toString() {
        return getName() + PARSE_CHARS
                + getShortName() + PARSE_CHARS
                + getClassString() + PARSE_CHARS
                + getTeacher();
    }

    public static class Builder {

        private String name, shortName, classString, teacher;

        public Builder(String name, String shortName, String classString, String teacher) {
            this.name = name;
            this.shortName = shortName;
            this.classString = classString;
            this.teacher = teacher;
        }

        public Builder() {

        }

        public synchronized Builder setName(String name) {
            this.name = name;
            return this;
        }

        public synchronized Builder setShortName(String shortName) {
            this.shortName = shortName;
            return this;
        }

        public synchronized Builder setClassString(String classString) {
            this.classString = classString;
            return this;
        }

        public synchronized Builder setTeacher(String teacher) {
            this.teacher = teacher;
            return this;
        }

        public synchronized Lesson get() {
            return new Lesson(name, shortName, classString, teacher);
        }
    }
}
