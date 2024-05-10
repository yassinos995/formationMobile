package com.example.proform.model;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class commande {
    private String uid;
    private String dateLimite;
    private String desc;
    private String destination;
    private String etat;
    private String idtransporter;

    // Constructor
    public commande() {
        // Default constructor required for calls to DataSnapshot.getValue(Commande.class)
    }

    public commande(String dateLimite, String desc, String destination) {
        this.dateLimite = dateLimite;
        this.desc = desc;
        this.destination = destination;
    }
    public String getDateLimite() {

        return dateLimite;
    }

    public void setDateLimite(String dateLimite) {

        this.dateLimite = dateLimite;
    }

    public String getDesc() {

        return desc;
    }

    public void setDesc(String desc) {

        this.desc = desc;
    }

    public String getDestination() {

        return destination;
    }

    public void setDestination(String destination) {

        this.destination = destination;
    }

    public String getEtat() {

        return etat;
    }

    public void setEtat(String etat) {

        this.etat = etat;
    }

    public String getIdtransporter() {

        return idtransporter;
    }

    public void setIdtransporter(String idtransporter) {

        this.idtransporter = idtransporter;
    }

    public String getUid() {
        return uid;

    }

    public void setUid(String uid) {
        this.uid=uid;
    }
}
