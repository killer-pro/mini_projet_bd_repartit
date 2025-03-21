package dic2.bdRepartit.miniProjet.Controller;

import dic2.bdRepartit.miniProjet.Model.Commande;
import dic2.bdRepartit.miniProjet.Service.CommandeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CommandeController {

    @Autowired
    private CommandeService commandeService;

    /**
     * Crée une nouvelle commande
     * @param idClient ID du client qui passe la commande
     * @param ville ville du client qui passe la commande
     * @param produitsCommandes Map contenant les IDs des produits et leurs quantités
     * @return La commande créée
     */
    @PostMapping("/commandes")
    public ResponseEntity<Commande> creerCommande(
            @RequestParam Long idClient,
            @RequestParam String ville,
            @RequestBody Map<Long, Integer> produitsCommandes) {

        Commande nouvelleCommande = commandeService.creerCommande(idClient , ville , produitsCommandes);
        return new ResponseEntity<>(nouvelleCommande, HttpStatus.CREATED);
    }

    /**
     * Récupère une commande par son ID
     * @param id ID de la commande
     * @param ville Ville où la commande a été passée (paramètre optionnel)
     * @return La commande avec ses détails
     */
    @GetMapping("/commandes/{id}")
    public ResponseEntity<Commande> getCommandeById(
            @PathVariable Long id,
            @RequestParam(required = false) String ville) {
        return ResponseEntity.ok(commandeService.getCommandeById(id, ville));
    }

    /**
     * Récupère toutes les commandes d'un client
     * @param idClient ID du client
     * @return Liste des commandes du client
     */
    @GetMapping("/clients/{idClient}/commandes")
    public ResponseEntity<List<Commande>> getCommandesByClientId(@PathVariable Long idClient) {
        return ResponseEntity.ok(commandeService.getCommandesByClientId(idClient));
    }
}