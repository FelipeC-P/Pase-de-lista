package com.example.paselista;

public class AlumnoModel {
    private String name;
    private boolean asistencia;

    public AlumnoModel() {

    }

    public AlumnoModel(String name, boolean asistencia) {
        this.name = name;
        this.asistencia = asistencia;
    }

    public String getName() {
        return name;
    }

    public boolean isAsistencia() {
        return asistencia;
    }

    public void setAsistencia(boolean asistencia) {
        this.asistencia = asistencia;
    }
}
