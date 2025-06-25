package com.example.cuidapoa.view.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.cuidapoa.R;

public class HomeFragment extends Fragment {

    private CardView cardVacinas;
    private CardView cardBuscaUBS;
    private CardView cardSuporte;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar views
        cardVacinas = view.findViewById(R.id.card_vacinas);
        cardBuscaUBS = view.findViewById(R.id.card_busca_ubs);
        cardSuporte = view.findViewById(R.id.card_suporte);

        // Configurar cliques
        cardVacinas.setOnClickListener(v -> {
            // Navegar para tela de vacinas
            Navigation.findNavController(v).navigate(R.id.nav_vacinas);
        });

        cardBuscaUBS.setOnClickListener(v -> {
            // Navegar para tela de busca UBS
            Navigation.findNavController(v).navigate(R.id.nav_ubs);
        });

        cardSuporte.setOnClickListener(v -> {
            // Navegar para tela de suporte
            Navigation.findNavController(v).navigate(R.id.nav_suporte);
        });
    }
}