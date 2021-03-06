package com.sequenceiq.cloudbreak.service.blueprint;

import static com.sequenceiq.cloudbreak.api.model.ResourceStatus.DEFAULT;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.sequenceiq.cloudbreak.api.model.ResourceStatus;
import com.sequenceiq.cloudbreak.common.model.user.IdentityUser;
import com.sequenceiq.cloudbreak.domain.Blueprint;
import com.sequenceiq.cloudbreak.domain.organization.Organization;
import com.sequenceiq.cloudbreak.init.blueprint.BlueprintLoaderService;
import com.sequenceiq.cloudbreak.init.blueprint.DefaultBlueprintCache;

@RunWith(MockitoJUnitRunner.class)
public class BlueprintLoaderServiceTest {

    private static final String LUCKY_MAN = "lucky_man";

    private static final String LOTTERY_WINNERS = "lottery_winners";

    private static final String JSON = "{\"validation\":{\"Blueprints\":{\"blueprint_name\":\"hdp-etl-edw-tp\","
            + "\"stack_name\":\"HDP\",\"stack_version\":\"2.5\"},\"configurations\":[{\"core-site\":{\"fs.trash.interval\":\"4320\"}},{\"hdfs-site\":"
            + "{\"dfs.namenode.safemode.threshold-pct\":\"0.99\"}},{\"hive-site\":{\"hive.exec.compress.output\":\"true\",\"hive.merge.mapfiles\""
            + ":\"true\",\"hive.server2.tez.initialize.default.sessions\":\"true\"}},{\"mapred-site\":{\"mapreduce.job.reduce.slowstart.completedmaps\""
            + ":\"0.7\",\"mapreduce.map.output.compress\":\"true\",\"mapreduce.output.fileoutputformat.compress\":\"true\"}},{\"yarn-site\""
            + ":{\"yarn.acl.enable\":\"true\"}}],\"host_groups\":[{\"name\":\"master\",\"configurations\":[],\"components\":[{\"name\""
            + ":\"APP_TIMELINE_SERVER\"},{\"name\":\"HCAT\"},{\"name\":\"HDFS_CLIENT\"},{\"name\":\"HISTORYSERVER\"},{\"name\":\"HIVE_CLIENT\"}"
            + ",{\"name\":\"HIVE_METASTORE\"},{\"name\":\"HIVE_SERVER\"},{\"name\":\"JOURNALNODE\"},{\"name\":\"LIVY_SERVER\"},{\"name\""
            + ":\"MAPREDUCE2_CLIENT\"},{\"name\":\"METRICS_COLLECTOR\"},{\"name\":\"METRICS_GRAFANA\"},{\"name\":\"METRICS_MONITOR\"},"
            + "{\"name\":\"MYSQL_SERVER\"},{\"name\":\"NAMENODE\"},{\"name\":\"PIG\"},{\"name\":\"RESOURCEMANAGER\"},{\"name\":\"SECONDARY_NAMENODE\"}"
            + ",{\"name\":\"SPARK2_CLIENT\"},{\"name\":\"SPARK2_JOBHISTORYSERVER\"},{\"name\":\"SQOOP\"},{\"name\":\"TEZ_CLIENT\"},"
            + "{\"name\":\"WEBHCAT_SERVER\"},{\"name\":\"YARN_CLIENT\"},{\"name\":\"ZEPPELIN_MASTER\"},{\"name\":\"ZOOKEEPER_CLIENT\"},"
            + "{\"name\":\"ZOOKEEPER_SERVER\"}],\"cardinality\":\"1\"},{\"name\":\"worker\",\"configurations\":[],\"components\":["
            + "{\"name\":\"DATANODE\"},{\"name\":\"METRICS_MONITOR\"},{\"name\":\"NODEMANAGER\"}],\"cardinality\":\"1+\"}]},\"inputs\": []}";

    @InjectMocks
    private BlueprintLoaderService underTest;

    @Mock
    private DefaultBlueprintCache blueprintCache;

    @Mock
    private DefaultBlueprintCache defaultBlueprintCache;

    @Mock
    private Organization organization;

    @Test
    public void testBlueprintLoaderWhenTheUserWhenUserHaveAllTheDefaultBlueprintThenItShouldReturnWithFalse() {
        Set<Blueprint> blueprints = generateDatabaseData(3);
        Map<String, Blueprint> defaultBlueprints = generateCacheData(3);
        when(defaultBlueprintCache.defaultBlueprints()).thenReturn(defaultBlueprints);

        boolean addingDefaultBlueprintsAreNecessaryForTheUser = underTest.addingDefaultBlueprintsAreNecessaryForTheUser(blueprints);

        Assert.assertFalse(addingDefaultBlueprintsAreNecessaryForTheUser);
    }

    @Test
    public void testBlueprintLoaderWhenTheUserIsANewOneInTheNewOrganizationThenItShouldReturnWithTrue() {
        Set<Blueprint> blueprints = generateDatabaseData(0);
        Map<String, Blueprint> defaultBlueprints = generateCacheData(2);
        when(defaultBlueprintCache.defaultBlueprints()).thenReturn(defaultBlueprints);

        boolean addingDefaultBlueprintsAreNecessaryForTheUser = underTest.addingDefaultBlueprintsAreNecessaryForTheUser(blueprints);

        Assert.assertTrue(addingDefaultBlueprintsAreNecessaryForTheUser);
    }

