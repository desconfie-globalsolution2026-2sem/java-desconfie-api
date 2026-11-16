package br.com.fiap.desconfie.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Libera o front-end (que roda em outro endereço, como a Vercel) para chamar a API.
 *
 * O navegador bloqueia chamadas entre endereços diferentes, a menos que a API responda
 * com os cabeçalhos Access-Control-*. Antes de um POST/PUT/DELETE o navegador manda uma
 * requisição OPTIONS ("posso?"), que é respondida aqui mesmo com 200.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest requisicao, HttpServletResponse resposta, FilterChain cadeia)
            throws ServletException, IOException {

        resposta.setHeader("Access-Control-Allow-Origin", "*");
        resposta.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
        resposta.setHeader("Access-Control-Allow-Headers", "Content-Type");
        // Permite que o front leia o cabeçalho Location devolvido nos cadastros (201)
        resposta.setHeader("Access-Control-Expose-Headers", "Location");
        resposta.setHeader("Access-Control-Max-Age", "3600");

        if ("OPTIONS".equalsIgnoreCase(requisicao.getMethod())) {
            resposta.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        cadeia.doFilter(requisicao, resposta);
    }
}
