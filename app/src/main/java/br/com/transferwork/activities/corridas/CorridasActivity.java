package br.com.transferwork.activities.corridas;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Objects;

import br.com.transferwork.R;
import br.com.transferwork.activities.usuario.LoginActivity;
import br.com.transferwork.activities.usuario.RequisicoesMotoristaActivity;
import br.com.transferwork.config.ConfiguracaoFirebase;
import br.com.transferwork.helper.UsuarioFirebase;
import br.com.transferwork.model.Requisicao;
import br.com.transferwork.model.Usuario;

public class CorridasActivity extends AppCompatActivity implements OnMapReadyCallback {
    //maps
    private LocationManager locationManager;
    private LocationListener locationListener;
    private LatLng localMotorista;
    private LatLng localPassageiro;
    private GoogleMap gMap;
    private Marker marcadorMotorista;
    private Marker marcadorPassageiro;

    //widgets
    private Button botaoAceitarCorrida;

    //firebase
    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference referenceFirebase;

    //usuarios
    private Usuario usuarioMotorista;
    private Usuario usuarioPassageiro;
    private String idRequisicao;
    private Requisicao requisicao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_corridas);

        user = UsuarioFirebase.getUsuarioAtual();
        referenceFirebase = ConfiguracaoFirebase.getDatabaseReference();
        carregarElementos();

        //recuperar dados do usuario
        if (getIntent().getExtras().isEmpty()){
            Log.e("DadosEnviados","Não existem dados nessa intent");
        }else if (getIntent().getExtras().containsKey("idRequisicao") && getIntent().getExtras().containsKey("motorista")){
            Bundle bundle = getIntent().getExtras();
            usuarioMotorista = (Usuario) bundle.getSerializable("motorista");
            idRequisicao = bundle.getString("idRequisicao");
            verificarStatusReq();
        }

    }

    private void verificarStatusReq() {
        DatabaseReference requisicoes = referenceFirebase.child("requisicoes").child(idRequisicao);
        requisicoes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //recuperar a requisição
                requisicao = dataSnapshot.getValue(Requisicao.class);

                //recuperar o passageiro e seu local
                usuarioPassageiro = requisicao.getPassageiro();
                localPassageiro = new LatLng(
                        Double.parseDouble(usuarioPassageiro.getLatitude()),
                        Double.parseDouble(usuarioMotorista.getLongitude())
                );

                switch (requisicao.getStatus()){
                    case Requisicao.STATUS_AGUARDANDO:
                        requisicaoAguardando();
                        break;
                    case Requisicao.STATUS_A_CAMINHO:
                        requisicaoAcaminho();
                        break;
                    case Requisicao.STATUS_FINALIZADO:
                        requisicaoFinalizada();
                        break;

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void requisicaoAguardando(){
botaoAceitarCorrida.setText(R.string.aceitar_corrida);
    }

    private  void requisicaoAcaminho(){
        botaoAceitarCorrida.setText(R.string.a_caminho_passageiro);

        //METODO PARA EXIBIR MARCADOR DO MOTORISTA
        adicionaMarcadorMotorista(localMotorista,usuarioMotorista.getNomeUsuario());
        //METODO PARA EXIBIR MARCADOR DO PASSAGEIRO
        adicionarMarkerPassageiro(localPassageiro,usuarioPassageiro.getNomeUsuario());
        //CENTRALIZAR OS MARCADORES
        centralizarMarcadores(marcadorMotorista,marcadorPassageiro);

    }

    private void centralizarMarcadores(Marker markerMotorista, Marker markerUsuario) {
//utilizar conceito de bounds(limites)
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        //DEFINIR QUAIS MARCADORES EXIBIR
        builder.include(markerMotorista.getPosition());
        builder.include(markerUsuario.getPosition());

        //cria os limites
        LatLngBounds bounds = builder.build();

        Integer boundLargura = getResources().getDisplayMetrics().widthPixels;
        Integer boundAltura = getResources().getDisplayMetrics().heightPixels;
        Double boundEspacamentoInterno = boundLargura*0.20;

        //parametros bounds,largura,altura,espaçamento interno
        gMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,boundLargura,boundAltura,boundEspacamentoInterno.intValue()));

    }

    private void adicionarMarkerPassageiro(LatLng localizacaoPassageiro, String tituloPassageiro) {

        if (marcadorPassageiro != null){
            marcadorPassageiro.remove();
        }

        marcadorPassageiro = gMap.addMarker(new MarkerOptions().
                position(localizacaoPassageiro).
                title(tituloPassageiro).
                snippet(tituloPassageiro+" está aqui.")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.usuario))
                );


    }

    private void adicionaMarcadorMotorista(LatLng localizacaoMotorista, String titulo) {

        //removendo o marcador toda vez que o usuario se locomover
        if (marcadorMotorista != null){
            marcadorMotorista.remove();
        }

        //criar um marcador
        marcadorMotorista = gMap.addMarker(new MarkerOptions()
                .position(localizacaoMotorista)
                .title(titulo)
                .snippet("Local que " + Objects.requireNonNull(auth.getCurrentUser()).getDisplayName() + " está.")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.carro))
        );

        //criar um zoom inicial

    }



    private  void requisicaoFinalizada(){

    }

    public void aceitarCorrida(View view) {

        //configurar requisição
        requisicao = new Requisicao();
        requisicao.setId(idRequisicao);
        requisicao.setMotorista(usuarioMotorista);
        requisicao.setStatus(Requisicao.STATUS_A_CAMINHO);

        //atualizar requisição
        requisicao.atualizarRequisicao();

    }

    //METODO RECUPERA REQUISIÇÃO APOS SER ACEITA


    public void carregarElementos(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.corrida);
        toolbar.setLogo(android.R.drawable.ic_menu_mapmode);
        toolbar.setPadding(20,0,10,0);
        toolbar.setLeft(10);

        auth = ConfiguracaoFirebase.getFirebaseAuth();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        Objects.requireNonNull(mapFragment).getMapAsync(this);

        botaoAceitarCorrida = findViewById(R.id.botao_aceitarCorrida_id);

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        recuperarLocalizacaoUsuario();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

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
                localMotorista = new LatLng(latitudeUsuario, longitudeUsuario);

                gMap.clear();
                //criar um marcador
                gMap.addMarker(new MarkerOptions()
                        .position(localMotorista)
                        .title("Meu local")
                        .snippet("Local que " + Objects.requireNonNull(auth.getCurrentUser()).getDisplayName() + " está.")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.carro))
                );

                //criar um zoom inicial
                gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(localMotorista, 18));

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

    private Context getContext(){
        return CorridasActivity.this;
    }

    private void deslogarBackPressed(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.deslogar_usuario);
        builder.setMessage(R.string.sair_app_message);
        builder.setIcon(android.R.drawable.ic_menu_info_details);
        builder.setPositiveButton(R.string.confirmar_message, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                auth = ConfiguracaoFirebase.getFirebaseAuth();
                auth.signOut();
                Intent intent = new Intent(getContext(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
        builder.setNegativeButton(R.string.cancelar_message, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

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
                deslogarBackPressed();
                break;

        }
        return super.onOptionsItemSelected(item);
    }
}
