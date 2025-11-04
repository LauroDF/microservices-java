package br.edu.atitus.product_service.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.atitus.product_service.entities.ProductEntity;
import br.edu.atitus.product_service.services.ProductService;

@RestController
@RequestMapping("products")
public class OpenProductController {

    private final ProductService productService;

    @Value("${server.port}")
    private int serverPort;

    public OpenProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{idProduct}/{targetCurrency}")
    public ResponseEntity<ProductEntity> getProduct(
            @PathVariable Long idProduct,
            @PathVariable String targetCurrency
    ) throws Exception {

        ProductEntity product = productService.getProductWithConversion(
                idProduct,
                targetCurrency,
                String.valueOf(serverPort)
        );

        return ResponseEntity.ok(product);
    }

    @GetMapping("/noconverter/{idProduct}")
    public ResponseEntity<ProductEntity> getNoConverter(@PathVariable Long idProduct) throws Exception {
        ProductEntity product = productService.getProductById(idProduct);
        product.setConvertedPrice(-1);
        product.setEnviroment("Product-service running on Port: " + serverPort);
        return ResponseEntity.ok(product);
    }
    
    @GetMapping("/{targetCurrency}")
    public ResponseEntity<Page<ProductEntity>> getAllProducts(
            @PathVariable String targetCurrency,
            @PageableDefault(page = 0, size = 5, sort = "description", direction = Direction.ASC)
            Pageable pageable
    ) throws Exception {
        
        // Busca paginada dos produtos
        Page<ProductEntity> products = productService.getAllProductsWithConversion(pageable, targetCurrency, String.valueOf(serverPort));

        return ResponseEntity.ok(products);
    }

    
}
