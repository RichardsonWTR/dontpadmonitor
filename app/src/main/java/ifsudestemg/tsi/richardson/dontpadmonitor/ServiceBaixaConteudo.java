package ifsudestemg.tsi.richardson.dontpadmonitor;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by richardson on 9/28/16.
 */
public class ServiceBaixaConteudo extends Service {
    public static final String ENDERECO_DONTPAD = "http://dontpad.com/";

    private Handler handler;
    private Runnable runnable;
    private int counter = 1;
    private static final int INTERVALO = 15000; //miliseconds

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void notifica(String url, float mudanca,int idDaNotificacao){
        int idNotificacao = idDaNotificacao;

        NotificationCompat.Builder biuder = new NotificationCompat.Builder(this);
        biuder.setContentTitle("Monitor de URL do Dontpad");
        biuder.setContentText(String.valueOf(mudanca) + "% de mudança em: dontpad.com/" + url);
        biuder.setAutoCancel(true);
        biuder.setSmallIcon(R.mipmap.ic_launcher);
        biuder.setVibrate(new long[] {0,300,200,300});
        biuder.setLights(Color.GREEN,3000,3000);
        biuder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        biuder.setNumber(1);

        Intent intent = new Intent(this,MainActivity.class);
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
        taskStackBuilder.addParentStack(MainActivity.class);
        taskStackBuilder.addNextIntent(intent);

        PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(idNotificacao,biuder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final Database database = new Database(getApplicationContext());
        final ArrayList<String> urlsDontpad;

        //Toast.makeText(getApplicationContext(), "Dados obtidos do BD", Toast.LENGTH_SHORT).show();
        List<Url> all = database.all();
        urlsDontpad = new ArrayList<>();
        for(Url url : all) {
            urlsDontpad.add(url.getUrl());
        }

        //Toast.makeText(getApplicationContext(),"Links a monitorar: " + urlsDontpad.size(),Toast.LENGTH_SHORT).show();
        //Toast.makeText(getApplicationContext(), "Serviço iniciado", Toast.LENGTH_SHORT).show();


        handler = new Handler();
        runnable = new Runnable(){

            @Override
            public void run() {
                List<Url> all = database.all();

                for(String url : urlsDontpad){
                    try {
                        String endereco = ENDERECO_DONTPAD + url;
                        String html = downloadContent(endereco);
                        int inicio = html.indexOf("<textarea id=\"text\">") + "<textarea id=\"text\">".length();
                        int fim = html.lastIndexOf("</textarea>");
                        String conteudoNovo = html.substring(inicio,fim);
                        Url urlObj = new Url(-1,endereco,conteudoNovo);
                        String conteudoAntigo = database.obterConteudo(url);

                        if(conteudoAntigo == null){ //Não existe no banco
                            database.insert(new Url(-1,url,conteudoNovo));
                        }else
                        if (conteudoAntigo.equals(conteudoNovo) == false){
                            // Conteúdo mudou, notificar!
                            //Quantidade de caracteres que mudou
                            int leveinsteinDistance = levenshteinDistance(conteudoAntigo, conteudoNovo);
                            //Calculando a porcentagem de mudança
                            float porcentagem;
                            if(conteudoAntigo.length() > 0)
                                porcentagem = conteudoNovo.length() * 100 / conteudoAntigo.length();
                            else
                                porcentagem = leveinsteinDistance;

                            //Criar um id de notificacao, com base na url
                            int idNotificacao = -1;
                            for(int i=0; i< url.length(); i++){
                                idNotificacao += Character.getNumericValue(url.charAt(i));
                            }

                            database.atualizar(url,conteudoNovo);

                            notifica(url,porcentagem,idNotificacao);
                            //Toast.makeText(getApplicationContext(),url + ", Conteúdo mudou: " + String.valueOf(porcentagem) + "%",Toast.LENGTH_SHORT).show();
                        }


                        //Toast.makeText(getApplicationContext(),"URL: " + url + "\n\n"+ conteudoNovo,Toast.LENGTH_LONG).show();

                    } catch (ServiceBaixaConteudoException e) {
                        Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
                //notifica();
                {
                    handler.postDelayed(this, INTERVALO);
                }
            }
        };

        handler.postDelayed(runnable,INTERVALO);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //Toast.makeText(this,"Serviço parado",Toast.LENGTH_SHORT).show();
        handler.removeCallbacks(runnable);
    }


    public static int levenshteinDistance (String lhs, String rhs) {
        int len0 = lhs.length() + 1;
        int len1 = rhs.length() + 1;

        // the array of distances
        int[] cost = new int[len0];
        int[] newcost = new int[len0];

        // initial cost of skipping prefix in String s0
        for (int i = 0; i < len0; i++) cost[i] = i;

        // dynamically computing the array of distances

        // transformation cost for each letter in s1
        for (int j = 1; j < len1; j++) {
            // initial cost of skipping prefix in String s1
            newcost[0] = j;

            // transformation cost for each letter in s0
            for (int i = 1; i < len0; i++) {
                // matching current letters in both strings
                int match = (lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1;

                // computing cost for each transformation
                int cost_replace = cost[i - 1] + match;
                int cost_insert = cost[i] + 1;
                int cost_delete = newcost[i - 1] + 1;

                // keep minimum cost
                newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
            }

            // swap cost/newcost arrays
            int[] swap = cost;
            cost = newcost;
            newcost = swap;
        }

        // the distance is the cost for transforming all letters in both strings
        return cost[len0 - 1];
    }

    public String downloadContent(String url) throws ServiceBaixaConteudoException {
        if(!isNetworkAvailable()) {
            throw new ServiceBaixaConteudoException("Sem conexão à Internet");
        }

        try {
            return new DownloadPageTask().execute(url).get();

        } catch (InterruptedException e) {

            throw new ServiceBaixaConteudoException("Inesperado: Interrupted Exception");

        } catch (ExecutionException e) {

            throw new ServiceBaixaConteudoException("Inesperado: Execution Exception");

        }
    }//downloadContent

    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null
        // otherwise check if we are connected
        return networkInfo != null && networkInfo.isConnected();
    }//isNetworkAvailable

    private class DownloadPageTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try{
                return downloadUrl(urls[0]);
            }catch(IOException e){
                return null;
            }
        }

        private String downloadUrl(String urlString) throws IOException {
            InputStream inputStream = null;
            // Apenas mostra os primeiros 500 caracteres da pagina
            //int len = 500;

            try{
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(20000); // milliseconds
                conn.setConnectTimeout(15000); //milliseconds
                conn.setRequestMethod("GET");
                conn.setDoInput(true);

                //Inicia a query
                conn.connect();
                int responseCode = conn.getResponseCode();
                Log.d("donwloadUrl:", "url: " + urlString + ", code: " + String.valueOf(responseCode));
                inputStream = conn.getInputStream();

                //Converte o inputStream em string
                return readIt(inputStream);
            }finally {
                if(inputStream != null)
                    inputStream.close();
            }
        }// downloadUrl

        // Lê um input stream por completo e o retorna como string
        public String readIt(InputStream inputStream) throws IOException {
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
            String line = bufferedReader.readLine();
            while(line != null){
                stringBuilder.append(line);
                stringBuilder.append("\n");
                line = bufferedReader.readLine();
            }
            return stringBuilder.toString();
        }//readIt
    }//innerclass

    public class ServiceBaixaConteudoException extends Exception {
        public ServiceBaixaConteudoException(String message) {
            super(message);
        }
    }

}//class
