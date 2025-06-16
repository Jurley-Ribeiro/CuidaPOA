package com.example.cuidapoa.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cuidapoa.R;
import com.example.cuidapoa.model.Vacina;
import com.google.android.material.chip.Chip;
import java.util.ArrayList;
import java.util.List;

public class VacinasAdapter extends RecyclerView.Adapter<VacinasAdapter.VacinaViewHolder> {

    private Context context;
    private List<Vacina> vacinas;
    private List<Vacina> vacinasFiltradas;
    private OnVacinaClickListener listener;

    public interface OnVacinaClickListener {
        void onVacinaClick(Vacina vacina);
    }

    public VacinasAdapter(Context context) {
        this.context = context;
        this.vacinas = new ArrayList<>();
        this.vacinasFiltradas = new ArrayList<>();
    }

    public void setOnVacinaClickListener(OnVacinaClickListener listener) {
        this.listener = listener;
    }

    public void setVacinas(List<Vacina> vacinas) {
        this.vacinas = vacinas;
        this.vacinasFiltradas = new ArrayList<>(vacinas);
        notifyDataSetChanged();
    }

    public void filtrar(String texto) {
        vacinasFiltradas.clear();

        if (texto.isEmpty()) {
            vacinasFiltradas.addAll(vacinas);
        } else {
            String textoLower = texto.toLowerCase();
            for (Vacina vacina : vacinas) {
                if (vacina.getNome().toLowerCase().contains(textoLower) ||
                        vacina.getDescricao().toLowerCase().contains(textoLower)) {
                    vacinasFiltradas.add(vacina);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VacinaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_vacina, parent, false);
        return new VacinaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VacinaViewHolder holder, int position) {
        Vacina vacina = vacinasFiltradas.get(position);
        holder.bind(vacina);
    }

    @Override
    public int getItemCount() {
        return vacinasFiltradas.size();
    }

    class VacinaViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView tvNome;
        private TextView tvDescricao;
        private TextView tvFaixaEtaria;
        private TextView tvDoses;
        private Chip chipDisponibilidade;
        private ImageView ivVacina;

        public VacinaViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_vacina);
            tvNome = itemView.findViewById(R.id.tv_nome_vacina);
            tvDescricao = itemView.findViewById(R.id.tv_descricao_vacina);
            tvFaixaEtaria = itemView.findViewById(R.id.tv_faixa_etaria);
            tvDoses = itemView.findViewById(R.id.tv_doses);
            chipDisponibilidade = itemView.findViewById(R.id.chip_disponibilidade);
            ivVacina = itemView.findViewById(R.id.iv_vacina);
        }

        public void bind(Vacina vacina) {
            tvNome.setText(vacina.getNome());
            tvDescricao.setText(vacina.getDescricao());
            tvFaixaEtaria.setText("Faixa etária: " + vacina.getFaixaEtaria());
            tvDoses.setText("Doses: " + vacina.getNumeroDoses());

            // Configurar chip de disponibilidade
            if (vacina.isDisponivel()) {
                chipDisponibilidade.setText(context.getString(R.string.vacinas_disponivel));
                chipDisponibilidade.setChipBackgroundColorResource(R.color.success);
            } else {
                chipDisponibilidade.setText(context.getString(R.string.vacinas_indisponivel));
                chipDisponibilidade.setChipBackgroundColorResource(R.color.error);
            }

            // Configurar clique
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onVacinaClick(vacina);
                }
            });
        }
    }
}
