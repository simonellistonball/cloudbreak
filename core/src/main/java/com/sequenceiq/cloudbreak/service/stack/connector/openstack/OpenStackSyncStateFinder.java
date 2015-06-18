package com.sequenceiq.cloudbreak.service.stack.connector.openstack;

import static com.sequenceiq.cloudbreak.domain.CloudPlatform.OPENSTACK;
import static com.sequenceiq.cloudbreak.service.stack.flow.InstanceSyncState.RUNNING;

import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.domain.CloudPlatform;
import com.sequenceiq.cloudbreak.service.stack.connector.SyncStateFinder;
import com.sequenceiq.cloudbreak.service.stack.flow.InstanceSyncState;

@Service
public class OpenStackSyncStateFinder implements SyncStateFinder {

    public InstanceSyncState getState(Long stackId, String instanceId) {
        return RUNNING;
    }

    @Override
    public CloudPlatform cloudPlatform() {
        return OPENSTACK;
    }
}
