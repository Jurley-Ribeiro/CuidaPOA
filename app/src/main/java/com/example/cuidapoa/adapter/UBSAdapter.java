package com.example.cuidapoa.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cuidapoa.R;
import com.example.cuidapoa.model.UBS;
import com.google.android.material.chip.Chip;
import java.util.ArrayList;
import java.util.List;

public class UBSAdapter extends RecyclerView.Adapter<UBSAdapter.UBSViewHolder> {

    private Context context;
    private List<UBS> ubsList;
    private List<UBS> ubsListFiltrada;
    private OnUBSClickListener listener;

    public interface OnUBSClickListener {
        void onUBSClick(UBS ubs);
        void onTelefoneClick(UBS ubs);
        void onEnderecoClick(UBS ubs);
    }

    public UBSAdapter(Context context) {
        this.context = context;
        this.ubsList = new ArrayList<>();
        this.ubsListFiltrada = new ArrayList<>();
    }

    public void setOnUBSClickListener(OnUBSClickListener listener) {
        this.listener = listener;
    }

    public void setUBSList(List<UBS> ubsList) {
        this.ubsList = ubsList;
        this.ubsListFiltrada = new ArrayList<>(ubsList);
        notifyDataSetChanged();
    }

    public void filtrar(String texto) {
        ubsListFiltrada.clear();

        if (texto.isEmpty()) {
            ubsListFiltrada.addAll(ubsList);
        } else {
            String textoLower = texto.toLowerCase();
            for (UBS ubs : ubsList) {
                if (ubs.getNome().toLowerCase().contains(textoLower) ||
                        ubs.getBairro().toLowerCase().contains(textoLower) ||
                        ubs.getEndereco().toLowerCase().contains(textoLower)) {
                    ubsListFiltrada.add(ubs);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UBSViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ubs, parent, false);
        return new UBSViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UBSViewHolder holder, int position) {
        UBS ubs = ubsListFiltrada.get(position);
        holder.bind(ubs);
    }

    @Override
    public int getItemCount() {
        return ubsListFiltrada.size();
    }

    class UBSViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView tvNome;
        private TextView tvEndereco;
        private TextView tvHorario;
        private TextView tvTelefone;
        private Chip chipStatus;
        private ImageButton btnTelefone;
        private ImageButton btnMapa;

        public UBSViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_ubs);
            tvNome = itemView.findViewById(R.id.tv_nome_ubs);
            tvEndereco = itemView.findViewById(R.id.tv_endereco_ubs);
            tvHorario = itemView.findViewById(R.id.tv_horario);
            tvTelefone = itemView.findViewById(R.id.tv_telefone);
            chipStatus = itemView.findViewById(R.id.chip_status);
            btnTelefone = itemView.findViewById(R.id.btn_telefone);
            btnMapa = itemView.findViewById(R.id.btn_mapa);
        }

        public void bind(UBS ubs) {
            tvNome.setText(ubs.getNome());
            tvEndereco.setText(ubs.getEnderecoCompleto());
            tvHorario.setText(ubs.getHorarioFuncionamento());
            tvTelefone.setText(ubs.getTelefone());

            // Configurar status
            if (ubs.isAbertaAgora()) {
                chipStatus.setText("Aberta");
                chipStatus.setChipBackgroundColorResource(R.color.success);
            } else {
                chipStatus.setText("Fechada");
                chipStatus.setChipBackgroundColorResource(R.color.error);
            }

            // Configurar cliques
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onUBSClick(ubs);
                }
            });

            btnTelefone.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTelefoneClick(ubs);
                }
            });

            btnMapa.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEnderecoClick(ubs);
                }
            });
        }
    }
}