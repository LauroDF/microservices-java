package br.edu.atitus.product_service.services;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import br.edu.atitus.product_service.clients.CurrencyClient;
import br.edu.atitus.product_service.clients.CurrencyResponse;
import br.edu.atitus.product_service.entities.ProductEntity;
import br.edu.atitus.product_service.repositories.ProductRepository;

//import java.util.List;

@Service
public class ProductService {

    private final ProductRepository repository;
    private final CurrencyClient currencyClient;

    public ProductService(ProductRepository repository, CurrencyClient currencyClient) {
        this.repository = repository;
        this.currencyClient = currencyClient;
    }

    // Circuit Breaker + Cache aplicados aqui
    @Cacheable(value = "productConversionCache", key = "#idProduct + '-' + #targetCurrency")
    @CircuitBreaker(name = "currencyService", fallbackMethod = "currencyFallback")
    public ProductEntity getProductWithConversion(Long idProduct, String targetCurrency, String serverPort) throws Exception {

        ProductEntity product = repository.findById(idProduct)
                .orElseThrow(() -> new Exception("Product not found"));

        product.setEnviroment("Product-service running on Port: " + serverPort);

        if (targetCurrency.equalsIgnoreCase(product.getCurrency())) {
            product.setConvertedPrice(product.getPrice());
        } else {
            CurrencyResponse currency = currencyClient.getCurrency(
                    product.getPrice(),
                    product.getCurrency(),
                    targetCurrency
            );

            product.setConvertedPrice(currency.getConvertedValue());
            product.setEnviroment(product.getEnviroment() + " - " + currency.getEnviroment());
        }

        return product;
    }

    // Fallback quando o currency-service está fora
    public ProductEntity currencyFallback(Long idProduct, String targetCurrency, String serverPort, Throwable t) throws Exception {
        ProductEntity product = repository.findById(idProduct)
                .orElseThrow(() -> new Exception("Product not found"));

        product.setConvertedPrice(-1);
        product.setEnviroment("Product-service running on Port: " + serverPort + " - Currency-service offline (Fallback)");
        return product;
    }
    
    public ProductEntity getProductById(Long idProduct) throws Exception {
        return repository.findById(idProduct)
                .orElseThrow(() -> new Exception("Produto não encontrado"));
    }
    
    public Page<ProductEntity> getAllProductsWithConversion(Pageable pageable, String targetCurrency, String serverPort) throws Exception {
        Page<ProductEntity> products = repository.findAll(pageable);

        for (ProductEntity product : products) {
            try {
                if (targetCurrency.equalsIgnoreCase(product.getCurrency())) {
                    product.setConvertedPrice(product.getPrice());
                } else {
                    CurrencyResponse currency = currencyClient.getCurrency(
                            product.getPrice(),
                            product.getCurrency(),
                            targetCurrency
                    );
                    product.setConvertedPrice(currency.getConvertedValue());
                    product.setEnviroment("Product-service running on Port: " + serverPort + " - " + currency.getEnviroment());
                }
            } catch (Exception e) {
                product.setConvertedPrice(-1);
                product.setEnviroment("Product-service running on Port: " + serverPort + " - Currency unavailable");
            }
        }

        return products;
    }

    
}
