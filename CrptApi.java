package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

class CrptApi {
    private static final String REQUEST_URL = "https://ismp.crpt.ru/api/v3/lk/documents/create";
    private final FixedWindowRequestLimiter requestLimiter;
    private final Serializer serializer;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        if (requestLimit <= 0) {
            throw new RuntimeException("Request limit must be positive");
        }
        this.serializer = new DocumentSerializer();
        this.requestLimiter = new FixedWindowRequestLimiter(timeUnit.toMillis(1), requestLimit);
    }

    public void createDocument(DocumentDto documentDto) throws IOException, InterruptedException {
        if (requestLimiter.allowRequest()) {
            var json = serializer.serialize(documentDto);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(REQUEST_URL))
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            try (HttpClient client = HttpClient.newHttpClient()) {
                client.send(request, HttpResponse.BodyHandlers.discarding());
            }

            System.out.println("OK\n");
        } else {
            System.out.println("Limit\n");
        }
    }

    private static DocumentDto createExampleDocument() {
        var json = """
                    {"description": { "participantInn": "string" }, "doc_id": "string", "doc_status": "string",
                    "doc_type": "LP_INTRODUCE_GOODS", "importRequest": true,
                    "owner_inn": "string", "participant_inn": "string", "producer_inn":
                    "string", "production_date": "2020-01-23", "production_type": "string",
                    "products": [ { "certificate_document": "string",
                    "certificate_document_date": "2020-01-23",
                    "certificate_document_number": "string", "owner_inn": "string",
                    "producer_inn": "string", "production_date": "2020-01-23",
                    "tnved_code": "string", "uit_code": "string", "uitu_code": "string" } ],
                    "reg_date": "2020-01-23", "reg_number": "string"}
                    """;
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, DocumentDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        var api = new CrptApi(TimeUnit.MINUTES, 3);
        var exampleDocument = createExampleDocument();

        // OK
        api.createDocument(exampleDocument);
        api.createDocument(exampleDocument);
        api.createDocument(exampleDocument);

        // Limit
        api.createDocument(exampleDocument);
    }

}

interface RequestLimiter {
    boolean allowRequest();
}

class FixedWindowRequestLimiter implements RequestLimiter {
    private long startTime = 0;
    private final int requestLimit;
    private final long timeInterval;
    private final AtomicInteger requestCount = new AtomicInteger(0);

    public FixedWindowRequestLimiter(long timeLimit, int requestLimit) {
        this.timeInterval = timeLimit;
        this.requestLimit = requestLimit;
    }

    @Override
    public synchronized boolean allowRequest() {
        resetIfTimeIntervalPassed();
        return requestCount.incrementAndGet() <= requestLimit;
    }

    private synchronized void resetIfTimeIntervalPassed() {
        var elapsedTime = System.currentTimeMillis() - startTime;
        if (elapsedTime > timeInterval) {
            requestCount.set(0);
            startTime = System.currentTimeMillis();
        }
    }
}

interface Serializer {
    String serialize(Object documentDto);
}

class DocumentSerializer implements Serializer {
    @Override
    public String serialize(Object documentDto) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(documentDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}


class DocumentDto {
    private DescriptionDto description;
    private String doc_id;
    private String doc_status;
    private String doc_type;
    private boolean importRequest;
    private String owner_inn;
    private String participant_inn;
    private String producer_inn;
    private Date production_date;
    private String production_type;
    private List<ProductDto> products;
    private Date reg_date;
    private String reg_number;

    public DocumentDto() { }

    public DocumentDto(DescriptionDto description, String doc_id, String doc_status, String doc_type, boolean importRequest, String owner_inn, String participant_inn, String producer_inn, Date production_date, String production_type, List<ProductDto> products, Date reg_date, String reg_number) {
        this.description = description;
        this.doc_id = doc_id;
        this.doc_status = doc_status;
        this.doc_type = doc_type;
        this.importRequest = importRequest;
        this.owner_inn = owner_inn;
        this.participant_inn = participant_inn;
        this.producer_inn = producer_inn;
        this.production_date = production_date;
        this.production_type = production_type;
        this.products = products;
        this.reg_date = reg_date;
        this.reg_number = reg_number;
    }

    public DescriptionDto getDescription() {
        return description;
    }

    public void setDescription(DescriptionDto description) {
        this.description = description;
    }

    public String getDoc_id() {
        return doc_id;
    }

