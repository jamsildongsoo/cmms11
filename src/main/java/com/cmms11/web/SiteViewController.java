package com.cmms11.web;

import com.cmms11.domain.site.SiteRequest;
import com.cmms11.domain.site.SiteResponse;
import com.cmms11.domain.site.SiteService;
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
 * 화면과 Service 계층을 연결하는 사업장(view) 컨트롤러.
 */
@Controller
@RequestMapping("/domain/site")
public class SiteViewController {

    private final SiteService service;

    public SiteViewController(SiteService service) {
        this.service = service;
    }

    @GetMapping("/list")
    public String list(
        @RequestParam(name = "q", required = false) String keyword,
        Pageable pageable,
        Model model
    ) {
        Page<SiteResponse> page = service.list(keyword, pageable);
        model.addAttribute("page", page);
        model.addAttribute("keyword", keyword);
        return "domain/site/list";
    }

    @GetMapping("/form")
    public String newForm(Model model) {
        model.addAttribute("site", emptySite());
        model.addAttribute("isNew", true);
        return "domain/site/form";
    }

    @GetMapping("/edit/{siteId}")
    public String editForm(@PathVariable String siteId, Model model) {
        model.addAttribute("site", service.get(siteId));
        model.addAttribute("isNew", false);
        return "domain/site/form";
    }

    @PostMapping("/save")
    public String save(
        @RequestParam String siteId,
        @RequestParam String name,
        @RequestParam(name = "note", required = false) String note,
        @RequestParam(name = "isNew", required = false) String isNew
    ) {
        SiteRequest request = new SiteRequest(siteId, name, note);
        if ("true".equals(isNew)) {
            service.create(request);
        } else {
            service.update(siteId, request);
        }
        return "redirect:/domain/site/list";
    }

    @PostMapping("/delete/{siteId}")
    public String delete(@PathVariable String siteId) {
        service.delete(siteId);
        return "redirect:/domain/site/list";
    }

    private SiteResponse emptySite() {
        return new SiteResponse(
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

