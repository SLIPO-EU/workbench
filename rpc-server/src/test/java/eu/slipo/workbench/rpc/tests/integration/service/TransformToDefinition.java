package eu.slipo.workbench.rpc.tests.integration.service;

import eu.slipo.workbench.common.model.process.ProcessDefinition;

@FunctionalInterface
interface TransformToDefinition
{
    ProcessDefinition buildDefinition(
        String procName, TransformFixture fixture, String resourceName)
    throws Exception;
}
