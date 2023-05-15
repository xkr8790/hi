package dev.itjung.matzip.services;

import dev.itjung.matzip.entities.RecoverContactCodeEntity;
import dev.itjung.matzip.entities.RegisterContactCodeEntity;
import dev.itjung.matzip.entities.RegisterEmailCodeEntity;
import dev.itjung.matzip.entities.UserEntity;
import dev.itjung.matzip.enums.*;
import dev.itjung.matzip.mappers.UserMapper;
import dev.itjung.matzip.utils.CryptoUtil;
import dev.itjung.matzip.utils.NCloudUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Date;


@Service
public class UserService {
    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine springTemplateEngine;
    private final UserMapper userMapper;

    @Autowired
    public UserService(JavaMailSender javaMailSender, SpringTemplateEngine springTemplateEngine, UserMapper userMapper) {
        this.javaMailSender = javaMailSender;
        this.springTemplateEngine = springTemplateEngine;
        this.userMapper = userMapper;
    }

    public SendRegisterContactCodeResult sendRegisterContactCode(RegisterContactCodeEntity registerContactCode) {
        if (registerContactCode == null ||
                registerContactCode.getContact() == null ||
                !registerContactCode.getContact().matches("^(010)(\\d{8})$")) {
            return SendRegisterContactCodeResult.FAILURE;
        }
        if (this.userMapper.selectUserByContact(registerContactCode.getContact()) != null) {
            return SendRegisterContactCodeResult.FAILURE_DUPLICATE;
        }
        String code = RandomStringUtils.randomNumeric(6);
        String salt = CryptoUtil.hashSha512(String.format("%s%s%f%f",
                registerContactCode.getContact(),
                code,
                Math.random(),
                Math.random()));
        Date createdAt = new Date();
        Date expiresAt = DateUtils.addMinutes(createdAt, 5);
        registerContactCode.setCode(code)
                .setSalt(salt)
                .setCreatedAt(createdAt)
                .setExpiresAt(expiresAt)
                .setExpired(false);
        NCloudUtil.sendSms(registerContactCode.getContact(), String.format("[맛집 회원가입] 인증번호 [%s]를 입력해 주세요.", registerContactCode.getCode()));
        return this.userMapper.insertRegisterContactCode(registerContactCode) > 0
                ? SendRegisterContactCodeResult.SUCCESS
                : SendRegisterContactCodeResult.FAILURE;
    }

    public SendRecoverContactCodeResult sendRecoverContactCode(RecoverContactCodeEntity recoverContactCode) {
        if (recoverContactCode == null ||
        recoverContactCode.getContact() == null ||
        !recoverContactCode.getContact().matches("^(010)(\\d{8})$")) {
            return SendRecoverContactCodeResult.FAILURE;
        }
        String code = RandomStringUtils.randomNumeric(6);
        String salt = CryptoUtil.hashSha512(String.format("%s%s%f%f",
                recoverContactCode.getContact(),
                code,
                Math.random(),
                Math.random()));
        Date createdAt = new Date();
        Date expiresAt = DateUtils.addMinutes(createdAt, 5);
        recoverContactCode.setCode(code)
                .setSalt(salt)
                .setCreatedAt(createdAt)
                .setExpiresAt(expiresAt)
                .setExpired(false);
        NCloudUtil.sendSms(recoverContactCode.getContact(),String.format("[맛집 이메일 찾기] 인증번호 [%s]를 입력해 주세요.", recoverContactCode.getCode()));
        return this.userMapper.insertRecoverContactCode(recoverContactCode) > 0
                ? SendRecoverContactCodeResult.SUCCESS
                : SendRecoverContactCodeResult.FAILURE;
    }

    public VerifyRegisterContactCodeResult verifyRegisterContactCodeResult(RegisterContactCodeEntity registerContactCodeEntity) {
        RegisterContactCodeEntity existingRegisterContactCodeEntity = this.userMapper.selectRegisterContactCodeByContactCodeSalt(registerContactCodeEntity.getContact(),
                registerContactCodeEntity.getCode(),
                registerContactCodeEntity.getSalt());
        if (existingRegisterContactCodeEntity == null) {
            return VerifyRegisterContactCodeResult.FAILURE;
        }
        Date currentDate = new Date();
        if (currentDate.compareTo(existingRegisterContactCodeEntity.getExpiresAt()) > 0) {
            return VerifyRegisterContactCodeResult.FAILURE_EXPIRED;
        }
        existingRegisterContactCodeEntity.setExpired(true);
        return this.userMapper.updateRegisterContactCode(existingRegisterContactCodeEntity) > 0
                ? VerifyRegisterContactCodeResult.SUCCESS
                : VerifyRegisterContactCodeResult.FAILURE;
    }

    public VerifyRecoverContactCodeResult recoverContactCodeResult(RecoverContactCodeEntity recoverContactCode) {
        RecoverContactCodeEntity existingRecoverContactCode = this.userMapper.selectRecoverContactCodeByContactCodeSalt(recoverContactCode.getContact(),
                recoverContactCode.getCode(),
                recoverContactCode.getSalt());
        if (existingRecoverContactCode == null) {
            return VerifyRecoverContactCodeResult.FAILURE;
        }
        Date currenDate = new Date();
        if (currenDate.compareTo(existingRecoverContactCode.getExpiresAt()) > 0) {
            return VerifyRecoverContactCodeResult.FAILURE_EXPIRED;
        }
        existingRecoverContactCode.setExpired(true);
        return this.userMapper.updateRecoverContactCode(existingRecoverContactCode) > 0
                ?VerifyRecoverContactCodeResult.SUCCESS
                :VerifyRecoverContactCodeResult.FAILURE;
    }

