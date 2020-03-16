package br.com.transferwork.activities.usuario;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import br.com.transferwork.R;
import br.com.transferwork.config.ConfiguracaoFirebase;
import br.com.transferwork.helper.UsuarioFirebase;
import br.com.transferwork.model.Requisicao;

public class RequisicoesMotoristaActivity extends AppCompatActivity {

    //Firebase
    private FirebaseAuth auth;
    private DatabaseReference databaseRef;
    private List<Requisicao> requisicaoList = new ArrayList<>();

    //layout
    private RecyclerView recyclerViewRequisicoesMotorista;
    private TextView textViewResultoRequisicaoMotorista;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requisicoes_motorista);
        carregarElementos();





    }

    public void carregarElementos(){
        //configurações iniciais
        auth = ConfiguracaoFirebase.getFirebaseAuth();
        databaseRef = ConfiguracaoFirebase.getDatabaseReference();
        getSupportActionBar().setTitle("Requisições");

        recyclerViewRequisicoesMotorista = findViewById(R.id.recyclerView_requisicoesMotorista);
        textViewResultoRequisicaoMotorista = findViewById(R.id.mensagem_Requisicao_motorista);

        recuperarRequisicoes();


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

                    //recuperar listagem de itens
                    for (DataSnapshot ds: dataSnapshot.getChildren()){
                        Requisicao requisicao = ds.getValue(Requisicao.class);
                        requisicaoList.add(requisicao);

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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
            }
        });
        builder.setNegativeButton(R.string.cancelar_message, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
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
                finish();
                break;

        }
        return super.onOptionsItemSelected(item);
    }
}
