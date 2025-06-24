package com.example.cuidapoa.view.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cuidapoa.R;
import com.example.cuidapoa.adapter.VacinasAdapter;
import com.example.cuidapoa.model.Vacina;
import com.example.cuidapoa.view.CadastroVacinaActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.List;

public class VacinasFragment extends Fragment implements VacinasAdapter.OnVacinaClickListener {

    private RecyclerView recyclerView;
    private VacinasAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private FloatingActionButton fabAdd;

    // Simular estado de login
    private boolean isAdmin = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_vacinas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar views
        recyclerView = view.findViewById(R.id.recycler_vacinas);
        progressBar = view.findViewById(R.id.progress_bar);
        tvEmpty = view.findViewById(R.id.tv_empty);
        fabAdd = view.findViewById(R.id.fab_add_vacina);

        // Verificar se é admin (simulado - em produção viria do login)
        isAdmin = getActivity().getIntent().getBooleanExtra("is_admin", false);

        // Mostrar FAB apenas para admin
        if (isAdmin) {
            fabAdd.setVisibility(View.VISIBLE);
            fabAdd.setOnClickListener(v -> abrirCadastroVacina());
        }

        // Configurar RecyclerView
        configurarRecyclerView();

        // Carregar vacinas
        carregarVacinas();
    }

    private void configurarRecyclerView() {
        adapter = new VacinasAdapter(requireContext());
        adapter.setOnVacinaClickListener(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
    }

    private void carregarVacinas() {
        mostrarCarregando(true);

        // Simular carregamento com dados de exemplo
        recyclerView.postDelayed(() -> {
            List<Vacina> todasVacinas = new ArrayList<>();

            // Adicionar vacinas de exemplo
            todasVacinas.addAll(criarVacinasExemplo());

            // Adicionar vacinas cadastradas pelo admin
            if (CadastroVacinaActivity.vacinasCadastradas != null) {
                todasVacinas.addAll(CadastroVacinaActivity.vacinasCadastradas);
            }

            adapter.setVacinas(todasVacinas);
            mostrarCarregando(false);

            if (todasVacinas.isEmpty()) {
                mostrarListaVazia(true);
            }
        }, 1000);
    }

    private List<Vacina> criarVacinasExemplo() {
        List<Vacina> vacinas = new ArrayList<>();

        vacinas.add(new Vacina(
                "BCG",
                "Protege contra formas graves de tuberculose",
                "Ao nascer",
                1,
                true
        ));

        vacinas.add(new Vacina(
                "Hepatite B",
                "Previne a hepatite B",
                "Ao nascer, 2, 4 e 6 meses",
                4,
                true
        ));

        vacinas.add(new Vacina(
                "Pentavalente",
                "Protege contra difteria, tétano, coqueluche, hepatite B e infecções por Haemophilus influenzae B",
                "2, 4 e 6 meses",
                3,
                true
        ));

        vacinas.add(new Vacina(
                "VIP (Poliomielite)",
                "Previne a poliomielite (paralisia infantil)",
                "2, 4 e 6 meses",
                3,
                false
        ));

        vacinas.add(new Vacina(
                "Pneumocócica 10V",
                "Protege contra pneumonia, otite, meningite e outras doenças causadas pelo Pneumococo",
                "2, 4, 6 e 12 meses",
                4,
                true
        ));

        vacinas.add(new Vacina(
                "Rotavírus",
                "Previne diarreia por rotavírus",
                "2 e 4 meses",
                2,
                true
        ));

        vacinas.add(new Vacina(
                "Meningocócica C",
                "Protege contra a doença meningocócica C",
                "3, 5 e 12 meses",
                3,
                true
        ));

        vacinas.add(new Vacina(
                "Febre Amarela",
                "Protege contra a febre amarela",
                "9 meses e 4 anos",
                2,
                false
        ));

        vacinas.add(new Vacina(
                "Tríplice Viral",
                "Protege contra sarampo, caxumba e rubéola",
                "12 e 15 meses",
                2,
                true
        ));

        vacinas.add(new Vacina(
                "DTP",
                "Reforço contra difteria, tétano e coqueluche",
                "15 meses e 4 anos",
                2,
                true
        ));

        vacinas.add(new Vacina(
                "Varicela",
                "Protege contra catapora",
                "4 anos",
                1,
                true
        ));

        vacinas.add(new Vacina(
                "HPV",
                "Previne o câncer de colo do útero e outras doenças causadas pelo HPV",
                "9 a 14 anos",
                2,
                true
        ));

        vacinas.add(new Vacina(
                "COVID-19",
                "Protege contra o coronavírus SARS-CoV-2",
                "A partir de 6 meses",
                2,
                true
        ));

        return vacinas;
    }

    private void mostrarCarregando(boolean mostrar) {
        progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(mostrar ? View.GONE : View.VISIBLE);
    }

    private void mostrarListaVazia(boolean vazia) {
        tvEmpty.setVisibility(vazia ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(vazia ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_vacinas, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.vacinas_buscar));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filtrar(newText);
                return true;
            }
        });
    }

    @Override
    public void onVacinaClick(Vacina vacina) {
        String mensagem = vacina.getNome() + " - " +
                (vacina.isDisponivel() ? "Disponível" : "Indisponível");
        Snackbar.make(requireView(), mensagem, Snackbar.LENGTH_SHORT).show();
    }

    private void abrirCadastroVacina() {
        Intent intent = new Intent(getActivity(), CadastroVacinaActivity.class);
        startActivityForResult(intent, 100);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == getActivity().RESULT_OK) {
            // Recarregar lista após cadastro
            carregarVacinas();
        }
    }
}