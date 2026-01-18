package io.orchestra.infra.security;

import io.orchestra.infra.persistence.entity.TenantEntity;
import io.orchestra.infra.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain
    ) throws ServletException, IOException
    {
        String keyRequest = request.getHeader("x-api-key");
        String keyResponse = "orchestra-livre-123";

        if (keyRequest == null || keyRequest.isEmpty()){
            response.sendError(401, "chave não encontrada");
            return;
        }

        if (keyResponse.equals(keyRequest)){
            TenantEntity tenant = new TenantEntity();
            tenant.setName("Teste");
            TenantContext.set(tenant);
        }

        else {
            response.sendError(401, "Chave incorreta");
            return;
        }

        try{
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
