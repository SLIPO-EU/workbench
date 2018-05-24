package eu.slipo.workbench.rpc.tests.integration.service;

import eu.slipo.workbench.common.model.process.ProcessDefinition;

@FunctionalInterface
interface InterlinkToDefinition
{
    ProcessDefinition buildDefinition(
            String procName, InterlinkFixture fixture,
            String resourceName, String output1Name, String output2Name)
        throws Exception;
}