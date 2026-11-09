package dasturlash.uz.dto.request;

import dasturlash.uz.enums.ProfileRole;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class ProfileFilterRequestDTO {

    private String name;
    private String surname;
    private String phone;
    private ProfileRole role;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate createdDateFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate createdDateTo;


}
