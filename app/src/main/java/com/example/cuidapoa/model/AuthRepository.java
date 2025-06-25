package com.example.cuidapoa.repository;

import androidx.annotation.NonNull;
import com.example.cuidapoa.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AuthRepository {
    private static AuthRepository instance;
    private final FirebaseAuth auth;
    private final DatabaseReference usuariosRef;
    private Usuario usuarioAtual;

    // Listener callbacks
    public interface OnAuthListener {
        void onSuccess();
        void onError(String error);
    }

    public interface OnUserLoadedListener {
        void onUserLoaded(Usuario usuario);
        void onError(String error);
    }

    private AuthRepository() {
        auth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        usuariosRef = database.getReference("usuarios");
    }

    public static AuthRepository getInstance() {
        if (instance == null) {
            instance = new AuthRepository();
        }
        return instance;
    }

    // LOGIN
    public void login(String email, String senha, OnAuthListener listener) {
        auth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Carregar dados do usuário após login bem-sucedido
                        carregarDadosUsuario(listener);
                    } else {
                        if (listener != null) {
                            listener.onError("Email ou senha incorretos");
                        }
                    }
                });
    }

    // LOGOUT
    public void logout() {
        auth.signOut();
        usuarioAtual = null;
    }

    // CRIAR CONTA (apenas para admins criarem outros admins)
    public void criarContaAdmin(String email, String senha, String nome, OnAuthListener listener) {
        auth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            // Criar perfil de usuário no database
                            Usuario novoUsuario = new Usuario(nome, email, "admin");
                            novoUsuario.setId(user.getUid());

                            usuariosRef.child(user.getUid()).setValue(novoUsuario)
                                    .addOnSuccessListener(aVoid -> {
                                        if (listener != null) listener.onSuccess();
                                    })
                                    .addOnFailureListener(e -> {
                                        if (listener != null) listener.onError(e.getMessage());
                                    });
                        }
                    } else {
                        if (listener != null) {
                            listener.onError("Erro ao criar conta: " + task.getException().getMessage());
                        }
                    }
                });
    }

    // CARREGAR DADOS DO USUÁRIO
    public void carregarDadosUsuario(OnAuthListener listener) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            usuariosRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    usuarioAtual = dataSnapshot.getValue(Usuario.class);
                    if (usuarioAtual != null) {
                        usuarioAtual.setId(user.getUid());
                        if (listener != null) listener.onSuccess();
                    } else {
                        // Usuário existe no Auth mas não no Database
                        // Criar perfil básico
                        Usuario novoUsuario = new Usuario(
                                user.getDisplayName() != null ? user.getDisplayName() : "Usuário",
                                user.getEmail(),
                                "cidadao"
                        );
                        novoUsuario.setId(user.getUid());

                        usuariosRef.child(user.getUid()).setValue(novoUsuario)
                                .addOnSuccessListener(aVoid -> {
                                    usuarioAtual = novoUsuario;
                                    if (listener != null) listener.onSuccess();
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    if (listener != null) {
                        listener.onError(databaseError.getMessage());
                    }
                }
            });
        } else {
            if (listener != null) {
                listener.onError("Usuário não autenticado");
            }
        }
    }

    // OBTER USUÁRIO ATUAL
    public Usuario getUsuarioAtual() {
        return usuarioAtual;
    }

    // VERIFICAR SE ESTÁ LOGADO
    public boolean isLogado() {
        return auth.getCurrentUser() != null;
    }

    // VERIFICAR SE É ADMIN
    public boolean isAdmin() {
        return usuarioAtual != null && usuarioAtual.isAdmin();
    }

    // OBTER FIREBASE USER
    public FirebaseUser getFirebaseUser() {
        return auth.getCurrentUser();
    }

    // RESETAR SENHA
    public void resetarSenha(String email, OnAuthListener listener) {
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (listener != null) listener.onSuccess();
                    } else {
                        if (listener != null) {
                            listener.onError("Erro ao enviar email de recuperação");
                        }
                    }
                });
    }

    // ATUALIZAR DADOS DO USUÁRIO
    public void atualizarDadosUsuario(Usuario usuario, OnAuthListener listener) {
        if (usuario.getId() != null) {
            usuariosRef.child(usuario.getId()).setValue(usuario)
                    .addOnSuccessListener(aVoid -> {
                        usuarioAtual = usuario;
                        if (listener != null) listener.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        if (listener != null) listener.onError(e.getMessage());
                    });
        }
    }
}