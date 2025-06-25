package com.example.cuidapoa.view.fragments;

import android.content.Intent;
import android.net.Uri;
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
import com.example.cuidapoa.adapter.UBSAdapter;
import com.example.cuidapoa.model.UBS;
import com.example.cuidapoa.repository.AuthRepository;
import com.example.cuidapoa.repository.UBSRepository;
import com.example.cuidapoa.util.SwipeToDeleteCallback;
import com.example.cuidapoa.view.CadastroUBSActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class UBSFragment extends Fragment implements UBSAdapter.OnUBSClickListener {

    private RecyclerView recyclerView;
    private UBSAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private FloatingActionButton fabAdd;

    // Firebase
    private UBSRepository ubsRepository;
    private AuthRepository authRepository;
    private boolean isAdmin = false;

    // Lista para controle local
    private List<UBS> ubsList = new ArrayList<>();

    // Para controle do "Desfazer"
    private UBS ubsRemovida;
    private int posicaoRemovida;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // Inicializar repositories
        ubsRepository = UBSRepository.getInstance();
        authRepository = AuthRepository.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ubs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar views
        recyclerView = view.findViewById(R.id.recycler_ubs);
        progressBar = view.findViewById(R.id.progress_bar);
        tvEmpty = view.findViewById(R.id.tv_empty);
        fabAdd = view.findViewById(R.id.fab_add_ubs);

        // Verificar se é admin
        isAdmin = authRepository.isAdmin();

        // Alternativamente, pode pegar da MainActivity
        if (getActivity() instanceof MainActivity) {
            isAdmin = ((MainActivity) getActivity()).isAdmin();
        }

        // Mostrar FAB apenas para admin
        if (isAdmin) {
            fabAdd.setVisibility(View.VISIBLE);
            fabAdd.setOnClickListener(v -> abrirCadastroUBS());
        } else {
            fabAdd.setVisibility(View.GONE);
        }

        // Configurar RecyclerView
        configurarRecyclerView();

        // Carregar UBS do Firebase
        carregarUBS();
    }

    private void configurarRecyclerView() {
        adapter = new UBSAdapter(requireContext(), isAdmin);
        adapter.setOnUBSClickListener(this);

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
                if (position >= 0 && position < ubsList.size()) {
                    UBS ubs = ubsList.get(position);
                    removerUBSComDesfazer(ubs, position);
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void carregarUBS() {
        mostrarCarregando(true);

        // Carregar UBS do Firebase
        ubsRepository.obterTodasUBS(new UBSRepository.OnUBSLoadedListener() {
            @Override
            public void onUBSLoaded(List<UBS> lista) {
                if (!isAdded()) return; // Verificar se o fragment ainda está anexado

                ubsList = new ArrayList<>(lista);

                // Se não houver UBS no Firebase e for a primeira vez, adicionar exemplos
                if (ubsList.isEmpty() && isAdmin) {
                    adicionarUBSExemplo();
                } else {
                    // Ordenar por nome
                    Collections.sort(ubsList, new Comparator<UBS>() {
                        @Override
                        public int compare(UBS u1, UBS u2) {
                            return u1.getNome().compareToIgnoreCase(u2.getNome());
                        }
                    });

                    adapter.setUBSList(ubsList);
                    mostrarCarregando(false);
                    mostrarListaVazia(ubsList.isEmpty());
                }
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;

                mostrarCarregando(false);

                // Se houver erro, tentar usar cache local
                ubsList = ubsRepository.getUBSCache();
                if (!ubsList.isEmpty()) {
                    adapter.setUBSList(ubsList);
                    Snackbar.make(requireView(), "Modo offline: mostrando dados salvos",
                            Snackbar.LENGTH_LONG).show();
                } else {
                    mostrarListaVazia(true);
                    Snackbar.make(requireView(), "Erro ao carregar UBS: " + error,
                            Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private void adicionarUBSExemplo() {
        // Lista de UBS exemplo para popular o banco inicialmente
        List<UBS> ubsExemplo = criarUBSExemplo();
        final int[] adicionadas = {0};
        final int total = ubsExemplo.size();

        for (UBS ubs : ubsExemplo) {
            ubsRepository.adicionarUBS(ubs, new UBSRepository.OnUBSOperationListener() {
                @Override
                public void onSuccess() {
                    adicionadas[0]++;
                    if (adicionadas[0] == total) {
                        // Todas foram adicionadas, recarregar lista
                        if (isAdded()) {
                            carregarUBS();
                        }
                    }
                }

                @Override
                public void onError(String error) {
                    adicionadas[0]++;
                    if (adicionadas[0] == total && isAdded()) {
                        carregarUBS();
                    }
                }
            });
        }
    }

    private List<UBS> criarUBSExemplo() {
        List<UBS> ubsList = new ArrayList<>();

        // UBS Centro
        UBS ubs1 = new UBS(
                "UBS Centro",
                "Rua Capitão Montanha, 27",
                "Centro Histórico",
                "(51) 3289-2600",
                "Segunda a Sexta: 7h às 19h"
        );
        ubs1.setAbertaAgora(true);
        ubs1.setServicos(Arrays.asList("Clínica Geral", "Pediatria", "Ginecologia", "Vacinação", "Farmácia"));
        ubs1.setEmail("centro@ubs.poa.br");
        ubs1.setLatitude(-30.0346);
        ubs1.setLongitude(-51.2177);
        ubsList.add(ubs1);

        // UBS Santa Marta
        UBS ubs2 = new UBS(
                "UBS Santa Marta",
                "Rua São Carlos, 545",
                "Floresta",
                "(51) 3289-2601",
                "Segunda a Sexta: 7h às 17h"
        );
        ubs2.setAbertaAgora(false);
        ubs2.setServicos(Arrays.asList("Clínica Geral", "Odontologia", "Vacinação"));
        ubsList.add(ubs2);

        // UBS Vila Nova
        UBS ubs3 = new UBS(
                "UBS Vila Nova",
                "Av. Caçapava, 249",
                "Vila Nova",
                "(51) 3289-2602",
                "Segunda a Sexta: 8h às 17h"
        );
        ubs3.setAbertaAgora(true);
        ubs3.setServicos(Arrays.asList("Clínica Geral", "Pediatria", "Vacinação", "Psicologia"));
        ubsList.add(ubs3);

        // UBS Restinga
        UBS ubs4 = new UBS(
                "UBS Restinga",
                "Rua Teodoro de Oliveira, 99",
                "Restinga",
                "(51) 3289-2603",
                "Segunda a Sexta: 7h às 19h"
        );
        ubs4.setAbertaAgora(true);
        ubs4.setServicos(Arrays.asList("Clínica Geral", "Pediatria", "Ginecologia", "Odontologia", "Vacinação"));
        ubsList.add(ubs4);

        // UBS Lomba do Pinheiro
        UBS ubs5 = new UBS(
                "UBS Lomba do Pinheiro",
                "Estrada João de Oliveira Remião, 5510",
                "Lomba do Pinheiro",
                "(51) 3289-2604",
                "Segunda a Sexta: 7h às 17h"
        );
        ubs5.setAbertaAgora(false);
        ubs5.setServicos(Arrays.asList("Clínica Geral", "Vacinação", "Farmácia"));
        ubsList.add(ubs5);

        // UBS Partenon
        UBS ubs6 = new UBS(
                "UBS Partenon",
                "Av. Bento Gonçalves, 3722",
                "Partenon",
                "(51) 3289-2605",
                "Segunda a Sexta: 7h30 às 18h30"
        );
        ubs6.setAbertaAgora(true);
        ubs6.setServicos(Arrays.asList("Clínica Geral", "Pediatria", "Ginecologia", "Vacinação"));
        ubsList.add(ubs6);

        // UBS Belém Novo
        UBS ubs7 = new UBS(
                "UBS Belém Novo",
                "Rua Florêncio Farias, 195",
                "Belém Novo",
                "(51) 3289-2606",
                "Segunda a Sexta: 8h às 17h"
        );
        ubs7.setAbertaAgora(false);
        ubs7.setServicos(Arrays.asList("Clínica Geral", "Odontologia", "Vacinação"));
        ubsList.add(ubs7);

        // UBS Rubem Berta
        UBS ubs8 = new UBS(
                "UBS Rubem Berta",
                "Rua Wolfram Metzler, 675",
                "Rubem Berta",
                "(51) 3289-2607",
                "Segunda a Sexta: 7h às 19h"
        );
        ubs8.setAbertaAgora(true);
        ubs8.setServicos(Arrays.asList("Clínica Geral", "Pediatria", "Psicologia", "Vacinação", "Farmácia"));
        ubsList.add(ubs8);

        return ubsList;
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
        inflater.inflate(R.menu.menu_ubs, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.ubs_buscar));

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
    public void onUBSClick(UBS ubs) {
        // Mostrar detalhes da UBS
        mostrarDetalhesUBS(ubs);
    }

    @Override
    public void onTelefoneClick(UBS ubs) {
        // Abrir discador com o telefone
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + ubs.getTelefone()));

        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Snackbar.make(requireView(), "Não foi possível abrir o discador", Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onEnderecoClick(UBS ubs) {
        // Abrir mapa com o endereço
        String endereco = Uri.encode(ubs.getEnderecoCompleto() + ", Porto Alegre, RS");
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + endereco);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            // Se não tiver Google Maps, abrir no navegador
            Uri webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + endereco);
            Intent webIntent = new Intent(Intent.ACTION_VIEW, webUri);
            startActivity(webIntent);
        }
    }

    @Override
    public void onEditClick(UBS ubs) {
        // Abrir tela de edição
        Intent intent = new Intent(getActivity(), CadastroUBSActivity.class);
        intent.putExtra("ubs_id", ubs.getId());
        startActivityForResult(intent, 200);
    }

    @Override
    public void onDeleteClick(UBS ubs) {
        // Mostrar confirmação antes de excluir
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Confirmar Exclusão")
                .setMessage("Deseja realmente excluir a UBS " + ubs.getNome() + "?")
                .setPositiveButton("Excluir", (dialog, which) -> {
                    excluirUBS(ubs);
                })
                .setNegativeButton("Cancelar", null)
                .setIcon(R.drawable.ic_delete_black_24dp)
                .show();
    }

    private void mostrarDetalhesUBS(UBS ubs) {
        StringBuilder servicos = new StringBuilder();
        if (ubs.getServicos() != null && !ubs.getServicos().isEmpty()) {
            for (String servico : ubs.getServicos()) {
                servicos.append("• ").append(servico).append("\n");
            }
        }

        String detalhes = "Endereço: " + ubs.getEnderecoCompleto() + "\n\n" +
                "Telefone: " + ubs.getTelefone() + "\n\n" +
                "Horário: " + ubs.getHorarioFuncionamento() + "\n\n";

        if (ubs.getEmail() != null && !ubs.getEmail().isEmpty()) {
            detalhes += "Email: " + ubs.getEmail() + "\n\n";
        }

        detalhes += "Status: " + (ubs.isAbertaAgora() ? "Aberta agora" : "Fechada") + "\n\n" +
                "Serviços oferecidos:\n" + servicos.toString();

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(ubs.getNome())
                .setMessage(detalhes)
                .setPositiveButton("Fechar", null)
                .setNeutralButton("Ligar", (dialog, which) -> onTelefoneClick(ubs))
                .setNegativeButton("Ver no Mapa", (dialog, which) -> onEnderecoClick(ubs))
                .show();
    }

    private void excluirUBS(UBS ubs) {
        // Remover do Firebase
        ubsRepository.excluirUBS(ubs.getId(), new UBSRepository.OnUBSOperationListener() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;

                Snackbar.make(requireView(),
                                "UBS " + ubs.getNome() + " excluída com sucesso",
                                Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(requireContext().getColor(R.color.success))
                        .show();
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;

                Snackbar.make(requireView(),
                                "Erro ao excluir UBS: " + error,
                                Snackbar.LENGTH_LONG)
                        .setBackgroundTint(requireContext().getColor(R.color.error))
                        .show();
            }
        });
    }

    private void removerUBSComDesfazer(UBS ubs, int position) {
        // Guardar para desfazer
        ubsRemovida = ubs;
        posicaoRemovida = position;

        // Remover da lista local temporariamente
        ubsList.remove(position);
        adapter.setUBSList(ubsList);

        // Mostrar Snackbar com ação de desfazer
        Snackbar snackbar = Snackbar.make(requireView(),
                        "UBS " + ubs.getNome() + " removida",
                        Snackbar.LENGTH_LONG)
                .setAction("Desfazer", v -> {
                    // Restaurar na lista local
                    ubsList.add(posicaoRemovida, ubsRemovida);
                    adapter.setUBSList(ubsList);
                    ubsRemovida = null;
                })
                .setBackgroundTint(requireContext().getColor(R.color.error));

        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                if (event != Snackbar.Callback.DISMISS_EVENT_ACTION && ubsRemovida != null) {
                    // Se não foi desfeito, excluir do Firebase
                    excluirUBS(ubsRemovida);
                    ubsRemovida = null;
                }
            }
        });

        snackbar.show();
    }

    private void abrirCadastroUBS() {
        Intent intent = new Intent(getActivity(), CadastroUBSActivity.class);
        startActivityForResult(intent, 200);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 200 && resultCode == getActivity().RESULT_OK) {
            // Recarregar lista após cadastro/edição
            carregarUBS();
        }
    }
}