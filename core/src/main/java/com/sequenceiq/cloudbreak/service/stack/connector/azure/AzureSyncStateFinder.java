package com.sequenceiq.cloudbreak.service.stack.connector.azure;

import static com.sequenceiq.cloudbreak.domain.CloudPlatform.AZURE;
import static com.sequenceiq.cloudbreak.service.stack.connector.azure.AzureStackUtil.NAME;
import static com.sequenceiq.cloudbreak.service.stack.connector.azure.AzureStackUtil.SERVICENAME;
import static com.sequenceiq.cloudbreak.service.stack.flow.InstanceSyncState.DELETED;
import static com.sequenceiq.cloudbreak.service.stack.flow.InstanceSyncState.RUNNING;
import static com.sequenceiq.cloudbreak.service.stack.flow.InstanceSyncState.STOPPED;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.sequenceiq.cloud.azure.client.AzureClient;
import com.sequenceiq.cloudbreak.domain.AzureCredential;
import com.sequenceiq.cloudbreak.domain.CloudPlatform;
import com.sequenceiq.cloudbreak.domain.Stack;
import com.sequenceiq.cloudbreak.repository.ResourceRepository;
import com.sequenceiq.cloudbreak.repository.StackRepository;
import com.sequenceiq.cloudbreak.service.stack.connector.SyncStateFinder;
import com.sequenceiq.cloudbreak.service.stack.flow.InstanceSyncState;

@Service
public class AzureSyncStateFinder implements SyncStateFinder {

    @Inject
    private ResourceRepository resourceRepository;
    @Inject
    private StackRepository stackRepository;
    @Inject
    private AzureStackUtil azureStackUtil;

    public InstanceSyncState getState(Long stackId, String instanceId) {
        Stack stack = stackRepository.findOneWithLists(stackId);
        Map<String, String> vmContext = createVMContext(instanceId);
        AzureCredential credential = (AzureCredential) stack.getCredential();
        AzureClient azureClient = azureStackUtil.createAzureClient(credential);
        InstanceSyncState instanceSyncState = DELETED;
        try {
            if ("Running".equals(azureClient.getVirtualMachineState(vmContext))) {
                instanceSyncState = RUNNING;
            } else if ("Suspended".equals(azureClient.getVirtualMachineState(vmContext))) {
                instanceSyncState = STOPPED;
            }
        } catch (Exception ex) {
            instanceSyncState = DELETED;
        }
        return instanceSyncState;
    }

    private Map<String, String> createVMContext(String vmName) {
        Map<String, String> context = new HashMap<>();
        context.put(SERVICENAME, vmName);
        context.put(NAME, vmName);
        return context;
    }

    @Override
    public CloudPlatform cloudPlatform() {
        return AZURE;
    }
}
