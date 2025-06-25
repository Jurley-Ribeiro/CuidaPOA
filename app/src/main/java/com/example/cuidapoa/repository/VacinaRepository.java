package com.example.cuidapoa.repository;

import androidx.annotation.NonNull;
import com.example.cuidapoa.model.Vacina;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class VacinaRepository {
    private static VacinaRepository instance;
    private final DatabaseReference vacinasRef;
    private List<Vacina> vacinasCache = new ArrayList<>();

    // Listener callbacks
    public interface OnVacinasLoadedListener {
        void onVacinasLoaded(List<Vacina> vacinas);
        void onError(String error);
    }

    public interface OnVacinaOperationListener {
        void onSuccess();
        void onError(String error);
    }

    private VacinaRepository() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        // Habilitar persistência offline
        try {
            database.setPersistenceEnabled(true);
        } catch (Exception e) {
            // Já foi habilitado anteriormente
        }
        vacinasRef = database.getReference("vacinas");
        // Manter dados sincronizados offline
        vacinasRef.keepSynced(true);
    }

    public static VacinaRepository getInstance() {
        if (instance == null) {
            instance = new VacinaRepository();
        }
        return instance;
    }

    // CREATE - Adicionar nova vacina
    public void adicionarVacina(Vacina vacina, OnVacinaOperationListener listener) {
        String id = vacinasRef.push().getKey();
        if (id != null) {
            vacina.setId(id);
            vacinasRef.child(id).setValue(vacina)
                    .addOnSuccessListener(aVoid -> {
                        if (listener != null) listener.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        if (listener != null) listener.onError(e.getMessage());
                    });
        }
    }

    // READ - Obter todas as vacinas
    public void obterTodasVacinas(OnVacinasLoadedListener listener) {
        vacinasRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Vacina> vacinas = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Vacina vacina = snapshot.getValue(Vacina.class);
                    if (vacina != null) {
                        vacina.setId(snapshot.getKey());
                        vacinas.add(vacina);
                    }
                }
                vacinasCache = vacinas;
                if (listener != null) {
                    listener.onVacinasLoaded(vacinas);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (listener != null) {
                    listener.onError(databaseError.getMessage());
                }
            }
        });
    }

    // READ - Obter vacinas uma única vez
    public void obterVacinasUmaVez(OnVacinasLoadedListener listener) {
        vacinasRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Vacina> vacinas = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Vacina vacina = snapshot.getValue(Vacina.class);
                    if (vacina != null) {
                        vacina.setId(snapshot.getKey());
                        vacinas.add(vacina);
                    }
                }
                if (listener != null) {
                    listener.onVacinasLoaded(vacinas);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (listener != null) {
                    listener.onError(databaseError.getMessage());
                }
            }
        });
    }

    // UPDATE - Atualizar vacina
    public void atualizarVacina(Vacina vacina, OnVacinaOperationListener listener) {
        if (vacina.getId() != null) {
            vacinasRef.child(vacina.getId()).setValue(vacina)
                    .addOnSuccessListener(aVoid -> {
                        if (listener != null) listener.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        if (listener != null) listener.onError(e.getMessage());
                    });
        }
    }

    // DELETE - Excluir vacina
    public void excluirVacina(String vacinaId, OnVacinaOperationListener listener) {
        vacinasRef.child(vacinaId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    if (listener != null) listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onError(e.getMessage());
                });
    }

    // Obter cache local (útil quando offline)
    public List<Vacina> getVacinasCache() {
        return new ArrayList<>(vacinasCache);
    }

    // Verificar se uma vacina existe pelo nome (para evitar duplicatas)
    public void verificarVacinaExiste(String nome, OnVacinaExistsListener listener) {
        vacinasRef.orderByChild("nome").equalTo(nome)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        boolean existe = dataSnapshot.exists();
                        if (listener != null) {
                            listener.onResult(existe);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        if (listener != null) {
                            listener.onResult(false);
                        }
                    }
                });
    }

    public interface OnVacinaExistsListener {
        void onResult(boolean exists);
    }
}