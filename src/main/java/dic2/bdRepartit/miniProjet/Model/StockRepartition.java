package dic2.bdRepartit.miniProjet.Model;

import lombok.*;
import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class StockRepartition implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long idCommande;
    private Long idProduit;
    private Integer quantite;
    private String villeSource;
    private String villeDestination;

    @Override
    public String toString() {
        return "StockRepartition [id=" + id + ", idCommande=" + idCommande + ", idProduit=" + idProduit +
                ", quantite=" + quantite + ", villeSource=" + villeSource + ", villeDestination=" + villeDestination + "]";
    }
}