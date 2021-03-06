package com.sequenceiq.cloudbreak.controller;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.api.endpoint.v1.ImageCatalogV1Endpoint;
import com.sequenceiq.cloudbreak.api.model.imagecatalog.ImageCatalogRequest;
import com.sequenceiq.cloudbreak.api.model.imagecatalog.ImageCatalogResponse;
import com.sequenceiq.cloudbreak.api.model.imagecatalog.ImagesResponse;
import com.sequenceiq.cloudbreak.api.model.imagecatalog.UpdateImageCatalogRequest;
import com.sequenceiq.cloudbreak.cloud.model.catalog.Images;
import com.sequenceiq.cloudbreak.common.model.user.IdentityUser;
import com.sequenceiq.cloudbreak.core.CloudbreakImageCatalogException;
import com.sequenceiq.cloudbreak.domain.ImageCatalog;
import com.sequenceiq.cloudbreak.domain.organization.Organization;
import com.sequenceiq.cloudbreak.domain.organization.User;
import com.sequenceiq.cloudbreak.service.RestRequestThreadLocalService;
import com.sequenceiq.cloudbreak.service.image.ImageCatalogService;
import com.sequenceiq.cloudbreak.service.image.StackImageFilterService;
import com.sequenceiq.cloudbreak.service.organization.OrganizationService;
import com.sequenceiq.cloudbreak.service.user.UserService;

@Component
@Transactional(TxType.NEVER)
public class ImageCatalogV1Controller implements ImageCatalogV1Endpoint {

    @Inject
    private ImageCatalogService imageCatalogService;

    @Inject
    private StackImageFilterService stackImageFilterService;

    @Inject
    @Named("conversionService")
    private ConversionService conversionService;

    @Inject
    private OrganizationService organizationService;

    @Inject
    private RestRequestThreadLocalService restRequestThreadLocalService;

    @Inject
    private UserService userService;

    @Override
    public List<ImageCatalogResponse> getPublics() {
        return getAll();
    }

    @Override
    public ImageCatalogResponse getByName(String name, boolean withImages) {
        ImageCatalogResponse imageCatalogResponse = convert(imageCatalogService.get(restRequestThreadLocalService.getRequestedOrgId(), name));
        Images images = imageCatalogService.propagateImagesIfRequested(restRequestThreadLocalService.getRequestedOrgId(), name, withImages);
        if (images != null) {
            imageCatalogResponse.setImagesResponse(conversionService.convert(images, ImagesResponse.class));
        }
        return imageCatalogResponse;
    }

    @Override
    public ImagesResponse getImagesByProvider(String platform) throws Exception {
        IdentityUser identityUser = restRequestThreadLocalService.getIdentityUser();
        User user = userService.getOrCreate(identityUser);
        Images images = imageCatalogService.getImagesOsFiltered(platform, null, identityUser, user).getImages();
        return conversionService.convert(images, ImagesResponse.class);
    }

    @Override
    public ImageCatalogResponse postPublic(ImageCatalogRequest imageCatalogRequest) {
        User user = userService.getOrCreate(restRequestThreadLocalService.getIdentityUser());
        return post(imageCatalogRequest, user);
    }

    @Override
    public ImageCatalogResponse postPrivate(ImageCatalogRequest imageCatalogRequest) {
        User user = userService.getOrCreate(restRequestThreadLocalService.getIdentityUser());
        return post(imageCatalogRequest, user);
    }

    @Override
    public ImagesResponse getImagesByProviderFromImageCatalog(String name, String platform) throws Exception {
        Images images = imageCatalogService.getImages(restRequestThreadLocalService.getRequestedOrgId(), name, platform).getImages();
        return conversionService.convert(images, ImagesResponse.class);
    }

    @Override
    public void deletePublic(String name) {
        IdentityUser identityUser = restRequestThreadLocalService.getIdentityUser();
        User user = userService.getOrCreate(identityUser);
        Organization organization = organizationService.get(restRequestThreadLocalService.getRequestedOrgId(), user);
        imageCatalogService.delete(organization.getId(), name, identityUser, user);
    }

    @Override
    public ImageCatalogResponse putPublic(UpdateImageCatalogRequest request) {
        User user = userService.getOrCreate(restRequestThreadLocalService.getIdentityUser());
        ImageCatalog imageCatalog = imageCatalogService.update(restRequestThreadLocalService.getRequestedOrgId(),
                conversionService.convert(request, ImageCatalog.class), user);
        return convert(imageCatalog);
    }

    @Override
    public ImageCatalogResponse putSetDefaultByName(String name) {
        IdentityUser identityUser = restRequestThreadLocalService.getIdentityUser();
        User user = userService.getOrCreate(identityUser);
        return conversionService.convert(imageCatalogService.setAsDefault(restRequestThreadLocalService.getRequestedOrgId(), name, identityUser, user),
                ImageCatalogResponse.class);
    }

    @Override
    public ImageCatalogRequest getRequestfromName(String name) {
        ImageCatalog imageCatalog = imageCatalogService.get(restRequestThreadLocalService.getRequestedOrgId(), name);
        return conversionService.convert(imageCatalog, ImageCatalogRequest.class);
    }

    @Override
    public ImagesResponse getImagesFromCustomImageCatalogByStack(String imageCatalogName, String stackName) throws CloudbreakImageCatalogException {
        Images images = stackImageFilterService.getApplicableImages(restRequestThreadLocalService.getRequestedOrgId(), imageCatalogName, stackName);
        return conversionService.convert(images, ImagesResponse.class);
    }

    @Override
    public ImagesResponse getImagesFromDefaultImageCatalogByStack(String stackName) throws Exception {
        Images images = stackImageFilterService.getApplicableImages(restRequestThreadLocalService.getRequestedOrgId(), stackName);
        return conversionService.convert(images, ImagesResponse.class);
    }

    private ImageCatalogResponse convert(ImageCatalog imageCatalog) {
        return conversionService.convert(imageCatalog, ImageCatalogResponse.class);
    }

    private <S, T> List<T> toJsonList(Iterable<S> objs, Class<T> clss) {
        return (List<T>) conversionService.convert(objs,
                TypeDescriptor.forObject(objs),
                TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(clss)));
    }

    private List<ImageCatalogResponse> getAll() {
        return toJsonList(imageCatalogService.findAllByOrganizationId(restRequestThreadLocalService.getRequestedOrgId()), ImageCatalogResponse.class);
    }

    private ImageCatalogResponse post(ImageCatalogRequest imageCatalogRequest, User user) {
        ImageCatalog imageCatalog = conversionService.convert(imageCatalogRequest, ImageCatalog.class);
        Long orgId = restRequestThreadLocalService.getRequestedOrgId();
        imageCatalog = imageCatalogService.create(imageCatalog, orgId, user);
        return convert(imageCatalog);
    }
}
