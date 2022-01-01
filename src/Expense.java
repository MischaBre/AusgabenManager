//Expenses Class, Stand 01.12.2021

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;

public class Expense implements Comparable<Expense> {

    private final String hash;
    private LocalDate date;
    private String consignor;
    private String consignorNumber;
    private String detail;
    private double amount;
    private String category;

    @Override
    public int compareTo(Expense expense) {
        return (int)(this.date.toEpochDay() - expense.date.toEpochDay());
    }

    @Override
    public String toString() {
        return date + " " + consignor + " " + amount;
    }

    public Expense(LocalDate date, String consignor, String consignorNumber, String detail, double amount) {
        this.date = date;
        this.consignor = consignor;
        this.consignorNumber = consignorNumber;
        this.detail = detail;
        this.amount = amount;
        this.category = "";
        this.hash = HashThis(GetExpenseString());
    }

    private String HashThis(String data) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        assert md != null;
        md.update(StandardCharsets.UTF_8.encode(data));
        return String.format("%032x", new BigInteger(1,md.digest()));
    }

    public void PrintExpense() {
        System.out.printf(hash + " " + date + " " + consignor + " " + consignorNumber + " " + detail +" " + amount + " " + "%n");
    }

    //Getter

    public String GetNiceExpenseString() { return date + " " + consignor + " " + consignorNumber + " " + detail +" " + amount + " "; }

    public String GetExpenseString() { return date + consignor  + consignorNumber + detail + amount; }

    public String getHash() { return hash; }

    public LocalDate getDate() {
        return date;
    }

    public String getConsignor() {
        return consignor;
    }

    public String getConsignorNumber() {
        return consignorNumber;
    }

    public String getDetail() {
        return detail;
    }

    public double getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    //Setter
    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setConsignor(String consignor) {
        this.consignor = consignor;
    }

    public void setConsignorNumber(String consignorNumber) {
        this.consignorNumber = consignorNumber;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
