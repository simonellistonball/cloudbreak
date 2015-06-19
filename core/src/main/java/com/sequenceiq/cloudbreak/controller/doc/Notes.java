package com.sequenceiq.cloudbreak.controller.doc;

public class Notes {

    public static final String BLUEPRINT_NOTES = "Ambari Blueprints are a declarative "
            + "definition of a Hadoop cluster. With a Blueprint, you specify a stack, the component layout "
            + "and the configurations to materialize a Hadoop cluster instance. Hostgroups defined in blueprints "
            + "can be associated to different templates, thus you can spin up a highly available cluster "
            + "running on different instance types. This will give you the option to group your "
            + "Hadoop services based on resource needs (e.g. high I/O, CPU or memory) "
            + "and create an infrastructure which fits your workload best.";
    public static final String TEMPLATE_NOTES = "A template gives developers and systems administrators "
            + "an easy way to create and manage a collection of cloud infrastructure related resources, "
            + "maintaining and updating them in an orderly and predictable fashion. Templates are cloud specific "
            + "- and on top of the infrastructural setup they collect the information such as the used machine images, "
            + "the datacenter location, instance types, and can capture and control region-specific infrastructure variations. "
            + "We support heterogenous clusters - this one Hadoop cluster can be built by combining different templates.";
    public static final String CREDENTIAL_NOTES = "Cloudbreak is launching Hadoop clusters on the user's behalf - "
            + "on different cloud providers. One key point is that Cloudbreak does not store your "
            + "Cloud provider account details (such as username, password, keys, private SSL certificates, etc). "
            + "We work around the concept that Identity and Access Management is fully controlled by you - the end user. "
            + "The Cloudbreak deployer is purely acting on behalf of the end user - without having access to the user's account.";
    public static final String STACK_NOTES = "Stacks are template instances - a running cloud infrastructure "
            + "created based on a template. Stacks are always launched on behalf of a cloud user account. "
            + "Stacks support a wide range of resources, allowing you to build a highly available, reliable, "
            + "and scalable infrastructure for your application needs.";
    public static final String CLUSTER_NOTES = "Clusters are materialised Hadoop services on a given infrastructure. "
            + "They are built based on a Blueprint (running the components and services specified) and on "
            + "a configured infrastructure Stack. Once a cluster is created and launched, it can be used the usual"
            + " way as any Hadoop cluster. We suggest to start with the Cluster's Ambari UI for an overview of your cluster.";
    public static final String RECIPE_NOTES = "Recipes are basically script extensions to a cluster that run on a set of nodes"
            + " before or after the Ambari cluster installation.";
    public static final String USAGE_NOTES = "Cloudbreak gives you an up to date overview of cluster usage based "
            + "on different filtering criteria (start/end date, users, providers, region, etc)";
    public static final String EVENT_NOTES = "Events are used to track stack creation initiated by cloudbreak users. "
            + "Events are generated by the backend when resources requested by the user become available or unavailable";
    public static final String NETWORK_NOTES = "Provider specific network settings could be configured by using Network resources.";

    public static final String USER_NOTES = "Users can be invited under an account by the administrator, and all resources "
            + "(e.g. resources, networks, blueprints, credentials, clusters) can be shared across account users";

    private Notes() {
    }
}
