package delivery_payment_service.application.controller;

import delivery_payment_service.application.service.ClientAddressService;
import delivery_payment_service.domain.model.ClientAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/addresses")
public class ClientAddressController {

    @Autowired
    private ClientAddressService addressService;

    @GetMapping
    public ResponseEntity<List<ClientAddress>> getAll() {
        return ResponseEntity.ok(addressService.getAllAddresses());
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<ClientAddress>> getByClientId(@PathVariable Long clientId) {
        return ResponseEntity.ok(addressService.getAddressesByClientId(clientId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientAddress> getById(@PathVariable Long id) {
        ClientAddress address = addressService.getAddressById(id);
        return address != null ? ResponseEntity.ok(address) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ClientAddress address,
                                    @RequestHeader("Authorization") String token) {
        try {
            ClientAddress saved = addressService.createAddress(address, token);
            return ResponseEntity.ok(saved);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody ClientAddress address) {
        try {
            ClientAddress updated = addressService.updateAddress(id, address);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return ResponseEntity.noContent().build();
    }
}
