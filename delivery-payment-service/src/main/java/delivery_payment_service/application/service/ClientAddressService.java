package delivery_payment_service.application.service;

import delivery_payment_service.domain.model.ClientAddress;
import delivery_payment_service.domain.repository.ClientAddressRepository;
import delivery_payment_service.infrastructure.client.UserClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientAddressService {

    @Autowired
    private ClientAddressRepository addressRepository;

    @Autowired
    private UserClient userClient;

    public List<ClientAddress> getAllAddresses() {
        return addressRepository.findAll();
    }

    public List<ClientAddress> getAddressesByClientId(Long clientId) {
        return addressRepository.findByClientId(clientId);
    }

    public ClientAddress getAddressById(Long id) {
        return addressRepository.findById(id).orElse(null);
    }

    public ClientAddress createAddress(ClientAddress address, String jwtToken) {
        if (!userClient.checkClientExists(address.getClientId(), jwtToken)) {
            throw new RuntimeException("Client does not exist");
        }
        return addressRepository.save(address);
    }

    public ClientAddress updateAddress(Long id, ClientAddress newData) {
        ClientAddress existing = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found"));
        existing.setCountry(newData.getCountry());
        existing.setCounty(newData.getCounty());
        existing.setCity(newData.getCity());
        existing.setStreet(newData.getStreet());
        existing.setPostalCode(newData.getPostalCode());
        return addressRepository.save(existing);
    }

    public void deleteAddress(Long id) {
        addressRepository.deleteById(id);
    }
}
