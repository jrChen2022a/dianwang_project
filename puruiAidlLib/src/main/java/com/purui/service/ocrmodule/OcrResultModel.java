package com.purui.service.ocrmodule;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;

class OcrResultModel {
    private List<Point> points;
    private List<Integer> wordIndex;
    private String label;
    private float confidence;
    private float cls_idx;
    private String cls_label;
    private float cls_confidence;

    protected OcrResultModel() {
        super();
        points = new ArrayList<>();
        wordIndex = new ArrayList<>();
    }

    protected void addPoints(int x, int y) {
        Point point = new Point(x, y);
        points.add(point);
    }

    protected void addWordIndex(int index) {
        wordIndex.add(index);
    }

    protected List<Point> getPoints() {
        return points;
    }

    protected List<Integer> getWordIndex() {
        return wordIndex;
    }

    protected String getLabel() {
        return label;
    }

    protected void setLabel(String label) {
        this.label = label;
    }

    protected float getConfidence() {
        return confidence;
    }

    protected void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    protected float getClsIdx() {
        return cls_idx;
    }

    protected void setClsIdx(float idx) {
        this.cls_idx = idx;
    }

    protected String getClsLabel() {
        return cls_label;
    }

    protected void setClsLabel(String label) {
        this.cls_label = label;
    }

    protected float getClsConfidence() {
        return cls_confidence;
    }

    protected void setClsConfidence(float confidence) {
        this.cls_confidence = confidence;
    }
}
