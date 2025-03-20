package dic2.bdRepartit.miniProjet.Model;

import lombok.*;
import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class Client implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String nom;
    private String ville;
    private String email;

    @Override
    public String toString() {
        return "Client [id=" + id + ", nom=" + nom + ", ville=" + ville + ", email=" + email + "]";
    }
}
