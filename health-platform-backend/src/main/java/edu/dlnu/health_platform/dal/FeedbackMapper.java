package edu.dlnu.health_platform.dal;

import edu.dlnu.health_platform.entity.Feedback;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * feedbacks 表 Mapper（注解 SQL 风格）
 * 一条任务记录同时包含患者、志愿者双方的评价
 */
@Mapper
public interface FeedbackMapper {

    //根据任务ID查询评价记录
    @Select("SELECT * FROM feedbacks WHERE assignment_id = #{assignmentId}")
    Feedback findByAssignmentId(@Param("assignmentId") Long assignmentId);

    //新增评价（患者侧填写）
    @Insert("INSERT INTO feedbacks (assignment_id, patient_score, patient_comment, is_public) " +
            "VALUES (#{assignmentId}, #{score}, #{content}, #{isPublic})")
    int insertPatient(@Param("assignmentId") Long assignmentId, @Param("score") Integer score,
                      @Param("content") String content, @Param("isPublic") Integer isPublic);

    //新增评价（志愿者侧填写）
    @Insert("INSERT INTO feedbacks (assignment_id, volunteer_score, volunteer_comment, is_public) " +
            "VALUES (#{assignmentId}, #{score}, #{content}, #{isPublic})")
    int insertVolunteer(@Param("assignmentId") Long assignmentId, @Param("score") Integer score,
                        @Param("content") String content, @Param("isPublic") Integer isPublic);

    //补充患者侧评价
    @Update("UPDATE feedbacks SET patient_score = #{score}, patient_comment = #{content}, is_public = #{isPublic} " +
            "WHERE assignment_id = #{assignmentId}")
    int updatePatient(@Param("assignmentId") Long assignmentId, @Param("score") Integer score,
                      @Param("content") String content, @Param("isPublic") Integer isPublic);

    //补充志愿者侧评价
    @Update("UPDATE feedbacks SET volunteer_score = #{score}, volunteer_comment = #{content}, is_public = #{isPublic} " +
            "WHERE assignment_id = #{assignmentId}")
    int updateVolunteer(@Param("assignmentId") Long assignmentId, @Param("score") Integer score,
                        @Param("content") String content, @Param("isPublic") Integer isPublic);

    //某志愿者获得的患者评价次数（平均评分计算用）
    @Select("SELECT COUNT(*) FROM feedbacks f " +
            "JOIN task_assignments t ON t.id = f.assignment_id " +
            "WHERE t.volunteer_id = #{volunteerId} AND f.patient_score IS NOT NULL")
    long countByVolunteer(@Param("volunteerId") Long volunteerId);
}
