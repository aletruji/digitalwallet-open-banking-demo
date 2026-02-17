package com.oepfelbaum.digitalwallet.natwest.api;

import com.oepfelbaum.digitalwallet.natwest.NatWestAuthService;
import com.oepfelbaum.digitalwallet.natwest.NatWestDataService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/natwest")
public class NatWestController {

    private final NatWestAuthService auth;
    private final NatWestDataService data;

    public NatWestController(NatWestAuthService auth, NatWestDataService data) {
        this.auth = auth;
        this.data = data;
    }

    @PostMapping("/connect")
    public Map<String, Object> connect() {
        return auth.connect();
    }

    @GetMapping("/accounts")
    public Map<String, Object> accounts() {
        return data.accounts();
    }

    @GetMapping("/accounts/{accountId}/balances")
    public Map<String, Object> balances(@PathVariable String accountId) {
        return data.balances(accountId);
    }

    @GetMapping("/accounts/{accountId}/transactions")
    public Map<String, Object> transactions(@PathVariable String accountId) {
        return data.transactions(accountId);
    }
}
