package com.client.appap.Actividades;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.client.appap.Modelo.Datos;
import com.client.appap.R;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import org.json.JSONException;
import org.json.JSONObject;


public class OtpActivity extends AppCompatActivity {
    private Button bCont;
    private ProgressBar pBar;
    private EditText cOTP;
    private String otpAppM;
    private String nTel, telAppM, urlBD;
    private int clave;
    private FirebaseApp app, secundaria;
    private FirebaseAuth auten, autenMain;
    private String auth, authBD2;
    private Datos dat;
    private TextView textFinal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        //Codigo HASH de la app es: F6oWIQlQ+AE

        //Esconder la barra superior de la APP
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();





        //Obtenemos los datos del bundle de la actividad anterior
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            dat = (Datos) extras.getParcelable("datos");  //Obtenemos el modelo de la actividad anterior
            if(dat!=null) {
                nTel = dat.getTelefono();
                urlBD = dat.getUrlDB();

            }
            if (nTel != null) {
                //Creamos el gson para guardar un json en shared preferences
                Gson gson = new Gson();
                String json = gson.toJson(dat);
                SharedPreferences sharedPref = this.getSharedPreferences("guardartel", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                //editor.putString("tel", nTel);
                editor.putString("datos", json); //Guardamos el objeto para cuando llegue el SMS
                editor.apply();
            }
        }


        //Enlazar views
        cOTP = (EditText) findViewById(R.id.ClaveOTP);
        bCont = (Button) findViewById(R.id.buttonContinuar);
        pBar = (ProgressBar) findViewById(R.id.progressBar);
        textFinal = (TextView) findViewById(R.id.textAbajo);

        pBar.setVisibility(View.INVISIBLE);
        //bCont.setVisibility(View.INVISIBLE);

        //Autenticar en la BD con el .json de AppM y AppA
        FirebaseOptions optAppA = new FirebaseOptions.Builder()
                .setProjectId("appaym-537c4")
                .setApplicationId("1:653015120606:android:f6638c71831d75e965a04f")
                .setApiKey("AIzaSyBN9-cz5VfCOW24MgQpooQRrsTnhgkbgL8")
                .setDatabaseUrl("https://appaym-537c4-default-rtdb.europe-west1.firebasedatabase.app")
                .build();
        FirebaseApp.initializeApp(this, optAppA, "primaria");
        app = FirebaseApp.getInstance("primaria");
        auten = FirebaseAuth.getInstance(app);


        // Manual de Firebase para poner el segundo proyecto:
        FirebaseOptions opciones = new FirebaseOptions.Builder()
                .setProjectId("smsretrieverservera")
                .setApplicationId("1:49070307835:android:eeefd2036219d1aa01f35e")
                .setApiKey("AIzaSyCO0wQa_fia6ojLkFCzLG-sft5XUWF2Skw")
                .build();

        // Autenticar la segunda BD para la app
        FirebaseApp.initializeApp(this , opciones, "secundaria");

        secundaria = FirebaseApp.getInstance("secundaria");
        autenMain = FirebaseAuth.getInstance(secundaria);
        //API Rest request


