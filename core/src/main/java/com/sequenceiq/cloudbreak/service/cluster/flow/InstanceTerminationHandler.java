package com.sequenceiq.cloudbreak.service.cluster.flow;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.domain.CloudPlatform;
import com.sequenceiq.cloudbreak.domain.InstanceGroup;
import com.sequenceiq.cloudbreak.domain.InstanceMetaData;
import com.sequenceiq.cloudbreak.domain.InstanceStatus;
import com.sequenceiq.cloudbreak.domain.Resource;
import com.sequenceiq.cloudbreak.domain.Stack;
import com.sequenceiq.cloudbreak.domain.Status;
import com.sequenceiq.cloudbreak.repository.HostMetadataRepository;
import com.sequenceiq.cloudbreak.repository.InstanceGroupRepository;
import com.sequenceiq.cloudbreak.repository.InstanceMetaDataRepository;
import com.sequenceiq.cloudbreak.repository.ResourceRepository;
import com.sequenceiq.cloudbreak.service.events.CloudbreakEventService;
import com.sequenceiq.cloudbreak.service.stack.connector.CloudPlatformConnector;
import com.sequenceiq.cloudbreak.service.stack.resource.ResourceBuilder;
import com.sequenceiq.cloudbreak.service.stack.resource.ResourceBuilderInit;

@Service
public class InstanceTerminationHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstanceTerminationHandler.class);

    @Inject
    private ResourceRepository resourceRepository;
    @Inject
    private InstanceMetaDataRepository instanceMetaDataRepository;
    @Inject
    private InstanceGroupRepository instanceGroupRepository;
    @Inject
    private HostMetadataRepository hostMetadataRepository;
    @javax.annotation.Resource
    private Map<CloudPlatform, List<ResourceBuilder>> instanceResourceBuilders;
    @javax.annotation.Resource
    private Map<CloudPlatform, ResourceBuilderInit> resourceBuilderInits;
    @javax.annotation.Resource
    private Map<CloudPlatform, CloudPlatformConnector> cloudPlatformConnectors;
    @Inject
    private CloudbreakEventService eventService;

    public void terminateInstance(Stack stack, InstanceMetaData instanceMetaData) {
        String message = String.format("Terminate instance %s.", instanceMetaData.getInstanceId());
        LOGGER.info(message);
        eventService.fireCloudbreakEvent(stack.getId(), Status.UPDATE_IN_PROGRESS.name(), message);
        Resource resource = resourceRepository.findByStackIdAndName(stack.getId(), instanceMetaData.getInstanceId());
        InstanceGroup ig = instanceGroupRepository.findOneByGroupNameInStack(stack.getId(), instanceMetaData.getInstanceGroup().getGroupName());
        ig.setNodeCount(ig.getNodeCount() - 1);
        instanceGroupRepository.save(ig);
        message = String.format("Delete '%s' node. and Decrease the nodecount on %s instancegroup",
                instanceMetaData.getInstanceId(), ig.getGroupName());
        LOGGER.info(message);
        eventService.fireCloudbreakEvent(stack.getId(), Status.UPDATE_IN_PROGRESS.name(), message);
        deleteResourceAndDependencies(stack, instanceMetaData);
        if (resourceRepository.findOne(resource.getId()) != null) {
            resourceRepository.delete(resource.getId());
        }
        instanceMetaData.setInstanceStatus(InstanceStatus.TERMINATED);
        instanceMetaDataRepository.save(instanceMetaData);
        LOGGER.info(String.format("The status of instanceMetadata with %s id and %s name setted to TERMINATED.",
                instanceMetaData.getId(), instanceMetaData.getInstanceId()));
    }

    private void deleteResourceAndDependencies(Stack stack, InstanceMetaData instanceMetaData) {
        LOGGER.info(String.format("Instance %s rollback started.", instanceMetaData.getInstanceId()));
        CloudPlatformConnector cloudPlatformConnector = cloudPlatformConnectors.get(stack.cloudPlatform());
        Set<String> instanceIds = new HashSet<>();
        instanceIds.add(instanceMetaData.getInstanceId());
        cloudPlatformConnector.removeInstances(stack, instanceIds, instanceMetaData.getInstanceGroup().getGroupName());
        LOGGER.info(String.format("Instance deleted with %s id and %s name.", instanceMetaData.getId(), instanceMetaData.getInstanceId()));
    }

}
