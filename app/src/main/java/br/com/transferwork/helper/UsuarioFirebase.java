package br.com.transferwork.helper;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import br.com.transferwork.activities.usuario.PassageiroActivity;
import br.com.transferwork.activities.usuario.RequisicoesMotoristaActivity;
import br.com.transferwork.config.ConfiguracaoFirebase;
import br.com.transferwork.model.Usuario;

public class UsuarioFirebase {

    public static FirebaseUser getUsuarioAtual() {
        FirebaseUser firebaseUser = ConfiguracaoFirebase.getFirebaseAuth().getCurrentUser();
        return firebaseUser;
    }

    public static Usuario getDadosUsuarioLogado(){
        FirebaseUser firebaseUser = getUsuarioAtual();

        Usuario usuario = new Usuario();
        usuario.setId(firebaseUser.getUid());
        usuario.setEmailUsuario(firebaseUser.getEmail());
        usuario.setNomeUsuario(firebaseUser.getDisplayName());

        return usuario;
    }

    public static boolean atualizarNomeUsuario(String nome) {
        FirebaseUser user = getUsuarioAtual();
        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(nome).build();
        user.updateProfile(profileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) {
                    Log.d("Perfil", "Erro ao atualizar nome usuario");
                } else {
                    Log.d("Perfil", "Sucesso ao atualizar nome usuario");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
                e.getMessage();
            }
        });

        return true;

    }

    public static void redirecionaTipoUsuario(final Activity a) {
        FirebaseUser user = getUsuarioAtual();
        if (user != null) {
            DatabaseReference reference = ConfiguracaoFirebase.getDatabaseReference()
                    .child("usuarios")
                    .child(getIdentificadorUsuario());

            //utilizar um listener para verificar se há algum usuario sendo pesquisado pelo banco
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //pesquisar usuario por nós
                    Usuario usuario = dataSnapshot.getValue(Usuario.class);

                    //acessar tipo usuario
                    String tipoUsuario = usuario.getTipoUsuario();
                    if (tipoUsuario.equalsIgnoreCase("P")) {

                        a.startActivity(new Intent(a, PassageiroActivity.class));

                    } else {

                        a.startActivity(new Intent(a, RequisicoesMotoristaActivity.class));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
    }

    public static String getIdentificadorUsuario() {
        return getUsuarioAtual().getUid();
    }

}
