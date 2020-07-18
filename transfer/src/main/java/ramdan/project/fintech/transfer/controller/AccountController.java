package ramdan.project.fintech.transfer.controller;

import lombok.val;
import lombok.var;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import ramdan.project.fintech.transfer.domain.Detail;
import ramdan.project.fintech.transfer.domain.Journal;
import ramdan.project.fintech.transfer.dto.*;
import ramdan.project.fintech.transfer.exception.OptimisticLockingFailure;
import ramdan.project.fintech.transfer.mapper.AccountMapper;
import ramdan.project.fintech.transfer.mapper.DetailMapper;
import ramdan.project.fintech.transfer.mapper.JournalMapper;
import ramdan.project.fintech.transfer.repository.AccountRepositry;
import ramdan.project.fintech.transfer.repository.DetailRepository;
import ramdan.project.fintech.transfer.repository.JournalRepository;
import ramdan.project.fintech.transfer.utils.PropertiesUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@RestController
public class AccountController {

    @Autowired
    private AccountRepositry accountRepositry;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private DetailRepository detailRepositry;

    @Autowired
    private DetailMapper detailMapper;

    @GetMapping("/account/{account}")
    public ResponseEntity<AccountDto> get(@PathVariable String account) {
        val result = accountMapper.toDto(accountRepositry.getOne(account));
        return ResponseEntity.ok(result);
    }
    @PostMapping("/account/create")
    public ResponseEntity<AccountDto> create(@RequestBody AccountDto dto) {
        val account = accountMapper.toEntity(dto);
        account.setBalance(BigDecimal.ZERO);
        if(account.getLimit()==null) {
            account.setLimit(BigDecimal.ZERO);
        }
        val result = accountMapper.toDto(accountRepositry.saveAndFlush(account));
        return ResponseEntity.ok(result);
    }
    @Transactional()
    @PostMapping("/account/update")
    public ResponseEntity<AccountDto> update(@RequestBody AccountDto dto) {
        val accountUpdate = accountMapper.toEntity(dto);
        var account = accountRepositry.getOne(accountUpdate.getNumber());
        if(!account.getVersion().equals(accountUpdate.getVersion())){
            throw new OptimisticLockingFailure();
        }
        // not update balance
        accountUpdate.setBalance(null);
        account = PropertiesUtils.copyNotNullProperties(accountUpdate,account);
        account = accountRepositry.save(account);
        try{
            accountRepositry.flush();
        }
        catch(OptimisticLockingFailureException ex){
            throw new OptimisticLockingFailure();
        }
        val result = accountMapper.toDto(account);
        return ResponseEntity.ok(result);
    }

    @Transactional(readOnly = true)
    @GetMapping("/account/history/{account}")
    public ResponseEntity<List<DetailDto>> history(
            @PathVariable String account,
            @RequestParam @DateTimeFormat(pattern="yyyy-MM-dd") Date from,
            @RequestParam @DateTimeFormat(pattern="yyyy-MM-dd")Date to) {

        val details = detailRepositry.findAllByAccount(account,from,to);
        return ResponseEntity.ok(detailMapper.toDto(details));
   }
}