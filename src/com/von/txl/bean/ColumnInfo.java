package com.von.txl.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ColumnInfo {
    private String headerName;
    private int maxLength;
    private List<String> values = new ArrayList<>();

    public ColumnInfo() {
    }

    public ColumnInfo(String headerName, int maxLength, List<String> values) {
        this.headerName = headerName;
        this.maxLength = maxLength;
        this.values = values;
    }

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColumnInfo that = (ColumnInfo) o;
        return headerName.equals(that.headerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(headerName);
    }

    @Override
    public String toString() {
        return "ColumnInfo{" +
                "headerName='" + headerName + '\'' +
                ", maxLength=" + maxLength +
                ", values=" + values +
                '}';
    }
}
