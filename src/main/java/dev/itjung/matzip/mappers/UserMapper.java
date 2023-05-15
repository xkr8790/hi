package dev.itjung.matzip.mappers;

import dev.itjung.matzip.entities.RecoverContactCodeEntity;
import dev.itjung.matzip.entities.RegisterContactCodeEntity;
import dev.itjung.matzip.entities.RegisterEmailCodeEntity;
import dev.itjung.matzip.entities.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    int insertRegisterContactCode(RegisterContactCodeEntity registerContactCode);

    int insertUser(UserEntity user);
    int updateUser(UserEntity user);
    int insertRegisterEmailCode(RegisterEmailCodeEntity registerEmailCode);
    UserEntity selectUserByEmail(@Param(value = "email") String email);

    UserEntity selectUserByNickName(@Param(value = "nickname") String nickname);

    UserEntity selectUserByContact(@Param(value = "contact") String contact);

    RegisterContactCodeEntity selectRegisterContactCodeByContactCodeSalt(@Param(value = "contact") String contact,
                                                                  @Param(value = "code") String code,
                                                                  @Param(value = "salt") String salt);
    int updateRegisterContactCode (RegisterContactCodeEntity registerContactCodeEntity);

    int updateRegisterEmailCode (RegisterEmailCodeEntity registerEmailCode);

    RegisterEmailCodeEntity selectRegisterEmailCodeByEmailCodeSalt(RegisterEmailCodeEntity registerEmailCode);

    int insertRecoverContactCode(RecoverContactCodeEntity recoverContactCode);

    RecoverContactCodeEntity selectRecoverContactCodeByContactCodeSalt(@Param(value = "contact") String contact,
                                                                       @Param(value = "code") String code,
                                                                       @Param(value = "salt") String salt);

    int updateRecoverContactCode(RecoverContactCodeEntity recoverContactCode);
}