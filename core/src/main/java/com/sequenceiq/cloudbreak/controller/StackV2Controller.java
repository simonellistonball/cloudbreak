package com.sequenceiq.cloudbreak.controller;

import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.api.endpoint.v2.StackV2Endpoint;
import com.sequenceiq.cloudbreak.api.model.AmbariAddressJson;
import com.sequenceiq.cloudbreak.api.model.AutoscaleStackResponse;
import com.sequenceiq.cloudbreak.api.model.CertificateResponse;
import com.sequenceiq.cloudbreak.api.model.GeneratedBlueprintResponse;
import com.sequenceiq.cloudbreak.api.model.PlatformVariantsJson;
import com.sequenceiq.cloudbreak.api.model.ReinstallRequestV2;
import com.sequenceiq.cloudbreak.api.model.UpdateClusterJson;
import com.sequenceiq.cloudbreak.api.model.stack.StackImageChangeRequest;
import com.sequenceiq.cloudbreak.api.model.stack.StackRequest;
import com.sequenceiq.cloudbreak.api.model.stack.StackResponse;
import com.sequenceiq.cloudbreak.api.model.stack.StackScaleRequestV2;
import com.sequenceiq.cloudbreak.api.model.stack.StackValidationRequest;
import com.sequenceiq.cloudbreak.api.model.stack.cluster.ClusterRepairRequest;
import com.sequenceiq.cloudbreak.api.model.users.UserNamePasswordJson;
import com.sequenceiq.cloudbreak.api.model.v2.StackV2Request;
import com.sequenceiq.cloudbreak.common.model.user.IdentityUser;
import com.sequenceiq.cloudbreak.domain.organization.Organization;
import com.sequenceiq.cloudbreak.domain.organization.User;
import com.sequenceiq.cloudbreak.domain.stack.Stack;
import com.sequenceiq.cloudbreak.service.ClusterCommonService;
import com.sequenceiq.cloudbreak.service.RestRequestThreadLocalService;
import com.sequenceiq.cloudbreak.service.StackCommonService;
import com.sequenceiq.cloudbreak.service.cluster.ClusterService;
import com.sequenceiq.cloudbreak.service.organization.OrganizationService;
import com.sequenceiq.cloudbreak.service.stack.CloudParameterCache;
import com.sequenceiq.cloudbreak.service.stack.StackService;
import com.sequenceiq.cloudbreak.service.user.UserService;

