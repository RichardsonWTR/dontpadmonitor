package ifsudestemg.tsi.richardson.dontpadmonitor;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class ListaDeLinks extends AppCompatActivity {
    private ListView lista;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_de_links);

        ArrayList<String> urlsDontpad = getIntent().getStringArrayListExtra("urls");
        lista = (ListView) findViewById(R.id.listView);

        ArrayAdapter arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,urlsDontpad);

        lista.setAdapter(arrayAdapter);
    }
}
