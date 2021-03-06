package ramdan.project.fintech.transfer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DetailDto {
    private String number;
    private Date date;
    private String account;
    private BigDecimal amount;
    private BigDecimal balance;
    private String remark1;
    private String remark2;
}
