package org.smartgresiter.jhpiego.util;

public class ServiceHeader implements BaseService {
    private String serviceHeaderName;

    public String getServiceHeaderName() {
        return serviceHeaderName;
    }

    public void setServiceHeaderName(String serviceHeaderName) {
        this.serviceHeaderName = serviceHeaderName;
    }

    @Override
    public int getType() {
        return TYPE_HEADER;
    }
}