    public VerifyRegisterEmailCodeResult verifyRegisterEmailCode(RegisterEmailCodeEntity registerEmailCode) {
        if (registerEmailCode.getEmail() == null ||
                registerEmailCode.getCode() == null ||
                registerEmailCode.getSalt() == null) {
            return VerifyRegisterEmailCodeResult.FAILURE;
        }

        registerEmailCode = this.userMapper.selectRegisterEmailCodeByEmailCodeSalt(registerEmailCode);
        if (registerEmailCode == null) {
            return VerifyRegisterEmailCodeResult.FAILURE;
        }

        if (new Date().compareTo(registerEmailCode.getExpiresAt()) > 0) {
            return VerifyRegisterEmailCodeResult.FAILURE_EXPIRED;
        }
        registerEmailCode.setExpired(true);
        UserEntity user = this.userMapper.selectUserByEmail(registerEmailCode.getEmail());
        user.setStatus("OKAY");

        return this.userMapper.updateRegisterEmailCode(registerEmailCode) > 0 && this.userMapper.updateUser(user) > 0
                ? VerifyRegisterEmailCodeResult.SUCCESS
                : VerifyRegisterEmailCodeResult.FAILURE;
    }

    public CheckEmailResult checkEmailResult(String email) {
        return this.userMapper.selectUserByEmail(email) == null
                ? CheckEmailResult.SUCCESS
                : CheckEmailResult.FAILURE_EMAIL_DUPLICATE;
    }

    public CheckNickNameResult checkNickNameResult(String nickname) {
        return this.userMapper.selectUserByNickName(nickname) == null
                ? CheckNickNameResult.OKAY
                : CheckNickNameResult.DUPLICATE;
    }

    public RegisterResult register(UserEntity user, RegisterContactCodeEntity registerContactCode) throws MessagingException {
        if (this.userMapper.selectUserByEmail(user.getEmail()) != null) {
            return RegisterResult.FAILURE_DUPLICATE_EMAIL;
        }
        if (this.userMapper.selectUserByContact(user.getContact()) != null) {
            return RegisterResult.FAILURE_DUPLICATE_CONTACT;
        }
        if (this.userMapper.selectUserByNickName(user.getNickname()) != null) {
            return RegisterResult.FAILURE_DUPLICATE_NICKNAME;
        }
        registerContactCode = this.userMapper.selectRegisterContactCodeByContactCodeSalt(registerContactCode.getContact(), registerContactCode.getCode(), registerContactCode.getSalt());
        if (registerContactCode == null || !registerContactCode.isExpired()) {
            return RegisterResult.FAILURE;
        }
        user.setPassword(CryptoUtil.hashSha512(user.getPassword()));
        user.setStatus("EMAIL_PENDING");
        System.out.println("메에에에에에로옹");
        user.setAdmin(false);

        RegisterEmailCodeEntity registerEmailCode = new RegisterEmailCodeEntity();
        registerEmailCode.setEmail(user.getEmail());
        registerEmailCode.setCode(RandomStringUtils.randomAlphanumeric(6));
        registerEmailCode.setSalt(CryptoUtil.hashSha512(String.format("%s%s%f%f",
                registerEmailCode.getEmail(),
                registerEmailCode.getCode(),
                Math.random(),
                Math.random())));
        registerEmailCode.setCreatedAt(new Date());
        registerEmailCode.setExpiresAt(DateUtils.addHours(registerEmailCode.getCreatedAt(), 1));
        registerEmailCode.setExpired(false);


        String url = String.format("http://localhost:6795/user/emailCode?email=%s&code=%s&salt=%s",
                registerEmailCode.getEmail(),
                registerEmailCode.getCode(),
                registerEmailCode.getSalt());
        Context context = new Context();
        context.setVariable("url", url);


        MimeMessage mimeMessage = this.javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        mimeMessageHelper.setSubject("[맛집 회원가입] 이메일 인증");
        mimeMessageHelper.setFrom("inst.xkr879011@gmail.com");
        mimeMessageHelper.setTo(user.getEmail());
        mimeMessageHelper.setText(this.springTemplateEngine.process("_registerEmail", context), true);
        this.javaMailSender.send(mimeMessage);

        return this.userMapper.insertUser(user) > 0 && this.userMapper.insertRegisterEmailCode(registerEmailCode) > 0
                ? RegisterResult.SUCCESS
                : RegisterResult.FAILURE;
    }

    public LoginResult login(UserEntity user) {
        if (user.getEmail() == null ||
                user.getPassword() == null) {
            return LoginResult.FAILURE;
        }
        UserEntity existingUser = this.userMapper.selectUserByEmail(user.getEmail());
        if (existingUser == null) {
            return LoginResult.FAILURE;
        }
        user.setPassword(CryptoUtil.hashSha512(user.getPassword()));
        if (!user.getPassword().equals(existingUser.getPassword())) {
            return LoginResult.FAILURE;
        }
        user.setNickname(existingUser.getNickname())
                .setContact(existingUser.getContact())
                .setStatus(existingUser.getStatus())
                .setAdmin(existingUser.isAdmin())
                .setRegisteredAt(existingUser.getRegisteredAt());
        if (user.getStatus().equals("DELETED")) {
            return LoginResult.FAILURE;
        }
        if (user.getStatus().equals("EMAIL_PENDING")) {
            return LoginResult.FAILURE_EMAIL_NOT_VERIFIED;
        }
        if (user.getStatus().equals("SUSPENDED")) {
            return LoginResult.FAILURE_SUSPENDED;
        }
        return LoginResult.SUCCESS;
    }
}
