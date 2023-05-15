package dev.itjung.matzip.controllers;

import dev.itjung.matzip.entities.RecoverContactCodeEntity;
import dev.itjung.matzip.entities.RegisterContactCodeEntity;
import dev.itjung.matzip.entities.RegisterEmailCodeEntity;
import dev.itjung.matzip.entities.UserEntity;
import dev.itjung.matzip.enums.*;
import dev.itjung.matzip.services.UserService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.mail.MessagingException;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping(value = "/user")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(value = "contactCode",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getContactCode(RegisterContactCodeEntity registerContactCode) {
        SendRegisterContactCodeResult result = this.userService.sendRegisterContactCode(registerContactCode);

        JSONObject responseObject = new JSONObject() {{
            put("result", result.name().toLowerCase());
        }};
        if (result == SendRegisterContactCodeResult.SUCCESS) {
            responseObject.put("salt", registerContactCode.getSalt());
        }
        return responseObject.toString();
    }

    @RequestMapping(value = "emailCode", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public ModelAndView getEmailCode(RegisterEmailCodeEntity registerEmailCode) {
        VerifyRegisterEmailCodeResult result = this.userService.verifyRegisterEmailCode(registerEmailCode);
            return new ModelAndView() {{
                setViewName("user/emailCode");
                addObject("result", result.name());
            }};

    }



    @RequestMapping(value = "contactCode",
            method = RequestMethod.PATCH,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String patchContactCode(RegisterContactCodeEntity registerContactCodeEntity) {
        VerifyRegisterContactCodeResult result = this.userService.verifyRegisterContactCodeResult(registerContactCodeEntity);
        JSONObject responseObject = new JSONObject() {{
            put("result", result.name().toLowerCase());
        }};
        return responseObject.toString();
    }

    @RequestMapping(value = "contactCodeRec",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getContactCodeRec(RecoverContactCodeEntity recoverContactCode){
        SendRecoverContactCodeResult result = this.userService.sendRecoverContactCode(recoverContactCode);
    }

    @RequestMapping(value = "contactCodeRec", method = RequestMethod.PATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String patchContactCodeRec(RecoverContactCodeEntity recoverContactCode) {
        VerifyRecoverContactCodeResult result = this.userService.recoverContactCodeResult(recoverContactCode);
        JSONObject responseObject = new JSONObject() {{
            put("result", result.name().toLowerCase());
        }};
        if (result == VerifyRecoverContactCodeResult.SUCCESS) {
            UserEntity user = this.userService.getUserByContact(recoverContactCode.getContact());
            responseObject.put("email", user.getEmail());
        }
        return responseObject.toString();
    }

    @RequestMapping(value = "emailCount",
            method = RequestMethod.GET)
    @ResponseBody
    public String getEmailCount(@RequestParam(value = "email") String email) {
        CheckEmailResult result = this.userService.checkEmailResult(email);
        JSONObject reponseObject = new JSONObject() {{
            put("result", result.name().toLowerCase());
        }};
        return reponseObject.toString();
    }

    @RequestMapping(value = "nicknameCount",
            method = RequestMethod.GET)
    @ResponseBody
    public String getNickNameCount(@RequestParam(value = "nickname") String nickname) {
        CheckNickNameResult result = this.userService.checkNickNameResult(nickname);
        JSONObject reponseObject = new JSONObject() {{
            put("result", result.name().toLowerCase());
        }};
        return reponseObject.toString();
    }

    @RequestMapping(value = "login",
    method = RequestMethod.GET)
    public ModelAndView getLogin() {
        ModelAndView modelAndView = new ModelAndView("home/index");
        return modelAndView;
    }



    @RequestMapping(value = "register",
    method = RequestMethod.POST,
    produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postRegister(UserEntity user,
                               RegisterContactCodeEntity registerContactCode) throws MessagingException {
        RegisterResult result = this.userService.register(user, registerContactCode);
        JSONObject responseObject = new JSONObject() {{
            put("result", result.name().toLowerCase());
        }};
        return responseObject.toString();
    }

    @RequestMapping(value = "login", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postLogin(HttpSession session,UserEntity user){
        LoginResult result = this.userService.login(user);
        if (result == LoginResult.SUCCESS) {
            session.setAttribute("user", user);
        }
        JSONObject responseObject = new JSONObject() {{
            put("result", result.name().toLowerCase());
        }};
        return responseObject.toString();
    }

    @RequestMapping(value = "contactCodeRec", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getContactCodeRec(RecoverContactCodeEntity recoverContactCode){
        SendRecoverContactCodeResult result = this.userService.sendRecoverContactCode(recoverContactCode);

        JSONObject responseObject = new JSONObject() {{
            put("result", result.name().toLowerCase());
        }};
        if (result == SendRecoverContactCodeResult.SUCCESS) {
            responseObject.put("salt", recoverContactCode.getSalt());
        }
        return responseObject.toString();
    }


    @RequestMapping(value = "logout",
            method = RequestMethod.GET)
    public ModelAndView getLogout() {
        ModelAndView modelAndView = new ModelAndView("/");
        return modelAndView;
    }
}


