package dic2.bdRepartit.miniProjet.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import dic2.bdRepartit.miniProjet.Model.Client;
import dic2.bdRepartit.miniProjet.Service.ClientService;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @PostMapping("/clients")
    public ResponseEntity<Client> createClient(@RequestBody Client client) {
        try {
            Client newClient = clientService.createClient(client);
            return new ResponseEntity<>(newClient, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Gestion des erreurs de validation
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            // Gestion des autres erreurs
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erreur lors de la création du client: " + e.getMessage());
        }
    }
}