        //Obtener token de Auth
        String url = "https://www.googleapis.com/identitytoolkit/v3/relyingparty/verifyPassword?key=AIzaSyCO0wQa_fia6ojLkFCzLG-sft5XUWF2Skw";
        Log.d("Test", "Aqui llego");
        // Request a string response from the provided URL.
        RequestQueue requestQueue = Volley.newRequestQueue(OtpActivity.this);
        JSONObject postData = new JSONObject();
        try {
            postData.put("email", "a@a.com");
            postData.put("password", "123456");
            postData.put("returnSecureToken", true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, postData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    auth = response.getString("idToken");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        requestQueue.add(jsonObjectRequest);

        if (nTel != null) {
            autenMain.signInWithEmailAndPassword("a@a.com", "123456").addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        String url;
                        if(urlBD == null) {
                             url = "https://smsretrieverservera-default-rtdb.europe-west1.firebasedatabase.app/numeros.json?auth=" + auth;
                        }else {
                             url = urlBD;
                        }
                        Log.d("Test", "Aqui llego");
                        // Request a string response from the provided URL.
                        RequestQueue requestQueue = Volley.newRequestQueue(OtpActivity.this);
                        JSONObject postData = new JSONObject();
                        try {
                            postData.put("tel", nTel);
                            postData.put("hash", "F6oWIQlQ+AE");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, postData, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                //Toast.makeText(getApplicationContext(), "Response: " + response, Toast.LENGTH_LONG).show();
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(getApplicationContext(), "Error URL no existente o sin permisos", Toast.LENGTH_SHORT).show();
                            }
                        });
                        requestQueue.add(jsonObjectRequest);
                    } else {
                        Toast.makeText(getApplicationContext(), "Error de auth", Toast.LENGTH_LONG).show();
                    }
                }
            });
            // Instantiate the RequestQueue.
        }


        //Comprobamos si recibimos algún otp de la AppA
        //obtenemos la nueva autenticacion de la BD entre AppA y AppM
        String urlBD2 = "https://www.googleapis.com/identitytoolkit/v3/relyingparty/verifyPassword?key=AIzaSyBN9-cz5VfCOW24MgQpooQRrsTnhgkbgL8";
        // Request a string response from the provided URL.
        RequestQueue requestQueueBD2 = Volley.newRequestQueue(OtpActivity.this);
        JSONObject postDataBD2 = new JSONObject();
        try {
            postDataBD2.put("email", "a@a.com");
            postDataBD2.put("password", "123456");
            postDataBD2.put("returnSecureToken", true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequestBD2 = new JsonObjectRequest(Request.Method.POST, urlBD2, postDataBD2, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    authBD2 = response.getString("idToken");
                    //Toast.makeText(getApplicationContext(), "Me he auntenticado:",Toast.LENGTH_SHORT).show();
                    Log.d("APIBien", "Me he auntenticado:");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        requestQueueBD2.add(jsonObjectRequestBD2);

        auten.signInWithEmailAndPassword("a@a.com", "123456").addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                DatabaseReference myRef = FirebaseDatabase.getInstance(app).getReference();
                myRef.child("numeros").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        RequestQueue queue = Volley.newRequestQueue(OtpActivity.this);
                        Log.d("DataChange", "Dentro del datachange");
                        String url2 = "https://appaym-537c4-default-rtdb.europe-west1.firebasedatabase.app/numeros.json?auth="+authBD2;
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                                (Request.Method.GET, url2, null, new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        try {
                                            telAppM = response.getString("num");
                                            otpAppM = response.getString("msg");
                                            textFinal.setText("El numero "+telAppM+" ha enviado: "+otpAppM);
                                            textFinal.setVisibility(View.VISIBLE);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.d("OTPError", "error network: "+error.networkResponse);
                                        Log.d("OTPError", "error string: "+error.toString());
                                    }
                                });
                        queue.add(jsonObjectRequest);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.w("Error", "SE ESTAN BORRANDO DATOS");
                    }
                });
            }
        });
    }

    public void onContinuar(View v) {
        //Comprobamos que el campo pasado no sea nulo
        if (!(cOTP.getText().toString().equals(""))&& (textFinal.getText()!=null)) {
            pBar.setVisibility(View.VISIBLE);
            clave = Integer.parseInt(String.valueOf(cOTP.getText()));
            //FIX#01 Guardar el num tel en SharedPreferences para obtenerlo despues en la invocación posterior.
            SharedPreferences sharedPref = getSharedPreferences("guardartel", MODE_PRIVATE);
            //String tel = sharedPref.getString("tel", "No ha llegado");
            Gson gson = new Gson();
            String json = sharedPref.getString("datos", "No ha llegado");
            Datos dat = gson.fromJson(json, Datos.class);
            String tel = dat.getTelefono();
            dat.setClave(String.valueOf(cOTP.getText())); //añadimos el OTP al modelo para pasarlo a la sig activity
            //Toast.makeText(this, "tel shared es: " + tel, Toast.LENGTH_LONG).show();
            //
            //Obtener token de Auth
            String url = "https://www.googleapis.com/identitytoolkit/v3/relyingparty/verifyPassword?key=AIzaSyCO0wQa_fia6ojLkFCzLG-sft5XUWF2Skw";
            Log.d("Test", "Aqui llego");
            // Request a string response from the provided URL.
            RequestQueue requestQueue = Volley.newRequestQueue(OtpActivity.this);
            JSONObject postData = new JSONObject();
            try {
                postData.put("email", "a@a.com");
                postData.put("password", "123456");
                postData.put("returnSecureToken", true);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, postData, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        auth = response.getString("idToken");
                        dat.setAuth(auth);      //añadimos el auth al modelo para pasarlo a la sig activity
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });
            requestQueue.add(jsonObjectRequest);
            //
            //Codigo correspondiente al envio por API Rest al Servidor para comprobar la clave OTP.
            autenMain.signInWithEmailAndPassword("a@a.com", "123456").addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        String url;
                        if(urlBD == null) {
                            url = "https://smsretrieverservera-default-rtdb.europe-west1.firebasedatabase.app/numeros.json?auth=" + auth;
                        }else {
                            url = urlBD;
                        }                        // Request a string response from the provided URL.
                        RequestQueue requestQueue = Volley.newRequestQueue(OtpActivity.this);
                        JSONObject newData = new JSONObject();
                        try {
                            newData.put("hash", null);
                            newData.put("tel", tel);
                            newData.put("otp", clave);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        JsonObjectRequest jsonObjectRequest2 = new JsonObjectRequest(Request.Method.PUT, url, newData, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d("FIN", "Pasamos a la siguiente actividad");
                                Intent verifIntent = new Intent(OtpActivity.this, verificadoActivity.class); //Mover de la Clase B a la C
                                verifIntent.putExtra("datos", dat);//Mandamos el token de auth para API REST.
                                verifIntent.putExtra("authBD2", authBD2);
                                startActivity(verifIntent);
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(getApplicationContext(), "Error URL no existente o sin permisos", Toast.LENGTH_SHORT).show();
                            }
                        });
                        requestQueue.add(jsonObjectRequest2);
                    } else {
                        Toast.makeText(getApplicationContext(), "Error de auth", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            // Instantiate the RequestQueue.

            //Código necesario para obtener el codigo hash de la app
            //AppSignatureHelper appSignatureHelper = new AppSignatureHelper(this);
            //Log.d(TAG,"El código hash de la app es: "+appSignatureHelper.getAppSignatures().get(0));
            //Codigo HASH de la app es: F6oWIQlQ+AE
        } else
            Toast.makeText(this, "Es necesario recibir un código OTP primero", Toast.LENGTH_LONG).show();

    }

}