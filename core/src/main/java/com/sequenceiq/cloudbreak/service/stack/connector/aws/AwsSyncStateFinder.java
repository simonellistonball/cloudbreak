package com.sequenceiq.cloudbreak.service.stack.connector.aws;

import static com.sequenceiq.cloudbreak.domain.CloudPlatform.AWS;
import static com.sequenceiq.cloudbreak.service.stack.flow.InstanceSyncState.DELETED;
import static com.sequenceiq.cloudbreak.service.stack.flow.InstanceSyncState.RUNNING;
import static com.sequenceiq.cloudbreak.service.stack.flow.InstanceSyncState.STOPPED;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.sequenceiq.cloudbreak.domain.AwsCredential;
import com.sequenceiq.cloudbreak.domain.CloudPlatform;
import com.sequenceiq.cloudbreak.domain.Stack;
import com.sequenceiq.cloudbreak.repository.StackRepository;
import com.sequenceiq.cloudbreak.service.stack.connector.SyncStateFinder;
import com.sequenceiq.cloudbreak.service.stack.flow.InstanceSyncState;

@Service
public class AwsSyncStateFinder implements SyncStateFinder {

    @Inject
    private StackRepository stackRepository;
    @Inject
    private AwsStackUtil awsStackUtil;

    public InstanceSyncState getState(Long stackId, String instanceId) {
        InstanceSyncState instanceSyncState = DELETED;
        Stack stack = stackRepository.findOneWithLists(stackId);
        Regions region = Regions.valueOf(stack.getRegion());
        AwsCredential credential = (AwsCredential) stack.getCredential();
        AmazonEC2Client amazonEC2Client = awsStackUtil.createEC2Client(region, credential);
        DescribeInstancesResult describeInstances = amazonEC2Client.describeInstances(new DescribeInstancesRequest().withInstanceIds(instanceId));
        try {
            Instance instance = describeInstances.getReservations().iterator().next().getInstances().iterator().next();
            if ("Stopped".equals(instance.getState().getName())) {
                instanceSyncState = STOPPED;
            } else if ("Running".equals(instance.getState().getName())) {
                instanceSyncState = RUNNING;
            }
        } catch (Exception ex) {
            instanceSyncState = DELETED;
        }
        return instanceSyncState;
    }

    @Override
    public CloudPlatform cloudPlatform() {
        return AWS;
    }
}
