package com.porfirio.orariprocida2011.entity;

import com.porfirio.orariprocida2011.R;
import com.porfirio.orariprocida2011.activities.OrariProcida2011Activity;

import java.util.Calendar;

public class Osservazione {
    private double windBeaufort;
    private int windDirection;
    private double windKmh;
    private String windDirectionString;
    private Calendar tempo;

    public Osservazione() {
        windBeaufort = 0.0;
        windDirection = 0;
        windKmh = 0.0;
        windDirectionString = "";
    }

    public Osservazione(double wb, int wd) {
        setWindBeaufort(wb);
        setWindDirection(wd);
    }

    public double getWindBeaufort() {
        return windBeaufort;
    }

    public void setWindBeaufort(Double wkmh) {
        if (wkmh <= 1)
            this.windBeaufort = (0.0);
        else if (wkmh > 1 && wkmh < 6)
            this.windBeaufort = (1 + (wkmh - 3) / (5 - 1));
        else if (wkmh >= 6 && wkmh < 12)
            this.windBeaufort = (2 + (wkmh - 8.5) / (11 - 6));
        else if (wkmh >= 12 && wkmh < 20)
            this.windBeaufort = (3 + (wkmh - 15.5) / (19 - 12));
        else if (wkmh >= 20 && wkmh < 29)
            this.windBeaufort = (4 + (wkmh - 24) / (28 - 20));
        else if (wkmh >= 29 && wkmh < 39)
            this.windBeaufort = (5 + (wkmh - 33.5) / (38 - 29));
        else if (wkmh >= 39 && wkmh < 50)
            this.windBeaufort = (6 + (wkmh - 44) / (49 - 39));
        else if (wkmh >= 50 && wkmh < 62)
            this.windBeaufort = (7 + (wkmh - 55.5) / (61 - 50));
        else if (wkmh >= 62 && wkmh < 75)
            this.windBeaufort = (8 + (wkmh - 68) / (74 - 62));
        else if (wkmh >= 75 && wkmh < 89)
            this.windBeaufort = (9 + (wkmh - 81.5) / (88 - 75));
        else if (wkmh >= 89 && wkmh < 103)
            this.windBeaufort = (10 + (wkmh - 95.5) / (102 - 89));
        else if (wkmh >= 103 && wkmh < 118)
            this.windBeaufort = (11 + (wkmh - 110) / (117 - 103));
        else if (wkmh >= 118)
            this.windBeaufort = (12.0);
    }

    public int getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(int windDirection) {
        this.windDirection = windDirection;
    }

    public String getWindDirectionString() {
        return windDirectionString;
    }

    public void setWindDirectionString(String s) {
        windDirectionString = s;
    }

    public void setWindDirectionString(int dir, OrariProcida2011Activity callingActivity) {
        if (dir == 0)
            windDirectionString = callingActivity.getString(R.string.nord);
        else if (dir == 45)
            windDirectionString = callingActivity.getString(R.string.nordEst);
        else if (dir == 90)
            windDirectionString = callingActivity.getString(R.string.est);
        else if (dir == 135)
            windDirectionString = callingActivity.getString(R.string.sudEst);
        else if (dir == 180)
            windDirectionString = callingActivity.getString(R.string.sud);
        else if (dir == 225)
            windDirectionString = callingActivity.getString(R.string.sudOvest);
        else if (dir == 270)
            windDirectionString = callingActivity.getString(R.string.ovest);
        else if (dir == 315)
            windDirectionString = callingActivity.getString(R.string.nordOvest);
    }

    public Double getWindKmh() {
        return windKmh;
    }

    public void setWindKmh(double wkmh) {
        if (windKmh == 0 || wkmh > 0)
            windKmh = wkmh;
    }

    public String getWindBeaufortString(OrariProcida2011Activity callingActivity) {
        int forza = Double.valueOf(windBeaufort).intValue();
        switch (forza) {
            case 0:
                return "" + callingActivity.getString(R.string.calma);
            case 1:
                return "" + callingActivity.getString(R.string.bavaDiVento);
            case 2:
                return "" + callingActivity.getString(R.string.brezzaLeggera);
            case 3:
                return "" + callingActivity.getString(R.string.brezzaTesa);
            case 4:
                return "" + callingActivity.getString(R.string.ventoModerato);
            case 5:
                return "" + callingActivity.getString(R.string.ventoTeso);
            case 6:
                return "" + callingActivity.getString(R.string.ventoFresco);
            case 7:
                return "" + callingActivity.getString(R.string.ventoForte);
            case 8:
                return "" + callingActivity.getString(R.string.burrasca);
            case 9:
                return "" + callingActivity.getString(R.string.burrascaForte);
            case 10:
                return "" + callingActivity.getString(R.string.tempesta);
            case 11:
                return "" + callingActivity.getString(R.string.fortunale);
            case 12:
                return "" + callingActivity.getString(R.string.uragano);
        }
        return "" + callingActivity.getString(R.string.errore);
    }


    public Calendar getTempo() {
        return tempo;
    }

    public void setTempo(Calendar tempo) {
        this.tempo = tempo;
    }
}
