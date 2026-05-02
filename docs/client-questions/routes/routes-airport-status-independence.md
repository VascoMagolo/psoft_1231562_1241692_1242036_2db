# WP#3A - Flight Routes

## Question

O enunciado refere que as rotas só podem ser criadas entre aeroportos registados e que os aeroportos têm um estado operacional (ex: fechado, em manutenção). No Modelo de Domínio, uma ROUTE deve ser invalidada automaticamente se um dos seus aeroportos (origem ou destino) mudar para o estado 'fechado'? Adicionalmente, a distância de uma rota é um valor fixo definido no momento da criação ou deve ser calculada dinamicamente com base nas coordenadas dos aeroportos?

## Answer

Viva. As rotas existem independentemente do estado operacional do aeroporto. Por exemplo: a rota LIS–OPO continua a existir mesmo que o aeroporto do Porto esteja temporariamente fechado para manutenção. No entanto, os voos agendados nessa rota poderiam ser afetados.

Quanto à distância, deve ser um valor fixo calculado no momento da criação da rota. Creio que podemos assumir que os aeroportos não mudam de lugar.
