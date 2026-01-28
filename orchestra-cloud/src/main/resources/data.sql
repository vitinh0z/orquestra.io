INSERT INTO tenants (tenant_id, name, api_key, is_active)
VALUES (
           '11111111-1111-1111-1111-111111111111',
           'Tenant de Desenvolvimento',
           'orchestra-livre-123',
           true
);


INSERT INTO gateways (id, tenant_id, gateway_name, encrypted_credential, priority, is_active)
VALUES (
           random_uuid(),
           '11111111-1111-1111-1111-111111111111',
           'STRIPE',
           '{"secretKey": "sk_test_fake_token_123"}',
           1,
           true
       );