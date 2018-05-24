package eu.slipo.workbench.rpc.tests.integration.service;

import eu.slipo.workbench.common.model.process.ProcessDefinition;

@FunctionalInterface
interface FuseToDefinition
{
    ProcessDefinition buildDefinition(
            String procName, FuseFixture fixture, String resourceName)
        throws Exception;
}