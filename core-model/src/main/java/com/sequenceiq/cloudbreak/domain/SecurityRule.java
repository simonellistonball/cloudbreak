package com.sequenceiq.cloudbreak.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class SecurityRule implements ProvisionEntity {

    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne
    @Column(nullable = false)
    private SecurityGroup securityGroup;
    private String cidr;
    private String ports;
    private String protocol;
    private boolean modifiable;

    public SecurityRule() {
    }

    public SecurityRule(String cidr) {
        this(cidr, true, null);
    }

    public SecurityRule(String cidr, boolean modifiable, SecurityGroup securityGroup) {
        this.cidr = cidr;
        this.securityGroup = securityGroup;
        this.modifiable = modifiable;
    }

    public Long getId() {
        return id;
    }

    public String getCidr() {
        return cidr;
    }

    public SecurityGroup getSecurityGroup() {
        return securityGroup;
    }

    public void setSecurityGroup(SecurityGroup securityGroup) {
        this.securityGroup = securityGroup;
    }

    public String getPorts() {
        return ports;
    }

    public void setPorts(String ports) {
        this.ports = ports;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public boolean isModifiable() {
        return modifiable;
    }

    public void setModifiable(boolean modifiable) {
        this.modifiable = modifiable;
    }

    @Override
    public String toString() {
        return "SecurityRule{"
                + "id=" + id
                + ", securityGroup=" + securityGroup
                + ", cidr='" + cidr + '\''
                + ", ports='" + ports + '\''
                + ", protocol='" + protocol + '\''
                + ", modifiable=" + modifiable
                + '}';
    }
}