@Component
@Transactional(TxType.NEVER)
public class StackV2Controller extends NotificationController implements StackV2Endpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(StackV2Controller.class);

    @Inject
    private StackCommonService stackCommonService;

    @Inject
    private ClusterCommonService clusterCommonController;

    @Inject
    private StackService stackService;

    @Inject
    @Named("conversionService")
    private ConversionService conversionService;

    @Inject
    private CloudParameterCache cloudParameterCache;

    @Inject
    private ClusterService clusterService;

    @Inject
    private OrganizationService organizationService;

    @Inject
    private UserService userService;

    @Inject
    private RestRequestThreadLocalService restRequestThreadLocalService;

    @Override
    public Set<StackResponse> getStacksInDefaultOrg() {
        return stackCommonService.getStacksInDefaultOrg();
    }

    @Override
    public Set<StackResponse> getPublics() {
        return stackCommonService.getStacksInDefaultOrg();
    }

    @Override
    public StackResponse getStackFromDefaultOrg(String name, Set<String> entries) {
        return stackCommonService.getStackFromDefaultOrg(name, entries);
    }

    @Override
    public StackResponse getPublic(String name, Set<String> entries) {
        return stackCommonService.getStackFromDefaultOrg(name, entries);
    }

    @Override
    public StackResponse get(Long id, Set<String> entries) {
        return stackCommonService.get(id, entries);
    }

    @Override
    public void deleteInDefaultOrg(String name, Boolean forced, Boolean deleteDependencies) {
        stackCommonService.deleteInDefaultOrg(name, forced, deleteDependencies);
    }

    @Override
    public void deletePrivate(String name, Boolean forced, Boolean deleteDependencies) {
        stackCommonService.deleteInDefaultOrg(name, forced, deleteDependencies);
    }

    @Override
    public void deleteById(Long id, Boolean forced, Boolean deleteDependencies) {
        stackCommonService.deleteById(id, forced, deleteDependencies);
    }

    @Override
    public Response putScaling(String name, StackScaleRequestV2 updateRequest) {
        User user = userService.getOrCreate(restRequestThreadLocalService.getIdentityUser());
        Organization organization = organizationService.get(restRequestThreadLocalService.getRequestedOrgId(), user);
        return stackCommonService.putScalingInOrganization(name, organization.getId(), updateRequest);
    }

    @Override
    public Response putStart(String name) {
        User user = userService.getOrCreate(restRequestThreadLocalService.getIdentityUser());
        Organization organization = organizationService.get(restRequestThreadLocalService.getRequestedOrgId(), user);
        return stackCommonService.putStartInOrganization(name, organization.getId());
    }

    @Override
    public Response putStop(String name) {
        User user = userService.getOrCreate(restRequestThreadLocalService.getIdentityUser());
        Organization organization = organizationService.get(restRequestThreadLocalService.getRequestedOrgId(), user);
        return stackCommonService.putStopInOrganization(name, organization.getId());
    }

    @Override
    public Response putSync(String name) {
        User user = userService.getOrCreate(restRequestThreadLocalService.getIdentityUser());
        Organization organization = organizationService.get(restRequestThreadLocalService.getRequestedOrgId(), user);
        return stackCommonService.putSyncInOrganization(name, organization.getId());
    }

    @Override
    public Response putReinstall(String name, ReinstallRequestV2 reinstallRequestV2) {
        User user = userService.getOrCreate(restRequestThreadLocalService.getIdentityUser());
        Stack stack = stackService.getByNameInOrg(name, restRequestThreadLocalService.getRequestedOrgId());
        UpdateClusterJson updateClusterJson = conversionService.convert(reinstallRequestV2, UpdateClusterJson.class);
        Organization organization = organizationService.get(restRequestThreadLocalService.getRequestedOrgId(), user);
        return clusterCommonController.put(stack.getId(), updateClusterJson, user, organization);
    }

    @Override
    public Response putPassword(String name, UserNamePasswordJson userNamePasswordJson) {
        User user = userService.getOrCreate(restRequestThreadLocalService.getIdentityUser());
        Organization organization = organizationService.get(restRequestThreadLocalService.getRequestedOrgId(), user);
        Stack stack = stackService.getByNameInOrg(name, organization.getId());
        UpdateClusterJson updateClusterJson = conversionService.convert(userNamePasswordJson, UpdateClusterJson.class);
        return clusterCommonController.put(stack.getId(), updateClusterJson, user, organization);
    }

    @Override
    public Map<String, Object> status(Long id) {
        return stackCommonService.status(id);
    }

    @Override
    public PlatformVariantsJson variants() {
        return stackCommonService.variants();
    }

    @Override
    public Response deleteInstance(Long stackId, String instanceId) {
        return stackCommonService.deleteInstance(stackId, instanceId, false);
    }

    @Override
    public Response deleteInstance(Long stackId, String instanceId, boolean forced) {
        return stackCommonService.deleteInstance(stackId, instanceId, forced);
    }

    @Override
    public Response deleteInstances(Long stackId, Set<String> instanceIds) {
        return stackCommonService.deleteInstances(stackId, instanceIds);
    }

    @Override
    public CertificateResponse getCertificate(Long stackId) {
        return stackCommonService.getCertificate(stackId);
    }

    @Override
    public Response validate(StackValidationRequest stackValidationRequest) {
        return stackCommonService.validate(stackValidationRequest);
    }

    @Override
    public StackResponse getStackForAmbari(AmbariAddressJson json) {
        return stackCommonService.getStackForAmbari(json);
    }

    @Override
    public Set<AutoscaleStackResponse> getAllForAutoscale() {
        return stackCommonService.getAllForAutoscale();
    }

    @Override
    public StackV2Request getRequestfromName(String name) {
        User user = userService.getOrCreate(restRequestThreadLocalService.getIdentityUser());
        Organization organization = organizationService.get(restRequestThreadLocalService.getRequestedOrgId(), user);
        return stackService.getStackRequestByNameInDefaultOrgForUser(name, user, organization);
    }

    @Override
    public StackResponse postPrivate(StackV2Request stackRequest) {
        IdentityUser identityUser = restRequestThreadLocalService.getIdentityUser();
        User user = userService.getOrCreate(identityUser);
        Organization organization = organizationService.get(restRequestThreadLocalService.getRequestedOrgId(), user);
        return stackCommonService.createInOrganization(conversionService.convert(stackRequest, StackRequest.class), identityUser, user, organization);
    }

    @Override
    public StackResponse postPublic(StackV2Request stackRequest) {
        IdentityUser identityUser = restRequestThreadLocalService.getIdentityUser();
        User user = userService.getOrCreate(identityUser);
        Organization organization = organizationService.get(restRequestThreadLocalService.getRequestedOrgId(), user);
        return stackCommonService.createInOrganization(conversionService.convert(stackRequest, StackRequest.class), identityUser, user, organization);
    }

    @Override
    public GeneratedBlueprintResponse postStackForBlueprint(StackV2Request stackRequest) {
        return stackCommonService.postStackForBlueprint(stackRequest);
    }

    @Override
    public void retry(String stackName) {
        User user = userService.getOrCreate(restRequestThreadLocalService.getIdentityUser());
        Organization organization = organizationService.get(restRequestThreadLocalService.getRequestedOrgId(), user);
        stackCommonService.retryInOrganization(stackName, organization.getId());
    }

    @Override
    public Response repairCluster(String name, ClusterRepairRequest clusterRepairRequest) {
        Stack stack = stackService.getByNameInOrg(name, restRequestThreadLocalService.getRequestedOrgId());
        clusterService.repairCluster(stack.getId(), clusterRepairRequest.getHostGroups(), clusterRepairRequest.isRemoveOnly());
        return Response.accepted().build();
    }

    @Override
    public Response changeImage(String stackName, StackImageChangeRequest stackImageChangeRequest) {
        User user = userService.getOrCreate(restRequestThreadLocalService.getIdentityUser());
        Organization organization = organizationService.get(restRequestThreadLocalService.getRequestedOrgId(), user);
        return stackCommonService.changeImageByNameInOrg(stackName, organization.getId(), stackImageChangeRequest);
    }
}
