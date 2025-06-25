package com.example.cuidapoa.repository;

import androidx.annotation.NonNull;
import com.example.cuidapoa.model.UBS;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class UBSRepository {
    private static UBSRepository instance;
    private final DatabaseReference ubsRef;
    private List<UBS> ubsCache = new ArrayList<>();

    // Listener callbacks
    public interface OnUBSLoadedListener {
        void onUBSLoaded(List<UBS> ubsList);
        void onError(String error);
    }

    public interface OnUBSOperationListener {
        void onSuccess();
        void onError(String error);
    }

    private UBSRepository() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        ubsRef = database.getReference("ubs");
        // Manter dados sincronizados offline
        ubsRef.keepSynced(true);
    }

    public static UBSRepository getInstance() {
        if (instance == null) {
            instance = new UBSRepository();
        }
        return instance;
    }

    // CREATE - Adicionar nova UBS
    public void adicionarUBS(UBS ubs, OnUBSOperationListener listener) {
        String id = ubsRef.push().getKey();
        if (id != null) {
            ubs.setId(id);
            ubsRef.child(id).setValue(ubs)
                    .addOnSuccessListener(aVoid -> {
                        if (listener != null) listener.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        if (listener != null) listener.onError(e.getMessage());
                    });
        }
    }

    // READ - Obter todas as UBS
    public void obterTodasUBS(OnUBSLoadedListener listener) {
        ubsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<UBS> ubsList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UBS ubs = snapshot.getValue(UBS.class);
                    if (ubs != null) {
                        ubs.setId(snapshot.getKey());
                        ubsList.add(ubs);
                    }
                }
                ubsCache = ubsList;
                if (listener != null) {
                    listener.onUBSLoaded(ubsList);
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

    // READ - Obter UBS uma única vez
    public void obterUBSUmaVez(OnUBSLoadedListener listener) {
        ubsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<UBS> ubsList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UBS ubs = snapshot.getValue(UBS.class);
                    if (ubs != null) {
                        ubs.setId(snapshot.getKey());
                        ubsList.add(ubs);
                    }
                }
                if (listener != null) {
                    listener.onUBSLoaded(ubsList);
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

    // READ - Buscar UBS por bairro
    public void buscarUBSPorBairro(String bairro, OnUBSLoadedListener listener) {
        ubsRef.orderByChild("bairro").equalTo(bairro)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<UBS> ubsList = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            UBS ubs = snapshot.getValue(UBS.class);
                            if (ubs != null) {
                                ubs.setId(snapshot.getKey());
                                ubsList.add(ubs);
                            }
                        }
                        if (listener != null) {
                            listener.onUBSLoaded(ubsList);
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

    // UPDATE - Atualizar UBS
    public void atualizarUBS(UBS ubs, OnUBSOperationListener listener) {
        if (ubs.getId() != null) {
            ubsRef.child(ubs.getId()).setValue(ubs)
                    .addOnSuccessListener(aVoid -> {
                        if (listener != null) listener.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        if (listener != null) listener.onError(e.getMessage());
                    });
        }
    }

    // DELETE - Excluir UBS
    public void excluirUBS(String ubsId, OnUBSOperationListener listener) {
        ubsRef.child(ubsId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    if (listener != null) listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onError(e.getMessage());
                });
    }

    // Obter cache local (útil quando offline)
    public List<UBS> getUBSCache() {
        return new ArrayList<>(ubsCache);
    }

    // Verificar se uma UBS existe pelo nome (para evitar duplicatas)
    public void verificarUBSExiste(String nome, OnUBSExistsListener listener) {
        ubsRef.orderByChild("nome").equalTo(nome)
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

    public interface OnUBSExistsListener {
        void onResult(boolean exists);
    }
}