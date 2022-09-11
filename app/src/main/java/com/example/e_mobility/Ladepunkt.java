package com.example.e_mobility;

public class Ladepunkt {
    private String Steckertype;
    private double kW;
    private String Key;

    public Ladepunkt(String Steckertype, double kW, String Key) {
        this.Steckertype = Steckertype;
        this.kW = kW;
        this.Key = Key;
    }

    public double getkW() {
        return this.kW;
    }

    public String toString() {
        return this.Steckertype + ", " + this.kW + "kW\n";
    }
}
