package com.client.appap.Actividades;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.client.appap.Modelo.Datos;
import com.client.appap.R;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.Credentials;
import com.google.android.gms.auth.api.credentials.CredentialsApi;
import com.google.android.gms.auth.api.credentials.HintRequest;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    //Codigo HASH de la appM es: F6oWIQlQ+AE

    private static final String TAG = "MenuInicial";
    private static final int RESOLVE_HINT = 200;       //Codigo de respuesta correcto para obtener el número de telefono
    private String numTel;
    private Button bCont;
    private ProgressBar pBar;
    private EditText textoMovil;
    private Datos datos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_AppBP);
        setContentView(R.layout.activity_telefono);
        //Esconder la barra superior de la APP
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();

        //Enlazar views
        bCont = (Button) findViewById(R.id.buttonContinuar);
        pBar = (ProgressBar) findViewById(R.id.progressBar);
        pBar.setVisibility(View.INVISIBLE);
        textoMovil = (EditText) findViewById(R.id.NumTel);

        //Creamos el modelo
        datos = new Datos();

        // Codigo para generar el hashkey
        //AppSignatureHelper appSignatureHashHelper = new AppSignatureHelper(MainActivity.this);
        //Log.d(TAG, "HashKey: " + appSignatureHashHelper.getAppSignatures().get(0));


    }

    public void onContinuar(View v) {
        //Obtener y usar el número de telefono
        numTel=textoMovil.getText().toString(); //<-- obtenemos el string correspondiente al numbero de telefono seleccionado
        datos.setTelefono(numTel);              //Guardamos el numero de tel en el modelo
        //Comprobamos que el campo pasado no sea nulo o contenga espacios o no contena el +
        if (!(numTel.isEmpty()) &&  (!(numTel.contains(" ")) && (numTel.contains("+")))){
        pBar.setVisibility(View.VISIBLE);
        Intent otpIntent = new Intent(MainActivity.this, OtpActivity.class); //Mover de la Clase A a la B
        //otpIntent.putExtra("tel", numTel);
        otpIntent.putExtra("datos", datos);
        Toast.makeText(this, "Num tel es: " + textoMovil.getText().toString(), Toast.LENGTH_LONG).show();
        //Pasamos el num de Telefono
        startActivity(otpIntent);
    }else Toast.makeText(this, "Es necesario pasar un número de teléfono en el formato correcto", Toast.LENGTH_LONG).show();
        //Código necesario para obtener el codigo hash de la app
        //AppSignatureHelper appSignatureHelper = new AppSignatureHelper(this);
        //Log.d(TAG,"El código hash de la app es: "+appSignatureHelper.getAppSignatures().get(0));
        //Codigo HASH de la app es: nXzGtk7rNLW
    }

    @Override
    public void onClick(View v) { }

}
