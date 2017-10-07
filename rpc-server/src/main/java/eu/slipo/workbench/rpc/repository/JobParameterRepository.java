package eu.slipo.workbench.rpc.repository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import eu.slipo.workbench.rpc.domain.JobParameterEntity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


@Repository
@Transactional(readOnly = true)
public interface JobParameterRepository extends JpaRepository<JobParameterEntity, Integer> 
{
    @Query("FROM JobParameter p WHERE p.jobName = :jobName")
    List<JobParameterEntity> findByJobName(@Param("jobName") String jobName);
}