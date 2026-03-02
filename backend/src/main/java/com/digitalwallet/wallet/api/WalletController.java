package com.oepfelbaum.digitalwallet.wallet.api;


import com.oepfelbaum.digitalwallet.wallet.dto.WalletAccountDetailsDto;
import com.oepfelbaum.digitalwallet.wallet.dto.WalletOverviewDto;
import com.oepfelbaum.digitalwallet.wallet.service.WalletService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService wallet;


    public WalletController(WalletService wallet) {
        this.wallet = wallet;

    }

    private void requireConsent() {
        if (!wallet.isConnected()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not connected");
        }
    }


    @GetMapping("/overview")
    public WalletOverviewDto overview() {
        requireConsent();
        return wallet.getOverview();
    }

    @DeleteMapping("/logout")
    public Map<String, Object> logout() {
        wallet.disconnect();
        return Map.of("status", "logged_out");
    }

    @PostMapping("/connect")
    public Map<String, Object> connect() {
        wallet.connect();
        return Map.of("status", "connected");
    }



    @GetMapping("/accounts/{accountId}")
    public WalletAccountDetailsDto accountDetails(
            @PathVariable String accountId,
            @RequestParam(required = false, defaultValue = "desc") String sort
    ) {
        requireConsent();
        return wallet.getAccountDetails(accountId, sort);
    }
}
