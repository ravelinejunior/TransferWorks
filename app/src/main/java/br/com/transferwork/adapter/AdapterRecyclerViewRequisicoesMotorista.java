package br.com.transferwork.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import br.com.transferwork.R;
import br.com.transferwork.model.Requisicao;
import br.com.transferwork.model.Usuario;

public class AdapterRecyclerViewRequisicoesMotorista extends RecyclerView.Adapter<AdapterRecyclerViewRequisicoesMotorista.MyViewHolder> {
   private List<Requisicao> requisicaoList;
   private Context c;
   private Usuario usuarioMotorista;

    public AdapterRecyclerViewRequisicoesMotorista(List<Requisicao> requisicaoList, Context context, Usuario usuarioMotorista) {
        this.requisicaoList = requisicaoList;
        this.c = context;
        this.usuarioMotorista = usuarioMotorista;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(c).inflate(R.layout.adapter_requisicoes_motorista,parent,false);
        return new MyViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Requisicao requisicao = requisicaoList.get(position);
        Usuario usuarioPassageiro = requisicao.getPassageiro();

        holder.nome.setText(usuarioPassageiro.getNomeUsuario());
        //calcular distancia do passageiro e exibir a distancia entre eles
        holder.distancia.setText("5km - Aproximadamente");
    }

    @Override
    public int getItemCount() {
        return requisicaoList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView nome;
        TextView distancia;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            nome = itemView.findViewById(R.id.textView_requisicaoMotorista_nomePassageiro);
            distancia = itemView.findViewById(R.id.textView_requisicaoMotorista_distanciaPassageiro);
        }
    }

}
