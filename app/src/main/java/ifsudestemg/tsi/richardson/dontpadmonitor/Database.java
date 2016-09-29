// Armazenando utilizando SQLite
package ifsudestemg.tsi.richardson.dontpadmonitor;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by richardson on 9/29/16.
 */
public class Database {
    private static final String DATABASE_NAME = "urls_dontpad";

    // Somente esta aplicação terá acesso ao BD
    private static final int DATABASE_ACCESS = 0;

    // Consultas SQL
    private static final String NOME_TABELA = "urls";
    private static final String SQL_STRUCT = "CREATE TABLE IF NOT " +
            "EXISTS " + NOME_TABELA + "(" +
            "id_ INTEGER PRIMARY KEY AUTOINCREMENT," +
            "url TEXT NOT NULL," +
            "conteudo TEXT NOT NULL);";
    private static final String SQL_INSERT = "INSERT INTO " + NOME_TABELA +
            " (url, conteudo) VALUES ('%s', '%s');";
    private static final String SQL_SELECT_ALL = "SELECT * FROM " + NOME_TABELA +
            " ORDER BY url;";
    private static final String SQL_CLEAR = "DROP TABLE IF EXISTS " + NOME_TABELA + ";";
    private static final String SQL_GET = "SELECT * FROM " + NOME_TABELA + " WHERE url = '%s';";
    private static final String SQL_UPDATE = "UPDATE " + NOME_TABELA + " SET conteudo = '%s' WHERE url = '%s';";

    private SQLiteDatabase database;
    private Cursor cursor;
    private int indexID, indexEndereco, indexConteudo;

    // Construtor
    public Database(Context context){
        // Utiliza o contexto da activity que vai manipular o BD
        database = context.openOrCreateDatabase(DATABASE_NAME, DATABASE_ACCESS, null);
        database.execSQL(SQL_STRUCT);
    }

    public void clear(){
        database.execSQL(SQL_CLEAR);
    }

    public void close(){
        database.close();
    }

    public void insert(Url url){
        Log.d("Insert: " , "URL: "+ url);
        String query = String.format(SQL_INSERT, url.getUrl(),url.getConteudo());
        database.execSQL(query);
    }

    public List<Url> all(){
        List<Url> urls = new ArrayList<>();
        Url url;

        cursor = database.rawQuery(SQL_SELECT_ALL,null);

        if(cursor.moveToFirst()){
            indexID = cursor.getColumnIndex("id_");
            indexEndereco = cursor.getColumnIndex("url");
            indexConteudo = cursor.getColumnIndex("conteudo");

            do{
                url = new Url(cursor.getInt(indexID),cursor.getString(indexEndereco),cursor.getString(indexConteudo));
                urls.add(url);
            }while(cursor.moveToNext());

            cursor.close();
        }
        return urls;
    }//all()

    // Obtém o conteúdo associado a url informada. Se não existe, retorna null
    public String obterConteudo(String url) {
        Log.d("obterConteudo","URL: " + url);
        String resultado = null;
        try {
            String query = String.format(SQL_GET, url);
            Log.d("obterConteudo:SQL",query);
            cursor = database.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                Log.d("obterConteudo","Valor encontrado!");
                indexConteudo = cursor.getColumnIndex("conteudo");
                resultado = cursor.getString(indexConteudo);
                cursor.close();
            }

        }catch(Exception e){
            Log.d("Exceção:obterConteúdo:",e.getMessage());
        }
        return resultado;
    }

    public void atualizar(String url, String conteudoNovo) {
        String query = String.format(SQL_UPDATE,conteudoNovo,url);
        database.execSQL(query);
    }
}// class
