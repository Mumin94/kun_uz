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


    public SmsSendResponseDTO send(SmsSendRequestDTO request) {
        SmsSendResponseDTO result = restClient.post().uri(smsServiceBaseUrl + "/api/v1/sms/send").contentType(MediaType.APPLICATION_JSON).body(request).retrieve().body(SmsSendResponseDTO.class);
        if (result == null) throw new BadRequestException("SMS service returned an empty response.");
        if (!result.isSuccess()) throw new BadRequestException("SMS service could not accept the message.");
        return result;
    }
}
