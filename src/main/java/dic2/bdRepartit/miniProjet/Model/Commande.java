package dic2.bdRepartit.miniProjet.Model;

import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class Commande implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long idClient;
    private LocalDateTime dateCommande;
    private String statut;
    private Double montantTotal;
    private String ville;
    private List<CommandeDetail> details;
    private List<StockRepartition> stockRepartitions;

    @Override
    public String toString() {
        return "Commande [id=" + id + ", idClient=" + idClient + ", dateCommande=" + dateCommande +
                ", statut=" + statut + ", montantTotal=" + montantTotal + ", ville=" + ville + "]";
    }
}