package br.com.transferwork.activities.usuario;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import br.com.transferwork.R;
import br.com.transferwork.activities.corridas.CorridasActivity;
import br.com.transferwork.activities.main.MainActivity;
import br.com.transferwork.adapter.AdapterRecyclerViewRequisicoesMotorista;
import br.com.transferwork.config.ConfiguracaoFirebase;
import br.com.transferwork.helper.UsuarioFirebase;
import br.com.transferwork.listeners.RecyclerItemClickListener;
import br.com.transferwork.model.Requisicao;
import br.com.transferwork.model.Usuario;

public class RequisicoesMotoristaActivity extends AppCompatActivity {

    //Firebase
    private FirebaseAuth auth;
    private DatabaseReference databaseRef;
    private List<Requisicao> requisicaoList = new ArrayList<>();
    private Usuario usuarioMotorista;

    //layout
    private RecyclerView recyclerViewRequisicoesMotorista;
    private TextView textViewResultoRequisicaoMotorista;
    private AdapterRecyclerViewRequisicoesMotorista adapterRecyclerViewRequisicoesMotorista;

    //Localização GeoMapa
    private LocationManager locationManager;
    private LocationListener locationListener;
    private GoogleMap gMap;
    private LatLng meuLocalMotorista;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requisicoes_motorista);
        carregarElementos();
        recuperarLocalizacaoUsuario();

    }

    public void carregarElementos(){
        //configurações iniciais
        auth = ConfiguracaoFirebase.getFirebaseAuth();
        databaseRef = ConfiguracaoFirebase.getDatabaseReference();
        usuarioMotorista = UsuarioFirebase.getDadosUsuarioLogado();

        Objects.requireNonNull(getSupportActionBar()).setTitle("Requisições");

        recyclerViewRequisicoesMotorista = findViewById(R.id.recyclerView_requisicoesMotorista);
        textViewResultoRequisicaoMotorista = findViewById(R.id.mensagem_Requisicao_motorista);

        //configurar Adapter e recyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerViewRequisicoesMotorista.setLayoutManager(layoutManager);
        recyclerViewRequisicoesMotorista.setHasFixedSize(true);
        adapterRecyclerViewRequisicoesMotorista = new AdapterRecyclerViewRequisicoesMotorista(requisicaoList,getContext(),usuarioMotorista);
        recyclerViewRequisicoesMotorista.setAdapter(adapterRecyclerViewRequisicoesMotorista);

        //configurar evento de clique
        recyclerViewRequisicoesMotorista.addOnItemTouchListener(new RecyclerItemClickListener(
                getContext(), recyclerViewRequisicoesMotorista,
                new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                //recuperar requisição
                Requisicao requisicao = requisicaoList.get(position);
                Intent i = new Intent(getContext(), CorridasActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                i.putExtra("idRequisicao",requisicao.getId());
                i.putExtra("motorista",usuarioMotorista);
                startActivity(i);
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        }
        ));
        recuperarRequisicoes();

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void recuperarLocalizacaoUsuario() {

        //configurar o location manager para recuperar os serviços de gps
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //recuperar lat e long
                String latitudeMotorista = String.valueOf(location.getLatitude());
                String longitudeMotorista= String.valueOf(location.getLongitude());

                if (!(latitudeMotorista.isEmpty() && longitudeMotorista.isEmpty())){
                    usuarioMotorista.setLatitude(latitudeMotorista);
                    usuarioMotorista.setLongitude(longitudeMotorista);
                    //impedir que o listener continue recebendo informações de localização
                    locationManager.removeUpdates(locationListener);
                    adapterRecyclerViewRequisicoesMotorista.notifyDataSetChanged();

                }else{
                    Log.e("Motorista","Lat e long: "+latitudeMotorista + ", "+longitudeMotorista);
                }

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
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    0,
                    0,
                    locationListener);
        }

    }

    private void recuperarRequisicoes() {

        DatabaseReference requisicoes = databaseRef.child("requisicoes");

        if (requisicoes != null){
            //listar apenas as requisições que ainda não foram aceitas
            Query requisicaoPesquisa = requisicoes.orderByChild("status").equalTo(Requisicao.STATUS_AGUARDANDO);

            requisicaoPesquisa.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.i("itensDataSnapshot",dataSnapshot.getKey()+" ,Children "+dataSnapshot.getChildrenCount());

                    //verificar se há requisições sendo buscadas
                    if (dataSnapshot.getChildrenCount() > 0){
                        textViewResultoRequisicaoMotorista.setVisibility(View.GONE);
                        recyclerViewRequisicoesMotorista.setVisibility(View.VISIBLE);
                    }else{
                        recyclerViewRequisicoesMotorista.setVisibility(View.GONE);
                        textViewResultoRequisicaoMotorista.setVisibility(View.VISIBLE);
                    }

                    requisicaoList.clear();
                    //recuperar listagem de itens
                    for (DataSnapshot ds: dataSnapshot.getChildren()){
                        Requisicao requisicao = ds.getValue(Requisicao.class);
                        requisicaoList.add(requisicao);

                    }

                    adapterRecyclerViewRequisicoesMotorista.notifyDataSetChanged();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        deslogarBackPressed();
    }

    private Context getContext(){
        return RequisicoesMotoristaActivity.this;
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
