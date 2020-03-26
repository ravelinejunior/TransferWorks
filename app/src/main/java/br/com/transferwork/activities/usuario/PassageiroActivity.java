package br.com.transferwork.activities.usuario;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import br.com.transferwork.R;
import br.com.transferwork.activities.main.MainActivity;
import br.com.transferwork.config.ConfiguracaoFirebase;
import br.com.transferwork.helper.UsuarioFirebase;
import br.com.transferwork.model.DestinoDigitado;
import br.com.transferwork.model.Requisicao;
import br.com.transferwork.model.Usuario;

public class PassageiroActivity extends AppCompatActivity implements OnMapReadyCallback {

    //Firebase
    private FirebaseAuth auth;
    private DatabaseReference referenceFirebase;
    private FirebaseUser firebaseUser;
    private Requisicao requisicao;

    //maps
    private LocationManager locationManager;
    private LocationListener locationListener;
    private GoogleMap gMap;
    private LatLng meuLocalPassageiro;

    private EditText digitarLocalDestinoPassageiro;
    private LinearLayout linearLayoutPassageiro;
    private Button botaoChamarCarroPassageiro;
    private boolean carroChamado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passageiro);

        //configurações iniciais Firebase
        referenceFirebase = ConfiguracaoFirebase.getDatabaseReference();
        firebaseUser = UsuarioFirebase.getUsuarioAtual();

        carregarComponentes();

        //ADICIONAR UM LISTENER PARA STATUS DA REQUISIÇÃO
        verificarStatusRequsisicao();

    }

    private void verificarStatusRequsisicao(){

        //criar nó para pegar requisição atual
        //recuperar todos os dados do usuario logado
        Usuario usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();
        DatabaseReference requisicoes = referenceFirebase.child("requisicoes");


       /* String idRequsicao = String.valueOf(requisicoes.getPath());
        DatabaseReference requisicaoAtual = requisicoes.child("requsicao_atual").
                                                child(usarioLogado.getId()).
                                                child(idRequsicao);
        Log.d("requisicaoAtual", "verificarStatusRequsisicao: "+requisicaoAtual.toString());
*/

        //metodo mais direto, recuperando e ordenando por id
        Query requisicaoPesquisa = requisicoes.orderByChild("passageiro/id")
                .equalTo(usuarioLogado.getId());

        //criar um listener para ouvir as requisições
        requisicaoPesquisa.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //criar uma lista para recuperar as requisições existentes
                List<Requisicao> requisicaoList = new ArrayList<>();

                //percorrendo os dados apenas uma vez para exibir o ultimo
                for (DataSnapshot dataSnapshot1: dataSnapshot.getChildren()){
                    requisicao = dataSnapshot1.getValue(Requisicao.class);
                    requisicaoList.add(requisicao);
                }

                //invertendo a lista e recuperando a primeira posição
                Collections.reverse(requisicaoList);

                if (requisicaoList.size() > 0 && requisicaoList != null){
                    requisicao = requisicaoList.get(0);
                    Log.d("resultado: ","valores obtidos: "+requisicao.toString());

                    switch (requisicao.getStatus()){
                        case  Requisicao.STATUS_AGUARDANDO:
                            Log.d("statusReq ",requisicao.getStatus());
                            linearLayoutPassageiro.setVisibility(View.INVISIBLE);
                            botaoChamarCarroPassageiro.setText(R.string.cancelar_corrida);
                            carroChamado = true;
                            break;
                    }
                }else{
                    Toast.makeText(PassageiroActivity.this, "Lista de requisições vazia.", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


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
                        .snippet("Local que " + Objects.requireNonNull(auth.getCurrentUser()).getDisplayName() + " está.")
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

        //verificar se carro ja foi chamado
        if (!carroChamado){
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
                    mensagem.append("Cidade: ").append(destinoDigitado.getCidade());
                    mensagem.append("\nCep: ").append(destinoDigitado.getCep());
                    mensagem.append("\nBairro: ").append(destinoDigitado.getBairro());
                    mensagem.append("\nRua: ").append(destinoDigitado.getRua());
                    mensagem.append("\nNúmero: ").append(destinoDigitado.getNumero());
                    mensagem.append("\nLatitude: ").append(destinoDigitado.getLatitude());
                    mensagem.append("\nLongitude: ").append(destinoDigitado.getLongitude());


                    AlertDialog.Builder builder = new AlertDialog.Builder(PassageiroActivity.this);
                    builder.setTitle(R.string.confirmar_endereco_title);
                    builder.setMessage(mensagem);
                    builder.setIcon(android.R.drawable.ic_dialog_map);
                    builder.setPositiveButton(R.string.confirmar_destino_positive_option, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //salvar requisição
                            salvarRequisicao(destinoDigitado,v);
                            linearLayoutPassageiro .setVisibility(View.GONE);
                            botaoChamarCarroPassageiro.setText(R.string.cancelar_corrida);

                            //carro foi chamado com sucesso
                            carroChamado = true;
                            digitarLocalDestinoPassageiro.clearFocus();

                        }
                    }).setNegativeButton(R.string.cancelar_option, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getApplicationContext(),"Cancelado.",Toast.LENGTH_SHORT).show();
                            dialog.cancel();
                            digitarLocalDestinoPassageiro.clearFocus();
                        }
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();

                    //carro foi chamado com sucesso
                    carroChamado = true;


                }

            }
        }else{
        Snackbar.make(v,"Ainda não há corrida iniciada.",Snackbar.LENGTH_SHORT).show();
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
        Objects.requireNonNull(mapFragment).getMapAsync(this);

        digitarLocalDestinoPassageiro = findViewById(R.id.passageiro_localDestino_id);
        //componentes
        EditText digitarMeuLocalPassageiro = findViewById(R.id.passageiro_meuLocal_id);
        botaoChamarCarroPassageiro = findViewById(R.id.botao_chamarCarro_id_Passageiro);
        linearLayoutPassageiro = findViewById(R.id.linearLayout_passageiro);



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

    @Override
    public void onBackPressed() {
        deslogarBackPressed();

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
                Intent intent = new Intent(getContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
        builder.setNegativeButton(R.string.cancelar_message, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                gMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                dialogInterface.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    private Context getContext() {
        return PassageiroActivity.this;
    }

}
