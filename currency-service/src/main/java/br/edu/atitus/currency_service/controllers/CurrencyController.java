package br.edu.atitus.currency_service.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.atitus.currency_service.clients.CurrencyBCClient;
import br.edu.atitus.currency_service.clients.CurrencyBCResponse;
import br.edu.atitus.currency_service.entities.CurrencyEntity;
import br.edu.atitus.currency_service.repositories.CurrencyRepository;
import br.edu.atitus.currency_service.utils.DateUtils;

@RestController
@RequestMapping("currency")
public class CurrencyController {

    private final CurrencyRepository repository;
    private final CurrencyBCClient currencyBCClient;
    private final CacheManager cacheManager;

    @Value("${server.port}")
    private int serverPort;

    private static final int MAX_TENTATIVAS = 7; // retrocede até 7 dias úteis

    public CurrencyController(CurrencyRepository repository, CurrencyBCClient currencyBCClient, CacheManager cacheManager) {
        super();
        this.repository = repository;
        this.currencyBCClient = currencyBCClient;
        this.cacheManager = cacheManager;
    }

    @GetMapping("/{value}/{source}/{target}")
    public ResponseEntity<CurrencyEntity> getConversion(
            @PathVariable double value,
            @PathVariable String source,
            @PathVariable String target) throws Exception {

        source = source.toUpperCase();
        target = target.toUpperCase();
        String dataSource = "None";
        String nameCache = "Currency";
        String keyCache = source + target;

        // verifica cache
        CurrencyEntity currency = cacheManager.getCache(nameCache).get(keyCache, CurrencyEntity.class);

        if (currency != null) {
            dataSource = "Cache";
        } else {
            currency = new CurrencyEntity();
            currency.setSource(source);
            currency.setTarget(target);

            if (source.equals(target)) {
                currency.setConversionRate(1);
                dataSource = "Same currency";
            } else {
                try {
                	double curSource = source.equals("BRL") ? 1 : buscarCotacaoComRetrocesso(source);
                	double curTarget = target.equals("BRL") ? 1 : buscarCotacaoComRetrocesso(target);


                    // consulta moeda de origem com retrocesso
                	if (!source.equals("BRL")) System.out.println("Buscando cotacao de " + source);
                	if (!target.equals("BRL")) System.out.println("Buscando cotacao de " + target);


                    currency.setConversionRate(curSource / curTarget);
                    dataSource = "API BCB";

                } catch (Exception e) {
                    // fallback banco local
                    currency = repository.findBySourceAndTarget(source, target)
                            .orElseThrow(() -> new Exception("Currency Unsupported"));
                    dataSource = "Local Database";
                }
            }

            // salva no cache
            cacheManager.getCache(nameCache).put(keyCache, currency);
        }

        currency.setConvertedValue(value * currency.getConversionRate());
        currency.setEnvironment("Currency running in port: " + serverPort + " - DataSource: " + dataSource);

        return ResponseEntity.ok(currency);
    }

    /**
     * Tenta buscar a cotação retrocedendo até MAX_TENTATIVAS dias úteis
     */
    private double buscarCotacaoComRetrocesso(String moeda) throws Exception {
        for (int i = 0; i < MAX_TENTATIVAS; i++) {
            String dataCotacao = DateUtils.getUltimoDiaUtilRetroativo(i);
            CurrencyBCResponse resp = currencyBCClient.getCurrency(moeda, dataCotacao);
            if (!resp.getValue().isEmpty()) {
                return resp.getValue().get(0).getCotacaoVenda();
            }
        }
        throw new Exception("Currency not found for " + moeda + " in last " + MAX_TENTATIVAS + " business days");
    }
}
