public class Banksetting implements Comparable<Banksetting>{

    private final String name;
    private final int skipLines;
    private final String delimiter;
    private final boolean replaceQuotes;
    private final String dateFormat;
    private final String charSet;
    private final int[] inputLines;

    public Banksetting(String name, String[] input) {
        this.name = name;
        this.skipLines = Integer.parseInt(input[0]);
        this.delimiter = input[1];
        this.replaceQuotes = Boolean.parseBoolean(input[2]);
        this.dateFormat = input[4];
        this.charSet = input[9];
        this.inputLines = new int[] {
                Integer.parseInt(input[3]),
                Integer.parseInt(input[5]),
                Integer.parseInt(input[6]),
                Integer.parseInt(input[7]),
                Integer.parseInt(input[8])};
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(Banksetting banksetting) {
        return this.name.compareTo(banksetting.getName());
    }

    public String getName() {
        return name;
    }

    public int getSkipLines() {
        return skipLines;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public boolean isReplaceQuotes() {
        return replaceQuotes;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public String getCharSet() { return charSet; }

    public int[] getInputLines() {
        return inputLines;
    }

    public String getSettingsString() {
        return name +
                "=" +
                skipLines +
                "," +
                delimiter +
                "," +
                replaceQuotes +
                "," +
                inputLines[0] +
                "," +
                dateFormat +
                "," +
                inputLines[1] +
                "," +
                inputLines[2] +
                "," +
                inputLines[3] +
                "," +
                inputLines[4] +
                "," +
                charSet;
    }
}
