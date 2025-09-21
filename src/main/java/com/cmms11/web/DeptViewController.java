package com.cmms11.web;

import com.cmms11.domain.dept.DeptRequest;
import com.cmms11.domain.dept.DeptResponse;
import com.cmms11.domain.dept.DeptService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 부서 기준정보 웹 화면 컨트롤러.
 */
@Controller
@RequestMapping("/domain/dept")
public class DeptViewController {

    private final DeptService service;

    public DeptViewController(DeptService service) {
        this.service = service;
    }

    @GetMapping("/list")
    public String list(
        @RequestParam(name = "q", required = false) String keyword,
        Pageable pageable,
        Model model
    ) {
        Page<DeptResponse> page = service.list(keyword, pageable);
        model.addAttribute("page", page);
        model.addAttribute("keyword", keyword);
        return "domain/dept/list";
    }

    @GetMapping("/form")
    public String newForm(Model model) {
        model.addAttribute("dept", emptyDept());
        model.addAttribute("isNew", true);
        return "domain/dept/form";
    }

    @GetMapping("/edit/{deptId}")
    public String editForm(@PathVariable String deptId, Model model) {
        model.addAttribute("dept", service.get(deptId));
        model.addAttribute("isNew", false);
        return "domain/dept/form";
    }

    @PostMapping("/save")
    public String save(
        @RequestParam String deptId,
        @RequestParam String name,
        @RequestParam(name = "note", required = false) String note,
        @RequestParam(name = "isNew", required = false) String isNew
    ) {
        DeptRequest request = new DeptRequest(deptId, name, note);
        if ("true".equals(isNew)) {
            service.create(request);
        } else {
            service.update(deptId, request);
        }
        return "redirect:/domain/dept/list";
    }

    @PostMapping("/delete/{deptId}")
    public String delete(@PathVariable String deptId) {
        service.delete(deptId);
        return "redirect:/domain/dept/list";
    }

    private DeptResponse emptyDept() {
        return new DeptResponse(
            null,
            null,
            null,
            "N",
            null,
            null,
            null,
            null
        );
    }
}

