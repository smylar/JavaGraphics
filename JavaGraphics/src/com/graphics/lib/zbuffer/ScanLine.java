package com.graphics.lib.zbuffer;

import com.graphics.lib.LineEquation;

public final class ScanLine {
    
    private final Double startY;
    private final Double endY;
    private final Double startZ;
    private final Double endZ;
    private final LineEquation startLine;
    private final LineEquation endLine;
    
    public ScanLine(Double startY, Double endY, Double startZ, Double endZ,
            LineEquation startLine, LineEquation endLine) {
        this.startY = startY;
        this.endY = endY;
        this.startZ = startZ;
        this.endZ = endZ;
        this.startLine = startLine;
        this.endLine = endLine;
    }

    public Double getStartY() {
        return startY;
    }

    public Double getEndY() {
        return endY;
    }

    public Double getStartZ() {
        return startZ;
    }

    public Double getEndZ() {
        return endZ;
    }

    public LineEquation getStartLine() {
        return startLine;
    }

    public LineEquation getEndLine() {
        return endLine;
    }
    
    static Builder builder() {
        return new Builder();
    }
    
    static class Builder {
        public Double startY = null;
        public Double endY = null;
        public Double startZ = null;
        public Double endZ = null;
        public LineEquation startLine = null;
        public LineEquation endLine = null;
        
        public ScanLine build() {
            return new ScanLine(startY, endY, startZ, endZ, startLine, endLine);
        }
    }
}
