import java.util.Arrays;

public class Banksetting implements Comparable<Banksetting>{

    private String name;
    private int skipLines;
    private String delimiter;
    private boolean replaceQuotes;
    private String dateFormat;
    private int[] inputLines;

    public Banksetting(String name, String[] input) {
        this.name = name;
        this.skipLines = Integer.parseInt(input[0]);
        this.delimiter = input[1];
        this.replaceQuotes = Boolean.parseBoolean(input[2]);
        this.dateFormat = input[4];
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

    public int[] getInputLines() {
        return inputLines;
    }
}