    public void setDoc_id(String doc_id) {
        this.doc_id = doc_id;
    }

    public String getDoc_status() {
        return doc_status;
    }

    public void setDoc_status(String doc_status) {
        this.doc_status = doc_status;
    }

    public String getDoc_type() {
        return doc_type;
    }

    public void setDoc_type(String doc_type) {
        this.doc_type = doc_type;
    }

    public boolean isImportRequest() {
        return importRequest;
    }

    public void setImportRequest(boolean importRequest) {
        this.importRequest = importRequest;
    }

    public String getOwner_inn() {
        return owner_inn;
    }

    public void setOwner_inn(String owner_inn) {
        this.owner_inn = owner_inn;
    }

    public String getParticipant_inn() {
        return participant_inn;
    }

    public void setParticipant_inn(String participant_inn) {
        this.participant_inn = participant_inn;
    }

    public String getProducer_inn() {
        return producer_inn;
    }

    public void setProducer_inn(String producer_inn) {
        this.producer_inn = producer_inn;
    }

    public Date getProduction_date() {
        return production_date;
    }

    public void setProduction_date(Date production_date) {
        this.production_date = production_date;
    }

    public String getProduction_type() {
        return production_type;
    }

    public void setProduction_type(String production_type) {
        this.production_type = production_type;
    }

    public List<ProductDto> getProducts() {
        return products;
    }

    public void setProducts(List<ProductDto> products) {
        this.products = products;
    }

    public Date getReg_date() {
        return reg_date;
    }

    public void setReg_date(Date reg_date) {
        this.reg_date = reg_date;
    }

    public String getReg_number() {
        return reg_number;
    }

    public void setReg_number(String reg_number) {
        this.reg_number = reg_number;
    }
}

class DescriptionDto {
    public String participantInn;

    public DescriptionDto() { }

    public DescriptionDto(String participantInn) {
        this.participantInn = participantInn;
    }

    public String getParticipantInn() {
        return participantInn;
    }

    public void setParticipantInn(String participantInn) {
        this.participantInn = participantInn;
    }
}

class ProductDto {
    private String certificate_document;
    private Date certificate_document_date;
    private String certificate_document_number;
    private String owner_inn;
    private String producer_inn;
    private Date production_date;
    private String tnved_code;
    private String uit_code;
    private String uitu_code;

    public ProductDto() { }

    public ProductDto(String certificate_document, Date certificate_document_date, String certificate_document_number, String owner_inn, String producer_inn, Date production_date, String tnved_code, String uit_code, String uitu_code) {
        this.certificate_document = certificate_document;
        this.certificate_document_date = certificate_document_date;
        this.certificate_document_number = certificate_document_number;
        this.owner_inn = owner_inn;
        this.producer_inn = producer_inn;
        this.production_date = production_date;
        this.tnved_code = tnved_code;
        this.uit_code = uit_code;
        this.uitu_code = uitu_code;
    }

    public String getCertificate_document() {
        return certificate_document;
    }

    public void setCertificate_document(String certificate_document) {
        this.certificate_document = certificate_document;
    }

    public Date getCertificate_document_date() {
        return certificate_document_date;
    }

    public void setCertificate_document_date(Date certificate_document_date) {
        this.certificate_document_date = certificate_document_date;
    }

    public String getCertificate_document_number() {
        return certificate_document_number;
    }

    public void setCertificate_document_number(String certificate_document_number) {
        this.certificate_document_number = certificate_document_number;
    }

    public String getOwner_inn() {
        return owner_inn;
    }

    public void setOwner_inn(String owner_inn) {
        this.owner_inn = owner_inn;
    }

    public String getProducer_inn() {
        return producer_inn;
    }

    public void setProducer_inn(String producer_inn) {
        this.producer_inn = producer_inn;
    }

    public Date getProduction_date() {
        return production_date;
    }

    public void setProduction_date(Date production_date) {
        this.production_date = production_date;
    }

    public String getTnved_code() {
        return tnved_code;
    }

    public void setTnved_code(String tnved_code) {
        this.tnved_code = tnved_code;
    }

    public String getUit_code() {
        return uit_code;
    }

    public void setUit_code(String uit_code) {
        this.uit_code = uit_code;
    }

    public String getUitu_code() {
        return uitu_code;
    }

    public void setUitu_code(String uitu_code) {
        this.uitu_code = uitu_code;
    }
}