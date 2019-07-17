package firebase.app.pruebas2.Entidades.firebase.logica;

import com.google.firebase.database.ServerValue;

public class Mensaje {

    private String mensaje;
    private String keyEmisor;
    private Object createdTimestamp;
    private boolean ConFoto;
    private String UrlFoto;
    private String NameFoto;
    private boolean IsAudio;
    private String UrlAudio;
    private String NameAudio;

    public Mensaje() {
        createdTimestamp = ServerValue.TIMESTAMP;
    }

    public Mensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getKeyEmisor() {
        return keyEmisor;
    }

    public void setKeyEmisor(String keyEmisor) {
        this.keyEmisor = keyEmisor;
    }

    public Object getCreatedTimestamp() {
        return createdTimestamp;
    }

    public boolean isConFoto() {
        return ConFoto;
    }

    public void setConFoto(boolean conFoto) {
        ConFoto = conFoto;
    }

    public String getUrlFoto() {
        return UrlFoto;
    }

    public void setUrlFoto(String urlFoto) {
        UrlFoto = urlFoto;
    }

    public String getNameFoto() {
        return NameFoto;
    }

    public void setNameFoto(String nameFoto) {
        NameFoto = nameFoto;
    }

    public boolean isAudio() {
        return IsAudio;
    }

    public void setAudio(boolean audio) {
        IsAudio = audio;
    }

    public String getUrlAudio() {
        return UrlAudio;
    }

    public void setUrlAudio(String urlAudio) {
        UrlAudio = urlAudio;
    }

    public String getNameAudio() {
        return NameAudio;
    }

    public void setNameAudio(String nameAudio) {
        NameAudio = nameAudio;
    }
}
