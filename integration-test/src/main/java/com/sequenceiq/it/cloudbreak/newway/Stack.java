package com.sequenceiq.it.cloudbreak.newway;

import static com.sequenceiq.it.cloudbreak.CloudbreakUtil.waitAndCheckClusterStatus;
import static com.sequenceiq.it.cloudbreak.CloudbreakUtil.waitAndCheckStackStatus;
import static com.sequenceiq.it.cloudbreak.CloudbreakUtil.waitAndExpectClusterFailure;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import com.sequenceiq.cloudbreak.api.model.ImageJson;
import com.sequenceiq.cloudbreak.api.model.stack.hardware.HardwareInfoGroupResponse;
import com.sequenceiq.cloudbreak.api.model.stack.hardware.HardwareInfoResponse;
import com.sequenceiq.cloudbreak.api.model.stack.instance.InstanceGroupResponse;
import com.sequenceiq.cloudbreak.api.model.stack.instance.InstanceMetaDataJson;
import com.sequenceiq.it.IntegrationTestContext;
import com.sequenceiq.it.cloudbreak.CloudbreakUtil;
import com.sequenceiq.it.cloudbreak.SshService;

public class Stack extends StackEntity {
    private static final Logger LOGGER = LoggerFactory.getLogger(Stack.class);

    public static Function<IntegrationTestContext, Stack> getTestContextStack(String key) {
        return testContext -> testContext.getContextParam(key, Stack.class);
    }

    static Function<IntegrationTestContext, Stack> getTestContextStack() {
        return getTestContextStack(StackEntity.STACK);
    }

    static Function<IntegrationTestContext, Stack> getNewStack() {
        return testContext -> new Stack();
    }

    public static Stack request() {
        return new Stack();
    }

    public static Stack isCreated() {
        Stack stack = new Stack();
        stack.setCreationStrategy(StackAction::createInGiven);
        return stack;
    }

    public static Action<Stack> post(String key) {
        return new Action<>(getTestContextStack(key), new StackPostStrategy());
    }

    public static Action<Stack> post(Strategy strategy) {
        return new Action<>(getTestContextStack(STACK), strategy);
    }

    public static Action<Stack> post() {
        return post(STACK);
    }

    public static Action<Stack> get(String key) {
        return new Action<>(getTestContextStack(key), StackAction::get);
    }

    public static Action<Stack> get(Strategy strategy) {
        return new Action<>(getTestContextStack(STACK), strategy);
    }

    public static Action<Stack> get() {
        return get(STACK);
    }

    public static Action<Stack> getAll() {
        return new Action<>(getNewStack(), StackAction::getAll);
    }

    public static Action<Stack> delete(String key, Strategy strategy) {
        return new Action<>(getTestContextStack(key), strategy);
    }

    public static Action<Stack> delete(String key) {
        return delete(key, StackAction::delete);
    }

    public static Action<Stack> delete() {
        return delete(STACK);
    }

    public static Action<Stack> delete(Strategy strategy) {
        return delete(STACK, strategy);
    }

    public static Action<Stack> makeNodeUnhealthy(String  hostgroup, int nodeCount) {
        return new Action<>(getTestContextStack(STACK), new UnhealthyNodeStrategy(hostgroup, nodeCount));
    }

    public static Assertion<Stack> assertThis(BiConsumer<Stack, IntegrationTestContext> check) {
        return new Assertion<>(getTestContextStack(GherkinTest.RESULT), check);
    }

    public static Assertion<Stack> waitAndCheckClusterAndStackAvailabilityStatus() {
        return assertThis((stack, t) -> {
            CloudbreakClient client = CloudbreakClient.getTestContextCloudbreakClient().apply(t);
            Assert.assertNotNull(stack.getResponse().getId());
            waitAndCheckStackStatus(client.getCloudbreakClient(), stack.getResponse().getId().toString(), "AVAILABLE");
            waitAndCheckClusterStatus(client.getCloudbreakClient(), stack.getResponse().getId().toString(), "AVAILABLE");
            waitAndCheckStackStatus(client.getCloudbreakClient(), stack.getResponse().getId().toString(), "AVAILABLE");
        });
    }

    public static Assertion<Stack> waitAndCheckClusterIsAvailable() {
        return assertThis((stack, t) -> {
            CloudbreakClient client = CloudbreakClient.getTestContextCloudbreakClient().apply(t);
            Assert.assertNotNull(stack.getResponse().getId());
            waitAndCheckClusterStatus(client.getCloudbreakClient(), stack.getResponse().getId().toString(), "AVAILABLE");
        });
    }

    public static Assertion<Stack> waitAndCheckStackIsAvailable() {
        return assertThis((stack, t) -> {
            CloudbreakClient client = CloudbreakClient.getTestContextCloudbreakClient().apply(t);
            Assert.assertNotNull(stack.getResponse().getId());
            waitAndCheckStackStatus(client.getCloudbreakClient(), stack.getResponse().getId().toString(), "AVAILABLE");
        });
    }

