package br.com.transferwork.activities.usuario;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import br.com.transferwork.R;
import br.com.transferwork.config.ConfiguracaoFirebase;
import br.com.transferwork.helper.UsuarioFirebase;
import br.com.transferwork.model.DestinoDigitado;
import br.com.transferwork.model.Requisicao;
import br.com.transferwork.model.Usuario;

public class PassageiroActivity extends AppCompatActivity implements OnMapReadyCallback {

    //Firebase
    private FirebaseAuth auth;

    //maps
    private LocationManager locationManager;
    private LocationListener locationListener;
    private GoogleMap gMap;
    private LatLng meuLocalPassageiro;

    //componentes
    private EditText digitarMeuLocalPassageiro;
    private EditText digitarLocalDestinoPassageiro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passageiro);

        carregarComponentes();

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;

        recuperarLocalizacaoUsuario();



    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void recuperarLocalizacaoUsuario() {


        //configurar o location manager para recuperar os serviços de gps
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //recuperar lat e long
                double latitudeUsuario = location.getLatitude();
                double longitudeUsuario = location.getLongitude();
                meuLocalPassageiro = new LatLng(latitudeUsuario, longitudeUsuario);

                gMap.clear();
                //criar um marcador
                gMap.addMarker(new MarkerOptions()
                        .position(meuLocalPassageiro)
                        .title("Meu local")
                        .snippet("Local que " + auth.getCurrentUser().getDisplayName() + " está.")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.usuario))
                );

                //criar um zoom inicial
                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(meuLocalPassageiro, 18));

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        //Solicitar atualizações de localização do usuario
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 3, locationListener);


        }

    }

    public void chamarCarro(final View v){
        String enderecoDestinoDigitado = digitarLocalDestinoPassageiro.getText().toString();
        if (enderecoDestinoDigitado.isEmpty() || enderecoDestinoDigitado == null){
            Snackbar.make(v,"Digite um endereço de destino sr(a) "+auth.getCurrentUser().getDisplayName(),Snackbar.LENGTH_SHORT).show();
        }else{

            Address addressDestinoDigitado = recuperarEnderecoDigitado(enderecoDestinoDigitado);

            //verificar se endereço digitado foi encontrado
            if (addressDestinoDigitado != null){
                //criar model de destino
                final DestinoDigitado destinoDigitado = new DestinoDigitado();
                //recupera a cidade
                destinoDigitado.setCidade(addressDestinoDigitado.getSubAdminArea());
                destinoDigitado.setCep(addressDestinoDigitado.getPostalCode());
                destinoDigitado.setBairro(addressDestinoDigitado.getSubLocality());
                destinoDigitado.setRua(addressDestinoDigitado.getThoroughfare());
                destinoDigitado.setNumero(addressDestinoDigitado.getFeatureName());
                destinoDigitado.setLatitude(String.valueOf(addressDestinoDigitado.getLatitude()));
                destinoDigitado.setLongitude(String.valueOf(addressDestinoDigitado.getLongitude()));

                StringBuilder mensagem = new StringBuilder();
                //stringbuilder cria uma string com varias linhas
                mensagem.append("Cidade: "+destinoDigitado.getCidade());
                mensagem.append("\nCep: "+destinoDigitado.getCep());
                mensagem.append("\nBairro: "+destinoDigitado.getBairro());
                mensagem.append("\nRua: "+destinoDigitado.getRua());
                mensagem.append("\nNúmero: "+destinoDigitado.getNumero());
                mensagem.append("\nLatitude: "+destinoDigitado.getLatitude());
                mensagem.append("\nLongitude: "+destinoDigitado.getLongitude());


                AlertDialog.Builder builder = new AlertDialog.Builder(PassageiroActivity.this);
                builder.setTitle("Confirme o endereço de destino.");
                builder.setMessage(mensagem);
                builder.setIcon(android.R.drawable.ic_dialog_map);
                builder.setPositiveButton("Confirmar destino.", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //salvar requisição
                        salvarRequisicao(destinoDigitado,v);
                        digitarLocalDestinoPassageiro.setText("");
                        digitarLocalDestinoPassageiro.clearFocus();

                    }
                }).setNegativeButton("Cancelar.", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(),"Cancelado.",Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                        digitarLocalDestinoPassageiro.clearFocus();
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();


            }

        }


    }

    private Address recuperarEnderecoDigitado(String enderecoDestinoDigitado) {

        //recupera os dados baseado nos dados do usuario
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        //recuperar uma lista de endereços
        try {
            List<Address> listaEnderecos = geocoder.getFromLocationName(enderecoDestinoDigitado,1);
            if (listaEnderecos != null && listaEnderecos.size() > 0){
                Address addressBuscados = listaEnderecos.get(0);

                double lat = addressBuscados.getLatitude();
                double longi = addressBuscados.getLongitude();

                return addressBuscados;
            }
        } catch (IOException e) {
            Toast.makeText(this, "Erro ao recuperar endereço!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        //caso nao retorne nada.
        return null;

    }

    public void salvarRequisicao(DestinoDigitado destinoDigitado,View view){
        Requisicao requisicao = new Requisicao();
        requisicao.setDestinoDigitado(destinoDigitado);

        //configurações de passageiro
        Usuario usuarioLogadoPassageiro = UsuarioFirebase.getDadosUsuarioLogado();
        usuarioLogadoPassageiro.setLatitude(String.valueOf(meuLocalPassageiro.latitude));
        usuarioLogadoPassageiro.setLongitude(String.valueOf(meuLocalPassageiro.longitude));

        requisicao.setPassageiro(usuarioLogadoPassageiro);
        requisicao.setStatus(Requisicao.STATUS_AGUARDANDO);
        requisicao.salvar(view);


    }


    public void carregarComponentes(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Iniciar viagem");
        toolbar.setLogo(android.R.drawable.ic_menu_mapmode);
        toolbar.setPadding(20,10,10,10);
        setSupportActionBar(toolbar);
        //config init
        auth = ConfiguracaoFirebase.getFirebaseAuth();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        digitarLocalDestinoPassageiro = findViewById(R.id.passageiro_localDestino_id);
        digitarMeuLocalPassageiro = findViewById(R.id.passageiro_meuLocal_id);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar_passageiro, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_sair:
                auth.signOut();
                finish();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

}
