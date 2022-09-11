package com.example.e_mobility;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class Ladestation {
    private String Betreiber;

    private String Straße;
    private String Hausnummer;
    private String Adresszusatz;
    private int Postleitzahl;
    private String Ort;
    private String Bundesland;
    private String Kreis;

    private float Breitengrad;
    private float Längengrad;

    private String Inbetriebnahmedatum;
    private String Anschlussleistung;
    private String Ladeeinrichtung;
    private int Anzahl_Ladepunkte;

    private boolean defekt = false;

    private ArrayList<Ladepunkt> Ladepunkte;

    public Ladestation(String Betreiber, String Straße, String Hausnummer, String Adresszusatz,
            int Postleitzahl, String Ort, String Bundesland, String Kreis
            , float Breitengrad, float Längengrad, String Inbetriebnahmedatum,
                       String Anschlussleistung, String Ladeeinrichtung,
                       int Anzahl_Ladepunkte, ArrayList<Ladepunkt> Ladepunkte) {
        this.Betreiber = Betreiber;
        this.Straße = Straße;
        this.Hausnummer = Hausnummer;
        this.Adresszusatz = Adresszusatz;
        this.Postleitzahl = Postleitzahl;
        this.Ort = Ort;
        this.Bundesland = Bundesland;
        this.Kreis = Kreis;

        this.Breitengrad = Breitengrad;
        this.Längengrad = Längengrad;
        this.Inbetriebnahmedatum = Inbetriebnahmedatum;
        this.Anschlussleistung = Anschlussleistung;
        this.Ladeeinrichtung = Ladeeinrichtung;
        this.Anzahl_Ladepunkte = Anzahl_Ladepunkte;
        this.Ladepunkte = Ladepunkte;
    }

    public boolean isDefekt() {
        return defekt;
    }

    public void setDefekt(boolean defekt) {
        this.defekt = defekt;
    }

    public String info() {
        String infos = "Betrieber: " + this.Betreiber + "\n" +
                "Adresse: " + this.Straße + " " + this.Hausnummer + ", " + this.Postleitzahl
                            + " " + this.Ort + ", " + this.Bundesland + "\n" +
                "Lat: " + this.Breitengrad + ", Lon: " + this.Längengrad + "\n" +
                "Inbetriebnahmedatum: " + this.Inbetriebnahmedatum + "\n" +
                "Anschlussleitung: " + this.Anschlussleistung + "\n" +
                "Ladeeinrichtung: " + this.Ladeeinrichtung + "\n\n" +
                "Infos: \n";

        for(Ladepunkt ladepunkt : Ladepunkte) {
            infos += ladepunkt.toString();
        }

        infos += "Status: ";
        if(this.defekt){
            infos += "defekt\n";
        }
        else {
            infos += "lauffähig\n";
        }
        return infos;
    }

    public String getBetreiber() {
        return this.Betreiber;
    }

    public void setBetreiber(String Betreiber) {
        this.Betreiber = Betreiber;
    }

    public String getStraße() {
        return Straße;
    }

    public void setStraße(String straße) {
        Straße = straße;
    }

    public String getAdresszusatz() {
        return Adresszusatz;
    }

    public void setAdresszusatz(String adresszusatz) {
        Adresszusatz = adresszusatz;
    }

    public int getPostleitzahl() {
        return Postleitzahl;
    }

    public void setPostleitzahl(int postleitzahl) {
        Postleitzahl = postleitzahl;
    }

    public String getOrt() {
        return Ort;
    }

    public void setOrt(String ort) {
        Ort = ort;
    }

    public String getBundesland() {
        return Bundesland;
    }

    public void setBundesland(String bundesland) {
        Bundesland = bundesland;
    }

    public String getKreis() {
        return Kreis;
    }

    public void setKreis(String kreis) {
        Kreis = kreis;
    }

    public float getBreitengrad() {
        return Breitengrad;
    }

    public void setBreitengrad(float breitengrad) {
        Breitengrad = breitengrad;
    }

    public float getLängengrad() {
        return Längengrad;
    }

    public void setLängengrad(float längengrad) {
        Längengrad = längengrad;
    }

    public String getInbetriebnahmedatum() {
        return Inbetriebnahmedatum;
    }

    public void setInbetriebnahmedatum(String inbetriebnahmedatum) {
        Inbetriebnahmedatum = inbetriebnahmedatum;
    }

    public String getAnschlussleistung() {
        return Anschlussleistung;
    }

    public void setAnschlussleistung(String anschlussleistung) {
        Anschlussleistung = anschlussleistung;
    }

    public String getLadeeinrichtung() {
        return Ladeeinrichtung;
    }

    public void setLadeeinrichtung(String ladeeinrichtung) {
        Ladeeinrichtung = ladeeinrichtung;
    }

    public int getAnzahl_Ladepunkte() {
        return Anzahl_Ladepunkte;
    }

    public void setAnzahl_Ladepunkte(int anzahl_Ladepunkte) {
        Anzahl_Ladepunkte = anzahl_Ladepunkte;
    }

    public ArrayList<Ladepunkt> getLadepunkte() {
        return Ladepunkte;
    }

    public void setLadepunkte(ArrayList<Ladepunkt> ladepunkte) {
        Ladepunkte = ladepunkte;
    }
}
