package ramdan.project.fintech.transfer;

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ramdan.project.fintech.transfer.controller.TransferController;
import ramdan.project.fintech.transfer.dto.*;
import ramdan.project.fintech.transfer.repository.AccountRepositry;

import javax.transaction.Transactional;
import java.util.Date;

import static javax.transaction.Transactional.TxType.REQUIRED;
import static javax.transaction.Transactional.TxType.SUPPORTS;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class TransferApplicationTests {

	@Autowired
	private AccountRepositry accountRepositry;

	@Autowired
	private TransferController transferController;

	@Test
	@DisplayName("TransferCommand money success")
	@Transactional(REQUIRED)
	void transfer_Money_success(){
		//accountRepositry.getOne("123456789").getBalance();
		val source = accountRepositry.getOne("123456789").getBalance();
		val beneficiary = accountRepositry.getOne("234567891").getBalance();
		val input = TransferCommand.builder()
				.no("TEST-1")
				.type(Type.TRANSFER)
				.source("123456789")
				.beneficiary("234567891")
				.amount(10.0)
				.build();

		transferController.transfer(input);

		assertEquals(source - 10.0,
				accountRepositry.getOne("123456789").getBalance());
		assertEquals( beneficiary + 10.0 ,
				accountRepositry.getOne("234567891").getBalance());

	}
	@Test
	@DisplayName("TransferCommand money 2 success")
	void transfer_Money2_success(){

		//val source = transferController.balance("123456789");
		//val beneficiary = transferController.balance("234567891");
		val input = TransferCommand.builder()
				.no("TEST-2")
				.type(Type.TRANSFER)
				.date(new Date())
				.source("123456789")
				.beneficiary("234567891")
				.amount(10.0)
				.build();

		val trx = transferController.transfer(input).getBody();

		val result = transferController.getJournal("TEST-2").getBody();

		assertEquals(
				JournalDto
						.builder()
						.number("TEST-2")
						.date(trx.getDate())
						.details(
								new DetailDto[]{
										DetailDto.builder()
												.number(trx.getNo())
												.account("123456789")
												.date(trx.getDate())
												.amount(-10.0)
												.build(),
										DetailDto.builder()
												.number(trx.getNo())
												.account("234567891")
												.date(trx.getDate())
												.amount(10.0)
												.build()
								}
						)
						.build()
				,result);
	}
}
