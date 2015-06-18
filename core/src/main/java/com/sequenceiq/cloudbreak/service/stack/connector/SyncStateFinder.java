package com.sequenceiq.cloudbreak.service.stack.connector;

import com.sequenceiq.cloudbreak.domain.CloudPlatform;
import com.sequenceiq.cloudbreak.service.stack.flow.InstanceSyncState;

public interface SyncStateFinder {

    InstanceSyncState getState(Long stackId, String instanceId);

    CloudPlatform cloudPlatform();

}
