package com.sequenceiq.cloudbreak.blueprint.knox;

import static com.sequenceiq.cloudbreak.api.model.stack.instance.InstanceGroupType.CORE;
import static com.sequenceiq.cloudbreak.api.model.stack.instance.InstanceGroupType.GATEWAY;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.sequenceiq.cloudbreak.TestUtil;
import com.sequenceiq.cloudbreak.blueprint.BlueprintPreparationObject;
import com.sequenceiq.cloudbreak.blueprint.BlueprintPreparationObject.Builder;
import com.sequenceiq.cloudbreak.blueprint.BlueprintTextProcessor;
import com.sequenceiq.cloudbreak.blueprint.filesystem.BlueprintTestUtil;
import com.sequenceiq.cloudbreak.blueprint.template.views.HostgroupView;
import com.sequenceiq.cloudbreak.blueprint.templates.GeneralClusterConfigs;
import com.sequenceiq.cloudbreak.domain.Blueprint;
import com.sequenceiq.cloudbreak.util.FileReaderUtils;
import com.sequenceiq.cloudbreak.util.JsonUtil;

public class KnoxConfigProviderTest {

    private KnoxConfigProvider configProvider = new KnoxConfigProvider();

    @Test
    public void extendBluePrintWithKnoxGatewayForMaster() throws IOException {
        String baseBlueprint = FileReaderUtils.readFileFromClasspath("blueprints-jackson/test-bp-with-core-site-with-knox.bp");
        String expectedBlueprint = FileReaderUtils.readFileFromClasspath("blueprints-jackson/test-bp-with-core-site-with-knox-result.bp");
        Blueprint blueprint = TestUtil.blueprint("name", baseBlueprint);
        BlueprintPreparationObject object = buildPreparationObjectWithGateway();

        BlueprintTextProcessor b = new BlueprintTextProcessor(blueprint.getBlueprintText());
        String actualBlueprint = configProvider.customTextManipulation(object, b).asText();

        JsonNode expectedNode = JsonUtil.readTree(expectedBlueprint);
        JsonNode resultNode = JsonUtil.readTree(actualBlueprint);
        Assert.assertEquals(expectedNode, resultNode);
    }

    @Test
    public void specialConditionFalse() {
        BlueprintPreparationObject object = Builder.builder()
                .withGeneralClusterConfigs(BlueprintTestUtil.generalClusterConfigs())
                .build();
        assertFalse(configProvider.specialCondition(object, ""));
    }

    @Test
    public void specialConditionTrue() {
        BlueprintPreparationObject object = buildPreparationObjectWithGateway();
        assertTrue(configProvider.specialCondition(object, ""));
    }

    private BlueprintPreparationObject buildPreparationObjectWithGateway() {
        GeneralClusterConfigs config = BlueprintTestUtil.generalClusterConfigs();
        Set<HostgroupView> hostGroupsView = new HashSet<>();
        HostgroupView hg1 = new HostgroupView("master", 0,  GATEWAY, 2);
        HostgroupView hg2 = new HostgroupView("slave_1", 0,  CORE, 2);
        HostgroupView hg3 = new HostgroupView("slave_2", 0,  GATEWAY, 2);
        hostGroupsView.add(hg1);
        hostGroupsView.add(hg2);
        hostGroupsView.add(hg3);
        config.setGatewayInstanceMetadataPresented(true);
        return Builder.builder()
                .withGeneralClusterConfigs(config)
                .withHostgroupViews(hostGroupsView)
                .build();
    }
}