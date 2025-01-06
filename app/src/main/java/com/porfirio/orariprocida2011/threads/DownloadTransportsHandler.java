package com.porfirio.orariprocida2011.threads;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.porfirio.orariprocida2011.R;
import com.porfirio.orariprocida2011.activities.OrariProcida2011Activity;
import com.porfirio.orariprocida2011.entity.Mezzo;
import com.porfirio.orariprocida2011.utils.Analytics;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class DownloadTransportsHandler extends Handler {

    private static final String NEWS_URL = "http://wpage.unina.it/ptramont/orari.csv";

    private final OrariProcida2011Activity act;

    // Semaphore declarations
    public static Semaphore taskDownload;
    public static Semaphore taskDownloadStart;
    private Calendar updateWebTimes;
    private final ArrayList<Mezzo> transportList;

    private Analytics analytics;

    public DownloadTransportsHandler(OrariProcida2011Activity orariProcida2011Activity) {
        act = orariProcida2011Activity;
        transportList = new ArrayList<>();
    }

    public void setAnalytics(Analytics analytics) {
        this.analytics = analytics;
    }

    public void fetchTransports() {
        new Thread(() -> {
            handleSemaphore(taskDownloadStart, true);

            analytics.send("App Event", "Download Mezzi Task");

            try {
                URL url = new URL(NEWS_URL);
                processTransportData(url);
            } catch (MalformedURLException e) {
                Log.e("DownloadHandler", "Invalid URL: " + e.getMessage(), e);
            } finally {
                handleSemaphore(taskDownload, false);
            }

            analytics.send("App Event", "Download Terminated");
        }).start();
    }

    private void processTransportData(URL url) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);

            try (InputStream in = conn.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {

                String updateLine = reader.readLine();
                if (updateLine == null) return;

                updateWebTimes = parseUpdateTimes(updateLine);

                if (!updateWebTimes.after(act.updateTimesIS)) {
                    Log.d("DownloadHandler", "No new updates available.");
                    return;
                }

                Log.d("DownloadHandler", "Found newer schedule from the web.");
                downloadAndSaveData(updateLine, reader);
                updateGui();

            } catch (IOException e) {
                Log.e("DownloadHandler", "Error reading transport data: " + e.getMessage(), e);
            }
        } catch (SocketTimeoutException e) {
            Log.e("DownloadHandler", "Connection timed out: " + e.getMessage(), e);
        } catch (IOException e) {
            Log.e("DownloadHandler", "Error opening connection: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private Calendar parseUpdateTimes(String updateLine) {
        StringTokenizer st = new StringTokenizer(updateLine, ",");
        Calendar calendar = (Calendar) act.updateTimesIS.clone();
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(st.nextToken()));
        calendar.set(Calendar.MONTH, Integer.parseInt(st.nextToken()));
        calendar.set(Calendar.YEAR, Integer.parseInt(st.nextToken()));
        return calendar;
    }

    private void downloadAndSaveData(String updateLine, BufferedReader reader) throws IOException {
        HttpURLConnection conn2 = null;
        try {
            URL newsUrl = new URL(act.getApplicationContext().getString(R.string.urlNews));
            conn2 = (HttpURLConnection) newsUrl.openConnection();

            try (InputStream in = conn2.getInputStream();
                 BufferedReader newsReader = new BufferedReader(new InputStreamReader(in));
                 FileOutputStream fos = act.openFileOutput("orari.csv", Context.MODE_PRIVATE)) {

                String newsLine = newsReader.readLine();

                fos.write(updateLine.getBytes());
                fos.write("\n".getBytes());
                transportList.clear();

                String line;
                while ((line = reader.readLine()) != null) {
                    Mezzo mezzo = parseMezzo(line);
                    transportList.add(mezzo);
                    fos.write(line.getBytes());
                    fos.write("\n".getBytes());
                }
            }
        } finally {
            if (conn2 != null) {
                conn2.disconnect();
            }
        }
    }

    private Mezzo parseMezzo(String line) {
        StringTokenizer st = new StringTokenizer(line, ",");
        return new Mezzo(
                st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(),
                st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(),
                st.nextToken(), st.nextToken(), st.nextToken(), st.nextToken(),
                st.nextToken(), st.nextToken()
        );
    }

    private void updateGui() {
        act.updateWebTimes = (Calendar) updateWebTimes.clone();
        act.ultimaLetturaOrariDaWeb = Calendar.getInstance();

        String str = String.format(Locale.getDefault(), "%s %d/%d/%d",
                act.getString(R.string.orariAggiornatiAl),
                act.updateWebTimes.get(Calendar.DAY_OF_MONTH),
                act.updateWebTimes.get(Calendar.MONTH) + 1,
                act.updateWebTimes.get(Calendar.YEAR)
        );

        act.transportList.clear();
        act.transportList.addAll(transportList);
        act.aggiornaLista();
        act.setMsgToast();

        Log.d("DownloadHandler", str);
        Log.d("DownloadHandler", "Transport data updated in the GUI.");
    }

    private void handleSemaphore(Semaphore semaphore, boolean acquire) {
        if (semaphore != null) {
            try {
                if (acquire) {
                    semaphore.acquire();
                } else if (!semaphore.tryAcquire(15L, TimeUnit.SECONDS)) {
                    Log.e("DownloadHandler", "Semaphore timeout.");
                    act.finish();
                }
            } catch (InterruptedException e) {
                Log.e("DownloadHandler", "Semaphore operation interrupted: " + e.getMessage(), e);
            } finally {
                semaphore.release();
            }
        }
    }
}
