package br.edu.atitus.product_service.clients;

import org.springframework.stereotype.Component;

@Component
public class CurrencyClientFallback implements CurrencyClient {

    @Override
    public CurrencyResponse getCurrency(double value, String source, String target) {
        CurrencyResponse fallback = new CurrencyResponse();
        fallback.setConvertedValue(-1.0);
        fallback.setEnviroment("Currency-service offline (Fallback)");
        return fallback;
    }
}
