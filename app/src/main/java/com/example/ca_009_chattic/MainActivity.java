package com.example.ca_009_chattic;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.view.View.OnClickListener;

import java.net.DatagramSocket;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private Button enviar,salir;
    private EditText usuario,mensaje, salida;
    private DatagramSocket escucha;
    private Thread hilo;
    private String recibido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        salir = (Button) findViewById(R.id.btnSalir);
        enviar = (Button) findViewById(R.id.btnEnviar);
        usuario = (EditText) findViewById(R.id.txtNombre);
        mensaje = (EditText) findViewById(R.id.txtMensaje);
        salida = (EditText) findViewById(R.id.txtSalida);

        hilo = new Thread(new Runnable() {
            @Override
            public void run() {
                try
                {
                    //Crear el socket en un puerto
                    escucha = new DatagramSocket(5555);
                    byte[] datos = new byte[1024];
                    DatagramPacket paquete = new DatagramPacket(datos,datos.length);
                    while(true)
                    {
                        //Recibir mensajes por el puerto 5555
                        escucha.receive(paquete);
                        byte[]informacion=paquete.getData();
                        recibido = new String(informacion);
                        recibido = recibido.substring(0,paquete.getLength());
                        //Hilos
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String cadena = salida.getText().toString() + "\n" + recibido;
                                salida.setText(cadena);
                                salida.setSelection(salida.getText().toString().length());
                            }
                        });
                    }
                }
                catch (Exception e)
                {
                    Log.e("Error Socket",e.toString());
                }
            }
        });

        try
        {
            hilo.setDaemon(true);
            hilo.start();
        }
        catch (Exception e)
        {
            Log.e("Error Hilo",e.toString());
        }

        enviar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try
                {
                    Thread hiloenviar = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try
                            {
                                DatagramSocket envia = new DatagramSocket();
                                envia.setBroadcast(true);
                                byte[] dato;
                                dato = (usuario.getText().toString()+": "+mensaje.getText().toString()).getBytes();
                                DatagramPacket paquete=  new DatagramPacket(dato,dato.length,InetAddress.getByName("255.255.255.255"),5555);
                                envia.send(paquete);
                                Log.i("Paquete","Mensaje enviado");
                                envia.close();
                            }
                            catch (Exception e)
                            {
                                Log.e("Error hilo enviar",e.toString());
                            }
                        }
                    });
                    hiloenviar.setDaemon(true);
                    hiloenviar.start();
                }
                catch (Exception e)
                {
                    Log.e("Error",e.toString());
                }
            }
        });

        salir.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void onDestroy()
    {
        hilo.interrupt();
        if(escucha!=null)
        {
            if(!escucha.isClosed())
            {
                escucha.close();
            }
        }
        super.onDestroy();
    }
}
