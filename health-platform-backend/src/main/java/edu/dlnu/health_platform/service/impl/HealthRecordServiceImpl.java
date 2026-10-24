package edu.dlnu.health_platform.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import edu.dlnu.health_platform.dal.HealthRecordMapper;
import edu.dlnu.health_platform.entity.HealthRecord;
import edu.dlnu.health_platform.service.HealthRecordService;
import edu.dlnu.health_platform.vo.Result;
import edu.dlnu.health_platform.vo.response.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class HealthRecordServiceImpl implements HealthRecordService {

    @Autowired
    private HealthRecordMapper healthRecordMapper;

    @Override
    public Result<PageResult<HealthRecord>> listByPatient(Long patientId, Integer pageNum, Integer pageSize) {
        return Result.success(PageResult.of(healthRecordMapper.findByPatient(new Page<>(pageNum, pageSize), patientId)));
    }

    @Override
    public Result<Void> add(Long patientId, HealthRecord record) {
        if (record == null) {
            return Result.fail("健康数据不能为空");
        }
        //至少上报一项指标（血压/心率/血糖/心情）
        if (record.getBloodPressureSys() == null && record.getBloodPressureDia() == null
                && record.getHeartRate() == null && record.getBloodSugar() == null
                && (record.getMoodStatus() == null || record.getMoodStatus().isBlank())) {
            return Result.fail("请至少填写一项健康指标");
        }
        record.setPatientId(patientId);
        //未填测量时间时默认当前时间
        if (record.getRecordTime() == null) {
            record.setRecordTime(LocalDateTime.now());
        }
        healthRecordMapper.insert(record);
        return Result.success();
    }

    @Override
    public long countAll() {
        return healthRecordMapper.countAll();
    }
}
