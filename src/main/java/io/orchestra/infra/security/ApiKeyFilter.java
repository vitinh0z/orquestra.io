package io.orchestra.infra.security;


import io.orchestra.infra.persistence.entity.TenantEntity;
import io.orchestra.infra.persistence.repository.tenant.TenantJpaRepository;
import io.orchestra.infra.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ApiKeyFilter extends OncePerRequestFilter {

    private final TenantJpaRepository persistenceGateway;


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain
    ) throws ServletException, IOException
    {
        String keyRequest = request.getHeader("x-api-key");

        if (keyRequest == null || keyRequest.isEmpty()){
            response.sendError(401, "chave não encontrada");
            return;
        }

        Optional<TenantEntity> tenant = persistenceGateway.findByApiKey(keyRequest);
        if (tenant.isPresent()){
           TenantEntity entity = tenant.get();

           if (!entity.isActive()){
               response.sendError(403, "Tenant inativo. Contate o suporte.");
               return;
           }

            TenantContext.set(entity);

            try{
                filterChain.doFilter(request, response);
            } finally {
                TenantContext.clear();
            }
        }
        else {
            response.sendError(401, "API Key inválida");
        }
    }
}
