package dic2.bdRepartit.miniProjet.Controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}