    public static Assertion<Stack> waitAndCheckClusterFailure(String keyword) {
        return assertThis((stack, t) -> {
            CloudbreakClient client = CloudbreakClient.getTestContextCloudbreakClient().apply(t);
            Assert.assertNotNull(stack.getResponse().getId());
            waitAndCheckStackStatus(client.getCloudbreakClient(), stack.getResponse().getId().toString(), "AVAILABLE");
            waitAndExpectClusterFailure(client.getCloudbreakClient(), stack.getResponse().getId().toString(), "CREATE_FAILED", keyword);
        });
    }

    public static Assertion<Stack> waitAndCheckClusterAndStackStoppedStatus() {
        return assertThis((stack, t) -> {
            CloudbreakClient client = CloudbreakClient.getTestContextCloudbreakClient().apply(t);
            Assert.assertNotNull(stack.getResponse().getId());
            waitAndCheckStackStatus(client.getCloudbreakClient(), stack.getResponse().getId().toString(), "STOPPED");
            waitAndCheckClusterStatus(client.getCloudbreakClient(), stack.getResponse().getId().toString(), "STOPPED");
            waitAndCheckStackStatus(client.getCloudbreakClient(), stack.getResponse().getId().toString(), "STOPPED");
        });
    }

    public static Assertion<Stack> waitAndCheckClusterDeleted() {
        return assertThis((stack, t) -> {
            CloudbreakClient client = CloudbreakClient.getTestContextCloudbreakClient().apply(t);
            Assert.assertNotNull(stack.getResponse().getId());
            waitAndCheckStackStatus(client.getCloudbreakClient(), stack.getResponse().getId().toString(), "DELETE_COMPLETED");
        });
    }

    public static Assertion<?> checkClusterHasAmbariRunning(String ambariPort, String ambariUser, String ambariPassword) {
        return assertThis((stack, t) -> {
            CloudbreakClient client = CloudbreakClient.getTestContextCloudbreakClient().apply(t);
            CloudbreakUtil.checkClusterAvailability(client.getCloudbreakClient().stackV2Endpoint(),
                    ambariPort,
                    stack.getResponse().getId().toString(),
                    ambariUser,
                    ambariPassword,
                    true);
        });
    }

    public static Assertion<Stack> checkClusterHasAmbariRunningThroughKnox(String ambariUser, String ambariPassword) {
        return assertThis((stack, context) -> {
            CloudbreakClient client = CloudbreakClient.getTestContextCloudbreakClient().apply(context);
            CloudbreakUtil.checkClusterAvailabilityThroughGateway(client.getCloudbreakClient().stackV2Endpoint(),
                    stack.getResponse().getId().toString(),
                    ambariUser,
                    ambariPassword);
        });
    }

    public static Assertion<Stack> checkRecipes(String[] searchOnHost, String[] files, String privateKey, String sshCommand,  int require) {
        return checkRecipes(searchOnHost, files, privateKey, Optional.ofNullable(sshCommand), require);
    }

    public static Assertion<Stack> checkRecipes(String[] searchOnHost, String[] files, String privateKey, int require) {
        return checkRecipes(searchOnHost, files, privateKey, Optional.empty(), require);
    }

    public static Assertion<Stack> checkRecipes(String[] searchOnHost, String[] files, String privateKey, Optional<String> sshCommand, int require) {
        return assertThis((stack, t) -> {
            List<String> ips = new ArrayList<>();
            List<InstanceGroupResponse> instanceGroups = stack.getResponse().getInstanceGroups();
            for (InstanceGroupResponse instanceGroup : instanceGroups) {
                if (Arrays.asList(searchOnHost).contains(instanceGroup.getGroup())) {
                    for (InstanceMetaDataJson metaData : instanceGroup.getMetadata()) {
                        ips.add(metaData.getPublicIp());
                    }
                }
            }
            int quantity = 0;
            try {
                quantity = new SshService().countFilesOnHostByExtensionAndPath(ips, files, sshCommand, privateKey, "success", "cloudbreak", 120000, require);
            } catch (Exception e) {
                LOGGER.error("Error occurred during ssh execution: " + e);
            }
            assertEquals(quantity, require, "The number of existing files is different than required.");
        });
    }

    public static Assertion<Stack> checkImage(String imageId, String imageCatalogName) {
        return assertThis((stack, t) -> {
            ImageJson image = stack.getResponse().getImage();
            assertEquals(imageId, image.getImageId());
            if (StringUtils.isNotBlank(imageCatalogName)) {
                assertEquals(imageCatalogName, image.getImageCatalogName());
            }
        });
    }

    public static Assertion<Stack> checkImagesDifferent() {
        return assertThis((stack, t) -> {
            Set<String> imageIds = stack.getResponse().getHardwareInfoGroups().stream()
                    .map(HardwareInfoGroupResponse::getHardwareInfos)
                    .map(hwInfoResponses -> hwInfoResponses.stream()
                            .map(HardwareInfoResponse::getImageId).collect(Collectors.toSet()))
                    .flatMap(Collection::stream).collect(Collectors.toSet());
            assertTrue(imageIds.size() > 1);
            assertTrue(imageIds.contains(stack.getResponse().getImage().getImageId()));
        });
    }

    public static Action<Stack> repair(String hostgroupName) {
        return new Action<>(getTestContextStack(), new RepairNodeStrategy(hostgroupName));
    }
}