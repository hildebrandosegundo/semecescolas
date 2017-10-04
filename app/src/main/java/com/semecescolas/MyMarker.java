package com.semecescolas;

/**
 * Created by hildebrandosegundo on 08/09/16.
 */
public class MyMarker
{
    private String mLabel;
    private String mIcon;
    private String codSemec;
    private String telefones;
    private String ideb;
    private String aluno;
    private String turmas;
    private String salas;
    private Double mLatitude;
    private Double mLongitude;

    public MyMarker(String icon,String label,String codSemec,String ideb, String telefones,String aluno,String turmas,String salas,Double latitude, Double longitude)
    {
        this.mLabel = label;
        this.mLatitude = latitude;
        this.mLongitude = longitude;
        this.mIcon = icon;
        this.codSemec = codSemec;
        this.telefones = telefones;
        this.ideb = ideb;
        this.aluno = aluno;
        this.turmas = turmas;
        this.salas = salas;

    }

    public String getIdeb() {return ideb;}

    public void setIdeb(String ideb) {this.ideb = ideb;}

    public String getCodSemec() {
        return codSemec;
    }

    public void setCodSemec(String codSemec) {
        this.codSemec = codSemec;
    }

    public String getTelefones() {
        return telefones;
    }

    public void setTelefones(String telefones) {
        this.telefones = telefones;
    }

    public String getAluno() {
        return aluno;
    }

    public void setAluno(String aluno) {
        this.aluno = aluno;
    }

    public String getTurmas() {
        return turmas;
    }

    public void setTurmas(String turmas) {
        this.turmas = turmas;
    }

    public String getSalas() {
        return salas;
    }

    public void setSalas(String salas) {
        this.salas = salas;
    }

    public String getmLabel()
    {
        return mLabel;
    }

    public void setmLabel(String mLabel)
    {
        this.mLabel = mLabel;
    }

    public String getmIcon()
    {
        return mIcon;
    }

    public void setmIcon(String icon)
    {
        this.mIcon = icon;
    }

    public Double getmLatitude()
    {
        return mLatitude;
    }

    public void setmLatitude(Double mLatitude)
    {
        this.mLatitude = mLatitude;
    }

    public Double getmLongitude()
    {
        return mLongitude;
    }

    public void setmLongitude(Double mLongitude)
    {
        this.mLongitude = mLongitude;
    }
}