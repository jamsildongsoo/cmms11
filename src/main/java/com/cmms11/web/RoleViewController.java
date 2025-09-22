package com.cmms11.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 권한 관리 화면용 뷰 컨트롤러.
 * 현재는 기능 준비중 메시지만 노출한다.
 */
@Controller
@RequestMapping("/domain/role")
public class RoleViewController {

    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("ready", Boolean.TRUE);
        return "domain/role/list";
    }
}