    @Test
    public void testBlueprintLoaderWhenTheUserIsANewOneInTheExistingOrganizationThenItShouldReturnWithTrue() {
        Set<Blueprint> blueprints = generateDatabaseData(1);
        Map<String, Blueprint> defaultBlueprints = generateCacheData(2);
        when(defaultBlueprintCache.defaultBlueprints()).thenReturn(defaultBlueprints);

        boolean addingDefaultBlueprintsAreNecessaryForTheUser = underTest.addingDefaultBlueprintsAreNecessaryForTheUser(blueprints);

        Assert.assertTrue(addingDefaultBlueprintsAreNecessaryForTheUser);
    }

    @Test
    public void testBlueprintLoaderWhenTheUserHasAllDefaultBlueprintButOneOfItWasChangeThenItShouldReturnWithTrue() {
        Set<Blueprint> blueprints = generateDatabaseData(3);
        Map<String, Blueprint> defaultBlueprints = generateCacheData(3, 1);
        when(defaultBlueprintCache.defaultBlueprints()).thenReturn(defaultBlueprints);

        boolean addingDefaultBlueprintsAreNecessaryForTheUser = underTest.addingDefaultBlueprintsAreNecessaryForTheUser(blueprints);

        Assert.assertTrue(addingDefaultBlueprintsAreNecessaryForTheUser);
    }

    @Test
    public void testLoadBlueprintsForTheSpecifiedUserWhenOneNewDefaultExistThenRepositoryShouldUpdateOnlyOneBlueprint() {
        Set<Blueprint> blueprints = generateDatabaseData(3);
        Map<String, Blueprint> defaultBlueprints = generateCacheData(3, 1);
        when(defaultBlueprintCache.defaultBlueprints()).thenReturn(defaultBlueprints);

        Collection<Blueprint> resultSet = underTest.loadBlueprintsForTheOrganization(blueprints, organization, this::mockSave);

        Assert.assertEquals(4L, resultSet.size());
    }

    @Test
    public void testLoadBlueprintsForTheSpecifiedUserIsNewOneAndNoDefaultBlueprintAddedThenAllDefaultShouldBeAdd() {
        Set<Blueprint> blueprints = generateDatabaseData(0);
        Map<String, Blueprint> defaultBlueprints = generateCacheData(3);
        when(defaultBlueprintCache.defaultBlueprints()).thenReturn(defaultBlueprints);

        Collection<Blueprint> resultSet = underTest.loadBlueprintsForTheOrganization(blueprints, organization, this::mockSave);

        Assert.assertEquals(3L, resultSet.size());
    }

    @Test
    public void testLoadBlueprintsForTheSpecifiedUserWhenEveryDefaultExistThenRepositoryShouldNotUpdateAnything() {
        Set<Blueprint> blueprints = generateDatabaseData(3);
        Map<String, Blueprint> defaultBlueprints = generateCacheData(3);
        when(defaultBlueprintCache.defaultBlueprints()).thenReturn(defaultBlueprints);

        Collection<Blueprint> resultSet = underTest.loadBlueprintsForTheOrganization(blueprints, organization, this::mockSave);

        Assert.assertEquals(3L, resultSet.size());
    }

    private Iterable<Blueprint> mockSave(Iterable<Blueprint> blueprints, Organization organization) {
        return blueprints;
    }

    private Map<String, Blueprint> generateCacheData(int cacheSize) {
        return generateCacheData(cacheSize, 0);
    }

    private Map<String, Blueprint> generateCacheData(int cacheSize, int startIndex) {
        Map<String, Blueprint> cacheData = new HashMap<>();
        for (int i = startIndex; i < cacheSize + startIndex; i++) {
            Blueprint blueprint = createBlueprint(DEFAULT, i);
            cacheData.put(blueprint.getName(), blueprint);
        }
        return cacheData;
    }

    private Set<Blueprint> generateDatabaseData(int cacheSize) {
        Set<Blueprint> databaseData = new HashSet<>();
        for (int i = 0; i < cacheSize; i++) {
            Blueprint blueprint = createBlueprint(DEFAULT, i);
            databaseData.add(blueprint);
        }
        return databaseData;
    }

    public static Blueprint createBlueprint(ResourceStatus resourceStatus, int index) {
        Blueprint blueprint = new Blueprint();
        blueprint.setId((long) index);
        blueprint.setAmbariName("test-validation" + index);
        blueprint.setBlueprintText(JSON + index);
        blueprint.setHostGroupCount(3);
        blueprint.setStatus(resourceStatus);
        blueprint.setDescription("test validation" + index);
        blueprint.setName("multi-node-hdfs-yarn" + index);
        blueprint.setOwner(LUCKY_MAN);
        blueprint.setAccount(LOTTERY_WINNERS);
        return blueprint;
    }

    public static IdentityUser identityUser() {
        return new IdentityUser(LUCKY_MAN, LUCKY_MAN, LOTTERY_WINNERS, new ArrayList<>(), LUCKY_MAN, LUCKY_MAN, new Date());
    }
}
