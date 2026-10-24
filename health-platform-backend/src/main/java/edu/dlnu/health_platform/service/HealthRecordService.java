package edu.dlnu.health_platform.service;

import edu.dlnu.health_platform.entity.HealthRecord;
import edu.dlnu.health_platform.vo.Result;
import edu.dlnu.health_platform.vo.response.PageResult;

public interface HealthRecordService {

    //我的健康记录（患者，分页）
    Result<PageResult<HealthRecord>> listByPatient(Long patientId, Integer pageNum, Integer pageSize);

    //添加健康记录（患者）
    Result<Void> add(Long patientId, HealthRecord record);

    //健康记录总数（看板统计）
    long countAll();
}
