package dic2.bdRepartit.miniProjet.Model;


import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class Produit implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String nom;
    private String description;
    private Double prix;
    private Integer stock;

    @Override
    public String toString() {
        return "Produit [id=" + id + ", nom=" + nom + ", description=" + description + ", prix=" + prix + ", stock=" + stock + "]";
    }
}