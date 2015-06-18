package com.sequenceiq.cloudbreak.service.stack.flow;

import static com.sequenceiq.cloudbreak.domain.InstanceStatus.REGISTERED;
import static com.sequenceiq.cloudbreak.domain.InstanceStatus.TERMINATED;
import static com.sequenceiq.cloudbreak.domain.InstanceStatus.UNREGISTERED;
import static com.sequenceiq.cloudbreak.domain.Status.AVAILABLE;
import static com.sequenceiq.cloudbreak.domain.Status.STOPPED;

import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.domain.CloudPlatform;
import com.sequenceiq.cloudbreak.domain.InstanceGroup;
import com.sequenceiq.cloudbreak.domain.InstanceMetaData;
import com.sequenceiq.cloudbreak.domain.Stack;
import com.sequenceiq.cloudbreak.domain.Status;
import com.sequenceiq.cloudbreak.repository.InstanceGroupRepository;
import com.sequenceiq.cloudbreak.repository.InstanceMetaDataRepository;
import com.sequenceiq.cloudbreak.repository.ResourceRepository;
import com.sequenceiq.cloudbreak.repository.StackUpdater;
import com.sequenceiq.cloudbreak.service.events.CloudbreakEventService;
import com.sequenceiq.cloudbreak.service.stack.StackService;
import com.sequenceiq.cloudbreak.service.stack.connector.SyncStateFinder;

@Service
public class StackSyncService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StackSyncService.class);

    @Inject
    private StackService stackService;
    @Inject
    private StackUpdater stackUpdater;
    @Inject
    private CloudbreakEventService eventService;
    @Inject
    private InstanceMetaDataRepository instanceMetaDataRepository;
    @Inject
    private InstanceGroupRepository instanceGroupRepository;
    @Inject
    private ResourceRepository resourceRepository;

    @javax.annotation.Resource
    private Map<CloudPlatform, SyncStateFinder> syncStateFinders;

    public void sync(Long stackId) throws Exception {
        Stack stack = stackService.getById(stackId);
        String statusReason = "State of the cluster infrastructure has been synchronized.";
        for (InstanceGroup instanceGroup : stack.getInstanceGroups()) {
            for (InstanceMetaData instanceMetaData : instanceGroup.getInstanceMetaData()) {
                InstanceSyncState state = syncStateFinders.get(stack.cloudPlatform()).getState(stackId, instanceMetaData.getInstanceId());
                switch (state) {
                    case DELETED:
                        if (!TERMINATED.equals(instanceMetaData.getInstanceStatus())) {
                            instanceGroup.setNodeCount(instanceGroup.getNodeCount() - 1);
                        }
                        instanceMetaData.setInstanceStatus(TERMINATED);
                        break;
                    case RUNNING:
                        if (!REGISTERED.equals(instanceMetaData.getInstanceStatus())) {
                            instanceGroup.setNodeCount(instanceGroup.getNodeCount() + 1);
                        }
                        instanceMetaData.setInstanceStatus(REGISTERED);
                        break;
                    case STOPPED:
                        if (!UNREGISTERED.equals(instanceMetaData.getInstanceStatus())) {
                            instanceGroup.setNodeCount(instanceGroup.getNodeCount() + 1);
                        }
                        instanceMetaData.setInstanceStatus(UNREGISTERED);
                        break;
                }
                instanceMetaDataRepository.save(instanceMetaData);
            }
            if (Status.stopStatusesForUpdate().contains(stack.getStatus())) {
                stackUpdater.updateStackStatus(stack.getId(), STOPPED, statusReason);
            } else if (Status.availableStatusesForUpdate().contains(stack.getStatus())) {
                stackUpdater.updateStackStatus(stack.getId(), AVAILABLE, statusReason);
            }
        }

    }
}
