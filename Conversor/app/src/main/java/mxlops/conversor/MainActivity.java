package mxlops.conversor;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        TextView textResult = findViewById(R.id.textResult);
        EditText inputNumber = findViewById(R.id.inputNumber);
        Button buttonConverter = findViewById(R.id.buttonConverter);
        RadioGroup rg = findViewById(R.id.checkedRadio);



        buttonConverter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(inputNumber.getText().toString().equals("")){
                    AlertDialog.Builder inputNull = new AlertDialog.Builder(MainActivity.this);
                    inputNull.setTitle("Atenção");
                    inputNull.setMessage("Por favor, insira um valor!");
                    inputNull.setNeutralButton("Ok", null);
                    inputNull.create().show();



                } else {

                    String moeda;

                    if (rg.getCheckedRadioButtonId() == R.id.ARS) {

                        moeda = "ARS";
                    } else if (rg.getCheckedRadioButtonId() == R.id.USD) {

                        moeda = "USD";
                    } else {

                        moeda = "EUR";
                    }

                    String number = inputNumber.getText().toString();
                    Double doubleNumber = Double.parseDouble(number);

                    try {

                        String val = new HTTPRequestCall().execute(moeda).get();
                        Double valor = Double.parseDouble(val);

                        Double result = valor * doubleNumber;

                        textResult.setText(result.toString());
                        textResult.setVisibility(View.VISIBLE);

                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                }
            }
        });
    }

}
class HTTPRequestCall extends AsyncTask<String, String, String>{

    private ObjectMapper mapper;

    HTTPRequestCall(){
        this.mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    }

    @Override
    protected String doInBackground(String... param){
        URL url;
        HttpsURLConnection connection = null;

        try {
            url = new URL("https://economia.awesomeapi.com.br/json/last/"+param[0]+"-BRL");
            connection = (HttpsURLConnection) url.openConnection();

            InputStream data = connection.getInputStream();

            String result = convertToString(data, param[0]);

            return result;

        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e){
            throw new RuntimeException(e);
        } finally{
            if(connection != null){
                connection.disconnect();
            }
        }

    }

    public String convertToString(InputStream inputStream, String param) throws IOException{
        InputStreamReader inputReader = new InputStreamReader(inputStream);

        BufferedReader reader = new BufferedReader(inputReader);

        StringBuffer sb = new StringBuffer();
        String str;
        while((str = reader.readLine()) != null){
            sb.append(str);
        }

        String val = parser(sb.toString(), param);

        return val;
    }

    public String parser(String json, String param) throws IOException{

        List<Map<String, String>> data = new ArrayList<>();

        JsonNode rootNode = mapper.readTree(json);
        JsonNode items = rootNode.get(param+"BRL");

        String itemsList = items.toString();

        data = mapper.readValue(itemsList, new TypeReference<List<Map<String, String>>>(){

        });

        for (Map<String, String> map : data){
            param = map.get("bid");
        }

        System.out.println(items.toString());

        return param;
    }


}

