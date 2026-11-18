package br.com.fiap.agrocompartilha.seguranca;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Gera e confere hashes de senha com PBKDF2-SHA256 (já vem no JDK).
 *
 * A senha nunca é gravada: o banco guarda só "iteracoes:salt:hash".
 * O salt é aleatório para cada usuário, então duas senhas iguais geram hashes diferentes.
 */
public final class Senha {

    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int ITERACOES = 120_000;
    private static final int TAMANHO_SALT = 16;
    private static final int TAMANHO_HASH_BITS = 256;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private Senha() {
    }

    public static String gerarHash(String senha) {
        byte[] salt = new byte[TAMANHO_SALT];
        ALEATORIO.nextBytes(salt);
        byte[] hash = calcular(senha.toCharArray(), salt, ITERACOES);
        Base64.Encoder base64 = Base64.getEncoder();
        return ITERACOES + ":" + base64.encodeToString(salt) + ":" + base64.encodeToString(hash);
    }

    public static boolean confere(String senhaDigitada, String hashGuardado) {
        if (senhaDigitada == null || hashGuardado == null) {
            return false;
        }
        String[] partes = hashGuardado.split(":");
        if (partes.length != 3) {
            return false;
        }
        int iteracoes = Integer.parseInt(partes[0]);
        byte[] salt = Base64.getDecoder().decode(partes[1]);
        byte[] esperado = Base64.getDecoder().decode(partes[2]);
        byte[] calculado = calcular(senhaDigitada.toCharArray(), salt, iteracoes);
        // Comparação em tempo constante: não revela quantos bytes acertaram
        return MessageDigest.isEqual(esperado, calculado);
    }

    private static byte[] calcular(char[] senha, byte[] salt, int iteracoes) {
        try {
            PBEKeySpec spec = new PBEKeySpec(senha, salt, iteracoes, TAMANHO_HASH_BITS);
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Algoritmo de senha indisponível no Java", e);
        }
    }

    public static void main(String[] args) {
        String senha = args.length > 0 ? args[0] : "agro123";
        System.out.println(gerarHash(senha));
    }
}
