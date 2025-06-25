package com.example.cuidapoa.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cuidapoa.R;
import com.example.cuidapoa.model.Vacina;
import com.google.android.material.chip.Chip;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class VacinasAdapter extends RecyclerView.Adapter<VacinasAdapter.VacinaViewHolder> {

    private Context context;
    private List<Vacina> vacinas;
    private List<Vacina> vacinasFiltradas;
    private OnVacinaClickListener listener;
    private boolean isAdmin;

    public interface OnVacinaClickListener {
        void onVacinaClick(Vacina vacina);
        void onEditClick(Vacina vacina);
        void onDeleteClick(Vacina vacina);
    }

    public VacinasAdapter(Context context, boolean isAdmin) {
        this.context = context;
        this.isAdmin = isAdmin;
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

        // Manter ordenação alfabética após filtrar
        Collections.sort(vacinasFiltradas, new Comparator<Vacina>() {
            @Override
            public int compare(Vacina v1, Vacina v2) {
                return v1.getNome().compareToIgnoreCase(v2.getNome());
            }
        });

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
        private ImageButton btnMenu;

        public VacinaViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_vacina);
            tvNome = itemView.findViewById(R.id.tv_nome_vacina);
            tvDescricao = itemView.findViewById(R.id.tv_descricao_vacina);
            tvFaixaEtaria = itemView.findViewById(R.id.tv_faixa_etaria);
            tvDoses = itemView.findViewById(R.id.tv_doses);
            chipDisponibilidade = itemView.findViewById(R.id.chip_disponibilidade);
            ivVacina = itemView.findViewById(R.id.iv_vacina);
            btnMenu = itemView.findViewById(R.id.btn_menu_vacina);
        }

        public void bind(Vacina vacina) {
            tvNome.setText(vacina.getNome());
            tvDescricao.setText(vacina.getDescricao());
            tvFaixaEtaria.setText("Faixa etária: " + vacina.getFaixaEtaria());

            // Tratar doses especiais
            String dosesTexto;
            if (vacina.getNumeroDoses() == -1) {
                dosesTexto = "Doses: Anual";
            } else if (vacina.getNumeroDoses() == 1) {
                dosesTexto = "Dose única";
            } else {
                dosesTexto = "Doses: " + vacina.getNumeroDoses();
            }
            tvDoses.setText(dosesTexto);

            // Configurar chip de disponibilidade
            if (vacina.isDisponivel()) {
                chipDisponibilidade.setText(context.getString(R.string.vacinas_disponivel));
                chipDisponibilidade.setChipBackgroundColorResource(R.color.success);
            } else {
                chipDisponibilidade.setText(context.getString(R.string.vacinas_indisponivel));
                chipDisponibilidade.setChipBackgroundColorResource(R.color.error);
            }

            // Configurar clique no card
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onVacinaClick(vacina);
                }
            });

            // Mostrar menu apenas para admin
            if (isAdmin) {
                btnMenu.setVisibility(View.VISIBLE);
                btnMenu.setOnClickListener(v -> mostrarMenuOpcoes(v, vacina));
            } else {
                btnMenu.setVisibility(View.GONE);
            }
        }

        private void mostrarMenuOpcoes(View view, Vacina vacina) {
            PopupMenu popup = new PopupMenu(context, view);
            popup.inflate(R.menu.menu_item_vacina);

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_edit) {
                    if (listener != null) {
                        listener.onEditClick(vacina);
                    }
                    return true;
                } else if (id == R.id.action_delete) {
                    if (listener != null) {
                        listener.onDeleteClick(vacina);
                    }
                    return true;
                }
                return false;
            });

            popup.show();
        }
    }
}