package br.com.fiap.agrocompartilha.service;

import java.util.regex.Pattern;

public final class Validacoes {

    private static final Pattern CPF = Pattern.compile("^\\d{11}$");
    private static final Pattern CNPJ = Pattern.compile("^\\d{14}$");
    /** Todos os dígitos iguais (ex.: 111.111.111-11) passam no cálculo, mas não são documentos reais. */
    private static final Pattern DIGITOS_REPETIDOS = Pattern.compile("^(\\d)\\1+$");

    /** DDD de 11 a 99 + fixo (8 dígitos, começa de 2 a 5) ou celular (9 dígitos, começa com 9). */
    private static final Pattern TELEFONE = Pattern.compile("^[1-9][1-9](?:[2-5]\\d{7}|9\\d{8})$");

    private static final Pattern NOME = Pattern.compile("^(?=.*\\p{L})[\\p{L}\\d .'&ºª-]{3,100}$");

    private static final Pattern MUNICIPIO = Pattern.compile("^[\\p{L}][\\p{L} '-]{1,79}$");

    private static final Pattern SENHA = Pattern.compile("^(?=.*\\p{L})(?=.*\\d).{6,72}$");

    private Validacoes() {
    }

    public static String somenteDigitos(String texto) {
        return texto == null ? "" : texto.replaceAll("\\D", "");
    }

    public static boolean documentoValido(String digitos) {
        return cpfValido(digitos) || cnpjValido(digitos);
    }

    public static boolean cpfValido(String cpf) {
        if (cpf == null || !CPF.matcher(cpf).matches() || DIGITOS_REPETIDOS.matcher(cpf).matches()) {
            return false;
        }
        return cpf.charAt(9) == digitoCpf(cpf, 9) && cpf.charAt(10) == digitoCpf(cpf, 10);
    }

    private static char digitoCpf(String cpf, int posicao) {
        int soma = 0;
        for (int i = 0; i < posicao; i++) {
            soma += (cpf.charAt(i) - '0') * (posicao + 1 - i);
        }
        int resto = (soma * 10) % 11;
        return (char) ('0' + (resto == 10 ? 0 : resto));
    }

    public static boolean cnpjValido(String cnpj) {
        if (cnpj == null || !CNPJ.matcher(cnpj).matches() || DIGITOS_REPETIDOS.matcher(cnpj).matches()) {
            return false;
        }
        int[] pesos1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] pesos2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        return cnpj.charAt(12) == digitoCnpj(cnpj, pesos1) && cnpj.charAt(13) == digitoCnpj(cnpj, pesos2);
    }

    private static char digitoCnpj(String cnpj, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            soma += (cnpj.charAt(i) - '0') * pesos[i];
        }
        int resto = soma % 11;
        return (char) ('0' + (resto < 2 ? 0 : 11 - resto));
    }

    public static boolean telefoneValido(String digitos) {
        return digitos != null && TELEFONE.matcher(digitos).matches();
    }

    public static boolean nomeValido(String nome) {
        return nome != null && NOME.matcher(nome.trim()).matches();
    }

    public static boolean municipioValido(String municipio) {
        return municipio != null && MUNICIPIO.matcher(municipio.trim()).matches();
    }

    public static boolean senhaValida(String senha) {
        return senha != null && SENHA.matcher(senha).matches();
    }
}
