package com.porfirio.orariprocida2011.entity;

import android.content.Context;

import com.porfirio.orariprocida2011.R;

import java.util.Calendar;

public class Mezzo {
	//gestite nel dettaglio segnalazioni per tipologia e motivi
	public final String nave;
	public final Calendar oraPartenza;
	public final Calendar oraArrivo;
	public final String portoPartenza;
	public final String portoArrivo;
	public final Calendar inizioEsclusione;
	public final Calendar fineEsclusione;
	public final String giorniSettimana;
	private final int[] segnalazioni = new int[100];
	private final Context callingContext;
	public int conferme = 0;
	public int tot = 0;
	public boolean conc = true;
	private boolean giornoSeguente;
	private boolean esclusione;
	private int orderInList;
	private double costoIntero;
	private double costoResidente;
	private boolean circaIntero=false;
	private boolean circaResidente=false;
	private String[] ragioni=new String[100];

	public Mezzo(Context c, String n, int op, int mp, int oa, int ma, String pp, String pa, int gie, int mie, int aie, int gfe, int mfe, int afe, String gs) {
		callingContext=c;
		ragioni= callingContext.getResources().getStringArray(R.array.strRagioni);
		for (int i=0;i<100;i++)
			segnalazioni[i] = 0;
		nave = n;
		oraPartenza=Calendar.getInstance();
		oraPartenza.set(Calendar.HOUR_OF_DAY, op);
		oraPartenza.set(Calendar.MINUTE, mp);
		oraArrivo=Calendar.getInstance();
		oraArrivo.set(Calendar.HOUR_OF_DAY, oa);
		oraArrivo.set(Calendar.MINUTE, ma);
		portoPartenza=pp;
		portoArrivo=pa;
		inizioEsclusione=Calendar.getInstance();
		fineEsclusione=Calendar.getInstance();
		esclusione=false;
		if (gie!=0){
			esclusione=true;
			inizioEsclusione.set(Calendar.DAY_OF_MONTH, gie);
			inizioEsclusione.set(Calendar.MONTH, mie-1); //i mesi sono contati da 0=gennaio
			inizioEsclusione.set(Calendar.YEAR, aie); //gli anni sono contati da 0=1900
			inizioEsclusione.set(Calendar.HOUR_OF_DAY,0);
			inizioEsclusione.set(Calendar.MINUTE,0);
			fineEsclusione.set(Calendar.DAY_OF_MONTH, gfe);
			fineEsclusione.set(Calendar.MONTH, mfe-1); //i mesi sono contati da 0=gennaio
			fineEsclusione.set(Calendar.YEAR, afe); //gli anni sono contati da 0=1900
			fineEsclusione.set(Calendar.HOUR_OF_DAY,23);
			fineEsclusione.set(Calendar.MINUTE,59);
		}
		giorniSettimana=gs;
		if (pp.contentEquals("Procida"))
			calcolaCosto(n,pa);
		else
			calcolaCosto(n,pp);


	}

	public Mezzo(Context c, String n, String op, String mp, String oa, String ma, String pp, String pa, String gie, String mie, String aie, String gfe, String mfe, String afe, String gs) {
		callingContext=c;
		ragioni = callingContext.getResources().getStringArray(R.array.strRagioni);
		for (int i=0;i<100;i++)
			segnalazioni[i] = 0;
		nave = n;
		oraPartenza=Calendar.getInstance();
		oraPartenza.set(Calendar.HOUR_OF_DAY, Integer.parseInt(op));
		oraPartenza.set(Calendar.MINUTE, Integer.parseInt(mp));
		oraArrivo=Calendar.getInstance();
		oraArrivo.set(Calendar.HOUR_OF_DAY, Integer.parseInt(oa));
		oraArrivo.set(Calendar.MINUTE, Integer.parseInt(ma));
		portoPartenza=pp;
		portoArrivo=pa;
		inizioEsclusione=Calendar.getInstance();
		fineEsclusione=Calendar.getInstance();
		esclusione=false;
		if (Integer.parseInt(gie)!=0){
			esclusione=true;
			inizioEsclusione.set(Calendar.DAY_OF_MONTH, Integer.parseInt(gie));
			inizioEsclusione.set(Calendar.MONTH, Integer.parseInt(mie)-1); //i mesi sono contati da 0=gennaio
			inizioEsclusione.set(Calendar.YEAR, Integer.parseInt(aie)); //gli anni sono contati da 0=1900
			inizioEsclusione.set(Calendar.HOUR_OF_DAY,0);
			inizioEsclusione.set(Calendar.MINUTE,0);
			fineEsclusione.set(Calendar.DAY_OF_MONTH, Integer.parseInt(gfe));
			fineEsclusione.set(Calendar.MONTH, Integer.parseInt(mfe)-1); //i mesi sono contati da 0=gennaio
			fineEsclusione.set(Calendar.YEAR, Integer.parseInt(afe)); //gli anni sono contati da 0=1900
			fineEsclusione.set(Calendar.HOUR_OF_DAY,23);
			fineEsclusione.set(Calendar.MINUTE,59);
		}
		giorniSettimana=gs;
		if (pp.contentEquals("Procida"))
			calcolaCosto(n,pa);
		else
			calcolaCosto(n,pp);

	}

