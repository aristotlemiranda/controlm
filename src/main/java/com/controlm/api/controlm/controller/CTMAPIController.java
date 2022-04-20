package com.controlm.api.controlm.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
public class CTMAPIController {

    static ControlMAgentSession controlMAgentSession = new ControlMAgentSession();
    static JsonObject jsonObject;


    @GetMapping("/sample")
    public void login() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException, JsonProcessingException {
        System.out.println("TEST");
        RestTemplate restTemplate = restTemplate();
        //ControlMAgentSession controlMAgentSession = new ControlMAgentSession();
        controlMAgentSession.setUsername("workbench");
        controlMAgentSession.setPassword("workbench");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> params = new HashMap<>();
        params.put("username", controlMAgentSession.getUsername());
        params.put("password", controlMAgentSession.getPassword());
        String postForObject = new ObjectMapper().writeValueAsString(params);

        HttpEntity<String> request = new HttpEntity<>(postForObject, headers);
        String url = "https://localhost:8443/automation-api/session/login";
        controlMAgentSession = restTemplate.postForObject(url, request, ControlMAgentSession.class);
        controlMAgentSession.setToken(controlMAgentSession.getToken());
        System.out.println("token -> " + controlMAgentSession.getToken());
        //this.logout();
    }

    @GetMapping("/deploy/get")
    public void deployGet() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException, JsonProcessingException {
        login();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer ".concat(controlMAgentSession.getToken()));
        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

        Map<String, String> uriVariables = new HashMap<>();
        uriVariables.put("format", "json");
        uriVariables.put("folder", "MMARIS_JOB_FOLDER");
        uriVariables.put("ctm", "workbench");
        //curl -X "https://localhost:8443/automation-api/deploy/jobs?format=json&folder=MMARIS_JOB_FOLDER&ctm=workbench"
        String url =  "https://localhost:8443/automation-api/deploy/jobs?format={format}&folder={folder}&ctm={ctm}";
        ResponseEntity<String> responseEntity = restTemplate().exchange(url, HttpMethod.GET, entity, String.class, uriVariables);
        jsonObject = JsonParser.parseString(responseEntity.getBody()).getAsJsonObject();
        System.out.println("DeployGet -> " + jsonObject.toString());
    }


    public void logout() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        System.out.println("Logging out");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer ".concat(controlMAgentSession.getToken()));
        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
        String url = "https://localhost:8443/automation-api/session/logout";
        ResponseEntity<ResponseMessage> responseEntity = restTemplate().postForEntity(url, entity, ResponseMessage.class);
        System.out.println("status code -> " + responseEntity.getStatusCode());
        System.out.println("body -> " + responseEntity.getBody());
    }


    @PostMapping("/deploy/transform")
    public void deployTransform() throws IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        String descriptor = "{\n" +
                "\n" +
                "\"DeployDescriptor\":\n" +
                "\n" +
                "        [\n" +
                "\n" +
                "                {\n" +
                "\t\t\t\t\t\t\"ApplyOn\": {\n" +
                "\t\t\t\t\t\t\t\"@\": \"RTGS-SGD-STRLMT\"\n" +
                "\t\t\t\t\t\t},\t\n" +
                "                        \"Property\":\"$.When.FromTime\",\n" +
                "                        \"Assign\": \"2222\"\n" +
                "\n" +
                "                }\n" +
                "\n" +
                "        ]\n" +
                "\n" +
                "}";

        byte[] definitionsFile = jsonObject.toString().getBytes();
        byte[] descriptorFile = descriptor.toString().getBytes();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("Authorization", "Bearer ".concat(controlMAgentSession.getToken()));

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("definitionsFile", createFileTempResource(definitionsFile));
        map.add("deployDescriptorFile", createFileTempResource(descriptorFile));

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(map, headers);
        ParameterizedTypeReference<String> typeReference = new ParameterizedTypeReference<String>() {};

        String url =  "https://localhost:8443/automation-api/deploy/transform";

        ResponseEntity<String> exchangeResponse = restTemplate().exchange(url, HttpMethod.POST, entity, typeReference);
        System.out.println("Response statusCode -> " + exchangeResponse.getStatusCode());
        System.out.println("Response body -> " + exchangeResponse.getBody());
    }

    @PostMapping("/deploy")
    public void deploy() throws IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        String descriptor = "{\n" +
                "\n" +
                "\"DeployDescriptor\":\n" +
                "\n" +
                "        [\n" +
                "\n" +
                "                {\n" +
                "\t\t\t\t\t\t\"ApplyOn\": {\n" +
                "\t\t\t\t\t\t\t\"@\": \"RTGS-SGD-STRLMT\"\n" +
                "\t\t\t\t\t\t},\t\n" +
                "                        \"Property\":\"$.When.FromTime\",\n" +
                "                        \"Assign\": \"2222\"\n" +
                "\n" +
                "                }\n" +
                "\n" +
                "        ]\n" +
                "\n" +
                "}";

        byte[] definitionsFile = jsonObject.toString().getBytes();
        byte[] descriptorFile = descriptor.toString().getBytes();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("Authorization", "Bearer ".concat(controlMAgentSession.getToken()));

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("definitionsFile", createFileTempResource(definitionsFile));
        map.add("deployDescriptorFile", createFileTempResource(descriptorFile));

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(map, headers);
        ParameterizedTypeReference<String> typeReference = new ParameterizedTypeReference<String>() {};

        String url =  "https://localhost:8443/automation-api/deploy";

        ResponseEntity<String> exchangeResponse = restTemplate().exchange(url, HttpMethod.POST, entity, typeReference);
        System.out.println("Response statusCode -> " + exchangeResponse.getStatusCode());
        System.out.println("Response body -> " + exchangeResponse.getBody());
    }


    private Resource createFileTempResource(byte[] content) throws IOException {
        Path tempFile = Files.createTempFile("DUMMY_LOCATION", ".json");
        Files.write(tempFile, content);
        return new FileSystemResource(tempFile.toFile());
    }

    public RestTemplate restTemplate()
            throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

        SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
                .loadTrustMaterial(null, acceptingTrustStrategy)
                .build();

        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());

        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(csf)
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory();

        requestFactory.setHttpClient(httpClient);
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        return restTemplate;
    }
}
