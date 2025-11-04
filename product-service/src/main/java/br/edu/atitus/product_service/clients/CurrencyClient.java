package br.edu.atitus.product_service.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "currency-service", fallback = CurrencyClientFallback.class)
public interface CurrencyClient {
    @GetMapping("/currency/{value}/{source}/{target}")
    CurrencyResponse getCurrency(@PathVariable double value,
                                 @PathVariable String source,
                                 @PathVariable String target
    );
}