	public String segnalazionePiuComune() {
		int max = 0;
		int spc = -1;
		for (int i = 0; i < segnalazioni.length; i++) {
			if (segnalazioni[i] > max) {
				max = segnalazioni[i];
				spc = i;
			}
		}
		if (spc >= 0)
			return ragioni[spc];
		else
			return "";
	}

	private void calcolaCosto(String n,String p) {
		//TODO Mettere costi precisi
		if (n.contentEquals("Traghetto Caremar") && p.contentEquals("Pozzuoli")){
			costoIntero = 7.90;
			setCircaIntero(true);
			costoResidente = 2.60;
			setCircaResidente(true);
			return;
		}
		if (n.contentEquals("Medmar") && p.contentEquals("Pozzuoli")){
			costoIntero = 7.90;
			setCircaIntero(true);
			costoResidente = 2.60;
			setCircaResidente(true);
			return;
		}
//		if (n.contentEquals("Procida Lines") && p.contentEquals("Pozzuoli")){
//			costoIntero=5;
//			costoResidente=2.5;
//			setCircaIntero(true);
//			setCircaResidente(true);
//			return;
//		}
		if ((n.contentEquals("Motonave Gestur") || n.contentEquals("Traghetto Gestur")) && p.contentEquals("Pozzuoli")) {
			costoIntero = 7.90;
			setCircaIntero(true);
			costoResidente = 2.60;
			setCircaResidente(true);
			return;
		}
		if (n.contentEquals("Traghetto Caremar") && p.contentEquals("Napoli Porta di Massa")){
			costoIntero = 10.60;
			costoResidente=3.10;
			setCircaIntero(true);
			setCircaResidente(true);
			return;
		}
		if (n.contentEquals("Aliscafo Caremar") && p.contentEquals("Napoli Beverello")){
			costoIntero = 14.40;
			costoResidente = 4.90;
			setCircaIntero(true);
			setCircaResidente(true);
			return;
		}		
		if (n.contentEquals("Aliscafo SNAV") && p.contentEquals("Napoli Beverello")){
			costoIntero = 14.40;
			costoResidente = 4.90;
			setCircaIntero(true);
			setCircaResidente(true);
			return;
		}
		if (n.contentEquals("Traghetto Caremar") && p.contentEquals("Ischia Porto")){
			costoIntero = 7.80;
			costoResidente = 2.50;
			setCircaIntero(true);
			setCircaResidente(true);
			return;
		}
		if (n.contentEquals("Aliscafo Caremar") && p.contentEquals("Ischia Porto")){
			costoIntero = 8.70;
			costoResidente = 2.70;
			setCircaIntero(true);
			setCircaResidente(true);
			return;
		}
		if (n.contentEquals("Aliscafo SNAV") && p.contentEquals("Casamicciola")){
			costoIntero = 8.70;
			costoResidente = 2.70;
			setCircaIntero(true);
			setCircaResidente(true);
			return;
		}
		if (n.contentEquals("Medmar") && p.contentEquals("Ischia Porto")){
			costoIntero = 7.80;
			costoResidente = 2.50;
			setCircaIntero(true);
			setCircaResidente(true);
			return;
		}
		if (n.contentEquals("Ippocampo") ){ //TODO Da verificare
			costoIntero = 7.90;
			setCircaIntero(true);
			costoResidente = 2.60;
			setCircaResidente(true);
			return;
		}
		if (n.contentEquals("Scotto Line")) { //TODO Da verificare
			costoIntero = 7.90;
			setCircaIntero(true);
			costoResidente = 2.60;
			setCircaResidente(true);
			return;
		}
//		if (n.contentEquals("Aladino") ){ //TODO Da verificare
//			costoIntero=0.00;
//			costoResidente=0.00;
//			setCircaIntero(true);
//			setCircaResidente(true);
//			return;
//		}

		if (n.contentEquals("LazioMar")) { //TODO Da verificare
			costoIntero = 7.90;
			setCircaIntero(true);
			costoResidente = 2.60;
			setCircaResidente(true);
		}
		
	}

	public boolean getGiornoSeguente() {
		return giornoSeguente;
	}

	public void setGiornoSeguente(boolean b) {
		giornoSeguente = b;
	}

	public int getOrderInList() {
		return orderInList;
	}

	public void setOrderInList(int orderInList) {
		this.orderInList = orderInList;
	}

	public void setId(int id) {
		int id1 = id;
	}

	public double getCostoIntero() {
		return costoIntero;
	}

	public double getCostoResidente() {
		return costoResidente;
	}

	public boolean isCircaIntero() {
		return circaIntero;
	}

	private void setCircaIntero(boolean circaIntero) {
		this.circaIntero = circaIntero;
	}

	public boolean isCircaResidente() {
		return circaResidente;
	}

	private void setCircaResidente(boolean circaResidente) {
		this.circaResidente = circaResidente;
	}

	public void addMotivo(String rigaMotivo) {
		Integer motivo=Integer.parseInt(rigaMotivo);
		if (motivo==99){
			conferme++;
		}
		else {
			if (tot>segnalazioni[motivo])
				conc=false;
			segnalazioni[motivo]++;
			tot++;
		}

	}

}
