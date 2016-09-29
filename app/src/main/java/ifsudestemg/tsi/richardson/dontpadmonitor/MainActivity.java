package ifsudestemg.tsi.richardson.dontpadmonitor;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private EditText txt;
    private Database d;
    private Button buttonIniciaOuParaService;
    private boolean servicoParado = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        d = new Database(getApplicationContext());
        List<Url> all = d.all();
        for(Url u:all)
            Log.d("Url salva:",u.toString());


        buttonIniciaOuParaService = (Button) findViewById(R.id.buttonIniciaOuParaService);
        buttonIniciaOuParaService.setText("Iniciar monitoria");

        txt = (EditText) findViewById(R.id.editText);

        txt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    String texto = txt.getText().toString().trim();

                    // Verifica se contêm espaços em branco na string
                    Pattern pattern = Pattern.compile("\\s");
                    Matcher matcher = pattern.matcher(texto);
                    boolean found = matcher.find();

                    if(found){
                        Toast.makeText(getApplicationContext(),"Não pode conter espaços em branco",Toast.LENGTH_SHORT).show();
                    }

                    else

                    if (texto.length() > 0) {
                        Toast.makeText(getApplicationContext(), "dontpad.com/" + texto + " adicionada à lista",Toast.LENGTH_SHORT).show();
                        d.insert(new Url(-1,texto,""));
                    }
                }
                return false;
            }
        });
    }

    // Trata o evento do botão “Iniciar serviço”
    public void iniciaOuParaServico(View view) {
        Intent intent = new Intent(getBaseContext(), ServiceBaixaConteudo.class);
        if(servicoParado == true) {
            startService(intent);
            buttonIniciaOuParaService.setText("Parar monitoria");
            servicoParado = false;
        }else{
            stopService(intent);
            buttonIniciaOuParaService.setText("Iniciar monitoria");
            servicoParado = true;
        }
    }

   // Trata o evento do botão 'Links monitorados'
    public void irParaListaDeLinks(View view){
        Intent intent = new Intent(getBaseContext(),ListaDeLinks.class);
        List<Url> all = d.all();
        ArrayList<String> urls = new ArrayList<>();
        for(Url u : all){
            urls.add(u.getUrl());
        }

        intent.putStringArrayListExtra("urls",urls);

        startActivity(intent);
   }
}
