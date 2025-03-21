package dic2.bdRepartit.miniProjet.Model;

import lombok.*;
import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class CommandeDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long idCommande;
    private Long idProduit;
    private Integer quantite;
    private Double prixUnitaire;
    private String ville;

    @Override
    public String toString() {
        return "CommandeDetail [id=" + id + ", idCommande=" + idCommande + ", idProduit=" + idProduit +
                ", quantite=" + quantite + ", prixUnitaire=" + prixUnitaire + ", ville=" + ville + "]";
    }
}