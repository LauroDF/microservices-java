package br.edu.atitus.product_service.clients;

public class CurrencyResponse {

    private Long id;
    private String from;
    private String to;
    private Double conversionFactor;
    private Double convertedValue;
    private String enviroment;

    public CurrencyResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public Double getConversionFactor() { return conversionFactor; }
    public void setConversionFactor(Double conversionFactor) { this.conversionFactor = conversionFactor; }

    public Double getConvertedValue() { return convertedValue; }
    public void setConvertedValue(Double convertedValue) { this.convertedValue = convertedValue; }

    public String getEnviroment() { return enviroment; }
    public void setEnviroment(String enviroment) { this.enviroment = enviroment; }
}
