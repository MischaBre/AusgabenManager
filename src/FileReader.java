import jdk.jfr.Category;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FileReader {

    private final TreeMap<String, Double> categories;
    private final TreeSet<Banksetting> banksettings;
    private final static String KEY = "lisaIstSuess0529";

    public FileReader() {

        categories = new TreeMap<>();
        banksettings = new TreeSet<>();

    }

    public String[] LoadFileIO(String filename, boolean encrypted, String charset) {
        try (InputStream inputStream = new FileInputStream(filename)) {
            byte[] charArray = inputStream.readAllBytes();
            String[] string;
            if (encrypted) {
                string = decryptAES(charArray, charset).split("\n");
            } else {
                string = new String(charArray, charset).split("\n");
            }
            return string;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String[1];
    }

    public void LoadFromCfg(String filename) {

        int type = 0;                                   //int type for switching between add to categories/types
        for (String line : LoadFileIO(filename, false, "UTF-8")) {
            switch (line) {
                case "[categories]":
                    type = 1;
                    break;
                case "[banksettings]":
                    type = 2;
                    break;
                case "":
                    type = 0;
                    break;
                default:
                    switch (type) {
                        case 1 -> categories.put(line, 0.0);
                        case 2 -> banksettings.add(ParseBanksettings(line));
                        default -> {
                        }
                    }
                    break;
            }
        }
    }

    public void SaveCfg(String filename) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("[categories]\n");
            categories.keySet().forEach(v -> sb.append(v).append("\n"));
            sb.append("\n");
            banksettings.forEach(b -> sb.append(b.getSettingsString()).append("\n"));

            FileOutputStream fos = new FileOutputStream(filename);
            fos.write(sb.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Expense> ImportExpensesFromCSV(String filename, Banksetting banksetting) {
        List<Expense> expenses = new ArrayList<>();
        int[] banksettingInputLines = banksetting.getInputLines();
        String[] string = LoadFileIO(filename, false, banksetting.getCharSet());
        String[] data;
        for (int i = banksetting.getSkipLines(); i < string.length; i++) {
            if (banksetting.isReplaceQuotes()) {
                string[i] = string[i].replaceAll("\"", "");
            }
            data = string[i].split(banksetting.getDelimiter());
            expenses.add(new Expense(
                    LocalDate.parse(data[banksettingInputLines[0]].trim(), DateTimeFormatter.ofPattern(banksetting.getDateFormat())),
                    banksettingInputLines[1] > 0 ? data[banksettingInputLines[1]].trim() : "",
                    banksettingInputLines[2] > 0 ? data[banksettingInputLines[2]].trim() : "",
                    banksettingInputLines[3] > 0 ? data[banksettingInputLines[3]].trim().replace("/\s{2,}/", "") : "",
                    -1 * Double.parseDouble(data[banksettingInputLines[4]].trim().replace(".", "").replace(',', '.')),
                    ""
            ));
        }
        return expenses;
    }

    public List<Expense> LoadExpensesFromFileIO(String filename) {
        List<Expense> expenses = new ArrayList<>();
        for (String s : LoadFileIO(filename, true, "UTF-8")) {
            expenses.add(StringToExpense(s));
        }
        return expenses;
    }

    public void SaveExpensesToEMFIO(String filename, List<Expense> expenses) throws FileNotFoundException {
        try {
            FileOutputStream fos = new FileOutputStream(filename);
            StringBuilder sb = new StringBuilder();
            expenses.stream()
                    .map(e -> (e.GetCSVExpenseString(";") + "\n"))
                    .forEach(sb::append);
            fos.write(encryptAES(sb.toString(), "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Expense StringToExpense(String string) {
        String[] data = string.split(";");
        return new Expense(
                LocalDate.parse(data[0].trim(), DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                data[1].trim(),
                data[2].trim(),
                data[3].trim(),
                Double.parseDouble(data[4].trim()),
                data.length > 5 ? data[5].trim() : ""
        );
    }

    private Banksetting ParseBanksettings(String input) {
        String[] i = input.split("=");
        return new Banksetting(i[0],i[1].split(","));
    }

    public byte[] encryptAES(String input, String charset) {
        try {
            Key aesKey = new SecretKeySpec(KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            return cipher.doFinal(input.getBytes(charset));
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return new byte[1];
    }

    public String decryptAES(byte[] input, String charset) {
        try {
            Key aesKey = new SecretKeySpec(KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            return new String(cipher.doFinal(input), charset);
        } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    public TreeMap<String, Double> getCategories() {
        return categories;
    }

    public TreeSet<Banksetting> getBanks() {
        return banksettings;
    }
}
