package dev.onyxstudios.foml.obj;

import de.javagl.obj.FloatTuple;
import de.javagl.obj.Mtl;

public class FOMLMaterial implements Mtl {
    private final Mtl material;
    private int tintIndex = -1;
    private boolean useDiffuseColor = false;

    public FOMLMaterial(Mtl material) {
        this.material = material;
    }

    @Override
    public String getName() {
        return material.getName();
    }

    @Override
    public FloatTuple getKa() {
        return material.getKa();
    }

    @Override
    public void setKa(float ka0, float ka1, float ka2) {
        material.setKa(ka0, ka1, ka2);
    }

    @Override
    public FloatTuple getKs() {
        return material.getKs();
    }

    @Override
    public void setKs(float ks0, float ks1, float ks2) {
        material.setKs(ks0, ks1, ks2);
    }

    @Override
    public FloatTuple getKd() {
        return material.getKd();
    }

    @Override
    public void setKd(float kd0, float kd1, float kd2) {
        material.setKd(kd0, kd1, kd2);
    }

    @Override
    public String getMapKd() {
        return material.getMapKd();
    }

    @Override
    public void setMapKd(String mapKd) {
        material.setMapKd(mapKd);
    }

    @Override
    public float getNs() {
        return material.getNs();
    }

    @Override
    public void setNs(float ns) {
        material.setNs(ns);
    }

    @Override
    public float getD() {
        return material.getD();
    }

    @Override
    public void setD(float d) {
        material.setD(d);
    }

    public int getTintIndex() {
        return this.tintIndex;
    }

    public void setTintIndex(int tintIndex) {
        this.tintIndex = tintIndex;
    }

    public boolean useDiffuseColor() {
        return this.useDiffuseColor;
    }

    public void setUseDiffuseColor() {
        this.useDiffuseColor = true;
    }
}
