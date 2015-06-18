package com.sequenceiq.cloudbreak.service.stack.connector.gcp;

import static com.sequenceiq.cloudbreak.domain.CloudPlatform.GCP;
import static com.sequenceiq.cloudbreak.service.stack.flow.InstanceSyncState.DELETED;
import static com.sequenceiq.cloudbreak.service.stack.flow.InstanceSyncState.RUNNING;
import static com.sequenceiq.cloudbreak.service.stack.flow.InstanceSyncState.STOPPED;

import java.io.IOException;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.google.api.services.compute.Compute;
import com.sequenceiq.cloudbreak.domain.CloudPlatform;
import com.sequenceiq.cloudbreak.domain.CloudRegion;
import com.sequenceiq.cloudbreak.domain.GcpCredential;
import com.sequenceiq.cloudbreak.domain.Stack;
import com.sequenceiq.cloudbreak.repository.ResourceRepository;
import com.sequenceiq.cloudbreak.repository.StackRepository;
import com.sequenceiq.cloudbreak.service.stack.connector.SyncStateFinder;
import com.sequenceiq.cloudbreak.service.stack.flow.InstanceSyncState;

@Service
public class GcpSyncStateFinder implements SyncStateFinder {

    @Inject
    private ResourceRepository resourceRepository;
    @Inject
    private StackRepository stackRepository;
    @Inject
    private GcpStackUtil gcpStackUtil;

    public InstanceSyncState getState(Long stackId, String instanceId) {
        Stack stack = stackRepository.findOneWithLists(stackId);
        GcpCredential credential = (GcpCredential) stack.getCredential();
        InstanceSyncState instanceSyncState = DELETED;
        Compute compute = gcpStackUtil.buildCompute(credential);
        try {
            Compute.Instances.Get get = compute.instances().get(credential.getProjectId(), CloudRegion.valueOf(stack.getRegion()).value(), instanceId);
            if ("RUNNING".equals(get.execute().getStatus())) {
                instanceSyncState = RUNNING;
            } else if ("TERMINATED".equals(get.execute().getStatus())) {
                instanceSyncState = STOPPED;
            }
        } catch (IOException e) {
            instanceSyncState = DELETED;
        }
        return instanceSyncState;
    }

    @Override
    public CloudPlatform cloudPlatform() {
        return GCP;
    }
}
