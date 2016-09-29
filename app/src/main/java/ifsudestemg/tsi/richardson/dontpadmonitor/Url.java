// Armazena um website e o seu conteúdo
package ifsudestemg.tsi.richardson.dontpadmonitor;

/**
 * Created by richardson on 9/29/16.
 */
public class Url {

    private int id;
    private String url,conteudo;

    public Url(int id, String url, String conteudo) {
        this.id = id;

        this.url = url;
        this.conteudo = conteudo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    @Override
    public String toString() {
        return "Url{" +
                "url='" + url + '\'' +
                ", conteudo='" + conteudo + '\'' +
                '}';
    }
}
