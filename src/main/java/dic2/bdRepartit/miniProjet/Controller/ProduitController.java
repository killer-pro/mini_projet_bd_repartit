package dic2.bdRepartit.miniProjet.Controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import dic2.bdRepartit.miniProjet.Model.Produit;
import dic2.bdRepartit.miniProjet.Service.ProduitService;

@RestController
@RequestMapping("/api")
public class ProduitController {

    @Autowired
    private ProduitService produitService;

    @GetMapping("/produits")
    public ResponseEntity<List<Produit>> getAllProduits() {
        return ResponseEntity.ok(produitService.getAllProduits());
    }

    @GetMapping("/produits/{id}")
    public ResponseEntity<Produit> getProduitById(@PathVariable Long id) {
        return ResponseEntity.ok(produitService.getProduitById(id));
    }

    @GetMapping("/produits/disponibles")
    public ResponseEntity<List<Produit>> getProduitsDisponibles(@RequestParam(required = false) String ville) {
        return ResponseEntity.ok(produitService.getProduitsDisponibles(ville));
    }

    @GetMapping("/clients/{clientId}/produits-recommandes")
    public ResponseEntity<List<Produit>> getProduitsRecommandes(@PathVariable Long clientId) {
        return ResponseEntity.ok(produitService.getProduitsRecommandesForClient(clientId));
    }

    @PostMapping("/produits")
    public ResponseEntity<Produit> createProduit(@RequestBody Produit produit) {
        produit.setId(null);
        Produit newProduit = produitService.createProduit(produit);
        return new ResponseEntity<>(newProduit, HttpStatus.CREATED);
    }

    @PostMapping("/produits/{id}/stock")
    public ResponseEntity<Produit> updateStock(
            @PathVariable Long id,
            @RequestParam Integer quantity,
            @RequestParam String ville) {
        Produit updatedProduit = produitService.updateStock(id, quantity, ville);
        return ResponseEntity.ok(updatedProduit);
    }

    @PostMapping("/produits/{id}/transfer")
    public ResponseEntity<String> transferStock(
            @PathVariable Long id,
            @RequestParam Integer quantity,
            @RequestParam String fromVille,
            @RequestParam String toVille) {
        produitService.transferStock(id, quantity, fromVille, toVille);
        return ResponseEntity.ok("Transfert de stock effectué avec succès");
    }

}