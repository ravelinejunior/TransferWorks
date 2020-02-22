package br.com.transferwork.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ConfiguracaoFirebase {
    private static DatabaseReference databaseReference;
    private static FirebaseAuth firebaseAuth;

    //retorna uma instancia do firebase
    public static DatabaseReference getDatabaseReference(){
        if (databaseReference == null){
            //puxar dados do no inicial
            databaseReference = FirebaseDatabase.getInstance().getReference();
        }
        return databaseReference;
    }

    //retorna um objeto autenticador
    public static FirebaseAuth getFirebaseAuth(){
        if (firebaseAuth == null){
            firebaseAuth = FirebaseAuth.getInstance();
        }
        return firebaseAuth;
    }
}
