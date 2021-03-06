package com.sequenceiq.cloudbreak.blueprint.filesystem.adls;

import java.util.Collection;

import com.sequenceiq.cloudbreak.api.model.filesystem.AdlsFileSystem;
import com.sequenceiq.cloudbreak.api.model.filesystem.FileSystemType;
import com.sequenceiq.cloudbreak.blueprint.filesystem.BaseFileSystemConfigurationsView;
import com.sequenceiq.cloudbreak.blueprint.filesystem.StorageLocationView;

public class AdlsFileSystemConfigurationsView extends BaseFileSystemConfigurationsView {

    private String accountName;

    private String clientId;

    private String credential;

    private String tenantId;

    private String adlsTrackingClusterNameKey;

    private String resourceGroupName;

    private String adlsTrackingClusterTypeKey;

    public AdlsFileSystemConfigurationsView(AdlsFileSystem adlsFileSystem, Collection<StorageLocationView> locations, boolean deafultFs) {
        super(adlsFileSystem.getStorageContainer(), deafultFs, locations);
        this.accountName = adlsFileSystem.getAccountName();
        this.clientId = adlsFileSystem.getClientId();
        this.credential = adlsFileSystem.getCredential();
        this.tenantId = adlsFileSystem.getTenantId();
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getCredential() {
        return credential;
    }

    public void setCredential(String credential) {
        this.credential = credential;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getAdlsTrackingClusterNameKey() {
        return adlsTrackingClusterNameKey;
    }

    public void setAdlsTrackingClusterNameKey(String adlsTrackingClusterNameKey) {
        this.adlsTrackingClusterNameKey = adlsTrackingClusterNameKey;
    }

    public String getResourceGroupName() {
        return resourceGroupName;
    }

    public void setResourceGroupName(String resourceGroupName) {
        this.resourceGroupName = resourceGroupName;
    }

    public String getAdlsTrackingClusterTypeKey() {
        return adlsTrackingClusterTypeKey;
    }

    public void setAdlsTrackingClusterTypeKey(String adlsTrackingClusterTypeKey) {
        this.adlsTrackingClusterTypeKey = adlsTrackingClusterTypeKey;
    }

    @Override
    public String getType() {
        return FileSystemType.ADLS.name();
    }
}
