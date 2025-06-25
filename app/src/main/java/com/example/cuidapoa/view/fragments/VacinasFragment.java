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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cuidapoa.MainActivity;
import com.example.cuidapoa.R;
import com.example.cuidapoa.adapter.VacinasAdapter;
import com.example.cuidapoa.model.Vacina;
import com.example.cuidapoa.repository.AuthRepository;
import com.example.cuidapoa.repository.VacinaRepository;
import com.example.cuidapoa.util.SwipeToDeleteCallback;
import com.example.cuidapoa.view.CadastroVacinaActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class VacinasFragment extends Fragment implements VacinasAdapter.OnVacinaClickListener {

    private RecyclerView recyclerView;
    private VacinasAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private FloatingActionButton fabAdd;

    // Firebase
    private VacinaRepository vacinaRepository;
    private AuthRepository authRepository;
    private boolean isAdmin = false;

    // Lista para controle local
    private List<Vacina> vacinasList = new ArrayList<>();

    // Para controle do "Desfazer"
    private Vacina vacinaRemovida;
    private int posicaoRemovida;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // Inicializar repositories
        vacinaRepository = VacinaRepository.getInstance();
        authRepository = AuthRepository.getInstance();
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

        // Verificar se é admin
        isAdmin = authRepository.isAdmin();

        // Alternativamente, pode pegar da MainActivity
        if (getActivity() instanceof MainActivity) {
            isAdmin = ((MainActivity) getActivity()).isAdmin();
        }

        // Mostrar FAB apenas para admin
        if (isAdmin) {
            fabAdd.setVisibility(View.VISIBLE);
            fabAdd.setOnClickListener(v -> abrirCadastroVacina());
        } else {
            fabAdd.setVisibility(View.GONE);
        }

        // Configurar RecyclerView
        configurarRecyclerView();

        // Carregar vacinas do Firebase
        carregarVacinas();
    }

    private void configurarRecyclerView() {
        adapter = new VacinasAdapter(requireContext(), isAdmin);
        adapter.setOnVacinaClickListener(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);

        // Adicionar swipe to delete apenas para admin
        if (isAdmin) {
            configurarSwipeToDelete();
        }
    }

    private void configurarSwipeToDelete() {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(requireContext()) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (position >= 0 && position < vacinasList.size()) {
                    Vacina vacina = vacinasList.get(position);
                    removerVacinaComDesfazer(vacina, position);
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void carregarVacinas() {
        mostrarCarregando(true);

        // Carregar vacinas do Firebase
        vacinaRepository.obterTodasVacinas(new VacinaRepository.OnVacinasLoadedListener() {
            @Override
            public void onVacinasLoaded(List<Vacina> vacinas) {
                if (!isAdded()) return; // Verificar se o fragment ainda está anexado

                vacinasList = new ArrayList<>(vacinas);

                // Se não houver vacinas no Firebase e for a primeira vez, adicionar exemplos
                if (vacinasList.isEmpty() && isAdmin) {
                    adicionarVacinasExemplo();
                } else {
                    // Ordenar por nome
                    Collections.sort(vacinasList, new Comparator<Vacina>() {
                        @Override
                        public int compare(Vacina v1, Vacina v2) {
                            return v1.getNome().compareToIgnoreCase(v2.getNome());
                        }
                    });

                    adapter.setVacinas(vacinasList);
                    mostrarCarregando(false);
                    mostrarListaVazia(vacinasList.isEmpty());
                }
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;

                mostrarCarregando(false);

                // Se houver erro, tentar usar cache local
                vacinasList = vacinaRepository.getVacinasCache();
                if (!vacinasList.isEmpty()) {
                    adapter.setVacinas(vacinasList);
                    Snackbar.make(requireView(), "Modo offline: mostrando dados salvos",
                            Snackbar.LENGTH_LONG).show();
                } else {
                    mostrarListaVazia(true);
                    Snackbar.make(requireView(), "Erro ao carregar vacinas: " + error,
                            Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private void adicionarVacinasExemplo() {
        // Lista de vacinas exemplo para popular o banco inicialmente
        List<Vacina> vacinasExemplo = criarVacinasExemplo();
        final int[] adicionadas = {0};
        final int total = vacinasExemplo.size();

        for (Vacina vacina : vacinasExemplo) {
            vacinaRepository.adicionarVacina(vacina, new VacinaRepository.OnVacinaOperationListener() {
                @Override
                public void onSuccess() {
                    adicionadas[0]++;
                    if (adicionadas[0] == total) {
                        // Todas foram adicionadas, recarregar lista
                        if (isAdded()) {
                            carregarVacinas();
                        }
                    }
                }

                @Override
                public void onError(String error) {
                    adicionadas[0]++;
                    if (adicionadas[0] == total && isAdded()) {
                        carregarVacinas();
                    }
                }
            });
        }
    }

    private List<Vacina> criarVacinasExemplo() {
        List<Vacina> vacinas = new ArrayList<>();

        vacinas.add(new Vacina("BCG",
                "Protege contra formas graves de tuberculose",
                "Ao nascer", 1, true));

        vacinas.add(new Vacina("Hepatite B",
                "Previne a hepatite B",
                "Ao nascer, 2, 4 e 6 meses", 4, true));

        vacinas.add(new Vacina("Pentavalente",
                "Protege contra difteria, tétano, coqueluche, hepatite B e infecções por Haemophilus influenzae B",
                "2, 4 e 6 meses", 3, true));

        vacinas.add(new Vacina("VIP (Poliomielite)",
                "Previne a poliomielite (paralisia infantil)",
                "2, 4 e 6 meses", 3, false));

        vacinas.add(new Vacina("Pneumocócica 10V",
                "Protege contra pneumonia, otite, meningite e outras doenças causadas pelo Pneumococo",
                "2, 4, 6 e 12 meses", 4, true));

        vacinas.add(new Vacina("Rotavírus",
                "Previne diarreia por rotavírus",
                "2 e 4 meses", 2, true));

        vacinas.add(new Vacina("Meningocócica C",
                "Protege contra a doença meningocócica C",
                "3, 5 e 12 meses", 3, true));

        vacinas.add(new Vacina("Febre Amarela",
                "Protege contra a febre amarela",
                "9 meses e 4 anos", 2, false));

        vacinas.add(new Vacina("Tríplice Viral",
                "Protege contra sarampo, caxumba e rubéola",
                "12 e 15 meses", 2, true));

        vacinas.add(new Vacina("DTP",
                "Reforço contra difteria, tétano e coqueluche",
                "15 meses e 4 anos", 2, true));

        vacinas.add(new Vacina("Varicela",
                "Protege contra catapora",
                "4 anos", 1, true));

        vacinas.add(new Vacina("HPV",
                "Previne o câncer de colo do útero e outras doenças causadas pelo HPV",
                "9 a 14 anos", 2, true));

        vacinas.add(new Vacina("COVID-19",
                "Protege contra o coronavírus SARS-CoV-2",
                "A partir de 6 meses", 2, true));

        return vacinas;
    }

    private void mostrarCarregando(boolean mostrar) {
        if (!isAdded()) return;
        progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(mostrar ? View.GONE : View.VISIBLE);
    }

    private void mostrarListaVazia(boolean vazia) {
        if (!isAdded()) return;
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
        // Mostrar detalhes da vacina
        mostrarDetalhesVacina(vacina);
    }

    @Override
    public void onEditClick(Vacina vacina) {
        // Abrir tela de edição
        Intent intent = new Intent(getActivity(), CadastroVacinaActivity.class);
        intent.putExtra("vacina_id", vacina.getId());
        startActivityForResult(intent, 100);
    }

    @Override
    public void onDeleteClick(Vacina vacina) {
        // Mostrar confirmação antes de excluir
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Confirmar Exclusão")
                .setMessage("Deseja realmente excluir a vacina " + vacina.getNome() + "?")
                .setPositiveButton("Excluir", (dialog, which) -> {
                    excluirVacina(vacina);
                })
                .setNegativeButton("Cancelar", null)
                .setIcon(R.drawable.ic_delete_black_24dp)
                .show();
    }

    private void mostrarDetalhesVacina(Vacina vacina) {
        StringBuilder detalhes = new StringBuilder();
        detalhes.append("Descrição: ").append(vacina.getDescricao()).append("\n\n");
        detalhes.append("Faixa Etária: ").append(vacina.getFaixaEtaria()).append("\n\n");

        if (vacina.getNumeroDoses() == -1) {
            detalhes.append("Doses: Anual\n\n");
        } else if (vacina.getNumeroDoses() == 1) {
            detalhes.append("Dose única\n\n");
        } else {
            detalhes.append("Número de Doses: ").append(vacina.getNumeroDoses()).append("\n\n");
        }

        detalhes.append("Disponibilidade: ")
                .append(vacina.isDisponivel() ? "Disponível" : "Indisponível");

        if (vacina.getObservacoes() != null && !vacina.getObservacoes().isEmpty()) {
            detalhes.append("\n\nObservações: ").append(vacina.getObservacoes());
        }

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(vacina.getNome())
                .setMessage(detalhes.toString())
                .setPositiveButton("Fechar", null)
                .setIcon(R.drawable.ic_vacina_03_24)
                .show();
    }

    private void excluirVacina(Vacina vacina) {
        // Remover do Firebase
        vacinaRepository.excluirVacina(vacina.getId(), new VacinaRepository.OnVacinaOperationListener() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;

                Snackbar.make(requireView(),
                                "Vacina " + vacina.getNome() + " excluída com sucesso",
                                Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(requireContext().getColor(R.color.success))
                        .show();
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;

                Snackbar.make(requireView(),
                                "Erro ao excluir vacina: " + error,
                                Snackbar.LENGTH_LONG)
                        .setBackgroundTint(requireContext().getColor(R.color.error))
                        .show();
            }
        });
    }

    private void removerVacinaComDesfazer(Vacina vacina, int position) {
        // Guardar para desfazer
        vacinaRemovida = vacina;
        posicaoRemovida = position;

        // Remover da lista local temporariamente
        vacinasList.remove(position);
        adapter.setVacinas(vacinasList);

        // Mostrar Snackbar com ação de desfazer
        Snackbar snackbar = Snackbar.make(requireView(),
                        "Vacina " + vacina.getNome() + " removida",
                        Snackbar.LENGTH_LONG)
                .setAction("Desfazer", v -> {
                    // Restaurar na lista local
                    vacinasList.add(posicaoRemovida, vacinaRemovida);
                    adapter.setVacinas(vacinasList);
                    vacinaRemovida = null;
                })
                .setBackgroundTint(requireContext().getColor(R.color.error));

        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                if (event != Snackbar.Callback.DISMISS_EVENT_ACTION && vacinaRemovida != null) {
                    // Se não foi desfeito, excluir do Firebase
                    excluirVacina(vacinaRemovida);
                    vacinaRemovida = null;
                }
            }
        });

        snackbar.show();
    }

    private void abrirCadastroVacina() {
        Intent intent = new Intent(getActivity(), CadastroVacinaActivity.class);
        startActivityForResult(intent, 100);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == getActivity().RESULT_OK) {
            // Recarregar lista após cadastro/edição
            carregarVacinas();
        }
    }
}