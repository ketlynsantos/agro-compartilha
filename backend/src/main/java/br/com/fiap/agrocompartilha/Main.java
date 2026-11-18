package br.com.fiap.agrocompartilha;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        // Porta padrão 8080; dá para trocar com: mvn exec:java -Dporta=8081
        String porta = System.getProperty("porta", "8080");
        String baseUri = "http://0.0.0.0:" + porta + "/api/";

        ResourceConfig config = new ResourceConfig()
            .packages("br.com.fiap.agrocompartilha.resource", "br.com.fiap.agrocompartilha.resource.mapper");

        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(baseUri), config);

        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdownNow));

        System.out.println("=================================================");
        System.out.println(" API do AgroCompartilha rodando!");
        System.out.println(" Teste: http://localhost:" + porta + "/api/equipamentos");
        System.out.println(" Abra o index.html da pasta frontend.");
        System.out.println(" Pressione Ctrl+C para parar.");
        System.out.println("=================================================");

        Thread.currentThread().join();
    }
}
