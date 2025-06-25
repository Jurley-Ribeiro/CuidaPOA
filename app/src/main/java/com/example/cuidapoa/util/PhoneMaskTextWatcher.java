package com.example.cuidapoa.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class PhoneMaskTextWatcher implements TextWatcher {

    private final EditText editText;
    private boolean isRunning = false;
    private boolean isDeleting = false;

    public PhoneMaskTextWatcher(EditText editText) {
        this.editText = editText;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        isDeleting = count > after;
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // Não faz nada aqui
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (isRunning || isDeleting) {
            return;
        }
        isRunning = true;

        // Remove todos os caracteres não numéricos
        String numeroLimpo = s.toString().replaceAll("[^\\d]", "");

        // Aplica a máscara
        String numeroFormatado = "";

        if (numeroLimpo.length() > 0) {
            // Adiciona o parêntese de abertura
            numeroFormatado = "(";

            // Adiciona o DDD (2 primeiros dígitos)
            if (numeroLimpo.length() >= 1) {
                numeroFormatado += numeroLimpo.substring(0, Math.min(2, numeroLimpo.length()));
            }

            // Adiciona o parêntese de fechamento
            if (numeroLimpo.length() >= 2) {
                numeroFormatado += ") ";
            }

            // Adiciona o primeiro bloco do número
            if (numeroLimpo.length() > 2) {
                int fim = Math.min(7, numeroLimpo.length());
                if (numeroLimpo.length() > 10) {
                    // Celular com 9 dígitos
                    fim = Math.min(7, numeroLimpo.length());
                } else {
                    // Fixo com 8 dígitos
                    fim = Math.min(6, numeroLimpo.length());
                }
                numeroFormatado += numeroLimpo.substring(2, fim);
            }

            // Adiciona o hífen
            if ((numeroLimpo.length() > 10 && numeroLimpo.length() > 6) ||
                    (numeroLimpo.length() <= 10 && numeroLimpo.length() > 5)) {
                numeroFormatado += "-";
            }

            // Adiciona o segundo bloco do número
            if (numeroLimpo.length() > 10 && numeroLimpo.length() > 6) {
                // Celular
                numeroFormatado += numeroLimpo.substring(7, Math.min(11, numeroLimpo.length()));
            } else if (numeroLimpo.length() <= 10 && numeroLimpo.length() > 5) {
                // Fixo
                numeroFormatado += numeroLimpo.substring(6, Math.min(10, numeroLimpo.length()));
            }
        }

        // Define o texto formatado
        editText.setText(numeroFormatado);
        editText.setSelection(numeroFormatado.length());

        isRunning = false;
    }

    /**
     * Remove a máscara e retorna apenas os números
     */
    public static String unmask(String s) {
        return s.replaceAll("[^\\d]", "");
    }

    /**
     * Aplica a máscara em um número limpo
     */
    public static String mask(String numeroLimpo) {
        if (numeroLimpo == null || numeroLimpo.isEmpty()) {
            return "";
        }

        // Remove qualquer caractere não numérico
        numeroLimpo = numeroLimpo.replaceAll("[^\\d]", "");

        if (numeroLimpo.length() < 10 || numeroLimpo.length() > 11) {
            return numeroLimpo;
        }

        String ddd = numeroLimpo.substring(0, 2);

        if (numeroLimpo.length() == 11) {
            // Celular: (XX) XXXXX-XXXX
            String parte1 = numeroLimpo.substring(2, 7);
            String parte2 = numeroLimpo.substring(7);
            return String.format("(%s) %s-%s", ddd, parte1, parte2);
        } else {
            // Fixo: (XX) XXXX-XXXX
            String parte1 = numeroLimpo.substring(2, 6);
            String parte2 = numeroLimpo.substring(6);
            return String.format("(%s) %s-%s", ddd, parte1, parte2);
        }
    }
}