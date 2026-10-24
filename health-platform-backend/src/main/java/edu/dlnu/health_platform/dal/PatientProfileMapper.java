package edu.dlnu.health_platform.dal;

import edu.dlnu.health_platform.entity.PatientProfile;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * patient_profiles 表 Mapper（注解 SQL 风格）
 */
@Mapper
public interface PatientProfileMapper {

    //根据用户ID查询档案
    @Select("SELECT * FROM patient_profiles WHERE user_id = #{userId}")
    PatientProfile findByUserId(@Param("userId") Long userId);

    //注册时创建空档案
    @Insert("INSERT INTO patient_profiles (user_id) VALUES (#{userId})")
    int insert(@Param("userId") Long userId);

    //更新档案
    @Update("UPDATE patient_profiles SET id_card_no = #{idCardNo}, birthday = #{birthday}, gender = #{gender}, " +
            "emergency_contact_name = #{emergencyContactName}, emergency_contact_phone = #{emergencyContactPhone}, " +
            "home_address = #{homeAddress}, allergy_history = #{allergyHistory}, " +
            "chronic_diseases = #{chronicDiseases}, medication_list = #{medicationList}, " +
            "building = #{building}, floor = #{floor}, room = #{room} WHERE user_id = #{userId}")
    int update(PatientProfile profile);
}
