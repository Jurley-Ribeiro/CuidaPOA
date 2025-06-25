package com.example.cuidapoa.view.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.cuidapoa.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

public class SuporteFragment extends Fragment {

    // Informações de contato
    private static final String EMAIL_SUPORTE = "suporte@cuidapoa.com.br";
    private static final String TELEFONE_SUPORTE = "(51) 3289-1234";
    private static final String WHATSAPP_NUMERO = "5551989123456"; // Número sem formatação

    private MaterialCardView cardEmail, cardTelefone, cardWhatsApp, cardFAQ, cardSobre, cardFeedback;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_suporte, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar views
        inicializarViews(view);

        // Configurar cliques
        configurarCliques();
    }

    private void inicializarViews(View view) {
        cardEmail = view.findViewById(R.id.card_email_suporte);
        cardTelefone = view.findViewById(R.id.card_telefone_suporte);
        cardWhatsApp = view.findViewById(R.id.card_whatsapp_suporte);
        cardFAQ = view.findViewById(R.id.card_faq);
        cardSobre = view.findViewById(R.id.card_sobre_app);
        cardFeedback = view.findViewById(R.id.card_feedback);
    }

    private void configurarCliques() {
        // Email
        cardEmail.setOnClickListener(v -> enviarEmail());

        // Telefone
        cardTelefone.setOnClickListener(v -> ligarSuporte());

        // WhatsApp
        cardWhatsApp.setOnClickListener(v -> abrirWhatsApp());

        // FAQ
        cardFAQ.setOnClickListener(v -> mostrarFAQ());

        // Sobre o App
        cardSobre.setOnClickListener(v -> mostrarSobreApp());

        // Feedback
        cardFeedback.setOnClickListener(v -> enviarFeedback());
    }

    private void enviarEmail() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{EMAIL_SUPORTE});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Dúvida sobre o CuidaPOA");
        intent.putExtra(Intent.EXTRA_TEXT, "Olá,\n\nGostaria de tirar uma dúvida sobre o aplicativo CuidaPOA.\n\n");

        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(Intent.createChooser(intent, "Enviar email"));
        } else {
            Snackbar.make(requireView(), "Nenhum aplicativo de email encontrado", Snackbar.LENGTH_LONG).show();
        }
    }

    private void ligarSuporte() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + TELEFONE_SUPORTE));

        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Snackbar.make(requireView(), "Não foi possível abrir o discador", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void abrirWhatsApp() {
        try {
            String mensagem = "Olá! Gostaria de tirar uma dúvida sobre o aplicativo CuidaPOA.";
            String url = "https://wa.me/" + WHATSAPP_NUMERO + "?text=" + Uri.encode(mensagem);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            // Se não tiver WhatsApp, abrir no navegador
            try {
                String url = "https://wa.me/" + WHATSAPP_NUMERO;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            } catch (Exception ex) {
                Snackbar.make(requireView(), "Não foi possível abrir o WhatsApp", Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    private void mostrarFAQ() {
        String[] perguntas = {
                "Como faço para encontrar uma UBS próxima?",
                "Quais vacinas estão disponíveis?",
                "Como sei o horário de funcionamento?",
                "Posso agendar consultas pelo app?",
                "Como faço login como gestor?",
                "O app funciona offline?"
        };

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Perguntas Frequentes")
                .setItems(perguntas, (dialog, which) -> {
                    mostrarRespostaFAQ(perguntas[which]);
                })
                .setPositiveButton("Fechar", null)
                .show();
    }

    private void mostrarRespostaFAQ(String pergunta) {
        String resposta = "";

        switch (pergunta) {
            case "Como faço para encontrar uma UBS próxima?":
                resposta = "Acesse a opção 'Busca UBS' no menu principal. Você pode pesquisar por nome, bairro ou endereço. Clique no botão de mapa para ver a localização.";
                break;

            case "Quais vacinas estão disponíveis?":
                resposta = "Acesse a opção 'Vacinas' no menu principal para ver a lista completa de vacinas disponíveis nas UBS. A disponibilidade pode variar por unidade.";
                break;

            case "Como sei o horário de funcionamento?":
                resposta = "O horário de cada UBS está disponível na lista de unidades. Procure a UBS desejada e veja o horário na descrição. O status (Aberta/Fechada) é atualizado em tempo real.";
                break;

            case "Posso agendar consultas pelo app?":
                resposta = "No momento, o app serve para consulta de informações. Para agendar consultas, entre em contato diretamente com a UBS pelo telefone disponível no app.";
                break;

            case "Como faço login como gestor?":
                resposta = "No menu lateral, clique em 'Área do Gestor'. Use suas credenciais fornecidas pela administração. Gestores podem atualizar informações de vacinas e UBS.";
                break;

            case "O app funciona offline?":
                resposta = "Sim! Uma vez carregadas, as informações ficam disponíveis offline. Porém, para atualizações e login é necessária conexão com internet.";
                break;
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(pergunta)
                .setMessage(resposta)
                .setPositiveButton("Entendi", null)
                .show();
    }

    private void mostrarSobreApp() {
        String sobre = "CuidaPOA v1.0\n\n" +
                "Desenvolvido como projeto final da disciplina de Desenvolvimento de Software III - Android\n\n" +
                "IFRS - Campus Porto Alegre\n\n" +
                "Este aplicativo facilita o acesso às informações das Unidades Básicas de Saúde de Porto Alegre, " +
                "permitindo consultar vacinas disponíveis, localizar unidades, verificar horários e serviços.\n\n" +
                "Desenvolvedor: Jurley C. Ribeiro\n" +
                "Ano: 2024";

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Sobre o CuidaPOA")
                .setMessage(sobre)
                .setIcon(R.drawable.ic_logo_splash)
                .setPositiveButton("Fechar", null)
                .show();
    }

    private void enviarFeedback() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{EMAIL_SUPORTE});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback - CuidaPOA");
        intent.putExtra(Intent.EXTRA_TEXT,
                "Tipo de feedback: [ ] Sugestão [ ] Problema [ ] Elogio\n\n" +
                        "Sua mensagem:\n\n\n" +
                        "---\n" +
                        "Informações do dispositivo:\n" +
                        "Android: " + android.os.Build.VERSION.RELEASE + "\n" +
                        "Modelo: " + android.os.Build.MODEL);

        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(Intent.createChooser(intent, "Enviar feedback"));
        }
    }
}