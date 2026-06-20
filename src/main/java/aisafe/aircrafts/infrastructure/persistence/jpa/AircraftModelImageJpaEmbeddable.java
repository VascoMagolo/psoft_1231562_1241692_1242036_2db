package aisafe.aircrafts.infrastructure.persistence.jpa;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;

@Embeddable
public class AircraftModelImageJpaEmbeddable {

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "image")
    private byte[] bytes;

    @Column(name = "image_content_type")
    private String contentType;

    public AircraftModelImageJpaEmbeddable() {}

    public byte[] getBytes() { return bytes; }
    public void setBytes(byte[] bytes) { this.bytes = bytes; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
}
