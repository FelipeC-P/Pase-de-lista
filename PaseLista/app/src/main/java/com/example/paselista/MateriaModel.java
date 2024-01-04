package com.example.paselista;

public class MateriaModel {
    private String name;
    public MateriaModel(){
        this.name="";
    }

    public MateriaModel(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
