package com.cmms11.web;

import com.cmms11.domain.func.FuncRequest;
import com.cmms11.domain.func.FuncResponse;
import com.cmms11.domain.func.FuncService;
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
 * 기능위치(view) 컨트롤러.
 */
@Controller
@RequestMapping("/domain/func")
public class FuncViewController {

    private final FuncService service;

    public FuncViewController(FuncService service) {
        this.service = service;
    }

    @GetMapping("/list")
    public String list(
        @RequestParam(name = "q", required = false) String keyword,
        Pageable pageable,
        Model model
    ) {
        Page<FuncResponse> page = service.list(keyword, pageable);
        model.addAttribute("page", page);
        model.addAttribute("keyword", keyword);
        return "domain/func/list";
    }

    @GetMapping("/form")
    public String newForm(Model model) {
        model.addAttribute("func", emptyFunc());
        model.addAttribute("isNew", true);
        return "domain/func/form";
    }

    @GetMapping("/edit/{funcId}")
    public String editForm(@PathVariable String funcId, Model model) {
        model.addAttribute("func", service.get(funcId));
        model.addAttribute("isNew", false);
        return "domain/func/form";
    }

    @PostMapping("/save")
    public String save(
        @RequestParam String funcId,
        @RequestParam String name,
        @RequestParam(name = "note", required = false) String note,
        @RequestParam(name = "isNew", required = false) String isNew
    ) {
        FuncRequest request = new FuncRequest(funcId, name, note);
        if ("true".equals(isNew)) {
            service.create(request);
        } else {
            service.update(funcId, request);
        }
        return "redirect:/domain/func/list";
    }

    @PostMapping("/delete/{funcId}")
    public String delete(@PathVariable String funcId) {
        service.delete(funcId);
        return "redirect:/domain/func/list";
    }

    private FuncResponse emptyFunc() {
        return new FuncResponse(
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

