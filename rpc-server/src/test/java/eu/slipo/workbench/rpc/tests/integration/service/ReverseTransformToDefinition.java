package eu.slipo.workbench.rpc.tests.integration.service;

import eu.slipo.workbench.common.model.process.ProcessDefinition;

@FunctionalInterface
interface ReverseTransformToDefinition
{
    ProcessDefinition buildDefinition(
        String procName, ReverseTransformFixture fixture, String resourceName)
    throws Exception;
}
