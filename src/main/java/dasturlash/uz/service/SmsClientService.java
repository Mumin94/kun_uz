package dasturlash.uz.service;

import dasturlash.uz.dto.request.SmsSendRequestDTO;
import dasturlash.uz.dto.response.SmsSendResponseDTO;
import dasturlash.uz.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class SmsClientService {
    private final RestClient restClient;
    @Value("${sms.service.base-url}")
    private String smsServiceBaseUrl;
    @Value("${sms.service.client-id}")
    private String clientId;


    public SmsSendResponseDTO send(SmsSendRequestDTO request) {
        request.setClientId(clientId);
        SmsSendResponseDTO result = restClient.post().uri(smsServiceBaseUrl + "/sms/send").contentType(MediaType.APPLICATION_JSON).body(request).retrieve().body(SmsSendResponseDTO.class);
        if (result == null) throw new BadRequestException("SMS service returned an empty response.");
        if (result.getId() == null || result.getId().isBlank()) throw new BadRequestException("SMS service could not accept the message.");
        if (!"SENT".equals(result.getStatus())) throw new BadRequestException("SMS service did not confirm sending.");
        return result;
    }
}
